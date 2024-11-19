package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import net.minecraft.world.Container;

public class XpEntity implements IEntityTraits<XpTraits> {
      public static final XpEntity INSTANCE = new XpEntity();

      @Override
      public Container createHopperContainer(BackpackEntity backpack, XpTraits traits) {
            return null;
      }
}
