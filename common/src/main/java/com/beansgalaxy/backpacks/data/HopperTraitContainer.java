package com.beansgalaxy.backpacks.data;

import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.MutableTraits;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public abstract class HopperTraitContainer<M extends MutableTraits> implements Container {
      private final BackpackEntity backpack;
      protected final M mutable;

      public HopperTraitContainer(BackpackEntity backpack, M mutable) {
            this.backpack = backpack;
            this.mutable = mutable;
      }

      @Override
      public boolean isEmpty() {
            return mutable.isEmpty();
      }

      @Override
      public void setChanged() {
            mutable.push();
      }

      @Override
      public boolean stillValid(Player player) {
            return true;
      }

      public boolean isFull() {
            return mutable.isFull();
      }
}
