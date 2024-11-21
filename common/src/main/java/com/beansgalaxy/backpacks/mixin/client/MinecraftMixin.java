package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.MinecraftAccessor;
import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.shorthand.storage.Shorthand;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftAccessor {
      @Shadow static Minecraft instance;
      @Shadow @Nullable public LocalPlayer player;
      @Shadow public ClientLevel level;

      @Shadow protected abstract boolean shouldRun(Runnable pRunnable);

      @Unique public final EnderStorage beans_Backpacks_2$enderStorage = new EnderStorage();

      @Override
      public EnderStorage beans_Backpacks_2$getEnder() {
            return beans_Backpacks_2$enderStorage;
      }

      @Inject(method = "startUseItem", cancellable = true, at = @At(value = "INVOKE",
                  ordinal = 0, target = "Lnet/minecraft/world/item/ItemStack;getCount()I"))
      private void hotkeyUseItemOn(CallbackInfo ci) {
            if(BackData.get(player).isActionKeyDown() && instance.hitResult instanceof BlockHitResult blockHitResult && KeyPress.INSTANCE.consumeActionUseOn(instance, blockHitResult))
                  ci.cancel();
      }

      @Inject(method = "startUseItem", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
                  ordinal = 1, target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
      private void hotkeyUseItem(CallbackInfo ci) {
            if(BackData.get(player).isActionKeyDown() && KeyPress.INSTANCE.consumeActionUse(level, player))
                  ci.cancel();
      }

      @Inject(method = "handleKeybinds", at = @At("TAIL"))
      private void handleBackpackKeybinds(CallbackInfo ci) {
            KeyPress keyPress = KeyPress.INSTANCE;
            while (keyPress.WEAPON_KEY.consumeClick()) {
                  Shorthand shorthand = Shorthand.get(player);
                  Inventory inventory = player.getInventory();
                  if (keyPress.UTILITY_KEY.isDown()) {
                        keyPress.UTILITY_KEY.consumeClick();
                        shorthand.resetSelected(inventory);
                        return;
                  }

                  shorthand.advanceWeapon(inventory);
            }
            while (keyPress.UTILITY_KEY.consumeClick()) {
                  Shorthand shorthand = Shorthand.get(player);
                  Inventory inventory = player.getInventory();
                  if (keyPress.WEAPON_KEY.isDown()) {
                        keyPress.UTILITY_KEY.consumeClick();
                        shorthand.resetSelected(inventory);
                        return;
                  }

                  shorthand.reduceWeapon(inventory);
            }
      }

}
