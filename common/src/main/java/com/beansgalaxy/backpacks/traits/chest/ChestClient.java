package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

public class ChestClient implements IClientTraits {
      static final ChestClient INSTANCE = new ChestClient();

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemstack, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {

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

      @Override
      public void appendTooltipLines(GenericTraits traits, List<Component> lines) {
            ChestTraits chestTraits = (ChestTraits) traits;
            ChestFields fields = chestTraits.fields();
            MutableComponent line;
            boolean columnIsOne = fields.columns == 1;
            boolean rowIsOne = fields.rows == 1;
            if (columnIsOne && rowIsOne)
                  line = Component.translatable("traits.beansbackpacks.inventory.chest.solo");
            else if (columnIsOne)
                  line = Component.translatable("traits.beansbackpacks.inventory.chest.line", fields.columns);
            else if (rowIsOne)
                  line = Component.translatable("traits.beansbackpacks.inventory.chest.line", fields.rows);
            else
                  line = Component.translatable("traits.beansbackpacks.inventory.chest.size", fields.columns, fields.rows);

            lines.add(line.withStyle(ChatFormatting.GOLD));
      }

      @Override
      public void appendEquipmentLines(GenericTraits traits, Consumer<Component> pTooltipAdder) {
            ChestTraits chestTraits = (ChestTraits) traits;
            ChestFields fields = chestTraits.fields();
            MutableComponent line;
            boolean columnIsOne = fields.columns == 1;
            boolean rowIsOne = fields.rows == 1;
            if (columnIsOne && rowIsOne)
                  line = Component.translatable("traits.beansbackpacks.equipment.chest.solo");
            else if (columnIsOne)
                  line = Component.translatable("traits.beansbackpacks.equipment.chest.line", fields.columns);
            else if (rowIsOne)
                  line = Component.translatable("traits.beansbackpacks.equipment.chest.line", fields.rows);
            else
                  line = Component.translatable("traits.beansbackpacks.equipment.chest.size", fields.columns, fields.rows);

            pTooltipAdder.accept(line.withStyle(ChatFormatting.GOLD));
      }

      @Override
      public @Nullable <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            return null;
      }

      public void openTinyMenu(ChestTraits chestTraits, Slot slot) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof AbstractContainerScreen<?> screen) {
                  ChestTraitScreen chestScreen = new ChestTraitScreen(screen, slot, chestTraits.fields());
                  minecraft.setScreen(chestScreen);
            }
      }
}
