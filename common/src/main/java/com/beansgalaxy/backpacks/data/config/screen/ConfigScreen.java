package com.beansgalaxy.backpacks.data.config.screen;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.ServerSave;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
      private final Screen lastScreen;
      private final IConfig[] pages;
      private ConfigRows[] rows;
      private ConfigRows currentPage;

      public ConfigScreen(Screen lastScreen, IConfig... pages) {
            super(Component.empty());
            this.lastScreen = lastScreen;
            this.pages = pages;
            for (IConfig page : this.pages) {
                  page.read();
            }
      }

      @Override
      public List<? extends GuiEventListener> children() {
            List<? extends GuiEventListener> children = super.children().stream().filter(in -> !(in != currentPage && in instanceof ConfigRows)).toList();
            return children;
      }

      @Override
      protected void init() {
            super.init();
            ArrayList<ConfigRows> rows = new ArrayList<>();
            ArrayList<PageTab> tabs = new ArrayList<>();
            int totalWidth = -20;
            for (IConfig page : pages) {
                  ConfigRows row = page.toRows(this, minecraft);
                  rows.add(row);

                  MutableComponent title = Component.translatable("screen.beansbackpacks.config.title" + row.config.getPath());
                  int titleWidth = minecraft.font.width(title);
                  totalWidth += 20 + titleWidth;
                  tabs.add(new PageTab(row, title, titleWidth));
            }

            this.rows = rows.toArray(ConfigRows[]::new);
            ServerSave.CONFIG.read(false);
            currentPage = this.rows[0];
            addWidgets();

            int x = (this.width - totalWidth) / 2;
            for (PageTab tab : tabs) {
                  addWidget(tab.row);

                  this.addRenderableWidget(new PlainTextButton(x, 20, tab.width, 20, tab.title,
                              in -> currentPage = tab.row, minecraft.font)
                  {
                        @Override public boolean isFocused() {
                              return currentPage == tab.row;
                        }
                  });

                  x += tab.width + 20;
            }
      }

      private record PageTab(ConfigRows row, Component title, int width) {}

      private void addWidgets() {
            int center = this.width / 2;
            this.addRenderableWidget(Button.builder(Component.translatable("screen.beansbackpacks.config.main.reset_all"), ($$0) -> {
                  for (ConfigRows.ConfigLabel row : currentPage.getRows())
                        row.resetToDefault();
            }).bounds(center - 165, this.height - 26, 70, 20).build());

            this.addRenderableWidget(Button.builder(Component.translatable("screen.beansbackpacks.config.main.undo_all"), ($$0) -> {
                  for (IConfig page : pages)
                        page.read(false);
            }).bounds(center - 80, this.height - 26, 70, 20).build());

            this.addRenderableWidget(Button.builder(Component.translatable("screen.beansbackpacks.config.main.save_and_exit"), ($$0) -> {
                  for (IConfig page : pages)
                        page.write();
                  this.minecraft.setScreen(this.lastScreen);
            }).bounds(center + 5, this.height - 26, 160, 20).build());
      }

      @Override
      public void onClose() {
            this.minecraft.setScreen(this.lastScreen);
      }

      public void onSave() {
            for (ConfigRows row : rows) {
                  for (ConfigRows.ConfigLabel label : row.getRows())
                        label.onSave();
            }
      }

      @Override
      public void render(GuiGraphics gui, int x, int y, float delta) {
            super.render(gui, x, y, delta);
            MutableComponent title = Component.literal(Constants.MOD_NAME).withStyle(ChatFormatting.BOLD);
            gui.drawCenteredString(font, title, minecraft.getWindow().getGuiScaledWidth() / 2, 6, 0xFFCCDDFF);
            if (currentPage != null)
                  currentPage.render(gui, x, y, delta);
      }



}
