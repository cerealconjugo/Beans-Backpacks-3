package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.quiver.QuiverTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Player.class)
public class PlayerMixin {

      @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
      private void getBackSlotItem(EquipmentSlot equipmentSlot, CallbackInfoReturnable<ItemStack> cir) {
            if (equipmentSlot == EquipmentSlot.BODY) {
                  BackData access = (BackData) ((Player) (Object) this).getInventory();
                  cir.setReturnValue(access.beans_Backpacks_3$getBody().getFirst());
            }
      }

      @Inject(method = "setItemSlot", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                  target = "Lnet/minecraft/world/entity/player/Player;verifyEquippedItem(Lnet/minecraft/world/item/ItemStack;)V"))
      private void setBackSlotItem(EquipmentSlot pSlot, ItemStack pStack, CallbackInfo ci) {
            if (EquipmentSlot.BODY.equals(pSlot)) {
                  Player player = (Player) (Object) this;
                  BackData access = (BackData) player.getInventory();
                  player.onEquipItem(EquipmentSlot.BODY, access.beans_Backpacks_3$getBody().set(0, pStack), pStack);
                  ci.cancel();
            }
      }

      @Inject(method = "getProjectile", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
                  target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getHeldProjectile(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/world/item/ItemStack;"))
      private void getBackpackProjectile(ItemStack pShootable, CallbackInfoReturnable<ItemStack> cir, Predicate<ItemStack> predicate) {
            Player player = (Player) (Object) this;
            QuiverTraits.runIfQuiverEquipped(player, (traits, slot, quiver) -> {
                  List<ItemStack> stacks = quiver.get(ITraitData.ITEM_STACKS);
                  if (stacks == null || stacks.isEmpty())
                        return false;

                  int selectedSlot = traits.getSelectedSlotSafe(PatchedComponentHolder.of(quiver), player);
                  ItemStack stack = stacks.get(selectedSlot);
                  if (predicate.test(stack)) {
                        cir.setReturnValue(stack);
                        return true;
                  }

                  return false;
            });
      }
}
