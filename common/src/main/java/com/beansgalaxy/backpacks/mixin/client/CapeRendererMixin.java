package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public class CapeRendererMixin {
      @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/EquipmentSlot;CHEST:Lnet/minecraft/world/entity/EquipmentSlot;"), method = "render", require = 1, allow = 1, cancellable = true)
      public void injectCapeRenderCheck(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, AbstractClientPlayer player, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
            if (CommonClient.cancelCapeRender(player)) {
                  ci.cancel();
            }
      }
}
