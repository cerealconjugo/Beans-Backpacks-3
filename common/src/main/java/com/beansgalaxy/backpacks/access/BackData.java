package com.beansgalaxy.backpacks.access;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface BackData {
      static BackData get(Player player) {
            return (BackData) player.getInventory();
      }

      boolean isActionKeyDown();

      void setActionKey(boolean actionKeyIsDown);

      boolean isMenuKeyDown();

      void setMenuKey(boolean menuKeyIsDown);

      NonNullList<ItemStack> beans_Backpacks_3$getBody();

}
