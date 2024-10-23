package com.beansgalaxy.backpacks.components;

import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.Traits;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class PlaceableComponent {
      public static final String NAME = "placeable";
      public final ResourceLocation modelLocation;

      public PlaceableComponent() {
            modelLocation = null;
      }

      public PlaceableComponent(ResourceLocation modelLocation) {
            this.modelLocation = modelLocation;
      }

      public Optional<ResourceLocation> getModelLocation() {
            return Optional.ofNullable(modelLocation);
      }

      public static Optional<PlaceableComponent> get(ItemStack stack) {
            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait != null && !referenceTrait.isEmpty())
                  return referenceTrait.getPlaceable();

            PlaceableComponent placeableComponent = stack.get(Traits.PLACEABLE);
            return Optional.ofNullable(placeableComponent);
      }

      public static final Codec<PlaceableComponent> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              ResourceLocation.CODEC.optionalFieldOf("model").forGetter(PlaceableComponent::getModelLocation)
                  ).apply(in, model -> new PlaceableComponent(model.orElse(null)))
      );

      public static final StreamCodec<RegistryFriendlyByteBuf, PlaceableComponent> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(RegistryFriendlyByteBuf buf, PlaceableComponent placeable) {
                  boolean modelNotEmpty = placeable.modelLocation != null;
                  buf.writeBoolean(modelNotEmpty);
                  if (modelNotEmpty)
                        buf.writeResourceLocation(placeable.modelLocation);
            }

            @Override
            public PlaceableComponent decode(RegistryFriendlyByteBuf buf) {
                  boolean modelNotEmpty = buf.readBoolean();
                  if (modelNotEmpty) {
                        ResourceLocation modelResourceLocation = buf.readResourceLocation();
                        return new PlaceableComponent(modelResourceLocation);
                  }

                  return new PlaceableComponent();
            }
      };
}
