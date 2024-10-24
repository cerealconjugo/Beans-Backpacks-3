package com.beansgalaxy.backpacks.events;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;

public class NetworkPackages {
      private static final String PROTOCOL = "1";
      public static SimpleChannel INSTANCE = null;
//                  NetworkRegistry.newSimpleChannel(
//                  new ResourceLocation(Constants.MOD_ID, "main"),
//                  () -> PROTOCOL,
//                  PROTOCOL::equals,
//                  PROTOCOL::equals);

      public static void register() {
            int i = 0;
            for (Network2S value : Network2S.values())
                  register2S(value.packet, i++);

            for (Network2C value : Network2C.values())
                  register2C(value.packet, i++);
      }

      private static <T extends Packet2S> void register2S(Network2S.DynamicLoaderPacket<? super RegistryFriendlyByteBuf, T> packet, int i) {
            NetworkPackages.INSTANCE.messageBuilder(packet.clazz, i, NetworkDirection.PLAY_TO_SERVER)
                        .encoder((msg, buf) -> packet.encode(buf, msg))
                        .decoder(packet::decode)
                        .consumerMainThread((msg, ctx) -> {
                              ServerPlayer sender = ctx.getSender();
                              msg.handle(sender);
                        }).add();
      }

      public static <T extends Packet2C> void register2C(Network2C.DynamicLoaderPacket<? super RegistryFriendlyByteBuf, T> packet, int i) {
            NetworkPackages.INSTANCE.messageBuilder(packet.clazz, i, NetworkDirection.PLAY_TO_CLIENT)
                        .encoder((msg, buf) -> packet.encode(buf, msg))
                        .decoder(packet::decode)
                        .consumerMainThread((msg, ctx) -> {
                              msg.handle();
                        }).add();
      }

      public static void C2S(Connection mgs) {
            INSTANCE.send(PacketDistributor.SERVER.noArg(), mgs);
      }

      public static void S2C(Connection msg, ServerPlayer player) {
            INSTANCE.send(PacketDistributor.PLAYER.with(player), msg);
      }

      public static void S2All(Connection msg) {
            INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
      }

}
