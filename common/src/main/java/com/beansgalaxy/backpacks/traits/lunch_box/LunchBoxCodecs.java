package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class LunchBoxCodecs implements ITraitCodec<LunchBoxTraits> {
      public static final LunchBoxCodecs INSTANCE = new LunchBoxCodecs();

      public static final Codec<LunchBoxTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").validate(size ->
                              size < 256 ? size > 0 ? DataResult.success(size)
                              : DataResult.error(() -> "The provided field \"size\" must be greater than 0; Provided=" + size, 1)
                              : DataResult.error(() -> "The provided field \"size\" must be smaller than 256; Provided=" + size, 255)
                        ).forGetter(BundleLikeTraits::size),
                        ModSound.MAP_CODEC.forGetter(LunchBoxTraits::sound)
            ).apply(in, (size, sound) -> new LunchBoxTraits(sound, size))
      );

      @Override
      public Codec<LunchBoxTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, LunchBoxTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeInt(traits.size());
      }, buf -> new LunchBoxTraits(
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readInt()
      ));

            @Override
      public StreamCodec<RegistryFriendlyByteBuf, LunchBoxTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
