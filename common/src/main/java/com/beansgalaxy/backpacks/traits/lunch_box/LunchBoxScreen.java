package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.screen.TinyTraitScreen;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleScreen;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LunchBoxScreen extends BundleScreen {
      boolean hoverNonEdible = false;

      public static void openScreen(BackpackEntity backpack, BundleLikeTraits traits) {
            LunchBoxScreen screen = new LunchBoxScreen(backpack, traits);
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(screen);
      }

      protected LunchBoxScreen(BackpackEntity backpack, BundleLikeTraits traits) {
            super(backpack, traits);
      }

      @Override
      protected void repopulateSlots(@NotNull GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
            ItemStack carried = getCarried();
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            List<ItemStack> nonEdibles = backpack.get(ITraitData.NON_EDIBLES);

            int size;
            int stacksSize;
            boolean hasSpace;
            boolean carriedEmpty = carried.isEmpty();
            if (stacks == null) {
                  hasSpace = true;
                  stacksSize = 1;
                  size = 1;
            }
            else {
                  hasSpace = traits.fullness(stacks).compareTo(Fraction.ONE) != 0;
                  stacksSize = stacks.size();
                  size = stacksSize + (hasSpace ? 1 : 0) + (carriedEmpty ? 0 : 1);
            }

            int nonEdibleSize = nonEdibles == null ? 0 : nonEdibles.size();

            boolean forCol = false;
            int columns = Math.min(size + nonEdibleSize, 4);
            int rows = 1;
            for (int i = columns; i <= size + nonEdibleSize; i++) {
                  if (i > columns * rows) {
                        if (forCol)
                              columns++;
                        else
                              rows++;
                        forCol = !forCol;
                  }
            }

            clearSlots();
            hoverNonEdible = false;
            boolean hoverOverAny = false;
            int left = leftPos + 11 - 28;
            int top = getTopPos() - 17;
            int width = columns * 18;
            int i = hasSpace ? -1 : 0;

            boolean shouldHoverNonEdibles = nonEdibles != null && carriedEmpty;
            for(int y = 0; y < rows; ++y) {
                  for(int x = 0; x < columns; ++x) {
                        BundleTraitSlot slot;
                        int nonEdiblesStart = size - 1;
                        if (i < nonEdiblesStart) {
                              slot = new BundleTraitSlot(left + x * 18, top + y * 18, i) {
                                    @Override
                                    public boolean isHovered() {
                                          boolean hovered = super.isHovered();
                                          if (shouldHoverNonEdibles && hovered) {
                                                hoverNonEdible = true;
                                          }

                                          return hovered && !shouldHoverNonEdibles;
                                    }
                              };
                              if (i == stacksSize)
                                    lastSlot = slot;
                        } else {
                              int index = i - nonEdiblesStart;
                              if (index == 0) {
                                    slot = new LunchTraitSlot(left + x * 18, top + y * 18, index) {
                                          @Override
                                          public boolean isHovered() {
                                                return shouldHoverNonEdibles
                                                            ? hoverNonEdible || super.isHovered()
                                                            : carriedEmpty && super.isHovered();
                                          }

                                          @Override
                                          protected void renderWidget(GuiGraphics gui, int i, int i1, float v) {
                                                if (isHovered && !shouldHoverNonEdibles && !carriedEmpty && lastSlot != null) {
                                                      int x1 = lastSlot.getX() + 9;
                                                      int y1 = lastSlot.getY() + 9;
                                                      gui.fill(x1 - 8, y1 - 8, x1 + 8, y1 + 8, 100, 0x88FFFFFF);
                                                }
                                                super.renderWidget(gui, i, i1, v);
                                          }
                                    };
                              }
                              else slot = new LunchTraitSlot(left + x * 18, top + y * 18, index);
                        }
                        boolean mouseOver = slot.isMouseOver(pMouseX, pMouseY);
                        if (mouseOver) {
                              hoverOverAny = true;
                        }
                        if (!shouldHoverNonEdibles && mouseOver) {
                              List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, slot.getItem());
                              Component first = tooltipFromItem.getFirst();
                              int titleWidth = font.width(first);
                              if (titleWidth > width)
                                    width = titleWidth;
                        }
                        addSlot(slot);
                        i++;
                  }
            }

            if (hoverOverAny && shouldHoverNonEdibles) {
                  List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, nonEdibles.get(0));
                  Component first = tooltipFromItem.getFirst();
                  int titleWidth = font.width(first);
                  if (titleWidth > width)
                        width = titleWidth;
            }

            TooltipRenderUtil.renderTooltipBackground(gui, left, top - 10, width, rows * 18 + 11, 0);

            for (TinyTraitSlot slot : slots) {
                  slot.render(gui, pMouseX, pMouseY, pPartialTick);
            }
      }

      public class LunchTraitSlot extends BundleTraitSlot {

            public LunchTraitSlot(int pX, int pY, int index) {
                  super(pX, pY, index);
            }

            public ItemStack getItem() {
                  List<ItemStack> stacks = backpack.get(ITraitData.NON_EDIBLES);
                  if (stacks == null || index == -1)
                        return ItemStack.EMPTY;

                  return index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
            }
      }
}
