package com.beansgalaxy.backpacks.mixin.common.data_features;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.ExperimentFlagAccess;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagRegistry;
import net.minecraft.world.flag.FeatureFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FeatureFlags.class)
public class FeatureFlagsMixin implements ExperimentFlagAccess {
      @Unique private static FeatureFlag BACKPACK_EXPERIMENTS;

      @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/flag/FeatureFlagRegistry$Builder;build()Lnet/minecraft/world/flag/FeatureFlagRegistry;"))
      private static void backpacks_injectNewFlag(CallbackInfo ci, @Local FeatureFlagRegistry.Builder builder) {
            BACKPACK_EXPERIMENTS = builder.create(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "experiments"));
      }

      @Override
      public FeatureFlag backpacks_flag() {
            return BACKPACK_EXPERIMENTS;
      }
}
