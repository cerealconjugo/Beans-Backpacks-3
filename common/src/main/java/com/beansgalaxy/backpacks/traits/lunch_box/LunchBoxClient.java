package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LunchBoxClient extends BundleClient {
      static final LunchBoxClient INSTANCE = new LunchBoxClient();

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty(holder)) {
                  BundleLikeTraits storageTraits = (BundleLikeTraits) trait;
                  Minecraft minecraft = Minecraft.getInstance();

                  LocalPlayer player = minecraft.player;
                  boolean carriedEmpty = player.containerMenu.getCarried().isEmpty();
                  boolean isQuickMove = BackData.get(player).isMenuKeyDown() || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
                  boolean noSpace = storageTraits.fullness(holder).compareTo(Fraction.ONE) == 0;
                  boolean hideEmptySlot = carriedEmpty || isQuickMove || noSpace;

                  Component title;
                  int selectedSlot = storageTraits.getSelectedSlot(holder, player);
                  if (selectedSlot == 0 && !hideEmptySlot) {
                        title = Component.empty();
                  } else {
                        ItemStack selected;
                        List<ItemStack> nonEdibles = holder.get(ITraitData.NON_EDIBLES);
                        if (nonEdibles != null) {
                              selected = nonEdibles.getFirst();
                        }
                        else {
                              int selectedSlotSafe = selectedSlot == 0 ? 0 : selectedSlot - 1;
                              List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
                              selected = stacks.get(selectedSlotSafe);
                        }

                        title = Constants.getName(selected);
                  }

                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, title);
                  gui.renderTooltip(minecraft.font, List.of(title), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      @Override @Nullable
      public <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            T traits = tooltip.traits();
            if (traits instanceof BundleLikeTraits bundleLikeTraits) {
                  PatchedComponentHolder holder = tooltip.holder();

                  boolean hasSpace = traits.fullness(tooltip.holder()).compareTo(Fraction.ONE) != 0;
                  List<ItemStack> stacks = holder.getOrDefault(ITraitData.ITEM_STACKS, List.of());
                  ArrayList<ItemStack> arrayList = new ArrayList<>(stacks);

                  int selectedSlot;
                  List<ItemStack> nonEdibles = holder.get(ITraitData.NON_EDIBLES);
                  if (nonEdibles != null) {
                        arrayList.addAll(nonEdibles);
                        selectedSlot = arrayList.size() - 1;
                  }
                  else {
                        Minecraft minecraft = Minecraft.getInstance();
                        long window = minecraft.getWindow().getWindow();
                        LocalPlayer player = minecraft.player;
                        boolean isQuickMove =  BackData.get(player).isMenuKeyDown() || InputConstants.isKeyDown(window, 340) || InputConstants.isKeyDown(window, 344);
                        boolean carriedEmpty = player.containerMenu.getCarried().isEmpty();
                        boolean hideEmptySlot = carriedEmpty || isQuickMove || !hasSpace;
                        int slot1 = bundleLikeTraits.getSelectedSlot(holder, player);
                        selectedSlot = hideEmptySlot && slot1 == 0
                                    ? 0
                                    : slot1 -1;
                  }

                  return new BundleTooltip(bundleLikeTraits, arrayList, tooltip, hasSpace, selectedSlot);
            }
            return null;
      }

      @Override
      public void getBarColor(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }

      @Override
      public void getBarWidth(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
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
