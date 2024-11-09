package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;

public abstract class MutableBundleLike<T extends BundleLikeTraits> implements MutableItemStorage {
      public final ITraitData<List<ItemStack>> stacks;
      private final PatchedComponentHolder holder;
      protected final T traits;

      public MutableBundleLike(T traits, PatchedComponentHolder holder) {
            this.holder = holder;
            this.traits = traits;
            stacks = ITraitData.ITEM_STACKS.get(holder);
      }

      public List<ItemStack> getItemStacks() {
            return stacks.get();
      }

      @Override
      public ModSound sound() {
            return traits.sound();
      }

      @Override
      public void push() {
            stacks.push();
      }

      @Override
      public boolean isEmpty() {
            return stacks.isEmpty();
      }

      @Override
      public ItemStack removeItem(int slot) {
            ItemStack returned = ItemStack.EMPTY;
            List<ItemStack> stacks = getItemStacks();
            if (stacks.size() > slot) {
                  ItemStack stack = stacks.get(slot);
                  int maxCount = stack.getMaxStackSize();
                  if (stack.getCount() > maxCount) {
                        stack.shrink(maxCount);
                        returned = stack.copyWithCount(maxCount);
                  } else
                        returned = stacks.remove(slot);
            }
            return returned;
      }

      public ItemStack addItem(ItemStack other, Player player) {
            return addItem(other, 0, player);
      }

      public ItemStack addItem(ItemStack inserted, int slot, Player player) {
            if (!traits.canItemFit(holder, inserted))
                  return null;

            int spaceLeft = this.getMaxAmountToAdd(inserted);
            int toInsert = Math.min(inserted.getCount(), spaceLeft);
            if (toInsert == 0)
                  return null;

            int count = toInsert;
            if (inserted.isStackable()) {
                  for (ItemStack stored : getItemStacks()) {
                        if (inserted.isEmpty() || count < 1)
                              return ItemStack.EMPTY;

                        if (ItemStack.isSameItemSameComponents(stored, inserted)) {
                              int insert = Math.min(stored.getMaxStackSize() - stored.getCount(), count);
                              stored.grow(insert);
                              inserted.shrink(insert);
                              count -= insert;
                        }
                  }
            }

            if (!inserted.isEmpty()) {
                  int selectedSlot = Math.min(getSelectedSlot(player), getItemStacks().size());
                  ItemStack split = inserted.split(count);
                  getItemStacks().add(selectedSlot, split);
                  traits.getSlotSelection(holder).grow(selectedSlot);
            }

            return inserted;
      }

      public int getMaxAmountToAdd(ItemStack stack) {
            Fraction size = Fraction.getFraction(traits.size(), 1);
            Fraction weight = Traits.getWeight(getItemStacks());
            Fraction weightLeft = size.subtract(weight);
            return Math.max(weightLeft.divideBy(Traits.getItemWeight(stack)).intValue(), 0);
      }

      public ItemStack removeItem(ItemStack other, Player player) {
            return removeItem(0);
      }

      public ItemStack splitItem(int slot) {
            List<ItemStack> stacks = getItemStacks();
            ItemStack stack = stacks.get(slot);
            ItemStack split = stack.split(Mth.ceil(stack.getCount() / 2f));

            if (stack.isEmpty() && slot < stacks.size())
                  stacks.remove(slot);

            return split;
      }

      public int getSelectedSlot(Player player) {
            return traits.getSelectedSlot(holder, player);
      }

      public int getSelectedSlotSafe(Player player) {
            return traits.getSelectedSlotSafe(holder, player);
      }

      @Override
      public Fraction fullness() {
            List<ItemStack> stacks = this.stacks.get();
            if (stacks == null) {
                  return Fraction.ZERO;
            }

            return Traits.getWeight(stacks, traits.size());
      }
}
