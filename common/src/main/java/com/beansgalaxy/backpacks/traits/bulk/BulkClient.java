package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jetbrains.annotations.Nullable;

public class BulkClient extends BundleClient {
      static final BulkClient INSTANCE = new BulkClient();

      @Override
      public @Nullable <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            T traits = tooltip.traits();
            if (traits instanceof BulkTraits bundleLikeTraits) {
                  return new BulkTooltip(bundleLikeTraits, tooltip.itemstack(), tooltip.title());
            }
            return null;
      }
}
