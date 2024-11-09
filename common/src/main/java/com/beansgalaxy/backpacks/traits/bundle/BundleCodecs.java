package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.SlotSelection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BundleCodecs implements ITraitCodec<BundleTraits> {
      public static final BundleCodecs INSTANCE = new BundleCodecs();

      public static final Codec<BundleTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").forGetter(BundleTraits::size),
                        ModSound.MAP_CODEC.forGetter(BundleTraits::sound)
            ).apply(in, (size, sound) -> new BundleTraits(null, sound, size))
      );

      @Override
      public Codec<BundleTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BundleTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            GenericTraits.encodeLocation(buf, traits);
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeInt(traits.size());
            SlotSelection.STREAM_CODEC.encode(buf, traits.selection);
      }, buf -> new BundleTraits(
                  GenericTraits.decodeLocation(buf),
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readInt(),
                  SlotSelection.STREAM_CODEC.decode(buf)
      ));

            @Override
      public StreamCodec<RegistryFriendlyByteBuf, BundleTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
