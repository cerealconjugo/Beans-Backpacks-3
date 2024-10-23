package com.beansgalaxy.backpacks.events;

import com.beansgalaxy.backpacks.client.renderer.BackFeature;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class AppendModelLayers implements LivingEntityFeatureRendererRegistrationCallback {
      @Override
      public void registerRenderers(
                  EntityType<? extends LivingEntity> entityType,
                  LivingEntityRenderer<?, ?> entityRenderer,
                  RegistrationHelper registrationHelper,
                  EntityRendererProvider.Context context
      ) {
            if (entityRenderer instanceof PlayerRenderer playerRenderer) {
                  ItemRenderer itemRenderer = context.getItemRenderer();
                  BlockRenderDispatcher blockDispatcher = context.getBlockRenderDispatcher();
                  EntityModelSet modelSet = context.getModelSet();
                  registrationHelper.register(new BackFeature(playerRenderer, itemRenderer, modelSet, blockDispatcher));
            }
      }
}
