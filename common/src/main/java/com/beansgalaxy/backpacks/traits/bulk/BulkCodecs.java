package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BulkCodecs implements ITraitCodec<BulkTraits> {
      public static final BulkCodecs INSTANCE = new BulkCodecs();

      public static final Codec<BulkTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").forGetter(BulkTraits::size),
                        ModSound.MAP_CODEC.forGetter(BulkTraits::sound)
            ).apply(in, (size, sound) -> new BulkTraits(null, sound, size))
      );

      @Override
      public Codec<BulkTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BulkTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            GenericTraits.encodeLocation(buf, traits);
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeInt(traits.size());
      }, buf -> new BulkTraits(
                  GenericTraits.decodeLocation(buf),
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readInt()
      ));
      
      @Override
      public StreamCodec<RegistryFriendlyByteBuf, BulkTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
