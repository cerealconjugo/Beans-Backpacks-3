package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.chest.screen.MenuChestScreen;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

public class ChestClient implements IClientTraits<ChestTraits> {
      static final ChestClient INSTANCE = new ChestClient();

      @Override
      public void renderTooltip(ChestTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {

      }

      @Override
      public void getBarWidth(ChestTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            Fraction fullness = trait.fullness(holder);
            if (trait.isFull(holder))
                  cir.setReturnValue(14);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  cir.setReturnValue(Mth.floor(value) + 1);
            }
      }

      @Override
      public void getBarColor(ChestTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            if (trait.isFull(holder))
                  cir.setReturnValue(Mth.color(0.9F, 0.2F, 0.3F));
            else
                  cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }

      @Override
      public void appendTooltipLines(ChestTraits traits, List<Component> lines) {
            MutableComponent line;
            boolean columnIsOne = traits.columns == 1;
            boolean rowIsOne = traits.rows == 1;
            if (columnIsOne && rowIsOne)
                  line = Component.translatable("traits.beansbackpacks.inventory.chest.solo");
            else if (columnIsOne)
                  line = Component.translatable("traits.beansbackpacks.inventory.chest.line", traits.columns);
            else if (rowIsOne)
                  line = Component.translatable("traits.beansbackpacks.inventory.chest.line", traits.rows);
            else
                  line = Component.translatable("traits.beansbackpacks.inventory.chest.size", traits.columns, traits.rows);

            lines.add(line.withStyle(ChatFormatting.GOLD));
      }

      @Override
      public void appendEquipmentLines(ChestTraits traits, Consumer<Component> pTooltipAdder) {
            MutableComponent line;
            boolean columnIsOne = traits.columns == 1;
            boolean rowIsOne = traits.rows == 1;
            if (columnIsOne && rowIsOne)
                  line = Component.translatable("traits.beansbackpacks.equipment.chest.solo");
            else if (columnIsOne)
                  line = Component.translatable("traits.beansbackpacks.equipment.chest.line", traits.columns);
            else if (rowIsOne)
                  line = Component.translatable("traits.beansbackpacks.equipment.chest.line", traits.rows);
            else
                  line = Component.translatable("traits.beansbackpacks.equipment.chest.size", traits.columns, traits.rows);

            pTooltipAdder.accept(line.withStyle(ChatFormatting.GOLD));
      }

      @Override
      public @Nullable ClientTooltipComponent getTooltipComponent(ChestTraits traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            return null;
      }

      public void openTinyMenu(ChestTraits chestTraits, Slot slot) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof AbstractContainerScreen<?> screen) {
                  MenuChestScreen.openWithSlot(minecraft, screen, chestTraits, slot);
            } else if (minecraft.screen instanceof MenuChestScreen chestScreen) {
                  chestScreen.onClose();
            }
      }

      public void openTinyMenu(ChestTraits chestTraits, InteractionHand hand, Player player) {
            for (Slot slot : player.inventoryMenu.slots) {
                  if (slot.getItem() == player.getItemInHand(hand)) {
                        Minecraft minecraft = Minecraft.getInstance();
                        MenuChestScreen.openWithHand(minecraft, player, chestTraits, slot);
                        return;
                  }
            }
      }

      public void swapTinyMenu(ChestTraits chestTraits, Slot slot) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof MenuChestScreen chestScreen) {
                  chestScreen.swap(chestTraits, slot);
            }
      }
}
