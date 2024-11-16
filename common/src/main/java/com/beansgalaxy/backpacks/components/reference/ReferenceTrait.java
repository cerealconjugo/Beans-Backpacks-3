package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.PlaceableComponent;
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
                              ReferenceRegistry reference = ReferenceRegistry.getNullable(location);
                              if (reference == null)
                                    return Optional.empty();

                              loadFromReferenceFields(reference);
                              return Optional.of(trait);
                        });
      }

      @NotNull
      public Optional<PlaceableComponent> getPlaceable() {
            return Optional.ofNullable(placeable).or(() -> {
                  ReferenceRegistry reference = ReferenceRegistry.getNullable(location);
                  if (reference == null)
                        return Optional.empty();

                  loadFromReferenceFields(reference);
                  return Optional.ofNullable(placeable);
            });
      }

      @NotNull
      public Optional<EquipableComponent> getEquipable() {
            return Optional.ofNullable(equipable).or(() -> {
                  ReferenceRegistry reference = ReferenceRegistry.getNullable(location);
                  if (reference == null)
                        return Optional.empty();

                  loadFromReferenceFields(reference);
                  return Optional.ofNullable(equipable);
            });
      }

      @NotNull
      public Optional<ItemAttributeModifiers> getAttributes() {
            return Optional.ofNullable(modifiers).or(() -> {
                  ReferenceRegistry reference = ReferenceRegistry.getNullable(location);
                  if (reference == null)
                        return Optional.empty();

                  loadFromReferenceFields(reference);
                  return Optional.ofNullable(modifiers);
            });
      }

      private void loadFromReferenceFields(ReferenceRegistry fields) {
            this.trait = fields.traits();
            this.placeable = fields.placeable();
            this.equipable = fields.equipable();
            this.modifiers = fields.modifiers();
      }

      public static final Codec<ReferenceTrait> CODEC = ResourceLocation.CODEC.flatXmap(location -> {
            ReferenceRegistry referenceRegistry = ReferenceRegistry.getNullable(location);
            if (referenceRegistry == null)
                  return DataResult.error(() -> "No trait is registered using the given location; " + location, new ReferenceTrait(location));

            return DataResult.success(new ReferenceTrait(location, referenceRegistry.traits().toReference(location), referenceRegistry.placeable(), referenceRegistry.equipable()));
      }, reference ->
            DataResult.success(reference.location)
      );

      public static final StreamCodec<? super RegistryFriendlyByteBuf, ReferenceTrait> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(RegistryFriendlyByteBuf buf, ReferenceTrait reference) {
                  ResourceLocation.STREAM_CODEC.encode(buf, reference.location);
                  boolean present = reference.getTrait().isPresent();
                  buf.writeBoolean(present);

            }

            @Override
            public ReferenceTrait decode(RegistryFriendlyByteBuf buf) {
                  ResourceLocation location = ResourceLocation.STREAM_CODEC.decode(buf);
                  if (buf.readBoolean()) {
                        ReferenceRegistry reference = ReferenceRegistry.get(location);
                        GenericTraits fields = reference.traits();
                        return new ReferenceTrait(location, fields.toReference(location), reference.placeable(), reference.equipable());
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
