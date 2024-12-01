package com.beansgalaxy.backpacks.util.data_fixers;

import com.beansgalaxy.backpacks.Constants;
import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class BPack2PlayerFix extends DataFix {
      public BPack2PlayerFix(Schema outputSchema, boolean changesType) {
            super(outputSchema, changesType);
      }

      @Override protected TypeRewriteRule makeRule() {
            return this.writeFixAndRead(
                        "backpacks-2 to backpacks-3 Back Slot",
                        this.getInputSchema().getType(References.PLAYER),
                        this.getOutputSchema().getType(References.PLAYER),
                        this::fixPlayerBackSlot
            );
      }

      private <T> Dynamic<T> fixPlayerBackSlot(Dynamic<T> player) {
            AtomicReference<Dynamic<T>> backSlot = new AtomicReference<>();
            AtomicReference<Dynamic<T>> contents = new AtomicReference<>();
            Dynamic<T> updated = player.update(
                        "Inventory",
                        inventory -> {
                              Dynamic<T> cast = inventory.castTyped(player.getOps());
                              return removeBackSlot(cast, backSlot, contents);
                        }
            );

            Dynamic<T> itemStack = backSlot.get();
            if (itemStack != null) {
                  Dynamic<T> backStack = saveContents(contents.get(), updateVersion(itemStack));
                  Dynamic<T> back = updated.emptyMap().set("back", backStack);
                  return updated.set(Constants.MOD_ID, back);
            }

            return updated;
      }

      public static <T> Dynamic<T> updateVersion(Dynamic<T> dynamic) {
            DataFixer dataFixer = DataFixers.getDataFixer();
            int version = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
            return dataFixer.update(References.ITEM_STACK, dynamic, 3469, version);
      }

      @Nullable
      private <T> Stream<? extends Dynamic<T>> itemsFromContents(Dynamic<T> stacks) {
            DynamicOps<T> ops = stacks.getOps();
            DataResult<Stream<T>> result = ops.getStream(stacks.getValue());
            if (result.isError())
                  return null;

            return result.getOrThrow().map(item ->
                        updateVersion(new Dynamic<>(ops, item))
            );
      }

      private <T> Dynamic<T> saveContents(Dynamic<T> contents, Dynamic<T> itemStack) {
            if (contents == null)
                  return itemStack;

            Optional<Stream<? extends Dynamic<T>>> optional1 = contents.get("Items").result().map(this::itemsFromContents);
            if (optional1.isEmpty())
                  return itemStack;

            Stream<? extends Dynamic<T>> items = optional1.get();
            Dynamic<T> components = itemStack.get("components").orElseEmptyMap();
            String trait = Constants.MOD_ID + ":data_item_list";

            Optional<? extends Dynamic<T>> optional = components.get(trait).result();
            Stream<? extends Dynamic<T>> stream1;
            if (optional.isEmpty())
                  stream1 = items;
            else {
                  Stream<? extends Dynamic<T>> itemList = itemsFromContents(optional.get());
                  if (itemList == null)
                        stream1 = items;
                  else
                        stream1 = Stream.concat(itemList, items);
            }

            Dynamic<T> item_list = components.createList(stream1);
            Dynamic<T> set = components.set(trait, item_list);
            return itemStack.set("components", set);
      }

      private <T> @NotNull Dynamic<T> removeBackSlot(Dynamic<T> inventory, AtomicReference<Dynamic<T>> backSlot, AtomicReference<Dynamic<T>> contents) {
            DynamicOps<T> ops = inventory.getOps();
            DataResult<Stream<T>> result = ops.getStream(inventory.getValue());
            if (result.isError())
                  return inventory;

            Stream<T> stream = result.getOrThrow();
            Stream<T> filter = stream.filter(value -> {
                  Dynamic<T> slot = new Dynamic<>(ops, value);
                  Optional<? extends Dynamic<T>> optBackSlot = slot.get("BackSlot").result();
                  if (optBackSlot.isEmpty())
                        return true;

                  backSlot.set(optBackSlot.get());
                  Optional<Dynamic<T>> optContents = slot.get("Contents").result();
                  optContents.ifPresent(contents::set);

                  return false;
            });

            T list = ops.createList(filter);
            return new Dynamic<>(ops, list);
      }

}
