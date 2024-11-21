package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2C;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SendWeaponSlot implements Packet2C {
      private final int player;
      private final int selectedSlot;
      private final ItemStack stack;

      private SendWeaponSlot(int player, int selectedSlot, ItemStack stack) {
            this.player = player;
            this.selectedSlot = selectedSlot;
            this.stack = stack;
      }

      public SendWeaponSlot(RegistryFriendlyByteBuf buf) {
            player = buf.readInt();
            selectedSlot = buf.readInt();
            stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
      }

      public static void send(ServerPlayer player, int selectedSlot, ItemStack stack) {
            new SendWeaponSlot(player.getId(), selectedSlot, stack).send2A(player);
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(player);
            buf.writeInt(selectedSlot);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.WEAPON_SLOT_2C;
      }

      @Override
      public void handle() {
            CommonClient.handleSendWeaponSlot(player, selectedSlot, stack);
      }

      public static Type<SendWeaponSlot> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":send_weapon_slot_c"));

      @Override
      public Type<SendWeaponSlot> type() {
            return ID;
      }
}
