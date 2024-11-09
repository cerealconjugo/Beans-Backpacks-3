package com.beansgalaxy.backpacks.events;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class CommonEvents {

      @SubscribeEvent
      public static void register(final RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            for (Network2S network : Network2S.values()) {
                  register(registrar, network.packet);
            }
            for (Network2C value : Network2C.values()) {
                  register(registrar, value.packet);
            }
      }

      private static <M extends Packet2C> void register(PayloadRegistrar registrar, Network2C.DynamicLoaderPacket<? super RegistryFriendlyByteBuf, M> packet) {
            registrar.playToClient(packet.type, packet, (m, iPayloadContext) ->
                        iPayloadContext.enqueueWork(m::handle)
            );
      }

      private static <M extends Packet2S> void register(PayloadRegistrar registrar, Network2S.DynamicLoaderPacket<? super RegistryFriendlyByteBuf, M> packet) {
            registrar.playToServer(packet.type, packet, (m, iPayloadContext) ->
                        iPayloadContext.enqueueWork(() ->
                                    m.handle(iPayloadContext.player())
                        )
            );
      }
}
