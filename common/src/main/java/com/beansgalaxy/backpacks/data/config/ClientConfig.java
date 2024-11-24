package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import net.minecraft.client.Minecraft;

import java.util.Collection;
import java.util.List;

public class ClientConfig implements IConfig {

      private final ConfigLine[] LINES = new ConfigLine[] {

      };

      @Override
      public String getPath() {
            return "-client";
      }

      @Override
      public Collection<ConfigLine> getLines() {
            return List.of(LINES);
      }

      @Override
      public ConfigRows toRows(ConfigScreen configScreen, Minecraft minecraft) {
            return new ClientConfigRows(configScreen, minecraft, this);
      }
}
