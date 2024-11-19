package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class XpCodecs implements ITraitCodec<XpTraits> {
      public static final XpCodecs INSTANCE = new XpCodecs();

      public static final Codec<XpTraits> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              PrimitiveCodec.INT.fieldOf("size").validate(size ->
                                    size < 238609312 ? size > 0 ? DataResult.success(size)
                                    : DataResult.error(() -> "The provided field \"size\" must be greater than 0; Provided=" + size, 1)
                                    : DataResult.error(() -> "The provided field \"size\" must be smaller than 238,609,312; Provided=" + size, 238609311)
                              ).forGetter(XpTraits::size),
                              ModSound.MAP_CODEC.forGetter(XpTraits::sound)
                  ).apply(in, (size, sound) -> new XpTraits(null, sound, size))
      );

      @Override
      public Codec<XpTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, XpTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            GenericTraits.encodeLocation(buf, traits);
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeInt(traits.size());
      }, buf -> new XpTraits(
                  GenericTraits.decodeLocation(buf),
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readInt()
      ));

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, XpTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
