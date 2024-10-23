package com.beansgalaxy.backpacks.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(BlockStateModelLoader.class)
public abstract class BlockStateModelMixin {

      @Shadow protected abstract void loadBlockStateDefinitions(ResourceLocation p_352059_, StateDefinition<Block, BlockState> p_352064_);

      private static final StateDefinition<Block, BlockState> BACKPACK_FAKE_DEFINITION = new StateDefinition.Builder<Block, BlockState>(Blocks.AIR)
                  .add(IntegerProperty.create("fullness", 0, 10))
                  .create(Block::defaultBlockState, BlockState::new);

      @Inject(method = "loadAllBlockStates", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
      private void loadBackpackStates(CallbackInfo ci) {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            Map<ResourceLocation, Resource> resourceMap = resourceManager.listResources("blockstates/backpack", resource ->
                        resource.getPath().endsWith(".json")
            );

            for (ResourceLocation resourceLocation : resourceMap.keySet()) {
                  ResourceLocation location = resourceLocation.withPath(path ->
                              path.replaceAll("blockstates/", "")
                                          .replaceAll(".json", "")
                  );

                  loadBlockStateDefinitions(location, BACKPACK_FAKE_DEFINITION);
            }
      }
}
