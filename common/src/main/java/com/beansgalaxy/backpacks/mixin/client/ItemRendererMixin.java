package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

      @Shadow @Final private ItemModelShaper itemModelShaper;

      @Shadow @Final private Minecraft minecraft;

      @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
      private BakedModel backpackRenderInGUI(BakedModel pModel, ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pCombinedLight, int pCombinedOverlay) {
            if (!pItemStack.isEmpty()) {
                  boolean flag = pDisplayContext == ItemDisplayContext.GUI || pDisplayContext == ItemDisplayContext.GROUND || pDisplayContext == ItemDisplayContext.FIXED;
                  if (!flag) {
                        BakedModel itemModel = itemModelShaper.getItemModel(pItemStack);
                        BakedModel resolve = itemModel.getOverrides().resolve(itemModel, CommonClient.NO_GUI_STAND_IN, null, null, 0);
                        if (resolve == null)
                              return pModel;

                        BakedModel resolve1 = resolve.getOverrides().resolve(resolve, pItemStack, minecraft.level, null, 0);
                        if (itemModel == resolve1)
                              return pModel;

                        return resolve1;
                  }
            }
            return pModel;
      }
}
