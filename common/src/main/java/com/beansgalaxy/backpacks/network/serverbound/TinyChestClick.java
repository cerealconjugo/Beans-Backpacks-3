package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class TinyChestClick implements Packet2S {
      private final int containerId;
      private final int containerSlot;
      private final int index;
      private final int button;

      public TinyChestClick(RegistryFriendlyByteBuf buf) {
            this.containerId = buf.readInt();
            this.containerSlot = buf.readInt();
            this.index = buf.readInt();
            this.button = buf.readInt();
      }

      public TinyChestClick(int containerId, int containerSlot, int index, int button) {
            this.containerId = containerId;
            this.containerSlot = containerSlot;
            this.index = index;
            this.button = button;
      }

      public static void send(int containerId, Slot slot, int index, int button) {
            new TinyChestClick(containerId, slot.index, index, button).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.TINY_CHEST_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(containerId);
            buf.writeInt(containerSlot);
            buf.writeInt(index);
            buf.writeInt(button);
      }

      @Override
      public void handle(Player sender) {
            AbstractContainerMenu menu = sender.containerMenu;
            if (menu.containerId != containerId)
                  return;

            Slot slot = menu.getSlot(containerSlot);
            ItemStack stack = slot.getItem();
            Optional<ChestTraits> optional = ChestTraits.get(stack);
            if (optional.isEmpty()) {
                  return;
            }

            ChestTraits chestTraits = optional.get();

            SlotAccess carriedAccess = new SlotAccess() {
                  public ItemStack get() {
                        return menu.getCarried();
                  }

                  public boolean set(ItemStack p_150452_) {
                        menu.setCarried(p_150452_);
                        return true;
                  }
            };

            chestTraits.tinyMenuClick(slot, index, button, carriedAccess);
      }

      public static Type<TinyChestClick> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":tiny_chest_click_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
