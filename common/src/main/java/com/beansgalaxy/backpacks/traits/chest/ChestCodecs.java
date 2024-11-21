package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ChestCodecs implements ITraitCodec<ChestTraits> {
      public static final ChestCodecs INSTANCE = new ChestCodecs();

      public static final Codec<ChestTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("rows").validate(rows ->
                              rows < 16 ? rows > 0 ? DataResult.success(rows)
                              : DataResult.error(() -> "The provided field \"rows\" must be greater than 0; Provided=" + rows, 1)
                              : DataResult.error(() -> "The provided field \"rows\" must be smaller than 16; Provided=" + rows, 15)
                        ).forGetter(chest -> chest.rows),
                        PrimitiveCodec.INT.fieldOf("columns").validate(columns ->
                              columns < 16 ? columns > 0 ? DataResult.success(columns)
                              : DataResult.error(() -> "The provided field \"columns\" must be greater than 0; Provided=" + columns, 1)
                              : DataResult.error(() -> "The provided field \"columns\" must be smaller than 16; Provided=" + columns, 15)
                        ).forGetter(chest -> chest.columns),
                        ModSound.MAP_CODEC.forGetter(ChestTraits::sound)
            ).apply(in, (rows, columns, sound) -> new ChestTraits(null, sound, rows, columns))
      );

      @Override
      public Codec<ChestTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, ChestTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeByte(traits.rows);
            buf.writeByte(traits.columns);
      }, buf ->
            new ChestTraits(null,
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readByte(),
                  buf.readByte()
      ));
      
      @Override
      public StreamCodec<RegistryFriendlyByteBuf, ChestTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
