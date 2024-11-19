package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.ModSound;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface MutableTraits {

      void push();

      default void push(CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(true);
            push();
      }

      ModSound sound();

      Fraction fullness();

      default boolean isEmpty() {
            int i = fullness().compareTo(Fraction.ZERO);
            return 0 >= i;
      }

      default boolean isFull() {
            int i = fullness().compareTo(Fraction.ONE);
            return i >= 0;
      }

      default InteractionResult interact(BackpackEntity backpack, Player player, InteractionHand hand) {
            return InteractionResult.PASS;
      }

     default void onPlace(BackpackEntity backpack, Player player, ItemStack backpackStack) {

     }

     default void onPickup(BackpackEntity backpack, Player player) {

     }

     default void onBreak(BackpackEntity backpack) {

     }

     default void onDamage(BackpackEntity backpack, int damage, boolean silent) {

     }

     default void entityTick(BackpackEntity backpack) {

     }
//
//      InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand);
//
//      void entityTick(BackpackEntity backpackEntity);
//
//      void damageTrait(BackpackEntity backpackEntity, int damage, boolean silent);
//
//      void onPlace(BackpackEntity backpackEntity, Player player, ItemStack backpackStack);
//
//      void onBreak(BackpackEntity backpackEntity);
//
//      void onPickup(BackpackEntity backpackEntity, Player player);
}
