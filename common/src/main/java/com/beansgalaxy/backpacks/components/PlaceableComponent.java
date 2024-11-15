package com.beansgalaxy.backpacks.components;

import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.Traits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record PlaceableComponent(@Nullable ResourceLocation modelLocation, ModSound sound) {
      public static final String NAME = "placeable";

      public PlaceableComponent(ModSound sound) {
            this(null, ModSound.HARD);
      }

      public Optional<ResourceLocation> getModelLocation() {
            return Optional.ofNullable(modelLocation());
      }

      public static Optional<PlaceableComponent> get(ItemStack stack) {
            PlaceableComponent placeableComponent = stack.get(Traits.PLACEABLE);
            if (placeableComponent != null)
                  return Optional.of(placeableComponent);

            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait != null && !referenceTrait.isEmpty())
                  return referenceTrait.getPlaceable();

            return Optional.empty();
      }

      public static final Codec<PlaceableComponent> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              ResourceLocation.CODEC.optionalFieldOf("model").forGetter(PlaceableComponent::getModelLocation),
                              ModSound.MAP_CODEC.forGetter(PlaceableComponent::sound)
                  ).apply(in, (model, sound) -> new PlaceableComponent(model.orElse(null), sound))
      );

      public static final StreamCodec<RegistryFriendlyByteBuf, PlaceableComponent> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(RegistryFriendlyByteBuf buf, PlaceableComponent placeable) {
                  ModSound.STREAM_CODEC.encode(buf, placeable.sound);

                  boolean modelNotEmpty = placeable.modelLocation != null;
                  buf.writeBoolean(modelNotEmpty);
                  if (modelNotEmpty)
                        buf.writeResourceLocation(placeable.modelLocation);
            }

            @Override
            public PlaceableComponent decode(RegistryFriendlyByteBuf buf) {
                  ModSound sound = ModSound.STREAM_CODEC.decode(buf);

                  boolean modelNotEmpty = buf.readBoolean();
                  if (modelNotEmpty) {
                        ResourceLocation modelResourceLocation = buf.readResourceLocation();
                        return new PlaceableComponent(modelResourceLocation, sound);
                  }

                  return new PlaceableComponent(sound);
            }
      };
}
