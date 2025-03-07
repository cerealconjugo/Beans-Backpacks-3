package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IEntityTraits<T extends GenericTraits> {

      default InteractionResult interact(BackpackEntity backpackEntity, T traits, Player player, InteractionHand hand) {
            return InteractionResult.PASS;
      }

      default void onPlace(BackpackEntity backpack, T traits, Player player, ItemStack backpackStack) {

      }

      default void onPickup(BackpackEntity backpack, T traits, Player player) {

      }

      default void onBreak(BackpackEntity backpack, T traits, boolean dropItems) {

      }

      default void onDamage(BackpackEntity backpack, T traits, boolean silent, ModSound sound) {
            backpack.wobble(10);
            backpack.breakAmount += 10;
            backpack.hop(0.1);
            if (!silent) {
                  float pitch = backpack.getRandom().nextFloat() * 0.3f;
                  sound.at(backpack, ModSound.Type.HIT, 1f, pitch + 0.9f);
            }
      }

      default void entityTick(BackpackEntity backpack, T traits) {

      }

      @Nullable
      default Container createHopperContainer(BackpackEntity backpack, T traits) {
            return null;
      }
}
