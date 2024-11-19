package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
      @Shadow @Final private Minecraft minecraft;

      @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/gui/GuiLayerManager;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
      public void render(GuiGraphics drawContext, DeltaTracker tickCounter, CallbackInfo callbackInfo) {
            if (minecraft.hitResult instanceof EntityHitResult hitResult && hitResult.getEntity() instanceof BackpackEntity backpack) {
                  backpack.getTraits().ifPresent(trait -> {
                        trait.client().renderEntityOverlay(minecraft, backpack, trait, drawContext, tickCounter);
                  });
            }
      }
}
