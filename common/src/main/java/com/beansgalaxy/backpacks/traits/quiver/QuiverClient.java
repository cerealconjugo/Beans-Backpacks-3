package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class QuiverClient extends BundleClient {
      static final QuiverClient INSTANCE = new QuiverClient();

      @Override
      public void getBarColor(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }
}
