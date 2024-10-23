package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.access.SimpleModelAccess;
import com.mojang.datafixers.util.Function8;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(SimpleBakedModel.class)
public class SimpleBakedModelMixin implements SimpleModelAccess {
      @Shadow @Final protected List<BakedQuad> unculledFaces;

      @Shadow @Final protected Map<Direction, List<BakedQuad>> culledFaces;

      @Shadow @Final protected boolean hasAmbientOcclusion;

      @Shadow @Final protected boolean isGui3d;

      @Shadow @Final protected boolean usesBlockLight;

      @Shadow @Final protected TextureAtlasSprite particleIcon;

      @Shadow @Final protected ItemTransforms transforms;

      @Shadow @Final protected ItemOverrides overrides;

      @Override
      public SimpleBakedModel modify(Function8<List<BakedQuad>, Map<Direction, List<BakedQuad>>, Boolean, Boolean, Boolean, TextureAtlasSprite, ItemTransforms, ItemOverrides, SimpleBakedModel> modify) {
            return modify.apply(unculledFaces, culledFaces, hasAmbientOcclusion, usesBlockLight, isGui3d, particleIcon, transforms, overrides);
      }
}
