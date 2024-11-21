package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandMixin {

      @Shadow
      @Final
      private Minecraft minecraft;
      @Shadow private ItemStack mainHandItem;

      @Shadow private float mainHandHeight;

      @Redirect(method = "tick", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
      private float redirectShortHandDelay(float f1, float f2, float f3) {
            LocalPlayer localPlayer = minecraft.player;
            Inventory inv = localPlayer.getInventory();
            if (inv.selected >= inv.items.size()) {
                  mainHandItem = localPlayer.getMainHandItem();
                  return CommonClient.getHandHeight(mainHandHeight);
            }

            return Mth.clamp(f1, f2, f3);
      }
}
