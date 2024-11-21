package com.beansgalaxy.backpacks.traits.bucket;

import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BucketCodecs implements ITraitCodec<BucketTraits> {
      public static final BucketCodecs INSTANCE = new BucketCodecs();

      Codec<BucketTraits> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              PrimitiveCodec.INT.fieldOf("size").validate(size ->
                                    size < 26523 ? size > 0 ? DataResult.success(size)
                                    : DataResult.error(() -> "The provided field \"size\" must be greater than 0; Provided=" + size, 1)
                                    : DataResult.error(() -> "The provided field \"size\" must be smaller than 26,523; Provided=" + size, 26522)
                              ).forGetter(BucketTraits::size),
                              ModSound.MAP_CODEC.forGetter(BucketTraits::sound)
                  ).apply(in, (size, sound) -> new BucketTraits(sound, size))
      );

      @Override
      public Codec<BucketTraits> codec() {
            return CODEC;
      }

      StreamCodec<RegistryFriendlyByteBuf, BucketTraits> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, BucketTraits traits) {
                  ModSound.STREAM_CODEC.encode(buf, traits.sound());
                  buf.writeInt(traits.size());
            }

            @Override
            public BucketTraits decode(RegistryFriendlyByteBuf buf) {
                  return new BucketTraits(
                              ModSound.STREAM_CODEC.decode(buf),
                              buf.readInt()
                  );
            }
      };

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, BucketTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
