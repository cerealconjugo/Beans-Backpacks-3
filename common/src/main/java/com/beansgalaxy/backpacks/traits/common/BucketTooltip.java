package com.beansgalaxy.backpacks.traits.common;

import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import com.beansgalaxy.backpacks.util.Tint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class BucketTooltip implements ClientTooltipComponent {
      private final Minecraft minecraft;
      private final ItemStack stack;
      private final Component title;
      private final TextureAtlasSprite sprite;
      private final Tint tint;
      private final int buckets;
      private final int bottles;
      private final int droplets;

      public BucketTooltip(ItemStack itemstack, Component title, TextureAtlasSprite sprite, Tint tint, int buckets, int bottles, int droplets) {
            this.minecraft = Minecraft.getInstance();
            this.stack = itemstack;
            this.title = title;
            this.sprite = sprite;
            this.tint = tint;
            this.buckets = buckets;
            this.bottles = bottles;
            this.droplets = droplets;
      }

      public BucketTooltip(ItemStack itemstack, Component title, ResourceLocation texture, Tint tint, int buckets, int bottles, int droplets) {
            this.minecraft = Minecraft.getInstance();
            this.stack = itemstack;
            this.title = title;
            this.sprite = minecraft.getGuiSprites().getSprite(texture);
            this.tint = tint;
            this.buckets = buckets;
            this.bottles = bottles;
            this.droplets = droplets;
      }

      @Override
      public int getHeight() {
            return 18;
      }

      @Override
      public int getWidth(Font font) {
            int xO = 19;
            if (buckets > 0) {
                  String line = "\uD83E\uDEA3" + buckets;
                  xO += font.width(line) + 2;
            }
            if (bottles > 0) {
                  String line = "\uD83E\uDDEA" + bottles;
                  xO += font.width(line) + 1;
            }
            if (droplets > 0) {
                  String line = droplets + "mb";
                  xO += font.width(line);
            }

            return xO;
      }

      @Override
      public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics gui) {
            int tooltipWidth = Math.max(font.width(title), getWidth(font));
            BundleTooltip.renderHoveredItemTooltip(minecraft, gui, font, mouseX, mouseY, tooltipWidth, stack);

            int x = mouseX + 1;
            int y = mouseY - 1;

            int xO = x + 19;
            int yO = y + 5;
            if (buckets > 0) {
                  String icon = "\uD83E\uDEA3";
                  gui.drawString(font, icon, xO, yO - 1, 0xFFFFFFFF);
                  String count = String.valueOf(buckets);
                  gui.drawString(font, count, xO + 8, yO, 0xFFFFFFFF);
                  xO += font.width(icon) + font.width(count) + 2;
            }
            if (bottles > 0) {
                  String icon = "\uD83E\uDDEA";
                  gui.drawString(font, icon, xO, yO - 1, 0xFFFFFFFF);
                  String count = String.valueOf(bottles);
                  gui.drawString(font, count, xO + 8, yO, 0xFFFFFFFF);
                  xO += font.width(icon) + font.width(count) + 2;
            }
            if (droplets > 0) {
                  gui.drawString(font, droplets + "mb", xO, yO, 0xFFFFFFFF);
            }

            gui.blit(x, y, 16, 16, 16, sprite, tint.getRed() / 255f, tint.getGreen() / 255f, tint.getBlue() / 255f, 1);
      }
}
