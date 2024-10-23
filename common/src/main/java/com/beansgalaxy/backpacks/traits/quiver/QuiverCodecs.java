package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.util.SlotSelection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class QuiverCodecs implements ITraitCodec<QuiverTraits, QuiverFields> {
      public static final QuiverCodecs INSTANCE = new QuiverCodecs();

      public static final Codec<QuiverTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").forGetter(QuiverTraits::size),
                        ModSound.MAP_CODEC.forGetter(QuiverTraits::sound),
                        ItemStack.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(QuiverTraits::stacks)
            ).apply(in, (size, sound, stacks) -> new QuiverTraits(new QuiverFields(size, sound), stacks))
      );

      @Override
      public Codec<QuiverTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, QuiverFields> STREAM_FIELDS_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, QuiverFields fields) {
                  buf.writeInt(fields.size());
                  ModSound.STREAM_CODEC.encode(buf, fields.sound());
                  IDeclaredFields.encodeLocation(buf, fields);
            }

            @Override
            public QuiverFields decode(RegistryFriendlyByteBuf buf) {
                  return new QuiverFields(
                              buf.readInt(),
                              ModSound.STREAM_CODEC.decode(buf)
                  ).toReference(IDeclaredFields.decodeLocation(buf));
            }
      };

      public static final StreamCodec<RegistryFriendlyByteBuf, QuiverTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            STREAM_FIELDS_CODEC.encode(buf, traits.fields());
            ItemStack.LIST_STREAM_CODEC.encode(buf, traits.stacks());
            SlotSelection.STREAM_CODEC.encode(buf, traits.slotSelection);
      }, buf -> {
            QuiverFields fields = STREAM_FIELDS_CODEC.decode(buf);
            List<ItemStack> stacks = ItemStack.LIST_STREAM_CODEC.decode(buf);
            SlotSelection slotSelection = SlotSelection.STREAM_CODEC.decode(buf);
            return new QuiverTraits(fields, slotSelection, stacks);
      });

            @Override
      public StreamCodec<RegistryFriendlyByteBuf, QuiverTraits> streamCodec() {
            return STREAM_CODEC;
      }
      
      public static final Codec<QuiverFields> FIELDS_CODEC = RecordCodecBuilder.create(
                  in -> in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(QuiverFields::size),
                              ModSound.MAP_CODEC.forGetter(QuiverFields::sound)
                  ).apply(in, QuiverFields::new)
      );
      
      @Override
      public Codec<QuiverFields> fieldCodec() {
            return FIELDS_CODEC;
      }
}
