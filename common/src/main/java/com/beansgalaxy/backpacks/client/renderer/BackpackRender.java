package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.Tint;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface BackpackRender {

      ModelLayerLocation BACKPACK_MODEL =
                  new ModelLayerLocation(ResourceLocation.parse(CommonClass.MOD_ID + ":backpack_model"), "main");

      BackpackModel<?> model();

      ItemRenderer itemRenderer();

      BlockRenderDispatcher blockRenderer();

      default void builtInLeatherModel(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, ItemStack pItemStack) {
            pose.translate(0, 13 / 16f, 0);
            DyedItemColor dyedItemColor = pItemStack.get(DataComponents.DYED_COLOR);
            int color = dyedItemColor == null ? CommonClass.DEFAULT_LEATHER_COLOR : dyedItemColor.rgb();

            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(CommonClass.MOD_ID, "textures/backpack/leather/base.png");
            VertexConsumer outer = pBufferSource.getBuffer(RenderType.entityCutout(location));
            model().renderToBuffer(pose, outer, pCombinedLight, OverlayTexture.NO_OVERLAY, color);

            Tint tint = new Tint(color);
            double brightness = tint.brightness();
            Tint.HSL hsl = tint.HSL();
            double lum = hsl.getLum();
            hsl.setLum((Math.cbrt(lum + 0.2) + lum) / 2).rotate(5).setSat(Math.sqrt((hsl.getSat() + brightness) / 2));
            int highColor = hsl.pushToNew().getRGBA();

            ResourceLocation highlight = ResourceLocation.fromNamespaceAndPath(CommonClass.MOD_ID, "textures/backpack/leather/highlights.png");
            VertexConsumer highVC = pBufferSource.getBuffer(RenderType.entityTranslucentCull(highlight));
            model().renderToBuffer(pose, highVC, pCombinedLight, OverlayTexture.NO_OVERLAY, highColor);

            pose.pushPose();
            pose.scale(0.99f, 0.99f, 0.99f);
            VertexConsumer inner = pBufferSource.getBuffer(RenderType.entityCutoutNoCull(location));
            model().renderMask(pose, inner, pCombinedLight, OverlayTexture.NO_OVERLAY, color);
            pose.popPose();
      }

      default void renderBackpack(@NotNull BackpackEntity backpack, float yaw, @NotNull PoseStack pose, @NotNull MultiBufferSource source, int light) {
            ItemStack stack = backpack.getEntityData().get(BackpackEntity.ITEM_STACK);
            Optional<EquipableComponent> optional = backpack.getEquipable();

            Boolean useLeatherModel = optional.map(equipable -> {
                  if (equipable.model() == null)
                        return false;

                  return equipable.model().isBuiltInLeatherModel();
            }).orElse(false);

            if (useLeatherModel) {
                  pose.pushPose();
                  pose.mulPose(Axis.YN.rotationDegrees(yaw + 180));
                  pose.mulPose(Axis.XP.rotationDegrees(180));
                  pose.translate(0, -10/16f, -4/16f);
                  builtInLeatherModel(pose, source, light, stack);

                  pose.popPose();
                  return;
            }

            pose.pushPose();
            pose.mulPose(Axis.YN.rotationDegrees(yaw + 180));

            ResourceLocation location = backpack.getPlaceable().modelLocation();
            if (location == null) {
                  pose.translate(0, 1/4f, 0);
                  pose.scale(0.5f, 0.5f, 0.5f);
                  itemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, source, backpack.level(), backpack.getId());

                  pose.popPose();
                  return;
            }

            pose.mulPose(Axis.XP.rotationDegrees(180));
            pose.translate(0, -10/16f, -4/16f);

            ModelResourceLocation modelLocation = Services.PLATFORM.getModelVariant(location);
            ModelManager modelmanager = this.itemRenderer().getItemModelShaper().getModelManager();
            BakedModel backpackModel = modelmanager.getModel(modelLocation);

//            if (backpackModel.equals(modelmanager.getMissingModel()))
//                  return;

            pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.translate(-8 / 16f, -12 / 16f, -8 / 16f - 0.001f);

            VertexConsumer buffer = source.getBuffer(Sheets.translucentItemSheet());
            this.blockRenderer().getModelRenderer().renderModel(pose.last(), buffer, null, backpackModel, 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
      }
}
