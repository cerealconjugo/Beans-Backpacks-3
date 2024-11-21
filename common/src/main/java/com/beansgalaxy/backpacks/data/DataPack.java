package com.beansgalaxy.backpacks.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;

public class DataPack {
      public static final HashSet<Item> WEAPON_ITEM = new HashSet<>();
      public static final HashSet<Item> WEAPON_REMOVED = new HashSet<>();
      public static final HashSet<Item> TOOL_ITEM = new HashSet<>();
      public static final HashSet<Item> TOOL_REMOVED = new HashSet<>();

      public static void load(ResourceManager resourceManager) {
            readItemList(resourceManager, WEAPON_ITEM, WEAPON_REMOVED, "weapons");
            readItemList(resourceManager, TOOL_ITEM, TOOL_REMOVED, "tools");
      }

      public static void readItemList(ResourceManager resourceManager, HashSet<Item> items, HashSet<Item> removed, String location) {
            Map<ResourceLocation, Resource> locations = resourceManager.listResources("modify",
                        (in) -> in.getPath().endsWith(location));

            locations.forEach( (resourceLocation, resource) -> {
                  try {
                        InputStream open = resource.open();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(open));

                        String line;
                        while ((line = reader.readLine()) != null) {
                              String[] split = line.replaceAll(" ", "").split(",");
                              for (String id: split) {
                                    if (id.startsWith("!"))
                                          removed.add(itemFromString(id.replace("!", "")));
                                    else
                                          items.add(itemFromString(id));
                              }
                        }
                  } catch (IOException e) {
                        throw new RuntimeException(e);
                  }
            });

            items.removeAll(removed);
            items.removeIf(in -> in.equals(Items.AIR));
            removed.removeIf(in -> in.equals(Items.AIR));
      }

      public static Item itemFromString(String string) {
            if (string == null)
                  return Items.AIR;
            return BuiltInRegistries.ITEM.get(ResourceLocation.parse(string));
      }
}
