package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.util.compat.BPack2ItemFix;
import com.beansgalaxy.backpacks.util.compat.BPack2PlayerFix;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.schemas.V3818_5;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;

@Mixin(DataFixers.class)
public abstract class DataFixersMixin {

      @Shadow @Final private static BiFunction<Integer, Schema, Schema> SAME_NAMESPACED;

      @Inject(method = "addFixers", at = @At("TAIL"))
      private static void beans_backpacks_2$dataFix(DataFixerBuilder pBuilder, CallbackInfo ci) {
            Schema schema1 = pBuilder.addSchema(3819, 9, SAME_NAMESPACED);
            pBuilder.addFixer(new BPack2PlayerFix(schema1, false));
            Schema schema3 = pBuilder.addSchema(3910, V3818_5::new);
            pBuilder.addFixer(new BPack2ItemFix(schema3, true));
      }
}
