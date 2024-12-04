package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(SmithingScreen.class)
public class SmithingScreenMixin {
      @Shadow @Nullable private ArmorStand armorStandPreview;

      @Inject(method = "updateArmorStandPreview", cancellable = true, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
      private void backpackUpdateArmorStandPreview(ItemStack pStack, CallbackInfo ci) {
            Optional<EquipableComponent> optional = EquipableComponent.get(pStack);
            if (optional.isEmpty())
                  return;

            EquipableComponent equipable = optional.get();
            for (EquipmentSlot slot : equipable.values()) {
                  this.armorStandPreview.setItemSlot(slot, pStack);
                  ci.cancel();
                  return;
            }
      }

      @Unique float beans_Backpacks_3$progress = 0f;
      @Unique float beans_Backpacks_3$yRotO = 0f;
      @Unique float beans_Backpacks_3$yRot = 0f;

      @Inject(method = "containerTick", at = @At("TAIL"))
      private void backpackCalculateArmorStandRot(CallbackInfo ci) {
            float y = beans_Backpacks_3$progress;
            double newProgress = y == 0 ? 0 : y == 1 ? 1
                                                   : y < 0.5 ? Math.pow(2, 20 * y - 10) / 2
                                                             : (2 - Math.pow(2, -20 * y + 10)) / 2;

            beans_Backpacks_3$yRotO = beans_Backpacks_3$yRot;
            this.beans_Backpacks_3$yRot = (float) newProgress * 180 - 150;
      }

      @Inject(method = "renderBg", at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V"))
      private void backpackSpinArmorStandPreview(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY, CallbackInfo ci) {
            ItemStack backSlot = armorStandPreview.getItemBySlot(EquipmentSlot.BODY);
            if (!backSlot.isEmpty()) {
                  if (beans_Backpacks_3$progress < 1)
                        beans_Backpacks_3$progress += 0.05f;
            } else if (beans_Backpacks_3$progress > 0)
                  beans_Backpacks_3$progress -= 0.05f;

            armorStandPreview.yBodyRot = Mth.lerp(pPartialTick, beans_Backpacks_3$yRotO, beans_Backpacks_3$yRot);
      }
}