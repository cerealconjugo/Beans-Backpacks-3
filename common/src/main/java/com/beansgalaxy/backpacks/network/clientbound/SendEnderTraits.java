package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.client.CommonAtClient;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.traits.IDeclaredFields;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class SendEnderTraits implements Packet2C {
      private final GenericTraits traits;
      private final UUID owner;
      private final ResourceLocation location;

      public SendEnderTraits(RegistryFriendlyByteBuf buf) {
            this.owner = buf.readUUID();
            location = ResourceLocation.STREAM_CODEC.decode(buf);

            TraitComponentKind<? extends GenericTraits, ? extends IDeclaredFields> kind = TraitComponentKind.STREAM_CODEC.decode(buf);
            traits = kind.streamCodec().decode(buf);
      }

      private SendEnderTraits(UUID owner, ResourceLocation location, GenericTraits traits) {
            this.owner = owner;
            this.location = location;
            this.traits = traits;
      }

      public static void send(ServerPlayer sender, UUID owner, ResourceLocation location) {
            GenericTraits traits = EnderStorage.get(sender.level()).get(owner, location);
            new SendEnderTraits(owner, location, traits).send2C(sender);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.ENDER_TRAIT_2C;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeUUID(owner);
            ResourceLocation.STREAM_CODEC.encode(buf, location);

            TraitComponentKind<GenericTraits, ? extends IDeclaredFields> kind = traits.kind();
            TraitComponentKind.STREAM_CODEC.encode(buf, kind);
            kind.encode(buf, traits);
      }

      @Override
      public void handle() {
            CommonAtClient.getEnderStorage().set(owner, location, traits);
      }

      public static Type<SendEnderTraits> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":send_ender_traits_c"));

      @Override
      public Type<SendEnderTraits> type() {
            return ID;
      }
}
