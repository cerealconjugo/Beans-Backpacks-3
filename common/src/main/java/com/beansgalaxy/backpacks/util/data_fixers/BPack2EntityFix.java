package com.beansgalaxy.backpacks.util.data_fixers;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.SimplestEntityRenameFix;

public class BPack2EntityFix extends SimplestEntityRenameFix {

      public BPack2EntityFix(Schema pOutputSchema, boolean pChangesType) {
            super("ntitysfgsef", pOutputSchema, pChangesType);
      }

      @Override public TypeRewriteRule makeRule() {
            System.out.println("rule");
            return super.makeRule();
      }

      @Override protected String rename(String id) {
            System.out.println("rename");
            System.out.println("rename");
            return switch (id) {
                  case "beansbackpacks:winged_backpack",
                       "beansbackpacks:ender_backpack"
                              -> "beansbackpacks:backpack";
                  default -> id;
            };
      }
}
