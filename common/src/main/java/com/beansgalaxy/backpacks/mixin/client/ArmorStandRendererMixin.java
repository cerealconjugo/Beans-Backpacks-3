package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.client.renderer.ArmorStandFeature;
import com.beansgalaxy.backpacks.client.renderer.BackFeature;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandRenderer.class)
public abstract class ArmorStandRendererMixin extends LivingEntityRenderer<ArmorStand, ArmorStandArmorModel> {
      public ArmorStandRendererMixin(EntityRendererProvider.Context pContext, ArmorStandArmorModel pModel, float pShadowRadius) {
            super(pContext, pModel, pShadowRadius);
      }

      @Inject(method = "<init>", at = @At("RETURN"))
      public void registerBackRenderer(EntityRendererProvider.Context context, CallbackInfo ci) {
            ItemRenderer itemRenderer = context.getItemRenderer();
            BlockRenderDispatcher blockDispatcher = context.getBlockRenderDispatcher();
            EntityModelSet modelSet = context.getModelSet();
            this.addLayer(new ArmorStandFeature(this, itemRenderer, modelSet, blockDispatcher));
      }
}
