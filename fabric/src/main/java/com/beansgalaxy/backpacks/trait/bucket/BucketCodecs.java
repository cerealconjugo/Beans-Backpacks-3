package com.beansgalaxy.backpacks.trait.bucket;

import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class BucketCodecs implements ITraitCodec<BucketTraits> {
      public static final BucketCodecs INSTANCE = new BucketCodecs();

      public static final Codec<BucketTraits> CODEC = RecordCodecBuilder.create(
                  in -> in.group(
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

      public static final StreamCodec<RegistryFriendlyByteBuf, BucketTraits> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, BucketTraits trait) {
                  ModSound.STREAM_CODEC.encode(buf, trait.sound());
                  buf.writeInt(trait.size());
            }

            @Override @NotNull
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
