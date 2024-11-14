package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
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
      private final Minecraft minecraft;
      private final Component title;
      private final ItemStack itemstack;

      public BulkTooltip(BulkTraits traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            this.minecraft = Minecraft.getInstance();
            this.title = title;
            this.itemstack = itemStack;
            BulkMutable.BulkStacks bulkStacks = holder.get(ITraitData.BULK_STACKS);
            if (bulkStacks == null || bulkStacks.isEmpty()) {
                  this.item = ItemStack.EMPTY;
                  this.amount = 0;
            }
            else {
                  this.item = bulkStacks.emptyStacks().getFirst().withItem(bulkStacks.itemHolder());
                  this.amount = bulkStacks.amount();
            }

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
            BundleTooltip.renderHoveredItemTooltip(minecraft, gui, font, x, y, tooltipWidth, itemstack);

            gui.drawString(font, "x" + this.amount, x + 22, y + 3, 0xFFFFFFFF);
            gui.renderFakeItem(item, x + 3, y - 1);
      }
}
