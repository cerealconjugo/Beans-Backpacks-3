package com.beansgalaxy.backpacks.trait.battery;

import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BatteryTooltip implements ClientTooltipComponent {
      private final ItemStack backpack;
      private final Minecraft minecraft;
      private final BatteryTraits batteryTraits;
      private final Component title;

      public BatteryTooltip(BatteryTraits batteryTraits, ItemStack backpack, Component title) {
            this.backpack = backpack;
            this.batteryTraits = batteryTraits;
            this.title = title;
            this.minecraft = Minecraft.getInstance();
      }

      @Override
      public int getHeight() {
            return 18;
      }

      @Override
      public int getWidth(@NotNull Font font) {
            return 19 + font.width("x" + this.batteryTraits.amount());
      }

      @Override
      public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics gui) {
            int tooltipWidth = Math.max(font.width(title), getWidth(font));
            BundleTooltip.renderHoveredItemTooltip(minecraft, gui, font, x, y, tooltipWidth, backpack);

            ItemStack stack = this.batteryTraits.stack();
            gui.renderFakeItem(stack, x + 3, y - 1);
            gui.renderItemDecorations(font, stack, x + 3, y - 1);
      }
}
