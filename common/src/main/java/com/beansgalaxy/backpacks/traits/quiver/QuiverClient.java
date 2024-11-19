package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class QuiverClient extends BundleClient {
      static final QuiverClient INSTANCE = new QuiverClient();

      @Override
      public void getBarColor(BundleLikeTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }
}
