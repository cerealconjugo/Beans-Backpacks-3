package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.registry.ModSound;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

public class BundleMenu extends AbstractContainerMenu {
      public static final int INV_OFFSET = 108;
      public final NonNullList<BundleSlots> backpackSlots = NonNullList.create();
      public final BackpackEntity backpack;
      private final int inventorySize;
      private final Player viewer;
      private BundleTraits.Mutable mutable;

      public BundleMenu(@Nullable MenuType<?> menuType, int id, Inventory playerInventory, BackpackEntity entity, BundleTraits.Mutable trait) {
            super(menuType, id);
            this.viewer = playerInventory.player;
            this.mutable = trait;
            this.backpack = entity;
            createInventorySlots(playerInventory);
            this.inventorySize = slots.size();
            updateSlots();
      }

      private void createInventorySlots(Inventory playerInventory) {
            for(int l = 0; l < 3; ++l) {
                  for(int k = 0; k < 9; ++k) {
                        this.addSlot(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 51 + INV_OFFSET));
                  }
            }
            for(int i1 = 0; i1 < 9; ++i1) {
                  this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 109 + INV_OFFSET));
            }
      }

      public void updateSlots() {
            boolean hasSpace = Traits.getWeight(mutable.getItemStacks()).compareTo(Fraction.getFraction(mutable.size(), 1)) < 0;
            int size = mutable.getContainerSize();
            int shift = Math.max(0, size - BundleSlots.MAX_SLOTS - (hasSpace? 0: 1));

            int slotsSize = slots.size();
            backpackSlots.clear();
            for (int i = 0; i < BundleSlots.MAX_SLOTS + 1; i++) {
                  BundleSlots slot = createSlot(i, shift, size, hasSpace);

                  backpackSlots.add(i, slot);
                  int index = i + inventorySize;
                  if (index >= slotsSize)
                        addSlot(slot);
                  else {
                        slot.index = index;
                        slots.set(index, slot);
                  }
            }
      }

      @Override
      public void slotsChanged(Container $$0) {
            super.slotsChanged($$0);
      }

      private BundleSlots createSlot(int i, int shift, int size, boolean hasSpace) {
            int backIndex = i + shift;
            BundleSlots bundleSlot;
            if (backIndex + shift < size) {
                  int[] xy = BundleSlots.getXY(size, backIndex, hasSpace);
                  bundleSlot = new BundleSlots(mutable, backIndex, xy[0], xy[1], BundleSlots.State.ACTIVE);
            } else if (hasSpace && backIndex + shift == size) {
                  int[] xy = BundleSlots.getXY(size, -1, true);
                  bundleSlot =  new BundleSlots(mutable, size, xy[0], xy[1], BundleSlots.State.EMPTY);
            } else {
                  bundleSlot =  new BundleSlots(mutable, backIndex, 0, 0, BundleSlots.State.HIDDEN);
            }
            return bundleSlot;
      }

      @Override
      public ItemStack quickMoveStack(Player player, int index) {
            Slot slot = this.slots.get(index);
            ItemStack clickedStack = slot.getItem();
            if (slot instanceof BundleSlots menuSlot) {
                  if (!BundleSlots.State.ACTIVE.equals(menuSlot.state))
                        return ItemStack.EMPTY;
                  int mutableIndex = index - inventorySize;
                  clickedStack = mutable.getItem(mutableIndex);
                  if (this.moveItemStackTo(clickedStack, 0, inventorySize, true))
                        mutable.sound().at(player, ModSound.Type.REMOVE);

                  if (clickedStack.isEmpty())
                        mutable.removeItemNoUpdate(mutableIndex);
            }
            else if (mutable.addItem(clickedStack, player) != null)
                  mutable.sound().at(player, ModSound.Type.INSERT);

            return ItemStack.EMPTY;
      }

      @Override
      public boolean stillValid(Player var1) {
            return true;
      }

      @Override
      public void clicked(int $$0, int $$1, ClickType $$2, Player player) {
            super.clicked($$0, $$1, $$2, player);
            updateSlots();
      }
}
