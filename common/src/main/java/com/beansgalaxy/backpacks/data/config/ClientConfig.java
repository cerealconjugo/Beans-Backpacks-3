package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import com.beansgalaxy.backpacks.data.options.ShorthandHUD;
import net.minecraft.client.Minecraft;

import java.util.Collection;
import java.util.List;

public class ClientConfig implements IConfig {
      public EnumConfigVariant<ShorthandHUD> shorthand_hud_location;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  shorthand_hud_location = new EnumConfigVariant<>("shorthand_hud_location", ShorthandHUD.NEAR_CENTER, ShorthandHUD.values())
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
