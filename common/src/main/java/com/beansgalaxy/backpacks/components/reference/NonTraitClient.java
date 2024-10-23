package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class NonTraitClient implements IClientTraits {
      static final NonTraitClient INSTANCE = new NonTraitClient();

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemstack, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {

      }

      @Override
      public void isBarVisible(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void getBarWidth(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {

      }

      @Override
      public void getBarColor(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {

      }

      @Override
      public <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            return null;
      }
}
