package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.client.CommonAtClient;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class SendEnderEntry implements Packet2C {
      private final GenericTraits traits;
      private final UUID owner;
      private final ResourceLocation location;
      private final Component name;

      public SendEnderEntry(RegistryFriendlyByteBuf buf) {
            this.owner = buf.readUUID();
            location = ResourceLocation.STREAM_CODEC.decode(buf);

            TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind = TraitComponentKind.STREAM_CODEC.decode(buf);
            traits = kind.streamCodec().decode(buf);

            String name = buf.readUtf();
            this.name = Component.Serializer.fromJson(name, buf.registryAccess());
      }

      private SendEnderEntry(UUID owner, ResourceLocation location, GenericTraits traits, Component name) {
            this.owner = owner;
            this.location = location;
            this.traits = traits;
            this.name = name;
      }

      public static void send(ServerPlayer sender, UUID owner, ResourceLocation location, GenericTraits traits, Component name) {
            new SendEnderEntry(owner, location, traits, name).send2C(sender);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.ENDER_ENTRY_2C;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeUUID(owner);
            ResourceLocation.STREAM_CODEC.encode(buf, location);

            TraitComponentKind<GenericTraits, ? extends IDeclaredFields> kind = traits.kind();
            TraitComponentKind.STREAM_CODEC.encode(buf, kind);
            kind.encode(buf, traits);

            String json = Component.Serializer.toJson(name, buf.registryAccess());
            buf.writeUtf(json);
      }

      @Override
      public void handle() {
            EnderStorage enderStorage = CommonAtClient.getEnderStorage();
            enderStorage.set(owner, location, traits, name);
      }

      public static Type<SendEnderEntry> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":send_ender_entry_c"));

      @Override
      public Type<SendEnderEntry> type() {
            return ID;
      }
}
