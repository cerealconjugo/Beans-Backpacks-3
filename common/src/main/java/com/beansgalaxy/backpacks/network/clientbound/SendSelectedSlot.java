package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2C;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class SendSelectedSlot implements Packet2C {
      private final int selectedSlot;

      private SendSelectedSlot(int selectedSlot) {
            this.selectedSlot = selectedSlot;
      }

      public SendSelectedSlot(RegistryFriendlyByteBuf buf) {
            selectedSlot = buf.readInt();
      }

      public static void send(ServerPlayer player, int selectedSlot) {
            new SendSelectedSlot(selectedSlot).send2C(player);
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(selectedSlot);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.SELECTED_SLOT_2C;
      }

      @Override
      public void handle() {
            CommonClient.handleSetSelectedSlot(selectedSlot);
      }

      public static Type<SendSelectedSlot> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":send_selected_slot_c"));

      @Override
      public Type<SendSelectedSlot> type() {
            return ID;
      }
}
