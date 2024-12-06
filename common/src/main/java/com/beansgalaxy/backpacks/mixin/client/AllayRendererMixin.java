package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.client.renderer.AllayFeature;
import com.beansgalaxy.backpacks.client.renderer.ArmorStandFeature;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.animal.allay.Allay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllayRenderer.class)
public abstract class AllayRendererMixin extends MobRenderer<Allay, AllayModel> {

      public AllayRendererMixin(EntityRendererProvider.Context pContext, AllayModel pModel, float pShadowRadius) {
            super(pContext, pModel, pShadowRadius);
      }

      @Inject(method = "<init>", at = @At("RETURN"))
      public void registerBackRenderer(EntityRendererProvider.Context context, CallbackInfo ci) {
            ItemRenderer itemRenderer = context.getItemRenderer();
            BlockRenderDispatcher blockDispatcher = context.getBlockRenderDispatcher();
            EntityModelSet modelSet = context.getModelSet();
            this.addLayer(new AllayFeature(this, itemRenderer, modelSet, blockDispatcher));
      }
}
