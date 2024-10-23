package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.platform.Services;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface Packet2S extends CustomPacketPayload {
      Network2S getNetwork();

      default void send2S() {
            Network2S network = getNetwork();
            network.debugMsgEncode();
            Services.PLATFORM.send(network, this);
      }

      void encode(RegistryFriendlyByteBuf buf);

      void handle(ServerPlayer sender);
}
