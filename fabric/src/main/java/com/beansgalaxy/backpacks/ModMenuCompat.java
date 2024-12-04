package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.data.config.ClientConfig;
import com.beansgalaxy.backpacks.data.config.ClientConfigRows;
import com.beansgalaxy.backpacks.data.config.CommonConfig;
import com.beansgalaxy.backpacks.data.config.CommonConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
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
                  HashMap<IConfig, Function<ConfigScreen, ConfigRows>> map = Maps.newHashMapWithExpectedSize(2);
                  Minecraft minecraft = Minecraft.getInstance();
                  ClientConfig client = new ClientConfig();
                  map.put(client, configScreen -> new ClientConfigRows(configScreen, minecraft, client));
                  CommonConfig common = new CommonConfig();
                  map.put(common, configScreen -> new CommonConfigRows(configScreen, minecraft, common));
                  return new ConfigScreen(screen, map);
            };
      }
}
