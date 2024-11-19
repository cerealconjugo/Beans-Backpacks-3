package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.data.HopperTraitContainer;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChestHopper extends HopperTraitContainer<ChestMutable> {

      public ChestHopper(BackpackEntity backpack, ChestTraits traits) {
            super(backpack, traits.mutable(backpack));
      }

      @Override
      public int getContainerSize() {
            return mutable.size();
      }

      @Override @NotNull
      public ItemStack getItem(int i) {
            if (i >= getContainerSize())
                  return ItemStack.EMPTY;

            return mutable.getItem(i);
      }

      @Override @NotNull
      public ItemStack removeItem(int i, int amount) {
            if (i >= getContainerSize() || amount == 0)
                  return ItemStack.EMPTY;

            ItemStack stack = getItem(i).split(amount);
            setChanged();

            return stack;
      }

      @Override @NotNull
      public ItemStack removeItemNoUpdate(int i) {
            if (i >= getContainerSize())
                  return ItemStack.EMPTY;

            ItemStack stack = mutable.getItem(i);
            mutable.setItem(i, ItemStack.EMPTY);
            setChanged();
            return stack;
      }

      @Override
      public void setItem(int i, @NotNull ItemStack itemStack) {
            if (!mutable.canItemFit(itemStack))
                  return;

            mutable.setItem(i, itemStack);
            setChanged();
      }

      @Override
      public void clearContent() {

      }
}
