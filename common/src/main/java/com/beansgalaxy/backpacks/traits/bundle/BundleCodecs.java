package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.util.SlotSelection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BundleCodecs implements ITraitCodec<BundleTraits, BundleFields> {
      public static final BundleCodecs INSTANCE = new BundleCodecs();

      public static final Codec<BundleTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").forGetter(BundleTraits::size),
                        ModSound.MAP_CODEC.forGetter(BundleTraits::sound),
                        ItemStack.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(BundleTraits::stacks)
            ).apply(in, (size, sound, stacks) -> new BundleTraits(new BundleFields(size, sound), stacks))
      );

      @Override
      public Codec<BundleTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BundleTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            BundleFields.STREAM_CODEC.encode(buf, traits.fields());
            ItemStack.LIST_STREAM_CODEC.encode(buf, traits.stacks());
            SlotSelection.STREAM_CODEC.encode(buf, traits.slotSelection);
      }, buf -> {
            BundleFields fields = BundleFields.STREAM_CODEC.decode(buf);
            List<ItemStack> stacks = ItemStack.LIST_STREAM_CODEC.decode(buf);
            SlotSelection slotSelection = SlotSelection.STREAM_CODEC.decode(buf);
            return new BundleTraits(fields, slotSelection, stacks);
      });

            @Override
      public StreamCodec<RegistryFriendlyByteBuf, BundleTraits> streamCodec() {
            return STREAM_CODEC;
      }

      public static final Codec<BundleFields> FIELDS_CODEC = RecordCodecBuilder.create(
                  in -> in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(BundleFields::size),
                              ModSound.MAP_CODEC.forGetter(BundleFields::sound)
                  ).apply(in, BundleFields::new)
      );

      @Override
      public Codec<BundleFields> fieldCodec() {
            return FIELDS_CODEC;
      }
}
