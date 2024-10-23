package com.beansgalaxy.backpacks.client;

import com.beansgalaxy.backpacks.access.MinecraftAccessor;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.network.clientbound.OpenChestTraits;
import com.beansgalaxy.backpacks.traits.chest.ChestMenu;
import com.beansgalaxy.backpacks.traits.chest.ChestScreen;
import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class CommonAtClient {

      public static void playSound(SoundEvent soundEvent, float volume, float pitch) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, pitch, volume));
      }

      public static EnderStorage getEnderStorage() {
            MinecraftAccessor instance = (MinecraftAccessor) Minecraft.getInstance();
            return instance.beans_Backpacks_2$getEnder();
      }

      public static Level getLevel() {
            return Minecraft.getInstance().level;
      }

      public static int getInt() {
            return 200;
      }

      public static void openChestTrait(OpenChestTraits pkt) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            AbstractContainerMenu containerMenu = player.containerMenu;
            Slot slot = containerMenu.getSlot(pkt.slotIndex());
            ItemStack backpack = slot.getItem();

            Optional<ChestTraits> optional = ChestTraits.get(backpack);
            if (optional.isEmpty()) {
                  return;
            }

            ChestTraits chestTraits = optional.get();
            ItemStack carried = containerMenu.getCarried();
            Inventory playerInventory = player.getInventory();
            ChestMenu chestMenu = new ChestMenu(pkt.containerId(), playerInventory, chestTraits, PatchedComponentHolder.of(backpack), containerMenu);
            player.containerMenu = chestMenu;

            MutableComponent name = Component.empty().append(backpack.getHoverName()).withStyle(ChatFormatting.DARK_GRAY);
            if (backpack.has(DataComponents.CUSTOM_NAME)) {
                  name.withStyle(ChatFormatting.ITALIC);
            }

            player.containerMenu.setCarried(carried);
            AbstractContainerScreen<?> previousScreen = minecraft.screen instanceof AbstractContainerScreen<?> previousContainer ? previousContainer : null;
            ChestScreen chestScreen = new ChestScreen(chestMenu, playerInventory, name, previousScreen);
            minecraft.setScreen(chestScreen);
      }

      public static void openInventory(Player player) {
            Minecraft.getInstance().setScreen(new InventoryScreen(player));
      }

      public static void closeChestTrait(Player player) {
            if (player instanceof LocalPlayer localPlayer) {
                  localPlayer.connection.send(new ServerboundContainerClosePacket(localPlayer.containerMenu.containerId));
            }
      }
}
