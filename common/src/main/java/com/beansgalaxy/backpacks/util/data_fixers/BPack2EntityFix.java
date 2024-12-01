package com.beansgalaxy.backpacks.util.data_fixers;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.EntityRenameFix;
import net.minecraft.util.datafix.fixes.SimpleEntityRenameFix;

import java.util.Objects;

public class BPack2EntityFix extends SimpleEntityRenameFix {

      public BPack2EntityFix(Schema pOutputSchema, boolean pChangesType) {
            super("backpacks-2 to backpacks-3 Entity", pOutputSchema, pChangesType);
      }

      @Override public TypeRewriteRule makeRule() {
            System.out.println("rule");
            System.out.println("rule");
            System.out.println("rule");
            return super.makeRule();
      }

      @Override protected Pair<String, Typed<?>> fix(String pEntityName, Typed<?> pTyped) {
            System.out.println("fix");
            System.out.println("fix");
            System.out.println("fix");
            return super.fix(pEntityName, pTyped);
      }

      @Override protected Pair<String, Dynamic<?>> getNewNameAndTag(String name, Dynamic<?> dynamic) {
            System.out.println("start");
            System.out.println("start");
            System.out.println("start");
            switch (name) {
                  case "beansbackpacks:backpack" -> {
                        System.out.println("generic");
                        System.out.println("generic");
                        System.out.println("generic");
                  }
                  case "beansbackpacks:ender_backpack" -> {
                        System.out.println("ender");
                        System.out.println("ender");
                        System.out.println("ender");
                  }
                  case "beansbackpacks:winged_backpack" -> {
                        System.out.println("winged");
                        System.out.println("winged");
                        System.out.println("winged");
                  }
            }
            return Pair.of(name, dynamic);
      }
}
