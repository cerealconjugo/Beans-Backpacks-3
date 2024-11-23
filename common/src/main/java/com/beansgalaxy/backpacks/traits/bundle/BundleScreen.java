package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.network.serverbound.TinyHotbarClick;
import com.beansgalaxy.backpacks.network.serverbound.TinyMenuClick;
import com.beansgalaxy.backpacks.network.serverbound.TinyMenuInteract;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.screen.TinyTraitScreen;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BundleScreen extends TinyTraitScreen {
      protected final BundleLikeTraits traits;
      protected final BackpackEntity backpack;
      protected BundleScreen.BundleTraitSlot lastSlot = null;

      public static void openScreen(BackpackEntity backpack, BundleLikeTraits traits) {
            BundleScreen screen = new BundleScreen(backpack, traits);
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(screen);
            TinyMenuInteract.send(backpack.getId(), true);
      }

      protected BundleScreen(BackpackEntity backpack, BundleLikeTraits traits) {
            super(Component.literal("Bundle Trait Screen"));
            this.backpack = backpack;
            this.traits = traits;
      }

      @Override @NotNull
      public List<? extends GuiEventListener> children() {
            return Stream.concat(super.children().stream(), slots.stream()).toList();
      }

      @Override
      protected void init() {
            Window window = minecraft.getWindow();
            int scaledHeight = window.getGuiScaledHeight();
            int scaledWidth = window.getGuiScaledWidth();
            leftPos = scaledWidth / 2;
            setTopPos(scaledHeight / 2);
            initHotBarSlots();
      }

      @Override
      public void onClose() {
            super.onClose();
            TinyMenuInteract.send(backpack.getId(), false);
      }

      @Override
      public void render(@NotNull GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
            PoseStack pose = gui.pose();
            pose.pushPose();

            repopulateSlots(gui, pMouseX, pMouseY, pPartialTick);
            super.render(gui, pMouseX, pMouseY, pPartialTick);

            List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, backpack.toStack());
            int width = 0;
            for (Component line : tooltipFromItem) {
                  int i = font.width(line);
                  if (i > width)
                        width = i;
            }

            width += 18 + 5 + 28;

            gui.renderTooltip(font, tooltipFromItem, Optional.empty(), leftPos - width, getTopPos() - 15);
      }

      protected void repopulateSlots(@NotNull GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
            ItemStack carried = getCarried();
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            boolean hasSpace;
            int size;

            if (stacks == null) {
                  hasSpace = true;
                  size = 1;
            }
            else {
                  hasSpace = traits.fullness(stacks).compareTo(Fraction.ONE) != 0;
                  size = stacks.size() + (hasSpace ? 1 : 0) + (carried.isEmpty() ? 0 : 1);
            }

            boolean forCol = false;
            int columns = Math.min(size, 4);
            int rows = 1;
            for (int i = columns; i <= size; i++) {
                  if (i > columns * rows) {
                        if (forCol)
                              columns++;
                        else
                              rows++;
                        forCol = !forCol;
                  }
            }

            clearSlots();
            int left = leftPos + 11 - 18;
            int top = getTopPos() - 17;
            int width = columns * 18;
            int i = hasSpace ? -1 : 0;

            for(int y = 0; y < rows; ++y) {
                  for(int x = 0; x < columns; ++x) {
                        BundleTraitSlot slot = new BundleTraitSlot(left + x * 18, top + y * 18, i);
                        if (slot.isMouseOver(pMouseX, pMouseY)) {
                              List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, slot.getItem());
                              Component first = tooltipFromItem.getFirst();
                              int titleWidth = font.width(first);
                              if (titleWidth > width)
                                    width = titleWidth;
                        }
                        addSlot(slot);
                        i++;
                        if (i == size - (carried.isEmpty() ? 0 : 1))
                              lastSlot = slot;
                  }
            }

            TooltipRenderUtil.renderTooltipBackground(gui, left, top - 10, width, rows * 18 + 11, 0);

            for (TinyTraitSlot slot : slots) {
                  slot.render(gui, pMouseX, pMouseY, pPartialTick);
            }
      }

      @Override
      protected void tinyHotbarClick(TinyClickType clickType, InventoryMenu menu, LocalPlayer player, int index) {
            traits.tinyHotbarClick(backpack, index, clickType, menu, player);
            TinyHotbarClick.send(backpack, index, clickType);
      }

      @Override
      protected void tinyMenuClick(int index, TinyClickType clickType, SlotAccess carriedAccess, LocalPlayer player) {
            traits.tinyMenuClick(backpack, index, clickType, carriedAccess, player);
            TinyMenuClick.send(backpack, index, clickType);
      }

      public class BundleTraitSlot extends TinyTraitScreen.TinyTraitSlot {

            public BundleTraitSlot(int pX, int pY, int index) {
                  super(pX, pY, index);
            }

            public ItemStack getItem() {
                  List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
                  if (stacks == null || index == -1)
                        return ItemStack.EMPTY;

                  return index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
            }

            @Override
            protected void renderWidget(GuiGraphics gui, int i, int i1, float v) {
                  ItemStack stack = getItem();
                  boolean hovered = isHovered();
                  int x = getX() + 9;
                  int y = getY() + 9;

                  if (!stack.isEmpty()) {
                        Minecraft minecraft = Minecraft.getInstance();
                        BundleTooltip.renderItem(minecraft, gui, stack, x, y, 50, false);
                        BundleTooltip.renderItemDecorations(gui, font, stack, x, y, 50);

                        if (hovered) {
                              List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, stack);
                              Component name = tooltipFromItem.getFirst();
                              int left = leftPos + 5 + 5 - 28;
                              int top = getTopPos() - 18;
                              gui.drawString(minecraft.font, name, left + 1, top - 9, 0xFFFFFFFF);
                              gui.fill(x - 8, y - 8, x + 8, y + 8, 100, 0x88FFFFFF);
                        }
                  } else if (hovered) {
                        if (this == slots.getFirst())
                              gui.fill(x - 8, y - 8, x + 8, y + 8, 100, 0x88FFFFFF);
                        else if (lastSlot != null) {
                              int x1 = lastSlot.getX() + 9;
                              int y1 = lastSlot.getY() + 9;
                              gui.fill(x1 - 8, y1 - 8, x1 + 8, y1 + 8, 100, 0x88FFFFFF);
                        }
                  }
            }
      }
}
