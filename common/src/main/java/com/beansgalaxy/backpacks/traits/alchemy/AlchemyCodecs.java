package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.util.SlotSelection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AlchemyCodecs implements ITraitCodec<AlchemyTraits, AlchemyFields> {
      public static final AlchemyCodecs INSTANCE = new AlchemyCodecs();

      public static final Codec<AlchemyTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").forGetter(AlchemyTraits::size),
                        ModSound.MAP_CODEC.forGetter(AlchemyTraits::sound),
                        ItemStack.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(AlchemyTraits::stacks)
            ).apply(in, (size, sound, stacks) -> new AlchemyTraits(new AlchemyFields(size, sound), stacks))
      );

      @Override
      public Codec<AlchemyTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyFields> STREAM_FIELDS_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, AlchemyFields fields) {
                  buf.writeInt(fields.size());
                  ModSound.STREAM_CODEC.encode(buf, fields.sound());
                  IDeclaredFields.encodeLocation(buf, fields);
            }

            @Override
            public AlchemyFields decode(RegistryFriendlyByteBuf buf) {
                  return new AlchemyFields(
                              buf.readInt(),
                              ModSound.STREAM_CODEC.decode(buf)
                  ).toReference(IDeclaredFields.decodeLocation(buf));
            }
      };

      public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            STREAM_FIELDS_CODEC.encode(buf, traits.fields());
            ItemStack.LIST_STREAM_CODEC.encode(buf, traits.stacks());
            SlotSelection.STREAM_CODEC.encode(buf, traits.slotSelection);
      }, buf -> {
            AlchemyFields fields = STREAM_FIELDS_CODEC.decode(buf);
            List<ItemStack> stacks = ItemStack.LIST_STREAM_CODEC.decode(buf);
            SlotSelection slotSelection = SlotSelection.STREAM_CODEC.decode(buf);
            return new AlchemyTraits(fields, slotSelection, stacks);
      });

            @Override
      public StreamCodec<RegistryFriendlyByteBuf, AlchemyTraits> streamCodec() {
            return STREAM_CODEC;
      }
      
      public static final Codec<AlchemyFields> FIELDS_CODEC = RecordCodecBuilder.create(
                  in -> in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(AlchemyFields::size),
                              ModSound.MAP_CODEC.forGetter(AlchemyFields::sound)
                  ).apply(in, AlchemyFields::new)
      );
      
      @Override
      public Codec<AlchemyFields> fieldCodec() {
            return FIELDS_CODEC;
      }
}
