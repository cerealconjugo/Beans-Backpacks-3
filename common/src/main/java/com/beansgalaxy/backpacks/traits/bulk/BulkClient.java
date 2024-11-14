package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BulkClient implements IClientTraits<BulkTraits> {
      static final BulkClient INSTANCE = new BulkClient();

      @Override
      public void renderTooltip(BulkTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            BulkMutable.BulkStacks bulkStacks = holder.get(ITraitData.BULK_STACKS);
            if (bulkStacks != null) {
                  Minecraft minecraft = Minecraft.getInstance();
                  BulkMutable.EmptyStack first = bulkStacks.emptyStacks().getFirst();
                  ItemStack stack = first.withItem(bulkStacks.itemHolder());
                  Component title = Constants.getName(stack);

                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, title);
                  gui.renderTooltip(minecraft.font, List.of(title), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      @Override
      public void getBarWidth(BulkTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            Fraction fullness = trait.fullness(holder);
            if (trait.isFull(holder))
                  cir.setReturnValue(14);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  cir.setReturnValue(Mth.floor(value) + 1);
            }
      }

      @Override
      public void getBarColor(BulkTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            if (trait.isFull(holder))
                  cir.setReturnValue(Mth.color(0.9F, 0.2F, 0.3F));
            else
                  cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }

      @Override
      public void appendTooltipLines(BulkTraits traits, List<Component> lines) {
            int size = traits.size();
            lines.add(Component.translatable("traits.beansbackpacks.inventory." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      @Override
      public void appendEquipmentLines(BulkTraits traits, Consumer<Component> pTooltipAdder) {
            int size = traits.size();
            pTooltipAdder.accept(Component.translatable("traits.beansbackpacks.equipment." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      @Override
      public @Nullable ClientTooltipComponent getTooltipComponent(BulkTraits traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            return new BulkTooltip(traits, itemStack, holder, title);
      }

      @Override
      public void renderEntityOverlay(Minecraft minecraft, BackpackEntity backpack, BulkTraits generic, GuiGraphics gui, DeltaTracker tick) {
            BulkMutable.BulkStacks bulkStacks = backpack.get(ITraitData.BULK_STACKS);
            if (bulkStacks == null) {
                  return;
            }

            BulkMutable.EmptyStack first = bulkStacks.emptyStacks().getFirst();
            ItemStack item = first.withItem(bulkStacks.itemHolder());
            int amount = bulkStacks.amount();

            Window window = minecraft.getWindow();
            int center = window.getGuiScaledWidth() / 2;
            int y = window.getGuiScaledHeight() / 2;

            Component name = Component.literal("x").append(String.valueOf(amount));
            Font font = minecraft.font;
            int x = center - font.width(name) / 2;
            int hOffset = y / 10;
            gui.drawString(font, name, x + 8, y + 4 + hOffset, 0xFFFFFFFF);
            gui.renderItem(item, x - 9, y + hOffset);
      }
}
