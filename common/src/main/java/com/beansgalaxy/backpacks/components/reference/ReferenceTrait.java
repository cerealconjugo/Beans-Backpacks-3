package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.PlaceableComponent;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ReferenceTrait {
      private final ResourceLocation location;
      private GenericTraits trait = null;
      private PlaceableComponent placeable = null;
      private EquipableComponent equipable = null;
      private ItemAttributeModifiers modifiers = ItemAttributeModifiers.EMPTY;

      public ReferenceTrait(ResourceLocation location) {
            this.location = location;
      }

      public ResourceLocation location() {
            return location;
      }

      public ReferenceTrait(ResourceLocation location, @NotNull GenericTraits traits, PlaceableComponent placeable, EquipableComponent equipable) {
            this(location);
            this.trait = traits;
            this.placeable = placeable;
            this.equipable = equipable;
      }

      public static void ifAttributesPresent(ItemStack stack, Consumer<ItemAttributeModifiers> run) {
            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait != null) {
                  referenceTrait.getAttributes().ifPresent(run);
            }
      }

      public ReferenceTrait update(ResourceLocation location, GenericTraits traits) {
            return new ReferenceTrait(location, traits, placeable, equipable);
      }

      @Override
      public boolean equals(Object o) {
            if (this == o)
                  return true;
            if (!(o instanceof ReferenceTrait that))
                  return false;
            return Objects.equals(location, that.location) && Objects.equals(trait, that.trait);
      }

      @Override
      public int hashCode() {
            return Objects.hash(location, trait);
      }

      public static ReferenceTrait of(String location) {
            return of(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, location));
      }

      public static ReferenceTrait of(String namespace, String location) {
            return of(ResourceLocation.fromNamespaceAndPath(namespace, location));
      }

      private static ReferenceTrait of(ResourceLocation location) {
            return new ReferenceTrait(location);
      }

      public static ReferenceTrait of(ResourceLocation location, GenericTraits traits) {
            ReferenceTrait reference = of(location);
            reference.trait = traits;
            return reference;
      }

      @NotNull
      public Optional<GenericTraits> getTrait() {
            return NonTrait.is(trait) ? Optional.empty()
                        : Optional.ofNullable(trait).or(() -> {
                              ReferenceFields reference = ReferenceTraitRegistry.get(location);
                              if (reference == null)
                                    return Optional.empty();

                              IDeclaredFields fields = reference.fields();
                              if (fields == null) {
                                    return Optional.empty();
                              }

                              GenericTraits blankTrait = fields.asBlankTrait();
                              this.trait = blankTrait;
                              loadFromReferenceFields(reference);
                              return Optional.ofNullable(blankTrait);
                        });
      }

      @NotNull
      public Optional<PlaceableComponent> getPlaceable() {
            return Optional.ofNullable(placeable).or(() -> {
                  ReferenceFields reference = ReferenceTraitRegistry.get(location);
                  if (reference == null)
                        return Optional.empty();

                  loadFromReferenceFields(reference);
                  return Optional.ofNullable(placeable);
            });
      }

      @NotNull
      public Optional<EquipableComponent> getEquipable() {
            return Optional.ofNullable(equipable).or(() -> {
                  ReferenceFields reference = ReferenceTraitRegistry.get(location);
                  if (reference == null)
                        return Optional.empty();

                  loadFromReferenceFields(reference);
                  return Optional.ofNullable(equipable);
            });
      }

      @NotNull
      public Optional<ItemAttributeModifiers> getAttributes() {
            return Optional.ofNullable(modifiers).or(() -> {
                  ReferenceFields reference = ReferenceTraitRegistry.get(location);
                  if (reference == null)
                        return Optional.empty();

                  loadFromReferenceFields(reference);
                  return Optional.ofNullable(modifiers);
            });
      }

      private void loadFromReferenceFields(ReferenceFields fields) {
            this.placeable = fields.placeable();
            this.equipable = fields.equipable();
            this.modifiers = fields.modifiers();
      }

      public static final Codec<ReferenceTrait> CODEC = new MapCodec<ReferenceTrait>() {
            private static final MapCodec<ResourceLocation> LOCATION_CODEC = ResourceLocation.CODEC.fieldOf("location");

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                  return LOCATION_CODEC.keys(ops);
            }

            @Override
            public <T> RecordBuilder<T> encode(ReferenceTrait input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                  RecordBuilder<T> suffix = input.getTrait().map(traits ->
                              traits.kind().encode(traits, ops, prefix)
                  ).orElse(prefix);

                  return LOCATION_CODEC.encode(input.location, ops, suffix);
            }

            @Override
            public <T> DataResult<ReferenceTrait> decode(DynamicOps<T> ops, MapLike<T> input) {
                  return LOCATION_CODEC.decode(ops, input).flatMap(location -> {
                        ReferenceFields referenceFields = ReferenceTraitRegistry.get(location);
                        if (referenceFields == null)
                              return DataResult.error(() -> "No trait is registered using the given location: " + location);

                        IDeclaredFields reference = referenceFields.fields();
                        if (!NonTrait.is(reference))
                              return reference.kind().codec().fieldOf("trait").decode(ops, input).map(
                                          trait -> {
                                                GenericTraits traitReference = trait.toReference(location);
                                                return new ReferenceTrait(location, traitReference, referenceFields.placeable(), referenceFields.equipable());
                                          }
                              );


                        return DataResult.success(new ReferenceTrait(location, NonTrait.INSTANCE, referenceFields.placeable(), referenceFields.equipable()));
                  });
            }
      }.codec();

      public static final StreamCodec<? super RegistryFriendlyByteBuf, ReferenceTrait> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(RegistryFriendlyByteBuf buf, ReferenceTrait reference) {
                  ResourceLocation.STREAM_CODEC.encode(buf, reference.location);
                  reference.getTrait().ifPresentOrElse(traits -> {
                        buf.writeBoolean(true);
                        traits.kind().encode(buf, traits);
                  }, () -> {
                        buf.writeBoolean(false);
                  });
            }

            @Override
            public ReferenceTrait decode(RegistryFriendlyByteBuf buf) {
                  ResourceLocation location = ResourceLocation.STREAM_CODEC.decode(buf);
                  if (buf.readBoolean()) {
                        ReferenceFields reference = ReferenceTraitRegistry.get(location);
                        IDeclaredFields fields = reference.fields();
                        GenericTraits traits = fields.kind().streamCodec().decode(buf);
                        return new ReferenceTrait(location, traits.toReference(location), reference.placeable(), reference.equipable());
                  }
                  return new ReferenceTrait(location);
            }
      };

      public <T extends GenericTraits> ReferenceTrait set(T trait) {
            this.trait = trait;
            return this;
      }

      public boolean isEmpty() {
            return false;
      }

}
