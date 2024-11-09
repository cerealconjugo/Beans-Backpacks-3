package com.beansgalaxy.backpacks.traits.bundle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BundleSlots extends Slot {
      public static final int SPACING = 18;
      public static final int COLUMN_START = 4;
      public static final int MAX_ROWS = 3;
      public static final int ADD_ROW = 9;
      public static final int MAX_SLOTS = 69;
      public static final ResourceLocation INPUT = ResourceLocation.withDefaultNamespace("sprites/empty_slot_input_large");
      public final State state;

      public BundleSlots(BundleMutable container, int slot, int x, int y, State state) {
            super(container, slot, x, y);
            this.state = state;
      }

      @Override
      public boolean isActive() {
            return !State.HIDDEN.equals(state);
      }

      @Override
      public boolean isHighlightable() {
            return !State.HIDDEN.equals(state);
      }

      @Override
      public ItemStack getItem() {
            if (!State.ACTIVE.equals(state))
                  return ItemStack.EMPTY;
            return super.getItem();
      }

      @Override
      public int getMaxStackSize() {
            return 9999;
      }


      @Override
      public boolean mayPickup(Player $$0) {
            return !State.EMPTY.equals(state);
      }

      public enum State {
            ACTIVE,
            EMPTY,
            HIDDEN
      }

      public static int[] getXY(int containerSize, int slot, boolean hasSpace) {
            int slots = Math.min(MAX_SLOTS, containerSize) + 1;
            int index;

            if (hasSpace) {
                  index = slot + 1;
            }
            else {
                  index = slot;
                  if (MAX_SLOTS >= containerSize)
                        slots -= 1;
            }

            int columns = COLUMN_START;
            int limit = COLUMN_START * MAX_ROWS;
            if (slots > limit) {
                  columns = Mth.ceil(slots / (MAX_ROWS + 0.0));
                  if (columns % 2 == 1) columns += 1;
            }
            if (columns > 9) { // AT 9 COLUMNS ADD A 4th ROW
                  columns = Mth.ceil(slots / (MAX_ROWS + 1.0));
                  if (columns % 2 == 1) columns += 1;
            }
            int offsetY = 0;
            if (columns > 11) { // AT 11 COLUMNS ADD A 5th ROW
                  columns = Mth.ceil(slots / (MAX_ROWS + 2.0));
                  if (columns % 2 == 1) columns += 1;
                  offsetY = -9;
            }
            if (columns > 13) {
                  columns = 14;
            }

            int shift = slots % columns;
            int firstRowSize = (slots - 1) % columns + 1;
            float offsetX;
            if (index < shift)
                  offsetX = (columns * 2 - firstRowSize) / 2f;
            else
                  offsetX = Math.min(columns, slots) / 2f;

            int k = index + columns - shift;
            int column = k % columns;
            int x = (int) ((column - offsetX) * SPACING) + 89;

            int row = (index - firstRowSize + columns) / columns;
            int pos = row * SPACING;
            int rows = (slots - 1) / columns;
            int top = rows * (SPACING / 2);
            int y = (pos - top) + 99 + offsetY;

            return new int[]{x, y};
      }
}
