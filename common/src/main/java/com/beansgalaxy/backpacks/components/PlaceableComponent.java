package com.beansgalaxy.backpacks.components;

import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.OptionalEitherMapCodec;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record PlaceableComponent(@Nullable ResourceLocation customModel, @Nullable ResourceLocation backpackTexture, @NotNull ModSound sound) {
      public static final String NAME = "placeable";

      public Optional<ResourceLocation> getModelLocation() {
            return Optional.ofNullable(customModel());
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

      public static final OptionalEitherMapCodec<ResourceLocation, ResourceLocation> DISPLAY_CODEC =
                  new OptionalEitherMapCodec<>(
                              "custom_model", ResourceLocation.CODEC,
                              "backpack_texture", ResourceLocation.CODEC
                  );

      public static final Codec<PlaceableComponent> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              DISPLAY_CODEC.forGetter(placeable -> {
                                    if (placeable.backpackTexture != null)
                                          return Optional.of(Either.right(placeable.backpackTexture));

                                    if (placeable.customModel != null)
                                          return Optional.of(Either.left(placeable.customModel));

                                    return Optional.empty();
                              }),
                              ModSound.MAP_CODEC.forGetter(PlaceableComponent::sound)
                  ).apply(in, (model, sound) -> {
                        if (model.isEmpty())
                              return new PlaceableComponent(null, null, sound);

                        Either<ResourceLocation, ResourceLocation> either = model.get();
                        return new PlaceableComponent(either.left().orElse(null), either.right().orElse(null), sound);
                  })
      );

      public static final StreamCodec<RegistryFriendlyByteBuf, PlaceableComponent> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(RegistryFriendlyByteBuf buf, PlaceableComponent placeable) {
                  ModSound.STREAM_CODEC.encode(buf, placeable.sound);

                  boolean hasModel = placeable.customModel != null;
                  buf.writeBoolean(hasModel);
                  if (hasModel)
                        buf.writeResourceLocation(placeable.customModel);

                  boolean hasTexture = placeable.backpackTexture != null;
                  buf.writeBoolean(hasTexture);
                  if (hasTexture)
                        buf.writeResourceLocation(placeable.backpackTexture);
            }

            @Override
            public PlaceableComponent decode(RegistryFriendlyByteBuf buf) {
                  ModSound sound = ModSound.STREAM_CODEC.decode(buf);

                  boolean hasModel = buf.readBoolean();
                  ResourceLocation model = hasModel
                              ? buf.readResourceLocation()
                              : null;

                  boolean hasTexture = buf.readBoolean();
                  ResourceLocation texture = hasTexture
                              ? buf.readResourceLocation()
                              : null;

                  return new PlaceableComponent(model, texture, sound);
            }
      };
}
