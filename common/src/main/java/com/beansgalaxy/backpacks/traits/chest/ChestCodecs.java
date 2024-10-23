package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ChestCodecs implements ITraitCodec<ChestTraits, ChestFields> {
      public static final ChestCodecs INSTANCE = new ChestCodecs();

      record SlotRecord(int index, ItemStack item) {
            public static final Codec<SlotRecord> CODEC = RecordCodecBuilder.create(in ->
                        in.group(
                                    PrimitiveCodec.INT.fieldOf("index").forGetter(SlotRecord::index),
                                    ItemStack.CODEC.fieldOf("item").forGetter(SlotRecord::item)
                        ).apply(in, SlotRecord::new)
            );

      }

      public static final Codec<ChestTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("rows").forGetter(chest -> chest.fields().rows),
                        PrimitiveCodec.INT.fieldOf("columns").forGetter(chest -> chest.fields().columns),
                        ModSound.MAP_CODEC.forGetter(ChestTraits::sound),
                        SlotRecord.CODEC.listOf().optionalFieldOf("slots", List.of()).forGetter(chest ->
                                    chest.stacks.int2ObjectEntrySet().stream().map(entry ->
                                                new SlotRecord(entry.getIntKey(), entry.getValue())).toList()
                        )
            ).apply(in, (rows, columns, sound, data) -> {
                  Int2ObjectArrayMap<ItemStack> stacks = slotsToIntMap(data, rows * columns);
                  return new ChestTraits(new ChestFields(rows, columns, sound), stacks);
            })
      );

      private static Int2ObjectArrayMap<ItemStack> slotsToIntMap(List<SlotRecord> slots, int size) {
            Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>(size);

            for (SlotRecord slot : slots)
                  if (slot.index < size)
                        map.put(slot.index, slot.item);

            return map;
      }

      @Override
      public Codec<ChestTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, ChestFields> STREAM_FIELDS_CODEC = StreamCodec.of((buf, fields) -> {
            buf.writeInt(fields.rows);
            buf.writeInt(fields.columns);
            ModSound.STREAM_CODEC.encode(buf, fields.sound());
            IDeclaredFields.encodeLocation(buf, fields);
      }, buf -> new ChestFields(
                        buf.readInt(),
                        buf.readInt(),
                        ModSound.STREAM_CODEC.decode(buf)
            ).toReference(IDeclaredFields.decodeLocation(buf))
      );

      public static final StreamCodec<RegistryFriendlyByteBuf, ChestTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            STREAM_FIELDS_CODEC.encode(buf, traits.fields());
            Int2ObjectMap.FastEntrySet<ItemStack> entries = traits.stacks.int2ObjectEntrySet();
            buf.writeInt(entries.size());
            entries.forEach(slot -> {
                  buf.writeInt(slot.getIntKey());
                  ItemStack.STREAM_CODEC.encode(buf, slot.getValue());
            });
      }, buf -> {
            ChestFields fields = STREAM_FIELDS_CODEC.decode(buf);
            int entriesSize = buf.readInt();

            int size = fields.columns * fields.rows;
            Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>(size);
            for (int i = 0; i < entriesSize && i < size; i++) {
                  int index = buf.readInt();
                  ItemStack item = ItemStack.STREAM_CODEC.decode(buf);
                  map.put(index, item);
            }

            return new ChestTraits(fields, map);
      });
      
      @Override
      public StreamCodec<RegistryFriendlyByteBuf, ChestTraits> streamCodec() {
            return STREAM_CODEC;
      }

      public static final Codec<ChestFields> FIELDS_CODEC = RecordCodecBuilder.create(
                  in -> in.group(
                              PrimitiveCodec.INT.fieldOf("rows").forGetter(ChestFields::rows),
                              PrimitiveCodec.INT.fieldOf("columns").forGetter(ChestFields::columns),
                              ModSound.MAP_CODEC.forGetter(ChestFields::sound)
                  ).apply(in, ChestFields::new)
      );

      @Override
      public Codec<ChestFields> fieldCodec() {
            return FIELDS_CODEC;
      }
}
