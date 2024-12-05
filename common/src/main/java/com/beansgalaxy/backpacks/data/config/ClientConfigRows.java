package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import com.beansgalaxy.backpacks.data.options.ShorthandHUD;
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
            ClientConfig config = (ClientConfig) this.config;
            return List.of(
                        new EnumConfigRow<>(config.shorthand_hud_location, ShorthandHUD.values()),
                        new ItemListConfigRow(config.elytra_model_equipment)
            );
      }

      @Override
      public Optional<GuiEventListener> getChildAt(double $$0, double $$1) {
            return super.getChildAt($$0, $$1);
      }
}
