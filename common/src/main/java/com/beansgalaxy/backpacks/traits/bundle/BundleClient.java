package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.network.serverbound.SyncSelectedSlot;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class BundleClient implements IClientTraits {
      static final BundleClient INSTANCE = new BundleClient();

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemstack, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty()) {
                  BundleLikeTraits storageTraits = (BundleLikeTraits) trait;
                  Minecraft minecraft = Minecraft.getInstance();

                  LocalPlayer player = minecraft.player;
                  boolean carriedEmpty = player.containerMenu.getCarried().isEmpty();
                  boolean isQuickMove = BackData.get(player).isMenuKeyDown() || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
                  boolean noSpace = storageTraits.fullness().compareTo(Fraction.ONE) == 0;
                  boolean hideEmptySlot = carriedEmpty || isQuickMove || noSpace;

                  Component title;
                  int selectedSlot = storageTraits.getSelectedSlot(player);
                  if (selectedSlot == 0 && !hideEmptySlot) {
                        title = Component.empty();
                  } else {
                        int selectedSlotSafe = selectedSlot == 0 ? 0 : selectedSlot - 1;
                        ItemStack selected = storageTraits.stacks().get(selectedSlotSafe);
                        title = Constants.getName(selected);
                  }

                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemstack, title);
                  gui.renderTooltip(minecraft.font, List.of(title), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      @Override
      public void getBarWidth(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            Fraction fullness = trait.fullness();
            if (trait.isFull())
                  cir.setReturnValue(14);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  cir.setReturnValue(Mth.floor(value) + 1);
            }
      }

      @Override
      public void getBarColor(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            if (trait.isFull())
                  cir.setReturnValue(Mth.color(0.9F, 0.2F, 0.3F));
            else
                  cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }

      @Override @Nullable
      public <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            T traits = tooltip.traits();
            if (traits instanceof BundleLikeTraits bundleLikeTraits) {
                  return new BundleTooltip(bundleLikeTraits, tooltip.itemstack(), tooltip.title());
            }
            return null;
      }

      @Override
      public boolean mouseScrolled(GenericTraits trait, Level level, Slot hoveredSlot, int containerId, int scrolled) {
            BundleLikeTraits storageTraits = (BundleLikeTraits) trait;
            LocalPlayer player = Minecraft.getInstance().player;

            int i = storageTraits.slotSelection.modSelectedSlot(player, slot -> {
                  boolean carriedEmpty = player.containerMenu.getCarried().isEmpty();
                  boolean isQuickMove =  BackData.get(player).isMenuKeyDown() || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
                  boolean hideEmptySlot = carriedEmpty || isQuickMove || trait.isFull();

                  int selectedSlot = slot - scrolled;
                  if (hideEmptySlot && slot == 0 && scrolled == -1)
                        selectedSlot++;

                  int size = storageTraits.stacks().size();
                  return size == 0 || selectedSlot < 0 ? 0
                              : Math.min(selectedSlot, size);
            });

            SyncSelectedSlot.send(containerId, hoveredSlot.index, i);

            return true;
      }
}
