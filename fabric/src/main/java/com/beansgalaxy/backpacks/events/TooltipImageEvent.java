package com.beansgalaxy.backpacks.events;

import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

public class TooltipImageEvent implements TooltipComponentCallback {
      @Override @Nullable
      public ClientTooltipComponent getComponent(TooltipComponent data) {
            if (data instanceof TraitTooltip<? extends GenericTraits> tooltip) {
                  GenericTraits traits = tooltip.traits();
                  return traits.client().getTooltipComponent(tooltip.traits(), tooltip.itemStack(), tooltip.holder(), tooltip.title());
            }
            return null;
      }
}
