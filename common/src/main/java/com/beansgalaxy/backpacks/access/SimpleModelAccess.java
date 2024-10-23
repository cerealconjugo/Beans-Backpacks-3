package com.beansgalaxy.backpacks.access;

import com.mojang.datafixers.util.Function8;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Map;

public interface SimpleModelAccess {

      SimpleBakedModel modify(Function8<List<BakedQuad>, Map<Direction, List<BakedQuad>>, Boolean, Boolean, Boolean, TextureAtlasSprite, ItemTransforms, ItemOverrides, SimpleBakedModel> modify);
}
