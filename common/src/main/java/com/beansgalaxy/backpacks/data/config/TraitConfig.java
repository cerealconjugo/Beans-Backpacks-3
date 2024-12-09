package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.ConfigLine;
import com.beansgalaxy.backpacks.data.config.types.ListConfigVariant;
import com.beansgalaxy.backpacks.platform.Services;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.realmsclient.util.JsonUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TraitConfig implements IConfig {
      public Map<String, JsonObject> traits = new HashMap<>();

      @Override
      public String getPath() {
            return "-traits";
      }

      @Override
      public Collection<ConfigLine> getLines() {
            return List.of();
      }

      @Override
      public void parse(String encodedConfig) {
            JsonObject object = JsonParser.parseString(encodedConfig).getAsJsonObject();
            object.asMap().forEach((string, element) -> {
                  if (element.isJsonObject()) {
                        traits.put(string, element.getAsJsonObject());
                  }
                  else {
                        Constants.LOG.error("Error while parsing beansbackpacks-traits.json5; " + string + "is not a Json Object");
                  }
            });
      }

      @Override
      public void write() {
            try {
                  Path path = Services.PLATFORM.getConfigPath();
                  Path resolve = path.resolve(Constants.MOD_ID + getPath() + ".json5");

                  StringBuilder builder = new StringBuilder("{");

                  Iterator<Map.Entry<String, JsonObject>> iterator = traits.entrySet().iterator();
                  while (iterator.hasNext()) {
                        Map.Entry<String, JsonObject> entry = iterator.next();
                        String name = entry.getKey();
                        builder.append("\n  \"").append(name).append("\": ");

                        StringWriter stringWriter = new StringWriter();
                        JsonWriter jsonWriter = new JsonWriter(stringWriter);
                        jsonWriter.setLenient(true);
                        JsonObject value = entry.getValue();
                        Streams.write(value, jsonWriter);
                        String string = stringWriter.toString();

                        builder.append(string);

                        if (iterator.hasNext())
                              builder.append(",");
                  }

                  String ironKey = "beansbackpacks:iron_backpack";
                  if (!traits.containsKey(ironKey)) {
                        builder.append("\n//\"").append(ironKey).append("\": {")
                               .append("\"bundle\":{\"size\":8}")
                               .append(",\"equipable\": {\"slots\": \"body\",\"backpack_texture\": \"beansbackpacks:iron\",\"sound_event\": [\"beansbackpacks:metal_equip\", \"beansbackpacks:metal_place\"]}")
                               .append(",\"placeable\": {\"backpack_texture\": \"beansbackpacks:iron\"}")
                               .append('}');
                  }

                  String legKey = "minecraft:leggings";
                  if (!traits.containsKey(legKey)) {
                        builder.append("\n//\"").append(legKey).append("\" : {")
                               .append("\"bundle\": {\"size\": 2,\"sound\": \"soft\"}")
                               .append(",\"equipable\": {\"slots\": \"legs\"}")
                               .append('}');
                  }

                  String bucketKey = "beansbackpacks:netherite_bucket";
                  if (!traits.containsKey(bucketKey)) {
                        builder.append("\n//\"").append(bucketKey).append("\" : {")
                               .append("\"bucket\": {\"size\": 8}")
                               .append('}');
                  }

                  String roseKey = "beansbackpacks:rose_boots";
                  if (!traits.containsKey(roseKey)) {
                        builder.append("\n//\"").append(roseKey).append("\" : {")
                               .append("\"equipable\": {\"slots\": \"feet\",\"equipment_model\": {\"leftLeg\": \"beansbackpacks:backpack/rose_boot_left\",\"rightLeg\": \"beansbackpacks:backpack/rose_boot_right\"},\"sound_event\": [\"item.armor.equip_generic\"]}")
                               .append(",\"modifiers\": ")
                               .append("[{\"type\": \"generic.scale\",\"amount\": 0.10,\"slot\": \"feet\",\"id\": \"rose_boot_step\",\"operation\": \"add_multiplied_base\"}")
                               .append(",{\"type\": \"generic.movement_speed\",\"amount\": 0.10,\"slot\": \"feet\",\"id\": \"rose_boot_step\",\"operation\": \"add_multiplied_base\"}")
                               .append(",{\"type\": \"generic.step_height\",\"amount\": 0.5,\"slot\": \"feet\",\"id\": \"rose_boot_step\",\"operation\": \"add_value\"}")
                               .append("]}");
                  }

                  builder.append("\n}");
                  String string = builder.toString();
                  Files.writeString(resolve, string);
            } catch (IOException e) {
                  e.printStackTrace();
            }
      }
}
