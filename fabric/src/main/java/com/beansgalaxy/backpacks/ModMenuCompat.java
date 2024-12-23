package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.data.config.*;
import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.bettercombat.fabric.client.ModMenuIntegration;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.function.Function;

public class ModMenuCompat implements ModMenuApi {

      @Override
      public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return screen -> {
                  ImmutableMap.Builder<IConfig, Function<ConfigScreen, ConfigRows>> map = ImmutableMap.builder();
                  Minecraft minecraft = Minecraft.getInstance();
                  ConfigScreen.buildPageMap(minecraft, map);
                  return new ConfigScreen(screen, map.build());
            };
      }
}
