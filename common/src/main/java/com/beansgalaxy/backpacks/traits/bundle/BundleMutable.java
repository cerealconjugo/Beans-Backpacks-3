package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BundleMutable extends MutableBundleLike<BundleTraits> implements Container {
      BundleMutable(BundleTraits traits, PatchedComponentHolder holder) {
            super(traits, holder);
      }

      @Override
      public int getContainerSize() {
            return traits.size();
      }

      @Override @NotNull
      public ItemStack getItem(int slot) {
            return slot >= this.getItemStacks().size() ? ItemStack.EMPTY : this.getItemStacks().get(slot);
      }

      @Override @NotNull
      public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = getItem(slot).split(amount);
            if (stack.isEmpty()) {
                  if (getContainerSize() > slot)
                        this.getItemStacks().remove(slot);
            }
            return stack;
      }

      @Override @NotNull
      public ItemStack removeItemNoUpdate(int i) {
            return removeItem(i);
      }

      @Override
      public void setItem(int slot, ItemStack stack) {
            int containerSize = getContainerSize();
            if (!stack.isEmpty())
                  if (getContainerSize() > slot)
                        getItemStacks().set(slot, stack);
                  else getItemStacks().add(slot, stack);
            else if (containerSize > slot)
                  getItemStacks().remove(slot);
      }

      @Override
      public void setChanged() {

      }

      @Override
      public boolean stillValid(Player player) {
            return true;
      }

      @Override
      public void clearContent() {
            getItemStacks().clear();
      }

      public int size() {
            return traits.size();
      }

      public ModSound sound() {
            return traits.sound();
      }
}
