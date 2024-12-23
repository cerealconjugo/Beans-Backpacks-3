package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.*;

public class TraitConfigRows extends ConfigRows {
      private MultiLineParentRow parentRow;

      public TraitConfigRows(ConfigScreen screen, Minecraft minecraft, TraitConfig config) {
            super(screen, minecraft, config);

            addEntry(new ConfigDescription(Component.translatable("config.beansbackpacks.traits.description")));
            this.parentRow = new MultiLineParentRow(config.traits);
            addEntry(parentRow);

            addEntry(new ConfigLabel(Component.translatable("config.beansbackpacks.traits.new")));
            addEntry(new NewTraitEntryRow(parentRow));
      }

      @Override
      public void resetToDefault() {
            TraitConfig config = (TraitConfig) this.config;
            parentRow = new MultiLineParentRow(config.traits);
      }

      @Override
      public void onSave() {
            TraitConfig traitConfig = (TraitConfig) config;
            Map<String, JsonObject> map = traitConfig.traits;
            map.clear();

            parentRow.map.forEach((name, row) -> {
                  String value = '{' + row.editBox.getValue() + '}';
                  JsonObject parse = JsonParser.parseString(value).getAsJsonObject();
                  map.put(name, parse);
            });
      }

      public class NewTraitEntryRow extends ConfigLabel {
            private final EditBox editBox;
            private final MultiLineParentRow parentRow;

            public NewTraitEntryRow(MultiLineParentRow parentRow) {
                  super(Component.literal("new_trait_row"));
                  this.editBox = new EditBox(minecraft.font, getRowWidth(), 12, Component.translatable("config.beansbackpacks.traits.new-title"));
                  editBox.setHint(Component.translatable("config.beansbackpacks.traits.new-box").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                  this.parentRow = parentRow;
            }

            @Override public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
                  if (pKeyCode == 257) {
                        String name = editBox.getValue();
                        parentRow.createRow(name, new JsonObject());
                        parentRow.reloadMap();
                        editBox.setValue("");
                  }
                  return super.keyPressed(pKeyCode, pScanCode, pModifiers);
            }

            @Override public void render(GuiGraphics guiGraphics, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
                  editBox.setX(getRowLeft());
                  editBox.setY(y);
                  editBox.render(guiGraphics, mouseX, mouseY, delta);
            }

            @Override public List<? extends GuiEventListener> children() {
                  return List.of(editBox);
            }

            @Override public List<? extends NarratableEntry> narratables() {
                  return List.of(editBox);
            }
      }

      public class MultiLineParentRow extends ConfigLabel {
            protected final HashMap<String, MultiLineEntry> map = new HashMap<>();

            public MultiLineParentRow(Map<String, JsonObject> traits) {
                  super(Component.literal("multiline_parent"));
                  traits.forEach(this::createRow);
                  reloadMap();
            }

            private void createRow(String name, JsonObject row) {
                  MultiLineEditBox editBox = new MultiLineEditBox(minecraft.font, 0, 9, getRowWidth(), 9 * 8, Component.literal(""), Component.literal("message"));

                  String string = row.toString();

                  StringBuilder builder = new StringBuilder(string);
                  builder.deleteCharAt(0).deleteCharAt(string.length() - 2);
                  editBox.setValue(builder.toString());
                  int height = editBox.getInnerHeight() + 9;
                  editBox.setHeight(height);

                  MultiLineEntry configRow = new MultiLineEntry(name, editBox);
                  map.put(name, configRow);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
                  for (MultiLineEntry value : map.values()) {
                        value.render(guiGraphics, y, x, mouseX, mouseY, delta);
                        y += value.getHeight();
                  }
            }

            @Override
            public int getHeight() {
                  return map.values().stream().mapToInt(MultiLineEntry::getHeight).sum();
            }

            private List<AbstractWidget> widgets = List.of();
            private void reloadMap() {
                  ImmutableList.Builder<AbstractWidget> builder = ImmutableList.builder();
                  for (MultiLineEntry value : map.values()) {
                        builder.add(value.editBox, value.confirm_remove, value.deny_remove, value.title);
                  }

                  widgets = builder.build();
            }

            @Override public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
                  if (pKeyCode == 258) {
                        return true;
                  }
                  return super.keyPressed(pKeyCode, pScanCode, pModifiers);
            }

