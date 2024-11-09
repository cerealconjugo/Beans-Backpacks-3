package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

public class NonTraitClient implements IClientTraits {
      static final NonTraitClient INSTANCE = new NonTraitClient();

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {

      }

      @Override
      public void isBarVisible(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void getBarWidth(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {

      }

      @Override
      public void getBarColor(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {

      }

      @Override
      public <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            return null;
      }

      @Override
      public void appendEquipmentLines(GenericTraits traits, Consumer<Component> pTooltipAdder) {
      }

      @Override
      public void appendTooltipLines(GenericTraits traits, List<Component> lines) {
      }
}
