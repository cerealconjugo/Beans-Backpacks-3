package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.ConfigLine;

import java.util.*;

public class FeaturesConfig implements IConfig {

      @Override
      public String getPath() {
            return "features";
      }

      @Override
      public Collection<ConfigLine> getLines() {
            return List.of();
      }
}
