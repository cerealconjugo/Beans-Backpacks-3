package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class XpTooltip implements ClientTooltipComponent {
      private final ItemStack backpack;
      private final Minecraft minecraft;
      private final int levels;
      private final float toNextLevel;

      public XpTooltip(XpTraits traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            this.minecraft = Minecraft.getInstance();
            int points = holder.getOrDefault(ITraitData.AMOUNT, 0);
            XpPackagable packagable = new XpPackagable(points);

            this.levels = packagable.experienceLevel;
            this.toNextLevel = packagable.experienceProgress;
            this.backpack = itemStack;

      }

      @Override
      public int getHeight() {
            return 14;
      }

      @Override
      public int getWidth(@NotNull Font font) {
            return font.width(this.levels + " Levels");
      }

      @Override
      public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics gui) {
            BundleTooltip.renderHoveredItemTooltip(minecraft, gui, font, x, y + 12, getWidth(font), backpack);

            int color = 0;
            String s = this.levels + " Levels";
            gui.drawString(font, s, x + 1, y, color, false);
            gui.drawString(font, s, x - 1, y, color, false);
            gui.drawString(font, s, x, y + 1, color, false);
            gui.drawString(font, s, x, y - 1, color, false);
            gui.drawString(font, s, x, y, 8453920, false);

            int sWidth = font.width(s);
            int i = (sWidth / 4);
            int barX = (sWidth - i * 4) / 2 + x;
            int topWidth = (int) (i * toNextLevel);

            gui.blit(ResourceLocation.fromNamespaceAndPath(CommonClass.MOD_ID, "textures/gui/xp_tooltip.png"), barX - 1, y + 9, 0, toNextLevel == 0 ? 3 : 0, 2, 3, 11, 6);
            for (int j = 0; j < i; j++) {
                  if (j < topWidth)
                        gui.blit(ResourceLocation.fromNamespaceAndPath(CommonClass.MOD_ID, "textures/gui/xp_tooltip.png"), barX + (j * 4) + 1, y + 9, 2, 0, 4, 3, 11, 6);
                  else
                        gui.blit(ResourceLocation.fromNamespaceAndPath(CommonClass.MOD_ID, "textures/gui/xp_tooltip.png"), barX + (j * 4) + 1, y + 9, 2, 3, 4, 3, 11, 6);
            }
            gui.blit(ResourceLocation.fromNamespaceAndPath(CommonClass.MOD_ID, "textures/gui/xp_tooltip.png"), barX + (i * 4) + 1, y + 9, 11, 3, 1, 3, 11, 6);
      }
}
