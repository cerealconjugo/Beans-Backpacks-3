package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import com.beansgalaxy.backpacks.data.options.ShorthandHUD;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ClientConfig implements IConfig {
      public EnumConfigVariant<ShorthandHUD> shorthand_hud_location;
      public HSetConfigVariant<Item> elytra_model_equipment;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  shorthand_hud_location = new EnumConfigVariant<>("shorthand_hud_location", ShorthandHUD.NEAR_CENTER, ShorthandHUD.values()),
                  elytra_model_equipment = HSetConfigVariant.Builder.create(Constants::itemShortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                                                                 .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in)))
                                                                 .defauString("minecraft:elytra")
                                                                 .comment("effects the position of the backpack on the player's back while these items are equipped in the chestplate slot")
                                                                 .build("elytra_model_equipment"),
      };

      @Override
      public String getPath() {
            return "-client";
      }

      @Override
      public Collection<ConfigLine> getLines() {
            return List.of(LINES);
      }

}
