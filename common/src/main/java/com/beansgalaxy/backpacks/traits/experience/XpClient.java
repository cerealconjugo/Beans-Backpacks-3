package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.ChatFormatting;
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
import java.util.function.Consumer;

public class XpClient implements IClientTraits {
      static final XpClient INSTANCE = new XpClient();

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty(holder)) {
                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, Component.empty());
                  gui.renderTooltip(Minecraft.getInstance().font, List.of(), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
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
      public @Nullable <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            T traits = tooltip.traits();
            if (traits instanceof XpTraits xpTraits) {
                  return new XpTooltip(tooltip);
            }
            return null;
      }

      @Override
      public void appendEquipmentLines(GenericTraits traits, Consumer<Component> pTooltipAdder) {
            XpTraits xpTraits = (XpTraits) traits;
            int size = xpTraits.size();
            pTooltipAdder.accept(Component.translatable("traits.beansbackpacks.equipment." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      @Override
      public void appendTooltipLines(GenericTraits traits, List<Component> lines) {
            XpTraits xpTraits = (XpTraits) traits;
            int size = xpTraits.size();
            lines.add(Component.translatable("traits.beansbackpacks.inventory." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }
}
