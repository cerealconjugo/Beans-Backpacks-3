package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.network.serverbound.TinyChestClick;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ChestTraitScreen extends Screen {
      private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.parse(Constants.MOD_ID + ":textures/gui/generic_scalable.png");
      private final AbstractContainerScreen<?> previousScreen;
      private final Slot slot;
      private final ChestTraits traits;
      private int leftPos;
      private int topPos;
      private boolean menuKeyLock = true;

      public ChestTraitScreen(AbstractContainerScreen<?> previousScreen, Slot slot, ChestTraits traits) {
            super(Component.literal(""));
            this.previousScreen = previousScreen;
            this.slot = slot;
            this.traits = traits;
      }

      @Override
      protected void init() {
            Window window = minecraft.getWindow();
            int scaledHeight = window.getGuiScaledHeight();
            int scaledWidth = window.getGuiScaledWidth();

            int columns = traits.columns;
            int rows = traits.rows;
            this.leftPos = scaledWidth / 2 - columns * 9;
            this.topPos = scaledHeight / 2 - rows * 9;

            for(int y = 0; y < traits.rows; ++y) {
                  for (int x = 0; x < traits.columns; ++x) {
                        int index = y * traits.columns + x;
                        ChestTraitSlot chestTraitSlot = new ChestTraitSlot(x * 18 + leftPos, y * 18 + topPos, index);
                        addRenderableWidget(chestTraitSlot);
                  }
            }
      }

      @Override
      public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            boolean clicked = super.mouseClicked(pMouseX, pMouseY, pButton);
            if (clicked)
                  return true;

            boolean mouseClicked = previousScreen.mouseClicked(pMouseX, pMouseY, pButton);
            if (mouseClicked) {
                  onClose();
            }
            return mouseClicked;
      }

      @Override
      public void onClose() {
            this.minecraft.setScreen(previousScreen);
      }

      @Override
      public void tick() {
            BackData backData = BackData.get(minecraft.player);
            boolean menuKeyDown = backData.isMenuKeyDown();
            if (menuKeyLock && !menuKeyDown)
                  menuKeyLock = false;

            super.tick();
      }

      Optional<ChestTraits> getTraits() {
            ItemStack stack = slot.getItem();
            return ChestTraits.get(stack);
      }

      @Override
      public void render(@NotNull GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
            PoseStack pose = gui.pose();
            pose.pushPose();
            pose.translate(0, 0, -1000);

            Window window = minecraft.getWindow();
            int scaledHeight = window.getGuiScaledHeight();
            int scaledWidth = window.getGuiScaledWidth();

            float distance = (float) pMouseX / scaledWidth;
            float i = distance * traits.columns * 18;
            int lookX = Mth.floor(i);
            previousScreen.render(gui, leftPos + lookX, scaledHeight / 2, pPartialTick);
            pose.popPose();

            for(int y = 0; y < traits.rows; ++y) {
                  for(int x = 0; x < traits.columns; ++x) {
                        gui.blit(CONTAINER_BACKGROUND, leftPos + x * 18, topPos + y * 18, 1, 0, 0, 18, 18, 256, 256);
                  }
            }

            super.render(gui, pMouseX, pMouseY, pPartialTick);

            AbstractContainerMenu menu = previousScreen.getMenu();
            ItemStack carried = menu.getCarried();
            BundleTooltip.renderItem(minecraft, gui, carried, pMouseX, pMouseY, 300, false);
            BundleTooltip.renderItemDecorations(gui, font, carried, pMouseX, pMouseY, 300);

            ItemStack backpack = slot.getItem();
            List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, backpack);

            int tooltipWidth = 0;
            for (Component line : tooltipFromItem) {
                  int j = font.width(line);
                  if (j > tooltipWidth)
                        tooltipWidth = j;
            }

            int x = leftPos - tooltipWidth - 20;
            int y;

            if (x < 5) {
                  x = leftPos;
                  y = topPos - tooltipFromItem.size() * 12 + 6;
            }
            else {
                  y = topPos - 5 + traits.columns * 4;
            }

            gui.renderTooltip(font, tooltipFromItem, Optional.empty(), x, y);
      }

      @Override
      public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            renderMenuBackground(pGuiGraphics);
      }

      public class ChestTraitSlot
                  extends AbstractWidget
      {
            private final int index;

            public ChestTraitSlot(int pX, int pY, int index) {
                  super(pX, pY, 18, 18, Component.empty());
                  this.index = index;
            }

            public ItemStack getItem() {
                  ItemContainerContents contents = slot.getItem().get(ITraitData.CHEST);
                  if (contents == null)
                        return ItemStack.EMPTY;

                  NonNullList<ItemStack> pList = NonNullList.withSize(traits.size(), ItemStack.EMPTY);
                  contents.copyInto(pList);
                  return index < pList.size() ? pList.get(index) : ItemStack.EMPTY;
            }

            @Override
            protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
                  ItemStack stack = getItem();
                  boolean hovered = isHovered();
                  int x = getX() + 9;
                  int y = getY() + 9;

                  if (!stack.isEmpty()) {
                        Minecraft minecraft = Minecraft.getInstance();
                        BundleTooltip.renderItem(minecraft, gui, stack, x, y, 50, false);
                        BundleTooltip.renderItemDecorations(gui, font, stack, x, y, 50);

                        if (hovered && previousScreen.getMenu().getCarried().isEmpty()) {
                              List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, stack);
                              Optional<TooltipComponent> tooltipImage = stack.getTooltipImage();
                              gui.renderTooltip(font, tooltipFromItem, tooltipImage, mouseX, mouseY);
                        }
                  }

                  if (hovered)
                        gui.fill(x - 8, y - 8, x + 8, y + 8, 100, 0x88FFFFFF);
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

            }

            @Override
            public void onClick(double pMouseX, double pMouseY) {
            }

            @Override
            public boolean mouseClicked(double pMouseX, double pMouseY, int button) {
                  if (this.active && this.visible && this.clicked(pMouseX, pMouseY)) {
                        Optional<ChestTraits> optional = getTraits();
                        if (optional.isEmpty())
                              return true;

                        ChestTraits traits = optional.get();
                        AbstractContainerMenu menu = previousScreen.getMenu();
                        if (!traits.canItemFit(PatchedComponentHolder.of(slot.getItem()), menu.getCarried()))
                              return true;

                        SlotAccess carriedAccess = new SlotAccess() {
                              public ItemStack get() {
                                    return menu.getCarried();
                              }

                              public boolean set(ItemStack p_150452_) {
                                    menu.setCarried(p_150452_);
                                    return true;
                              }
                        };

                        traits.tinyMenuClick(slot, index, button, carriedAccess);
                        TinyChestClick.send(menu.containerId, slot, index, button);

                        return true;
                  }
                  return false;
            }

            @Override
            public void playDownSound(SoundManager pHandler) {

            }
      }
}
