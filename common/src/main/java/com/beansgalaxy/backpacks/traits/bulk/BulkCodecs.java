package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BulkCodecs implements ITraitCodec<BulkTraits, BulkFields> {
      public static final BulkCodecs INSTANCE = new BulkCodecs();

      protected record BulkData(Holder<Item> item, List<BulkTraits.EmptyStack> stacks) {
            public static final Codec<BulkData> CODEC = RecordCodecBuilder.create(in ->
                        in.group(
                                    BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(BulkData::item),
                                    BulkTraits.EmptyStack.EMPTY_STACK_CODEC.listOf().fieldOf("stacks").forGetter(BulkData::stacks)
                        ).apply(in, BulkData::new)
            );

            public static final StreamCodec<RegistryFriendlyByteBuf, BulkData> STREAM_CODEC = new StreamCodec<>() {
                  @Override
                  public void encode(RegistryFriendlyByteBuf buf, BulkData bulkData) {
                        ByteBufCodecs.holderRegistry(Registries.ITEM).encode(buf, bulkData.item);
                        BulkTraits.EmptyStack.LIST_EMPTY_STACK_STREAM_CODEC.encode(buf, bulkData.stacks);
                  }

                  @Override @NotNull
                  public BulkData decode(RegistryFriendlyByteBuf buf) {
                        Holder<Item> item = ByteBufCodecs.holderRegistry(Registries.ITEM).decode(buf);
                        List<BulkTraits.EmptyStack> stacks = BulkTraits.EmptyStack.LIST_EMPTY_STACK_STREAM_CODEC.decode(buf);
                        return new BulkData(item, stacks);
                  }
            };
      }

      public static final Codec<BulkTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").forGetter(BulkTraits::size),
                        ModSound.MAP_CODEC.forGetter(BulkTraits::sound),
                        BulkData.CODEC.optionalFieldOf("data", new BulkData(BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR), List.of())).forGetter(traits -> new BulkData(traits.item, traits.emptyStacks))
            ).apply(in, (size, sound, data) -> new BulkTraits(new BulkFields(size, sound), data.item, data.stacks))
      );

      @Override
      public Codec<BulkTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BulkTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            BulkFields.STREAM_CODEC.encode(buf, traits.fields());
            ByteBufCodecs.holderRegistry(Registries.ITEM).encode(buf, traits.item);
            BulkTraits.EmptyStack.LIST_EMPTY_STACK_STREAM_CODEC.encode(buf, traits.emptyStacks);
      }, buf -> {
            BulkFields fields = BulkFields.STREAM_CODEC.decode(buf);
            Holder<Item> item = ByteBufCodecs.holderRegistry(Registries.ITEM).decode(buf);
            List<BulkTraits.EmptyStack> stacks = BulkTraits.EmptyStack.LIST_EMPTY_STACK_STREAM_CODEC.decode(buf);
            return new BulkTraits(fields, item, stacks);
      });
      
      @Override
      public StreamCodec<RegistryFriendlyByteBuf, BulkTraits> streamCodec() {
            return STREAM_CODEC;
      }

      public static final Codec<BulkFields> FIELDS_CODEC = RecordCodecBuilder.create(
                  in -> in.group(
                              PrimitiveCodec.INT.fieldOf("size").forGetter(BulkFields::size),
                              ModSound.MAP_CODEC.forGetter(BulkFields::sound)
                  ).apply(in, BulkFields::new)
      );

      @Override
      public Codec<BulkFields> fieldCodec() {
            return FIELDS_CODEC;
      }
}
