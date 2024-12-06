package com.beansgalaxy.backpacks.mixin.client;

import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.allay.Allay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllayModel.class)
public class AllayModelMixin {
      @Shadow @Final private ModelPart root;

      @Inject(method = "setupAnim(Lnet/minecraft/world/entity/animal/allay/Allay;FFFFF)V", at = @At("TAIL"))
      private void backpackSetupAnim(Allay pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
            if (pEntity.getItemBySlot(EquipmentSlot.BODY).isEmpty())
                  return;

            root.z += 3;
            root.y -= 1;
      }
}
