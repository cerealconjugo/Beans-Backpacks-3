package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BatteryTooltip implements ClientTooltipComponent {
      private final Minecraft minecraft;
      private final Component title;
      private final ItemStack itemstack;
      private final PatchedComponentHolder holder;

      public BatteryTooltip(ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            this.minecraft = Minecraft.getInstance();
            this.itemstack = itemStack;
            this.holder = holder;
            this.title = title;
      }

      @Override
      public int getHeight() {
            return 18;
      }

      @Override
      public int getWidth(@NotNull Font font) {
            Long amount = holder.getOrDefault(ITraitData.LONG, 0L);
            return 19 + font.width("x" + amount);
      }

      @Override
      public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics gui) {
            int tooltipWidth = Math.max(font.width(title), getWidth(font));
            BundleTooltip.renderHoveredItemTooltip(minecraft, gui, font, x, y, tooltipWidth, itemstack);

            ItemStack stack = holder.get(ITraitData.SOLO_STACK);
            if (stack == null || stack.isEmpty())
                  return;

            gui.renderFakeItem(stack, x + 3, y - 1);
            gui.renderItemDecorations(font, stack, x + 3, y - 1);
      }
}
