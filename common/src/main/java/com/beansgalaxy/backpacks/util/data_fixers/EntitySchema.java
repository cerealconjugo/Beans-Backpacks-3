package com.beansgalaxy.backpacks.util.data_fixers;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EntitySchema extends NamespacedSchema {
      public EntitySchema(int pVersionKey, Schema pParent) {
            super(pVersionKey, pParent);
      }

      @Override public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
            System.out.println("schema");
            System.out.println("schema");
            System.out.println("schema");
            Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
            schema.registerSimple(map, "beansbackpacks:backpack");
            schema.registerSimple(map, "beansbackpacks:ender_backpack");
            schema.registerSimple(map, "beansbackpacks:winged_backpack");
            return map;
      }
}
