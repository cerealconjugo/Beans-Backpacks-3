package com.beansgalaxy.backpacks.trait.battery;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class BatteryClient implements IClientTraits {
      static final BatteryClient INSTANCE = new BatteryClient();

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemstack, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty()) {
                  BatteryTraits batteryTraits = (BatteryTraits) trait;
                  MutableComponent energy = Component.literal(energyToReadable(batteryTraits.amount()) + "/" + energyToReadable(batteryTraits.size()) + " E");
                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemstack, energy);
                  gui.renderTooltip(Minecraft.getInstance().font, List.of(energy), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      private static String energyToReadable(int energy) {
            String s = String.valueOf(energy);
            int length = s.length();
            if (length > 9) {
                  double b = energy / 1000000000.0;
                  String billions = asString(b, 3, 'B');
                  return billions;
            }

            if (length > 6) {
                  double m = energy / 1000000.0;
                  String millions = asString(m, 2, 'M');
                  return millions;
            }

            if (length > 3) {
                  double t = energy / 1000.0;
                  String thousands = asString(t, 1, 'K');
                  return thousands;
            }

            return s;
      }

      private static @NotNull String asString(double number, int accuracy, char label) {
            String s1 = String.valueOf(number);
            String[] split = s1.split("\\.");

            StringBuilder sb = new StringBuilder(split[0]).append('.');
            if (split.length == 1) {
                  sb.append("0".repeat(accuracy));
            } else {
                  String s = split[1];
                  int length = s.length();
                  if (length > accuracy) {
                        sb.append(s, 0, accuracy);
                  } else {
                        sb.append(s).append("0".repeat(accuracy - length));
                  }
            }

            String thousands = sb.append(label).toString();
            return thousands;
      }

      @Override
      public void getBarWidth(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            Fraction fullness = trait.fullness();
            if (fullness.equals(Fraction.ONE))
                  cir.setReturnValue(14);
            else if (fullness.getNumerator() == 0) {
                  cir.setReturnValue(0);
            } else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  cir.setReturnValue(Mth.floor(value) + 1);
            }
      }

      @Override
      public void getBarColor(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            BatteryTraits batteryTraits = (BatteryTraits) trait;
            if (batteryTraits.stack().isEmpty())
                  cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
            else
                  cir.setReturnValue(Mth.color(0.9F, 1F, 0.3F));
      }

      @Override @Nullable
      public <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            T traits = tooltip.traits();
            if (traits instanceof BatteryTraits batteryTraits) {
                  return new BatteryTooltip(batteryTraits, tooltip.itemstack(), tooltip.title());
            }
            return null;
      }
}
