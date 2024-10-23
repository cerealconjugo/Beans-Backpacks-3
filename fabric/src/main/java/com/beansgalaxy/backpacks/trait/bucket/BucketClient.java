package com.beansgalaxy.backpacks.trait.bucket;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.Tint;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class BucketClient implements IClientTraits {
      static final BucketClient INSTANCE = new BucketClient();

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemstack, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty()) {
                  BucketTraits bucketTraits = (BucketTraits) trait;
                  Component name = FluidVariantAttributes.getName(bucketTraits.fluid);
                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemstack, name);
                  gui.renderTooltip(Minecraft.getInstance().font, List.of(name), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      @Override
      public void getBarWidth(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            Fraction fullness = trait.fullness();
            if (fullness.equals(Fraction.ONE))
                  cir.setReturnValue(14);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  cir.setReturnValue(Mth.floor(value) + 1);
            }
      }

      @Override
      public void getBarColor(GenericTraits trait, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            if (trait.fullness().equals(Fraction.ONE))
                  cir.setReturnValue(Mth.color(0.9F, 0.2F, 0.3F));
            else
                  cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }

      @Override
      public <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            T traits = tooltip.traits();
            if (traits instanceof BucketTraits trait) {
                  FluidVariant fluid = trait.fluid;
                  long amount = trait.amount;
                  TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
                  Tint tint = new Tint(FluidVariantRendering.getColor(fluid));
                  int buckets = (int) (amount / FluidConstants.BUCKET);
                  int bottles = (int) ((amount % FluidConstants.BUCKET) / FluidConstants.BOTTLE);
                  int droplets = (int) (amount % FluidConstants.BUCKET % FluidConstants.BOTTLE);
                  return new BucketTooltip(tooltip.itemstack(), tooltip.title(), sprite, tint, buckets, bottles, droplets);
            }
            return null;
      }
}
