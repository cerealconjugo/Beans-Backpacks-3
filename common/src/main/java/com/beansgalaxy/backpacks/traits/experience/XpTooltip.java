package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class XpTooltip implements ClientTooltipComponent {
      private final ItemStack backpack;
      private final Minecraft minecraft;
      private final int levels;
      private final float toNextLevel;
      private final boolean isFull;

      public XpTooltip(XpTraits xpTraits, ItemStack backpack) {
            this.isFull = xpTraits.isFull();
            int points = xpTraits.points();

            int level = 0;
            float toNextLevel = 0;
            int xp = points;
            while (xp > 0) {
                  int lostXp;
                  if (level >= 30)
                        lostXp = 112 + (level - 30) * 9;
                  else if (level >= 15)
                        lostXp = 37 + (level - 15) * 5;
                  else
                        lostXp = 7 + level * 2;

                  if (lostXp > xp) {
                        toNextLevel = (float) xp / lostXp;
                        xp = 0;
                  }
                  else {
                        xp -= lostXp;
                        level++;
                  }
            }

            this.levels = level;
            this.toNextLevel = toNextLevel;
            this.backpack = backpack;
            this.minecraft = Minecraft.getInstance();
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

            int color = 0;// isFull ? 0x17753b : 0;
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

            gui.blit(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/xp_tooltip.png"), barX - 1, y + 9, 0, toNextLevel == 0 ? 3 : 0, 2, 3, 11, 6);
            for (int j = 0; j < i; j++) {
                  if (j < topWidth)
                        gui.blit(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/xp_tooltip.png"), barX + (j * 4) + 1, y + 9, 2, 0, 4, 3, 11, 6);
                  else
                        gui.blit(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/xp_tooltip.png"), barX + (j * 4) + 1, y + 9, 2, 3, 4, 3, 11, 6);
            }
            gui.blit(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/xp_tooltip.png"), barX + (i * 4) + 1, y + 9, 11, 3, 1, 3, 11, 6);
      }
}
