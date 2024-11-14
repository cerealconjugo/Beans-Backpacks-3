package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import net.minecraft.world.Container;

public class XpEntity implements IEntityTraits<XpTraits> {
      public static final XpEntity INSTANCE = new XpEntity();

      @Override
      public Container createHopperContainer(BackpackEntity backpack, XpTraits traits) {
            return null;
      }
}