            @Override public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                  boolean fal = false;
                  if (fal) {
                        int i = getRowWidth() / 2;
                        int j = getX() + TraitConfigRows.this.width / 2;
                        int left = j - i;
                        int right = j + i;

                        if (pMouseX < left || pMouseX > right)
                              return false;

                        for (AbstractWidget child : widgets) {
                              if (!child.isActive() || !child.visible)
                                    continue;

                              int topPos = child.getY();
                              int height = child.getHeight();
                              int botPos = topPos + height;

                              if (pMouseY > topPos && pMouseY < botPos) {
                                    int x = child.getX();
                                    if (pMouseX > x && pMouseX < x + child.getWidth()) {
                                          return child.mouseClicked(pMouseX, pMouseY, pButton);
                                    }
                              }
                        }
                  }
                  return super.mouseClicked(pMouseX, pMouseY, pButton);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                  return widgets;
            }

            @Override
            public List<? extends GuiEventListener> children() {
                  return widgets;
            }

            public class MultiLineEntry {
                  private final String entryName;
                  private final MultiLineEditBox editBox;
                  private final Button confirm_remove;
                  private final Button deny_remove;
                  private final Button title;

                  public MultiLineEntry(String name, MultiLineEditBox editBox) {
                        this.entryName = name;
                        this.editBox = editBox;

                        Font font = minecraft.font;

                        int yesWidth = font.width(CommonComponents.GUI_YES);
                        this.confirm_remove = new PlainTextButton(getRowRight() - 40, 0, yesWidth, 9, CommonComponents.GUI_YES, button -> {
                              MultiLineParentRow row = MultiLineParentRow.this;
                              row.map.remove(entryName);
                              row.reloadMap();
                        }, font);
                        confirm_remove.visible = false;

                        int noWidth = font.width(CommonComponents.GUI_NO);
                        this.deny_remove = new PlainTextButton(getRowRight() - 40, 0, noWidth, 9, CommonComponents.GUI_NO, button -> {
                              MultiLineEntry.this.title.active = true;
                              MultiLineEntry.this.confirm_remove.visible = false;
                              MultiLineEntry.this.deny_remove.visible = false;
                        }, font);
                        deny_remove.visible = false;

                        this.title = new PlainTextButton(0, 0, getRowWidth() - yesWidth - noWidth, 9, Component.literal(entryName), button -> {
                              confirm_remove.visible = true;
                              deny_remove.visible = true;
                              Button title = MultiLineEntry.this.title;
                              title.active = false;
                        }, font) {
                              private final Component removal_title = Component.literal(entryName).withStyle(ChatFormatting.STRIKETHROUGH, ChatFormatting.RED);
                              @Override public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                                    Component component;
                                    if (!active) {
                                          component = removal_title;
                                          MutableComponent text = Component.translatable("config.beansbackpacks.traits.remove-trait").withStyle(ChatFormatting.GRAY);
                                          int width = font.width(text);
                                          pGuiGraphics.drawString(font, text, getRowRight() - yesWidth - noWidth - width - 10, this.getY(), 16777215 | Mth.ceil(this.alpha * 255.0F) << 24);

                                    } else if (isHovered)
                                          component = removal_title;
                                    else
                                          component = getMessage();

                                    pGuiGraphics.drawString(font, component, this.getX(), this.getY(), 16777215 | Mth.ceil(this.alpha * 255.0F) << 24);
                              }
                        };
                  }

                  public void render(GuiGraphics guiGraphics, int y, int x, int mouseX, int mouseY, float delta) {
                        confirm_remove.setY(y);
                        deny_remove.setY(y);
                        title.setY(y);
                        int rightPos = getRowRight() - confirm_remove.getWidth();
                        confirm_remove.setX(rightPos);
                        deny_remove.setX(rightPos - deny_remove.getWidth() - 4);
                        title.setX(x + 2);
                        confirm_remove.render(guiGraphics, mouseX, mouseY, delta);
                        deny_remove.render(guiGraphics, mouseX, mouseY, delta);
                        title.render(guiGraphics, mouseX, mouseY, delta);

                        editBox.setY(y + 11);
                        editBox.setX(x);

                        int height = editBox.getInnerHeight() + 9;
                        editBox.setHeight(height);
                        editBox.render(guiGraphics, mouseX, mouseY, delta);
                  }

                  public int getHeight() {
                        return editBox.getHeight() + 15;
                  }
            }
      }
}
