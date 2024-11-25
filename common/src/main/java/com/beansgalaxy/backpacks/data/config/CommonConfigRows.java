package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import net.minecraft.client.Minecraft;

import java.util.List;

public class CommonConfigRows extends ConfigRows {
      public CommonConfigRows(ConfigScreen screen, Minecraft minecraft, CommonConfig config) {
            super(screen, minecraft, config);
      }

      @Override
      public List<ConfigLabel> getRows() {
            CommonConfig config = (CommonConfig) this.config;

            return List.of(
                        new IntConfigRow(config.tool_belt_size),
                        new IntConfigRow(config.shorthand_size),
                        new ItemListConfigRow(config.tool_belt_additions),
                        new ItemListConfigRow(config.shorthand_additions)
            );
      }
}
