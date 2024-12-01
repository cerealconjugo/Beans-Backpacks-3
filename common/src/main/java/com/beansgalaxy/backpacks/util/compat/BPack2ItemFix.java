package com.beansgalaxy.backpacks.util.compat;

import com.beansgalaxy.backpacks.components.ender.EmptyEnderItem;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.bundle.BundleTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class BPack2ItemFix extends DataFix {
      public BPack2ItemFix(Schema outputSchema, boolean changesType) {
            super(outputSchema, changesType);
      }

      @Override protected TypeRewriteRule makeRule() {
            return this.writeFixAndRead("backpacks-2 to backpacks-3 items", this.getInputSchema().getType(References.ITEM_STACK), this.getOutputSchema().getType(References.ITEM_STACK), (p_331180_) -> {
                  Optional<Dynamic<?>> optional = ItemStackData.read(p_331180_).map((data) -> {
                        fixItemStack(data);
                        return data.write();
                  });
                  return DataFixUtils.orElse(optional, p_331180_);
            });
      }

      private void fixItemStack(ItemStackData<?> data) {
            String item = data.item;
            switch (item) {
                  case "beansbackpacks:metal_backpack" -> {
                        Optional<? extends Dynamic<?>> optional = data.components.get("minecraft:custom_data").result();
                        if (optional.isEmpty()) {
                              data.item = "beansbackpacks:iron_backpack";
                              break;
                        }

                        Dynamic<?> dynamic = optional.get();
                        DataResult<CustomData> parse = CustomData.CODEC.parse(dynamic);
                        if (parse.isError()) {
                              data.components.remove("minecraft:custom_data");
                              data.item = "beansbackpacks:iron_backpack";
                              break;
                        }

                        CustomData customData = parse.getOrThrow();
                        CompoundTag compoundTag = customData.copyTag();
                        String backpack_id = compoundTag.getString("backpack_id");
                        switch (backpack_id) {
                              case "gold" -> data.item = "beansbackpacks:gold_backpack";
                              case "netherite" -> data.item = "beansbackpacks:netherite_backpack";
                              default -> data.item = "beansbackpacks:iron_backpack";
                        }

                        compoundTag.remove("backpack_id");
                        if (compoundTag.isEmpty())
                              data.components.remove("minecraft:custom_data");
                  }
                  case "beansbackpacks:winged_backpack" -> {
                        data.item = "minecraft:elytra";
                        Stream<? extends Dynamic<?>> stream = Stream.of(data.write());
                        Dynamic<?> itemList = data.components.createList(stream);
                        data.setComponent("beansbackpacks:data_item_list", itemList);

                        data.item = "beansbackpacks:backpack";
                        data.removeComponent("minecraft:enchantments");
                        data.encode(
                                    "beansbackpacks:bundle",
                                    Traits.BUNDLE.codec(),
                                    new BundleTraits(ModSound.CRUNCH, 5)
                        ).encode(
                                    "minecraft:item_name",
                                    DataComponents.ITEM_NAME.codec(),
                                    Component.translatable("item.beansbackpacks.legacy.winged_backpack")
                        ).encode(
                                    "minecraft:rarity",
                                    DataComponents.RARITY.codec(),
                                    Rarity.UNCOMMON
                        );
                  }
                  case "beansbackpacks:ender_backpack" -> {
                        data.item = "beansbackpacks:empty_ender_pouch";
                        data.encode(
                                    "beansbackpacks:empty_ender",
                                    Traits.EMPTY_ENDER.codec(),
                                    new EmptyEnderItem.UnboundEnderTraits(EnderStorage.LEGACY_ENDER_LOCATION)
                        ).encode(
                                    "minecraft:item_name",
                                    DataComponents.ITEM_NAME.codec(),
                                    Component.translatable("item.beansbackpacks.legacy.ender_backpack")
                        );
                  }
            }
      }

      static class ItemStackData<T> {
            private String item;
            private final int count;
            private Dynamic<T> components;
            private final Dynamic<T> remainder;

            private ItemStackData(String pItem, int pCount, Dynamic<T> pNbt) {
                  this.item = NamespacedSchema.ensureNamespaced(pItem);
                  this.count = pCount;
                  this.components = pNbt.get("components").orElseEmptyMap();
                  this.remainder = pNbt.remove("components");
            }

            public static Optional<? extends ItemStackData<?>> read(Dynamic<?> pTag) {
                  return pTag.get("id").asString().apply2stable((p_331191_, p_330701_) ->
                                  new ItemStackData<>(p_331191_, p_330701_.intValue(), pTag.remove("id").remove("count")), pTag.get("count").asNumber()).result();
            }

            public void setComponent(String pComponent, Dynamic<?> pValue) {
                  this.components = this.components.set(pComponent, pValue);
            }

            public void setComponent(String pComponent, OptionalDynamic<?> pValue) {
                  pValue.result().ifPresent((p_332105_) -> {
                        this.components = this.components.set(pComponent, p_332105_);
                  });
            }

            public void removeComponent(String pComponent) {
                  this.components = components.remove(pComponent);
            }

            public Dynamic<?> write() {
                  Dynamic<?> dynamic = this.remainder.emptyMap().set("id", this.remainder.createString(this.item)).set("count", this.remainder.createInt(this.count));
                  if (!this.components.equals(this.remainder.emptyMap())) {
                        dynamic = dynamic.set("components", this.components);
                  }

                  return mergeRemainder(dynamic, this.remainder);
            }

            private static <T> Dynamic<T> mergeRemainder(Dynamic<T> pTag, Dynamic<?> pRemainder) {
                  DynamicOps<T> dynamicops = pTag.getOps();
                  return dynamicops.getMap(pTag.getValue()).flatMap((p_330670_) ->
                                dynamicops.mergeToMap(pRemainder.convert(dynamicops).getValue(), p_330670_)).map((p_331482_) ->
                                        new Dynamic<>(dynamicops, p_331482_)).result().orElse(pTag);
            }

            public boolean is(String pItem) {
                  return this.item.equals(pItem);
            }

            public boolean is(Set<String> pItems) {
                  return pItems.contains(this.item);
            }

            public <M> ItemStackData<T> encode(String name, Codec<M> codec, M defaultInstance) {
                  DynamicOps<T> ops = components.getOps();
                  DataResult<T> encode = codec.encodeStart(ops, defaultInstance);
                  if (encode.isSuccess()) {
                        T value = encode.getOrThrow();
                        Dynamic<T> dynamic = new Dynamic<>(ops, value);
                        setComponent(name, dynamic);
                  }
                  else encode.ifError(error -> {
                        System.out.println(error.message());
                  });

                  return this;
            }
      }
}
