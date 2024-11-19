package com.beansgalaxy.backpacks.traits.chest.screen;

import com.beansgalaxy.backpacks.network.serverbound.TinyHotbarClick;
import com.beansgalaxy.backpacks.network.serverbound.TinyMenuClick;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class EntityChestScreen extends TinyChestScreen {
      private final BackpackEntity backpack;

      public static void openScreen(BackpackEntity backpack, ChestTraits traits) {
            EntityChestScreen screen = new EntityChestScreen(backpack, traits);
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(screen);
      }

      public EntityChestScreen(BackpackEntity backpack, ChestTraits traits) {
            super(traits);
            this.backpack = backpack;
      }

      @Override
      protected void init() {
            super.init();
            initHotBarSlots();
      }

      @Override
      protected void tinyHotbarClick(TinyClickType clickType, InventoryMenu menu, LocalPlayer player, int index) {
            traits.tinyHotbarClick(backpack, index, clickType, menu, player);
            TinyHotbarClick.send(backpack, index, clickType);
      }

      @Override
      protected void tinyMenuClick(int index, TinyClickType clickType, SlotAccess carriedAccess, LocalPlayer player) {
            traits.tinyMenuClick(backpack, index, clickType, carriedAccess, player);
            TinyMenuClick.send(backpack, index, clickType);
      }

      @Override
      public ItemStack getStack() {
            return backpack.toStack();
      }

      @Override
      public PatchedComponentHolder getHolder() {
            return backpack;
      }

      @Override
      public boolean isFocused() {
            return true;
      }
}
