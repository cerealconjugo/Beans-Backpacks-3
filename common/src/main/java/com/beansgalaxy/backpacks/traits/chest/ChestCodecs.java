package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ChestCodecs implements ITraitCodec<ChestTraits> {
      public static final ChestCodecs INSTANCE = new ChestCodecs();

      public static final Codec<ChestTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("rows").forGetter(chest -> chest.rows),
                        PrimitiveCodec.INT.fieldOf("columns").forGetter(chest -> chest.columns),
                        ModSound.MAP_CODEC.forGetter(ChestTraits::sound)
            ).apply(in, (rows, columns, sound) -> new ChestTraits(null, sound, rows, columns))
      );

      @Override
      public Codec<ChestTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, ChestTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            GenericTraits.encodeLocation(buf, traits);
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeInt(traits.rows);
            buf.writeInt(traits.columns);
      }, buf ->
            new ChestTraits(
                  GenericTraits.decodeLocation(buf),
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readInt(),
                  buf.readInt()
      ));
      
      @Override
      public StreamCodec<RegistryFriendlyByteBuf, ChestTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
