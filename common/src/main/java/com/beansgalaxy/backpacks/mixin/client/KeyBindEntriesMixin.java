package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KeyBindsList.KeyEntry.class)
public class KeyBindEntriesMixin {
      @Shadow @Final private Button changeButton;
      @Shadow @Final private Component name;

      @Inject(method = "refreshEntry", at = @At(value = "INVOKE", ordinal = 1, shift = At.Shift.AFTER,
                  target = "Lnet/minecraft/client/gui/components/Button;setTooltip(Lnet/minecraft/client/gui/components/Tooltip;)V"))
      private void changeBackpackKeyHover(CallbackInfo ci) {
            CommonClient.modifyBackpackKeyDisplay(name, this.changeButton);
      }

}
