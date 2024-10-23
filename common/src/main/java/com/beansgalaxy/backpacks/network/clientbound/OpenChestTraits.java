package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.client.CommonAtClient;
import com.beansgalaxy.backpacks.network.Network2C;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;

public record OpenChestTraits(int slotIndex, int containerSlot, int containerId) implements Packet2C {

      public OpenChestTraits(RegistryFriendlyByteBuf buf) {
            this(buf.readInt(), buf.readInt(), buf.readInt());
      }

      public static void send(ServerPlayer sender, int containerId, Slot slot) {
            new OpenChestTraits(slot.index, slot.getContainerSlot(), containerId).send2C(sender);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.OPEN_CHEST_2C;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(slotIndex);
            buf.writeInt(containerSlot);
            buf.writeInt(containerId);
      }

      @Override
      public void handle() {
            CommonAtClient.openChestTrait(this);
      }

      public static Type<OpenChestTraits> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":open_chest_trait_c"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
