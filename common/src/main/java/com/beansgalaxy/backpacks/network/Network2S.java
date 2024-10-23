package com.beansgalaxy.backpacks.network;

import com.beansgalaxy.backpacks.network.serverbound.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum Network2S {
      HOTKEY_2S(SyncHotkey.ID, SyncHotkey::encode, SyncHotkey::new, SyncHotkey::handle),
      PLACE_BACKPACK_2S(BackpackUseOn.ID, BackpackUseOn::encode, BackpackUseOn::new, BackpackUseOn::handle),
      USE_BACKPACK_2S(BackpackUse.ID, BackpackUse::encode, BackpackUse::new, BackpackUse::handle),
      PICK_BLOCK_2S(PickBlock.ID, PickBlock::encode, PickBlock::new, PickBlock::handle),
      TINY_CHEST_2S(TinyChestClick.ID, TinyChestClick::encode, TinyChestClick::new, TinyChestClick::handle),
      SYNC_SELECTED_SLOT_2S(SyncSelectedSlot.ID, SyncSelectedSlot::encode, SyncSelectedSlot::new, SyncSelectedSlot::handle),
      ;

      public final DynamicLoaderPacket<? super RegistryFriendlyByteBuf, ?> packet;
      <T extends Packet2S> Network2S(CustomPacketPayload.Type<T> id, BiConsumer<T, RegistryFriendlyByteBuf> encoder, Function<RegistryFriendlyByteBuf, T> decoder, BiConsumer<T, ServerPlayer> handle) {
            this.packet = new DynamicLoaderPacket<>(id, encoder, decoder, handle);
      }

      public void debugMsgEncode() {
//            System.out.println("encode = " + packet);
      }

      public void debugMsgDecode() {
//            System.out.println("decode = " + packet);
      }

      public class DynamicLoaderPacket<B extends RegistryFriendlyByteBuf, T extends Packet2S> implements StreamCodec<B, T> {
            public final CustomPacketPayload.Type<T> type;
            private final BiConsumer<T, B> encoder;
            private final Function<B, T> decoder;
            private final BiConsumer<T, ServerPlayer> handle;

            private DynamicLoaderPacket(CustomPacketPayload.Type<T> type, BiConsumer<T, B> encoder, Function<B, T> decoder, BiConsumer<T, ServerPlayer> handle) {
                  this.type = type;
                  this.encoder = encoder;
                  this.decoder = decoder;
                  this.handle = handle;
            }

            @Override @NotNull
            public T decode(@NotNull B buf) {
                  debugMsgDecode();
                  return decoder.apply(buf);
            }

            @Override
            public void encode(@NotNull B buf, @NotNull T msg) {
                  debugMsgEncode();
                  encoder.accept(msg, buf);
            }

            public void handle(T msg, ServerPlayer player) {
                  handle.accept(msg, player);
            }
      }

}
