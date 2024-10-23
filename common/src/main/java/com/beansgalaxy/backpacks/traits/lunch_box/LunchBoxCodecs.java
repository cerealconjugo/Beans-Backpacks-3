package com.beansgalaxy.backpacks.traits.lunch_box;

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

public class LunchBoxCodecs implements ITraitCodec<LunchBoxTraits, LunchBoxFields> {
      public static final LunchBoxCodecs INSTANCE = new LunchBoxCodecs();

      public static final Codec<LunchBoxTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").forGetter(LunchBoxTraits::size),
                        ModSound.MAP_CODEC.forGetter(LunchBoxTraits::sound),
                        ItemStack.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(LunchBoxTraits::stacks),
                        ItemStack.CODEC.listOf().optionalFieldOf("nonEdibles", List.of()).forGetter(LunchBoxTraits::nonEdibles)
            ).apply(in, (size, sound, stacks, nonEdibles) -> new LunchBoxTraits(new LunchBoxFields(size, sound), stacks, nonEdibles))
      );

      @Override
      public Codec<LunchBoxTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, LunchBoxTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            LunchBoxFields.STREAM_CODEC.encode(buf, traits.fields());
            ItemStack.LIST_STREAM_CODEC.encode(buf, traits.stacks());
            ItemStack.LIST_STREAM_CODEC.encode(buf, traits.nonEdibles());
            SlotSelection.STREAM_CODEC.encode(buf, traits.slotSelection);
      }, buf -> {
            LunchBoxFields fields = LunchBoxFields.STREAM_CODEC.decode(buf);
            List<ItemStack> stacks = ItemStack.LIST_STREAM_CODEC.decode(buf);
            List<ItemStack> nonEdibles = ItemStack.LIST_STREAM_CODEC.decode(buf);
            SlotSelection slotSelection = SlotSelection.STREAM_CODEC.decode(buf);
            return new LunchBoxTraits(fields, slotSelection, stacks, nonEdibles);
      });

            @Override
      public StreamCodec<RegistryFriendlyByteBuf, LunchBoxTraits> streamCodec() {
            return STREAM_CODEC;
      }

      public static final Codec<LunchBoxFields> FIELDS_CODEC = RecordCodecBuilder.create(
                  in -> in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(LunchBoxFields::size),
                              ModSound.MAP_CODEC.forGetter(LunchBoxFields::sound)
                  ).apply(in, LunchBoxFields::new)
      );

      @Override
      public Codec<LunchBoxFields> fieldCodec() {
            return FIELDS_CODEC;
      }
}
