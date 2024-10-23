package com.beansgalaxy.backpacks.access;

import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public interface ChestTraitAccess {

      void openChestTraits(ChestTraits chestTraits, Slot slot, AbstractContainerMenu menu);

}
