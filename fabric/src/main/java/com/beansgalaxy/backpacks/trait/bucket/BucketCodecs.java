package com.beansgalaxy.backpacks.trait.bucket;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class BucketCodecs implements ITraitCodec<BucketTraits, BucketFields> {
      public static final BucketCodecs INSTANCE = new BucketCodecs();

      record BucketData(FluidVariant fluid, long amount) {
            public static final BucketData EMPTY = new BucketData(FluidVariant.blank(), 0);

            public static final Codec<BucketData> CODEC = RecordCodecBuilder.create(
                        in -> in.group(
                                    FluidVariant.CODEC.fieldOf("fluid").forGetter(BucketData::fluid),
                                    PrimitiveCodec.LONG.fieldOf("amount").forGetter(BucketData::amount)
                        ).apply(in, BucketData::new)
            );

            public static final StreamCodec<? super RegistryFriendlyByteBuf, BucketData> STREAM_CODEC = new StreamCodec<>() {

                  @Override
                  public void encode(RegistryFriendlyByteBuf buf, BucketData trait) {
                        FluidVariant.PACKET_CODEC.encode(buf, trait.fluid);
                        buf.writeLong(trait.amount);
                  }

                  @Override
                  public BucketData decode(RegistryFriendlyByteBuf buf) {
                        FluidVariant fluid = FluidVariant.PACKET_CODEC.decode(buf);
                        long amount = buf.readLong();
                        return new BucketData(fluid, amount);
                  }
            };

      }

      public static final Codec<BucketTraits> CODEC = RecordCodecBuilder.create(
                  in -> in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(BucketTraits::size),
                              ModSound.MAP_CODEC.forGetter(BucketTraits::sound),
                              BucketData.CODEC.optionalFieldOf("data", BucketData.EMPTY).forGetter(traits -> new BucketData(traits.fluid, traits.amount))
                  ).apply(in, (size, sound, data) -> new BucketTraits(new BucketFields(null, sound, size), data.fluid, data.amount))
      );

      @Override
      public Codec<BucketTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BucketTraits> STREAM_CODEC = new StreamCodec<>() {
            @Override
            @NotNull
            public BucketTraits decode(RegistryFriendlyByteBuf buf) {
                  BucketData data = BucketData.STREAM_CODEC.decode(buf);
                  BucketFields fields = BucketFields.STREAM_CODEC.decode(buf);
                  return new BucketTraits(fields, data.fluid, data.amount);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, BucketTraits trait) {
                  BucketData.STREAM_CODEC.encode(buf, new BucketData(trait.fluid, trait.amount));
                  BucketFields.STREAM_CODEC.encode(buf, trait.fields());
            }
      };

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, BucketTraits> streamCodec() {
            return STREAM_CODEC;
      }

      public static final Codec<BucketFields> FIELDS_CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(BucketFields::size),
                              ModSound.MAP_CODEC.forGetter(BucketFields::sound)
                  ).apply(in, BucketFields::new)
      );

      @Override
      public Codec<BucketFields> fieldCodec() {
            return FIELDS_CODEC;
      }
}
