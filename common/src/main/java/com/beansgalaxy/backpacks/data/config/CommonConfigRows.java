package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CommonConfigRows extends ConfigRows {
      public CommonConfigRows(ConfigScreen screen, Minecraft minecraft, CommonConfig config) {
            super(screen, minecraft, config);
      }

      @Override
      public List<ConfigLabel> getRows() {
            CommonConfig config = (CommonConfig) this.config;

            return List.of(
            new ConfigLabel(Component.translatable("config.beansbackpacks.common.shorthandToolBelt")),
                        new IntConfigRow(config.tool_belt_size),
                        new IntConfigRow(config.shorthand_size),
                        new ItemListConfigRow(config.tool_belt_additions),
                        new ItemListConfigRow(config.shorthand_additions),
                        new BoolConfigRow(config.tool_belt_break_items),
            new ConfigLabel(Component.translatable("config.beansbackpacks.common.keepInventory")),
                        new BoolConfigRow(config.keep_back_on_death),
                        new BoolConfigRow(config.keep_tool_belt_on_death),
                        new BoolConfigRow(config.keep_shorthand_on_death)
            );
      }
}
