package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.client.KeyPress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

      @Shadow @Final protected Minecraft minecraft;

      @Inject(method = "tick", at = @At("TAIL"))
      public void tick(CallbackInfo ci) {
            KeyPress.INSTANCE.tick(minecraft, ((LocalPlayer) (Object) this));
      }
}
