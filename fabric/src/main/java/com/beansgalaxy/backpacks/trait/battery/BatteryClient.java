package com.beansgalaxy.backpacks.trait.battery;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.BatteryTooltip;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BatteryClient implements IClientTraits {
      static final BatteryClient INSTANCE = new BatteryClient();

      @Override
      public void isBarVisible(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Boolean> cir) {
            if (holder.has(ITraitData.SOLO_STACK) || holder.has(ITraitData.LONG))
                  cir.setReturnValue(true);
      }

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty(holder)) {
                  BatteryTraits batteryTraits = (BatteryTraits) trait;
                  Long amount = holder.getOrDefault(ITraitData.LONG, 0L);
                  MutableComponent energy = Component.literal(energyToReadable(amount) + "/" + energyToReadable(batteryTraits.size()) + " E");
                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, energy);
                  gui.renderTooltip(Minecraft.getInstance().font, List.of(energy), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      private static String energyToReadable(long energy) {
            String s = String.valueOf(energy);
            int length = s.length();
            if (length < 3)
                  return s;

            char[] chars = s.toCharArray();
            if (length > 12)
                  return chars[0] + "." + chars[1] + "*10^" + length;

            StringBuilder builder = new StringBuilder();
            int i = 0;
            while (i < length % 3) {
                  builder.append(chars[i]);
                  i++;
            }

            builder.append('.').append(chars[i]);

            if (length > 6) {
                  builder.append(chars[i + 1]);
                  if (length > 9) {
                        builder.append('b');
                  }
                  else {
                        builder.append('m');
                  }
            } else {
                  builder.append('k');
            }

            return builder.toString();
      }

      @Override
      public void getBarWidth(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            Fraction fullness = trait.fullness(holder);
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
      public void getBarColor(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            if (!holder.has(ITraitData.SOLO_STACK))
                  cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
            else
                  cir.setReturnValue(Mth.color(0.9F, 1F, 0.3F));
      }

      @Override @Nullable
      public <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            T traits = tooltip.traits();
            if (traits instanceof BatteryTraits batteryTraits) {
                  return new BatteryTooltip(tooltip);
            }
            return null;
      }

      @Override
      public void appendEquipmentLines(GenericTraits traits, Consumer<Component> pTooltipAdder) {
            BatteryTraits batteryTraits = (BatteryTraits) traits;
            long size = batteryTraits.size();
            pTooltipAdder.accept(Component.translatable("traits.beansbackpacks.equipment." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      @Override
      public void appendTooltipLines(GenericTraits traits, List<Component> lines) {
            BatteryTraits batteryTraits = (BatteryTraits) traits;
            long size = batteryTraits.size();
            lines.add(Component.translatable("traits.beansbackpacks.inventory." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }
}
