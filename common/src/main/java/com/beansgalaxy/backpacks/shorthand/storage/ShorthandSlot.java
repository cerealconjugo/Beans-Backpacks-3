package com.beansgalaxy.backpacks.shorthand.storage;

import com.beansgalaxy.backpacks.data.DataPack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;

public abstract class ShorthandSlot extends Slot {
      private final ShortContainer shortContainer;

      public ShorthandSlot(ShortContainer shortContainer, int pSlot, int pX, int pY) {
            super(shortContainer, pSlot, pX, pY);
            this.shortContainer = shortContainer;
      }

      @Override
      public boolean isActive() {
            return getContainerSlot() < shortContainer.getContainerSize();
      }

      public static class WeaponSlot extends ShorthandSlot {

            public WeaponSlot(Shorthand shorthand, int slot) {
                  super(shorthand.weapons, slot, getX(slot), getY(slot));
            }

            public static int getX(int slot) {
                  return 152 - (slot * 18);
            }

            public static int getY(int slot) {
                  return 164;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                  Item item = stack.getItem();
                  return item instanceof TieredItem
                  || item instanceof ProjectileWeaponItem
                  || item instanceof MaceItem
                  || DataPack.WEAPON_ITEM.contains(item);
            }
      }

      public static class ToolSlot extends ShorthandSlot {

            public ToolSlot(Shorthand shorthand, int slot) {
                  super(shorthand.tools, slot, getX(slot), getY(slot));
            }

            public static int getX(int slot) {
                  return 8 + (slot * 18);
            }

            public static int getY(int slot) {
                  return 164;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                  Item item = stack.getItem();
                  return item instanceof DiggerItem ||
                              item instanceof ShearsItem ||
                              DataPack.TOOL_ITEM.contains(item);
            }
      }
}
