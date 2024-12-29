package com.beansgalaxy.backpacks.traits.stacking;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

public class StackingClient implements IClientTraits<StackingTraits> {
      public static final StackingClient INSTANCE = new StackingClient();

      @Override
      public void renderTooltip(StackingTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {

      }

      @Override
      public void appendTooltipLines(StackingTraits traits, List<Component> lines) {

      }

      @Override
      public void appendEquipmentLines(StackingTraits traits, Consumer<Component> pTooltipAdder) {

      }

      @Override
      public @Nullable ClientTooltipComponent getTooltipComponent(StackingTraits traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            return null;
      }

      @Override
      public boolean isBarVisible(StackingTraits trait, PatchedComponentHolder holder) {
            return false;
      }

      @Override
      public int getBarWidth(StackingTraits trait, PatchedComponentHolder holder) {
            return 0;
      }

      @Override
      public int getBarColor(StackingTraits trait, PatchedComponentHolder holder) {
            return 0;
      }
}
