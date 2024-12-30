package com.beansgalaxy.backpacks.util;

import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

public abstract class DraggingContainer {
      public int backpackDragType = 0;
      public Slot backpackDraggedSlot = null;
      public final HashMap<Slot, ItemStack> backpackDraggedSlots = new HashMap<>();

      public abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type);
}
