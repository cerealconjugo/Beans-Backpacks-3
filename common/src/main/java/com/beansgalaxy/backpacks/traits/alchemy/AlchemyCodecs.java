package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.SlotSelection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class AlchemyCodecs implements ITraitCodec<AlchemyTraits> {
      public static final AlchemyCodecs INSTANCE = new AlchemyCodecs();

      public static final Codec<AlchemyTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").validate(size ->
                              size < 256 ? size > 0 ? DataResult.success(size)
                              : DataResult.error(() -> "The provided field \"size\" must be greater than 0; Provided=" + size, 1)
                              : DataResult.error(() -> "The provided field \"size\" must be smaller than 256; Provided=" + size, 255)
                        ).forGetter(BundleLikeTraits::size),
                        ModSound.MAP_CODEC.forGetter(AlchemyTraits::sound)
            ).apply(in, (size, sound) -> new AlchemyTraits(null, sound, size))
      );

      @Override
      public Codec<AlchemyTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            GenericTraits.encodeLocation(buf, traits);
            buf.writeInt(traits.size());
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            SlotSelection.STREAM_CODEC.encode(buf, traits.selection);
      }, buf ->
            new AlchemyTraits(
                        GenericTraits.decodeLocation(buf),
                        ModSound.STREAM_CODEC.decode(buf),
                        buf.readInt(),
                        SlotSelection.STREAM_CODEC.decode(buf)
            )
      );

            @Override
      public StreamCodec<RegistryFriendlyByteBuf, AlchemyTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
