package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.SlotSelection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class LunchBoxCodecs implements ITraitCodec<LunchBoxTraits> {
      public static final LunchBoxCodecs INSTANCE = new LunchBoxCodecs();

      public static final Codec<LunchBoxTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").forGetter(LunchBoxTraits::size),
                        ModSound.MAP_CODEC.forGetter(LunchBoxTraits::sound)
            ).apply(in, (size, sound) -> new LunchBoxTraits(null, sound, size))
      );

      @Override
      public Codec<LunchBoxTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, LunchBoxTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            GenericTraits.encodeLocation(buf, traits);
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeInt(traits.size());
            SlotSelection.STREAM_CODEC.encode(buf, traits.selection);
      }, buf -> new LunchBoxTraits(
                  GenericTraits.decodeLocation(buf),
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readInt(),
                  SlotSelection.STREAM_CODEC.decode(buf)
      ));

            @Override
      public StreamCodec<RegistryFriendlyByteBuf, LunchBoxTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
