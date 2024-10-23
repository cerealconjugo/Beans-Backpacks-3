package com.beansgalaxy.backpacks.trait.battery;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BatteryCodecs implements ITraitCodec<BatteryTraits, BatteryFields> {
      public static final BatteryCodecs INSTANCE = new BatteryCodecs();

      private record BatteryData(ItemStack stack, int amount) {
            public static final Codec<BatteryData> CODEC = RecordCodecBuilder.create(in ->
                        in.group(
                                    ItemStack.OPTIONAL_CODEC.fieldOf("stack").forGetter(BatteryData::stack),
                                    Codec.INT.fieldOf("amount").forGetter(BatteryData::amount)
                        ).apply(in, BatteryData::new)
            );
      }

      public static final Codec<BatteryTraits> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(BatteryTraits::size),
                              PrimitiveCodec.LONG.fieldOf("speed").forGetter(BatteryTraits::speed),
                              ModSound.MAP_CODEC.forGetter(BatteryTraits::sound),
                              BatteryData.CODEC.optionalFieldOf("data", new BatteryData(Items.AIR.getDefaultInstance(), 0)).forGetter(traits -> new BatteryData(traits.stack(), traits.amount()))
                  ).apply(in, (size, speed, sound, data) -> new BatteryTraits(new BatteryFields(size, speed, sound), data.stack, data.amount))
      );

      @Override
      public Codec<BatteryTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BatteryTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            BatteryFields.STREAM_CODEC.encode(buf, traits.fields());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, traits.stack());
            buf.writeInt(traits.amount());
      }, buf -> {
            BatteryFields fields = BatteryFields.STREAM_CODEC.decode(buf);
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            int amount = buf.readInt();
            return new BatteryTraits(fields, stack, amount);
      });

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, BatteryTraits> streamCodec() {
            return STREAM_CODEC;
      }

      public static final Codec<BatteryFields> FIELDS_CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(BatteryFields::size),
                              PrimitiveCodec.LONG.fieldOf("speed").forGetter(BatteryFields::speed),
                              ModSound.MAP_CODEC.forGetter(BatteryFields::sound)
                  ).apply(in, BatteryFields::new)
      );

      @Override
      public Codec<BatteryFields> fieldCodec() {
            return FIELDS_CODEC;
      }
}