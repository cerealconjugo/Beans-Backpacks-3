package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class LunchBoxClient extends BundleClient {
      static final LunchBoxClient INSTANCE = new LunchBoxClient();

      @Override
      public void getBarColor(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }

      @Override
      public void getBarWidth(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            Fraction fullness = trait.fullness();
            if (trait.isEmpty())
                  cir.setReturnValue(0);
            else if (fullness.equals(Fraction.ONE))
                  cir.setReturnValue(13);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(12, 1)).floatValue();
                  cir.setReturnValue(Mth.floor(value) + 1);
            }
      }
}
