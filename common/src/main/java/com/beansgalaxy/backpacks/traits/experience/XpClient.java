package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class XpClient implements IClientTraits {
      static final XpClient INSTANCE = new XpClient();

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemstack, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty()) {
                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemstack, Component.empty());
                  gui.renderTooltip(Minecraft.getInstance().font, List.of(), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
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
      public @Nullable <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            T traits = tooltip.traits();
            if (traits instanceof XpTraits xpTraits) {
                  return new XpTooltip(xpTraits, tooltip.itemstack());
            }
            return null;
      }
}
