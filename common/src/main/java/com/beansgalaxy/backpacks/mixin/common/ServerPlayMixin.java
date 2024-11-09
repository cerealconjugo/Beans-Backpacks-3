package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.screen.BackSlot;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayMixin {

      @Shadow public ServerPlayer player;

      @Inject(method = "handleSetCreativeModeSlot", cancellable = true,
                  at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
      public void setCreativeBackSlot(ServerboundSetCreativeModeSlotPacket ctx, CallbackInfo ci) {
            int slotIndex = ctx.slotNum();
            ItemStack stack = ctx.itemStack();

            boolean outOfRange = player.inventoryMenu.slots.size() < slotIndex || slotIndex < 0;
            boolean flag2 = stack.isEmpty() || stack.getDamageValue() >= 0 && stack.getCount() <= 64 && !stack.isEmpty();
            if (!outOfRange && flag2) {
                  Slot slot = this.player.inventoryMenu.getSlot(slotIndex);
                  if (slot instanceof BackSlot) {
                        slot.setByPlayer(stack);
                        this.player.inventoryMenu.broadcastChanges();
                        ci.cancel();
                  }
            }
      }
}
