package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.chest.screen.EntityChestScreen;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class ChestEntity implements IEntityTraits<ChestTraits> {
      public static final ChestEntity INSTANCE = new ChestEntity();

      @Override
      public InteractionResult interact(BackpackEntity backpackEntity, ChestTraits traits, Player player, InteractionHand hand) {
            if (player.level().isClientSide) {
                  EntityChestScreen.openScreen(backpackEntity.viewable, traits);
            }
            return InteractionResult.SUCCESS;
      }

      @Override
      public Container createHopperContainer(BackpackEntity backpack, ChestTraits traits) {
            return new ChestHopper(backpack, traits);
      }
}
