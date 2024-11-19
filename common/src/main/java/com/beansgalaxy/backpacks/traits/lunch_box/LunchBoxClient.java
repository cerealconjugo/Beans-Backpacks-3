package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class LunchBoxClient extends BundleClient {
      static final LunchBoxClient INSTANCE = new LunchBoxClient();

      @Override
      public void renderTooltip(BundleLikeTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty(holder)) {
                  Minecraft minecraft = Minecraft.getInstance();

                  LocalPlayer player = minecraft.player;
                  boolean carriedEmpty = player.containerMenu.getCarried().isEmpty();
                  boolean isQuickMove = BackData.get(player).isMenuKeyDown() || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
                  boolean noSpace = trait.fullness(holder).compareTo(Fraction.ONE) == 0;
                  boolean hideEmptySlot = carriedEmpty || isQuickMove || noSpace;

                  Component title;
                  int selectedSlot = trait.getSelectedSlot(holder, player);
                  if (selectedSlot == 0 && !hideEmptySlot) {
                        title = Component.empty();
                  } else {
                        ItemStack selected;
                        List<ItemStack> nonEdibles = holder.get(ITraitData.NON_EDIBLES);
                        if (nonEdibles != null && carriedEmpty) {
                              selected = nonEdibles.getFirst();
                        }
                        else {
                              int selectedSlotSafe = selectedSlot == 0 ? 0 : selectedSlot - 1;
                              List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
                              selected = stacks.get(selectedSlotSafe);
                        }

                        title = CommonClass.getName(selected);
                  }

                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, title);
                  gui.renderTooltip(minecraft.font, List.of(title), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      @Override @Nullable
      public ClientTooltipComponent getTooltipComponent(BundleLikeTraits traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            List<ItemStack> stacks = holder.getOrDefault(ITraitData.ITEM_STACKS, List.of());
            List<ItemStack> nonEdibles = holder.get(ITraitData.NON_EDIBLES);
            return new LunchBoxTooltip(traits, itemStack, stacks, nonEdibles, holder, title);
      }

      @Override
      public void getBarColor(BundleLikeTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }

      @Override
      public void getBarWidth(BundleLikeTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            Fraction fullness = trait.fullness(holder);
            if (trait.isEmpty(holder))
                  cir.setReturnValue(0);
            else if (fullness.equals(Fraction.ONE))
                  cir.setReturnValue(13);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(12, 1)).floatValue();
                  cir.setReturnValue(Mth.floor(value) + 1);
            }
      }
}
