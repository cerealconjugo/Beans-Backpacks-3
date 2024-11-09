package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class XpCodecs implements ITraitCodec<XpTraits> {
      public static final XpCodecs INSTANCE = new XpCodecs();

      public static final Codec<XpTraits> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(XpTraits::size),
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
