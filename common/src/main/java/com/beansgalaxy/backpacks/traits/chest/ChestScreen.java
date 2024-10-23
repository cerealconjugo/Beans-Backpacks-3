package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ChestScreen extends AbstractContainerScreen<ChestMenu> implements MenuAccess<ChestMenu> {
      private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.parse(Constants.MOD_ID + ":textures/gui/generic_scalable.png");
      private final int rows;
      private final AbstractContainerScreen<?> previousScreen;

      public ChestScreen(ChestMenu menu, Inventory inv, Component title, AbstractContainerScreen<?> previousScreen) {
            super(menu, inv, title);
            this.rows = menu.rows;
            this.imageHeight = 114 + this.rows * 18;
            this.inventoryLabelY = this.imageHeight - 94;
            this.previousScreen = previousScreen;
      }

      @Override
      public void onClose() {
            minecraft.setScreen(previousScreen);
      }

      public void render(GuiGraphics $$0, int $$1, int $$2, float $$3) {
            super.render($$0, $$1, $$2, $$3);
            this.renderTooltip($$0, $$1, $$2);
      }

      protected void renderBg(GuiGraphics gui, float $$1, int $$2, int $$3) {
            int $$4 = (this.width - this.imageWidth) / 2;
            int centerY = (this.height - this.imageHeight) / 2;
            for (Slot slot : menu.traitSlots) {
                  gui.blit(CONTAINER_BACKGROUND, leftPos + slot.x - 1, topPos + slot.y - 1, 1, 0, 0, 18, 18, 256, 256);
            }

            int bottomY = centerY + this.rows * 18 + 17;
            if (menu.columns > 9) {
                  int o = (menu.columns - 9) * 9;
                  int left = leftPos - o;

                  gui.blit(CONTAINER_BACKGROUND, left, topPos, 1, 0, 84, this.imageWidth - 8, 17, 256, 256);
                  gui.blit(CONTAINER_BACKGROUND, leftPos + o, topPos, 0, 84, this.imageWidth, 17);

                  for (int i = 0; i < rows; i++) {
                        gui.blit(CONTAINER_BACKGROUND, left, centerY + 17 + 18 * i, 0, 92, 7, 18);
                        gui.blit(CONTAINER_BACKGROUND, leftPos + imageWidth + o - 7, centerY + 17 + 18 * i, 169, 92, 7, 18);
                  }

                  gui.blit(CONTAINER_BACKGROUND, left, bottomY, 0, 215, o + 4, 17);
                  gui.blit(CONTAINER_BACKGROUND, leftPos + imageWidth - 4, bottomY, imageWidth - o - 4, 215, o + 4, 17);

            } else {
                  gui.blit(CONTAINER_BACKGROUND, $$4, centerY, 0, 84, this.imageWidth, 21);
                  gui.blit(CONTAINER_BACKGROUND, $$4, centerY + 21, 0, 92, this.imageWidth, 18);
                  for (int i = 0; i < rows; i++) {
                        gui.blit(CONTAINER_BACKGROUND, $$4, centerY + 21 + 18 * i, 0, 92, this.imageWidth, 18);
                  }
            }
            gui.blit(CONTAINER_BACKGROUND, $$4, bottomY, 0, 126, this.imageWidth, 96);
      }
}
