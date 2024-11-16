package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.client.CommonAtClient;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
      @Unique public final LivingEntity instance = (LivingEntity) (Object) this;

      @Inject(method = "getDrinkingSound", at = @At("HEAD"), cancellable = true)
      private void lunchBoxDrinkingSound(ItemStack pStack, CallbackInfoReturnable<SoundEvent> cir) {
            LunchBoxTraits.firstIsPresent(pStack, instance, food -> {
                  cir.setReturnValue(food.getDrinkingSound());
            });
      }

      @Inject(method = "getEatingSound", at = @At("HEAD"), cancellable = true)
      private void lunchBoxEatingSound(ItemStack pStack, CallbackInfoReturnable<SoundEvent> cir) {
            LunchBoxTraits.firstIsPresent(pStack, instance, food -> {
                  cir.setReturnValue(food.getEatingSound());
            });
      }
}
