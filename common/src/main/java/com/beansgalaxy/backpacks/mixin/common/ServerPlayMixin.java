package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.screen.BackSlot;
import com.beansgalaxy.backpacks.shorthand.Shorthand;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

      @Inject(method = "handleSetCarriedItem", at = @At(value = "HEAD", shift = At.Shift.AFTER), cancellable = true)
      private void handleShorthandCarriedItem(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
            int size = player.getInventory().items.size();
            if (packet.getSlot() >= size) {
                  Shorthand shorthand = Shorthand.get(player);
                  if (packet.getSlot() > size + shorthand.size() - 1)
                        return;

                  if (this.player.getInventory().selected != packet.getSlot() && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                        this.player.stopUsingItem();
                  }

                  this.player.getInventory().selected = packet.getSlot();
                  this.player.resetLastActionTime();
                  ci.cancel();
            }
      }
}
