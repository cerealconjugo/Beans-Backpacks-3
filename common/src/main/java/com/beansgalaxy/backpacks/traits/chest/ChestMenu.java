package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.client.CommonAtClient;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChestMenu extends AbstractContainerMenu {
      private final ChestTraits.Mutable mutable;
      public final int rows;
      public final int columns;
      protected final List<Slot> traitSlots;
      private final PatchedComponentHolder holder;

      public ChestMenu(int containerId, Inventory playerInventory, ChestTraits traits, PatchedComponentHolder holder, AbstractContainerMenu menu) {
            this(containerId, traits, holder);
            this.previousMenu = menu;

            int $$5 = (this.rows - 4) * 18;
            int $$10;
            int $$9;

            for($$10 = 0; $$10 < 3; ++$$10) {
                  for($$9 = 0; $$9 < 9; ++$$9) {
                        this.addSlot(new Slot(playerInventory, $$9 + $$10 * 9 + 9, 8 + $$9 * 18, 103 + $$10 * 18 + $$5));
                  }
            }

            for($$10 = 0; $$10 < 9; ++$$10) {
                  this.addSlot(new Slot(playerInventory, $$10, 8 + $$10 * 18, 161 + $$5));
            }
      }

      private ChestMenu(int containerId, ChestTraits traits, PatchedComponentHolder holder) {
            super(null, containerId);

            this.mutable = traits.mutable();
            this.rows = traits.fields().rows();
            this.columns = traits.fields().columns();
            this.holder = holder;

            int colO = 81 + -columns * 9;
            ArrayList<Slot> list = new ArrayList<>();

            int $$10;
            int $$9;
            for($$10 = 0; $$10 < this.rows; ++$$10) {
                  for($$9 = 0; $$9 < this.columns; ++$$9) {
                        int slotIndex = $$9 + $$10 * columns;
                        Slot slot = new Slot(mutable, slotIndex, 8 + $$9 * 18 + colO, 18 + $$10 * 18) {

                              @Override
                              public void setChanged() {
                                    ChestTraits freeze = mutable.freeze();
                                    holder.set(Traits.CHEST, freeze);

                              }
                        };
                        list.add(slot);
                        this.addSlot(slot);
                  }
            }

            this.traitSlots = list;
      }

      public boolean stillValid(Player $$0) {
            return this.mutable.stillValid($$0);
      }

      public ItemStack quickMoveStack(Player $$0, int $$1) {
            ItemStack $$2 = ItemStack.EMPTY;
            Slot $$3 = this.slots.get($$1);
            if ($$3 != null && $$3.hasItem()) {
                  ItemStack $$4 = $$3.getItem();
                  $$2 = $$4.copy();
                  if ($$1 < this.rows * 9) {
                        if (!this.moveItemStackTo($$4, this.rows * 9, this.slots.size(), true)) {
                              return ItemStack.EMPTY;
                        }
                  } else if (!this.moveItemStackTo($$4, 0, this.rows * 9, false)) {
                        return ItemStack.EMPTY;
                  }

                  if ($$4.isEmpty()) {
                        $$3.setByPlayer(ItemStack.EMPTY);
                  } else {
                        $$3.setChanged();
                  }
            }

            return $$2;
      }

      @Override
      public void broadcastChanges() {
            super.broadcastChanges();
      }

      public void removed(Player player) {
            super.removed(player);
            this.mutable.stopOpen(player);
            openPreviousMenu(player);

            if (player.level().isClientSide)
                  CommonAtClient.closeChestTrait(player);
      }

      public AbstractContainerMenu previousMenu = null;

      public void openPreviousMenu(Player player) {
            ItemStack carried = getCarried();
            setCarried(ItemStack.EMPTY);

            if (previousMenu == null)
                  previousMenu = player.inventoryMenu;

            previousMenu.transferState(this);
            player.containerMenu = previousMenu;
            player.containerMenu.setCarried(carried);
      }
}
