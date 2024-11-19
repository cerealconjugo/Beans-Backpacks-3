package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class TinyHotbarClick implements Packet2S {
      private final int entityId;
      private final int index;
      private final TinyClickType clickType;

      public TinyHotbarClick(RegistryFriendlyByteBuf buf) {
            this.entityId = buf.readInt();
            this.index = buf.readInt();
            clickType = buf.readEnum(TinyClickType.class);
      }

      public TinyHotbarClick(int entityId, int index, TinyClickType clickType) {
            this.entityId = entityId;
            this.index = index;
            this.clickType = clickType;
      }

      public static void send(BackpackEntity backpack, int index, TinyClickType clickType) {
            new TinyHotbarClick(backpack.getId(), index, clickType).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.TINY_HOTBAR_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(entityId);
            buf.writeInt(index);
            buf.writeEnum(clickType);
      }

      @Override
      public void handle(Player sender) {
            Entity entity = sender.level().getEntity(entityId);
            if (entity instanceof BackpackEntity backpack) {
                  Optional<GenericTraits> optional = backpack.getTraits();
                  if (optional.isPresent() && optional.get() instanceof ItemStorageTraits storageTraits) {
                        storageTraits.tinyHotbarClick(backpack, index, clickType, sender.inventoryMenu, sender);
                  }
            }
      }

      public static Type<TinyHotbarClick> ID = new Type<>(ResourceLocation.parse(CommonClass.MOD_ID + ":tiny_hotbar_click_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
