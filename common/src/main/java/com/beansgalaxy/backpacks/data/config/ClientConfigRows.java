package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;
import java.util.Optional;

public class ClientConfigRows extends ConfigRows {
      public ClientConfigRows(ConfigScreen screen, Minecraft minecraft, ClientConfig config) {
            super(screen, minecraft, config);
      }

      @Override
      public List<ConfigLabel> getRows() {
            return List.of(

            );
      }

      @Override
      public Optional<GuiEventListener> getChildAt(double $$0, double $$1) {
            return super.getChildAt($$0, $$1);
      }
}
