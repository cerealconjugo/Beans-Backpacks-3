package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class XpCodecs implements ITraitCodec<XpTraits, XpFields> {
      public static final XpCodecs INSTANCE = new XpCodecs();

      public static final Codec<XpTraits> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(XpTraits::size),
                              ModSound.MAP_CODEC.forGetter(XpTraits::sound),
                              PrimitiveCodec.INT.optionalFieldOf("levels", 0).forGetter(XpTraits::points)
                  ).apply(in, (size, sound, levels) -> new XpTraits(new XpFields(size, sound), levels))
      );

      @Override
      public Codec<XpTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, XpTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            XpFields.STREAM_CODEC.encode(buf, traits.fields());
            buf.writeInt(traits.points());
      }, buf -> {
            XpFields fields = XpFields.STREAM_CODEC.decode(buf);
            int levels = buf.readInt();
            return new XpTraits(fields, levels);
      });

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, XpTraits> streamCodec() {
            return STREAM_CODEC;
      }

      public static final Codec<XpFields> FIELDS_CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              Codec.INT.fieldOf("size").forGetter(XpFields::size),
                              ModSound.MAP_CODEC.forGetter(XpFields::sound)
                  ).apply(in, XpFields::new)
      );

      @Override
      public Codec<XpFields> fieldCodec() {
            return FIELDS_CODEC;
      }
}
