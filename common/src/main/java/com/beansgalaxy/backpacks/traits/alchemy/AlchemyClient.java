package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class AlchemyClient extends BundleClient {
      static final AlchemyClient INSTANCE = new AlchemyClient();

      @Override
      public void getBarColor(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }
}
