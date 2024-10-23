package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BulkTooltip implements ClientTooltipComponent {
      private final int amount;
      private final ItemStack item;
      private final ItemStack backpack;
      private final Minecraft minecraft;
      private final Component title;

      public BulkTooltip(BulkTraits bulkTraits, ItemStack backpack, Component title) {
            this.amount = bulkTraits.amount();
            this.backpack = backpack;
            this.title = title;
            this.item = bulkTraits.item.value().getDefaultInstance();
            this.minecraft = Minecraft.getInstance();
      }

      @Override
      public int getHeight() {
            return 18;
      }

      @Override
      public int getWidth(@NotNull Font font) {
            return 19 + font.width("x" + this.amount);
      }

      @Override
      public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics gui) {
            int firstWidth = font.width(title);
            int tooltipWidth = Math.max(firstWidth, getWidth(font));
            BundleTooltip.renderHoveredItemTooltip(minecraft, gui, font, x, y, tooltipWidth, backpack);

            gui.drawString(font, "x" + this.amount, x + 22, y + 3, 0xFFFFFFFF);
            gui.renderFakeItem(item, x + 3, y - 1);
      }
}
