package com.beansgalaxy.backpacks.trait.bucket;

import com.beansgalaxy.backpacks.FabricMain;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.common.BucketTooltip;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.Tint;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BucketClient implements IClientTraits<BucketTraits> {
      static final BucketClient INSTANCE = new BucketClient();

      @Override
      public void renderTooltip(BucketTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty(holder)) {
                  FluidVariant fluid = holder.getOrDefault(FabricMain.DATA_FLUID, FluidVariant.blank());
                  Component name = FluidVariantAttributes.getName(fluid);
                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, name);
                  gui.renderTooltip(Minecraft.getInstance().font, List.of(name), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      @Override
      public void renderEntityOverlay(Minecraft minecraft, BackpackEntity backpack, BucketTraits trait, GuiGraphics gui, DeltaTracker tick) {
            FluidVariant fluid = backpack.get(FabricMain.DATA_FLUID);
            if (fluid == null)
                  return;

            Long amount = backpack.get(ITraitData.LONG);
            if (amount == null) {
                  return;
            }

            TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
            Tint tint = new Tint(FluidVariantRendering.getColor(fluid));
            int buckets = (int) (amount / FluidConstants.BUCKET);
            int bottles = (int) ((amount % FluidConstants.BUCKET) / FluidConstants.BOTTLE);
            int droplets = (int) (amount % FluidConstants.BUCKET % FluidConstants.BOTTLE);

            Window window = minecraft.getWindow();
            int center = window.getGuiScaledWidth() / 2;
            int y = window.getGuiScaledHeight() / 2;

            Font font = minecraft.font;
            int hOffset = y + (y / 10) + 5;

            List<Consumer<Integer>> icons = new ArrayList<>();

            int x = 0;
            if (buckets > 0) {
                  String icon = "\uD83E\uDEA3";
                  String count = String.valueOf(buckets);
                  int bucketX = x;
                  icons.add(integer -> {
                        gui.drawString(font, icon, integer + bucketX, hOffset - 1, 0xFFFFFFFF);
                        gui.drawString(font, count, integer + bucketX + 8, hOffset, 0xFFFFFFFF);
                  });
                  x += font.width(icon) + font.width(count) + 1;
            }
            if (bottles > 0) {
                  String icon = "\uD83E\uDDEA";
                  String count = String.valueOf(bottles);
                  int bottleX = x;
                  icons.add(integer -> {
                        gui.drawString(font, icon, integer + bottleX, hOffset - 1, 0xFFFFFFFF);
                        gui.drawString(font, count, integer + bottleX + 8, hOffset, 0xFFFFFFFF);
                  });
                  x += font.width(icon) + font.width(count) + 1;
            }
            if (droplets > 0) {
                  String count = droplets + "mb";
                  int dropletX = x;
                  icons.add(integer -> {
                        gui.drawString(font, count, integer + dropletX + 8, hOffset, 0xFFFFFFFF);
                  });
                  x += font.width(count);
            }

            int leftPos = center - (x / 2);
            for (Consumer<Integer> icon : icons) {
                  icon.accept(leftPos + 10);
            }
            gui.blit(leftPos - 8, hOffset - 5, 16, 16, 16, sprite, tint.getRed() / 255f, tint.getGreen() / 255f, tint.getBlue() / 255f, 1);
      }

      @Override
      public int getBarWidth(BucketTraits trait, PatchedComponentHolder holder) {
            Fraction fullness = trait.fullness(holder);
            if (fullness.equals(Fraction.ONE))
                  return (14);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  return (Mth.floor(value) + 1);
            }
      }

      @Override
      public int getBarColor(BucketTraits trait, PatchedComponentHolder holder) {
            if (trait.fullness(holder).equals(Fraction.ONE))
                  return RED_BAR;
            else
                  return BAR_COLOR;
      }

      @Override
      public ClientTooltipComponent getTooltipComponent(BucketTraits traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            FluidVariant fluid = holder.getOrDefault(FabricMain.DATA_FLUID, FluidVariant.blank());
            long amount = holder.getOrDefault(ITraitData.LONG, 0L);
            TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluid);
            Tint tint = new Tint(FluidVariantRendering.getColor(fluid));
            int buckets = (int) (amount / FluidConstants.BUCKET);
            int bottles = (int) ((amount % FluidConstants.BUCKET) / FluidConstants.BOTTLE);
            int droplets = (int) (amount % FluidConstants.BUCKET % FluidConstants.BOTTLE);
            return new BucketTooltip(itemStack, title, sprite, tint, buckets, bottles, droplets);
      }

      @Override
      public void appendEquipmentLines(BucketTraits traits, Consumer<Component> pTooltipAdder) {
            long size = traits.size();
            pTooltipAdder.accept(Component.translatable("traits.beansbackpacks.equipment." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      @Override
      public void appendTooltipLines(BucketTraits traits, List<Component> lines) {
            long size = traits.size();
            lines.add(Component.translatable("traits.beansbackpacks.inventory." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }
}
