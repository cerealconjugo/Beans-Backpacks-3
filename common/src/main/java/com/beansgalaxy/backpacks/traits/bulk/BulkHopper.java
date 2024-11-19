package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.data.HopperTraitContainer;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BulkHopper extends HopperTraitContainer<BulkMutable> {

      public BulkHopper(BackpackEntity backpack, BulkTraits traits) {
            super(backpack, traits.mutable(backpack));
      }

      @Override
      public int getContainerSize() {
            return 1;
      }

      @Override @NotNull
      public ItemStack getItem(int i) {
            BulkMutable.BulkStacks bulkList = mutable.bulkList.get();
            if (bulkList.isEmpty())
                  return ItemStack.EMPTY;

            List<BulkMutable.EmptyStack> emptyStacks = bulkList.emptyStacks();
            BulkMutable.EmptyStack first = emptyStacks.getFirst();
            Holder<Item> itemHolder = bulkList.itemHolder();
            int maxStackSize = first.getMaxStackSize(itemHolder);
            int min = Math.min(maxStackSize, first.amount);
            ItemStack stack = first.withItem(itemHolder, min);

            setChanged();
            return stack;
      }

      @Override @NotNull
      public ItemStack removeItem(int i, int amount) {
            BulkMutable.BulkStacks bulkList = mutable.bulkList.get();
            if (bulkList.isEmpty())
                  return ItemStack.EMPTY;

            List<BulkMutable.EmptyStack> emptyStacks = bulkList.emptyStacks();
            BulkMutable.EmptyStack first = emptyStacks.getFirst();
            Holder<Item> itemHolder = bulkList.itemHolder();
            int maxStackSize = first.getMaxStackSize(itemHolder);
            int min = Math.min(maxStackSize, first.amount);
            int count = Math.min(min, amount);
            ItemStack stack = first.splitItem(itemHolder, count);

            setChanged();
            return stack;
      }

      @Override @NotNull
      public ItemStack removeItemNoUpdate(int i) {
            BulkMutable.BulkStacks bulkList = mutable.bulkList.get();
            if (bulkList.isEmpty())
                  return ItemStack.EMPTY;

            List<BulkMutable.EmptyStack> emptyStacks = bulkList.emptyStacks();
            BulkMutable.EmptyStack first = emptyStacks.getFirst();
            Holder<Item> itemHolder = bulkList.itemHolder();
            int maxStackSize = first.getMaxStackSize(itemHolder);
            int min = Math.min(maxStackSize, first.amount);
            ItemStack stack = first.splitItem(itemHolder, min);

            setChanged();
            return stack;
      }

      @Override
      public void setItem(int i, @NotNull ItemStack itemStack) {
            ItemStack stack = mutable.addItem(itemStack);
            if (stack != null)
                  setChanged();
      }

      @Override
      public void clearContent() {

      }
}
