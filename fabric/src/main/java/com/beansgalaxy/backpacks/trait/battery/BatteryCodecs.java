package com.beansgalaxy.backpacks.trait.battery;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BatteryCodecs implements ITraitCodec<BatteryTraits> {
      public static final BatteryCodecs INSTANCE = new BatteryCodecs();

      public static final Codec<BatteryTraits> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              PrimitiveCodec.LONG.fieldOf("size").forGetter(BatteryTraits::size),
                              PrimitiveCodec.LONG.fieldOf("speed").forGetter(BatteryTraits::speed),
                              ModSound.MAP_CODEC.forGetter(BatteryTraits::sound)
                  ).apply(in, (size, speed, sound) -> new BatteryTraits(null, sound, size, speed))
      );

      @Override
      public Codec<BatteryTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BatteryTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            GenericTraits.encodeLocation(buf, traits);
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeLong(traits.size());
            buf.writeLong(traits.speed());
      }, buf -> new BatteryTraits(
                  GenericTraits.decodeLocation(buf),
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readLong(),
                  buf.readLong()
      ));

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, BatteryTraits> streamCodec() {
            return STREAM_CODEC;
      }

}