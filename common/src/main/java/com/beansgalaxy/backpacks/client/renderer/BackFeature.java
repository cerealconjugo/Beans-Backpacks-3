package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipmentModel;
import com.beansgalaxy.backpacks.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class BackFeature extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> implements BackpackRender {
      private final BackpackModel<AbstractClientPlayer> backpackModel;
      private final ItemRenderer itemRenderer;
      private final BlockRenderDispatcher blockDispatcher;

      public BackFeature(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> pRenderer, ItemRenderer itemRenderer, EntityModelSet modelSet, BlockRenderDispatcher blockDispatcher) {
            super(pRenderer);
            this.itemRenderer = itemRenderer;
            this.backpackModel = new BackpackModel<>(modelSet.bakeLayer(BACKPACK_MODEL));
            this.blockDispatcher = blockDispatcher;
      }

      @Override
      public BackpackModel<?> model() {
            return backpackModel;
      }

      @Override
      public ItemRenderer itemRenderer() {
            return itemRenderer;
      }

      @Override
      public BlockRenderDispatcher blockRenderer() {
            return blockDispatcher;
      }

      @Override
      public void render(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, AbstractClientPlayer player, float limbAngle, float limbDistance, float delta, float animationProgress, float playerHeadYaw, float playerHeadPitch) {
            EquipableComponent.runIfPresent(player, (equipable, slot) -> {
                  if (!equipable.slots().test(slot))
                        return;

                  ItemStack itemStack = player.getItemBySlot(slot);
                  EquipmentModel model = equipable.model();
                  if (model == null)
                        return;

                  if (model.isBuiltInLeatherModel()) {
                        pose.pushPose();
                        this.getParentModel().body.translateAndRotate(pose);

                        pose.translate(0.0F, (player.isCrouching() ? 1 / 16f : 0), 0.0F);
                        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
                              pose.translate(0.0F, -1 / 16f, 1 / 16f);

                        builtInLeatherModel(pose, pBufferSource, pCombinedLight, itemStack);
                        pose.popPose();
                        return;
                  }

                  model.attachments().forEach((attachment, location) -> {
                        pose.pushPose();
                        switch (attachment) {
                              case HEAD -> {
                                    this.getParentModel().head.translateAndRotate(pose);
                                    pose.translate(0, -12/16f, 0);
                              }
                              case BODY -> {
                                    this.getParentModel().body.translateAndRotate(pose);
                                    if (EquipmentSlot.BODY == slot && player.isCrouching())
                                          pose.translate(0.0F, 1 / 16f, 0);
                              }
                              case BACK -> {
                                    this.getParentModel().body.translateAndRotate(pose);
                                    if (player.isCrouching())
                                          pose.translate(0.0F, 1 / 16f, 0);
                                    if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
                                          pose.translate(0.0F, -1 / 16f, 1 / 16f);
                              }
                              case L_ARM -> {
                                    this.getParentModel().leftArm.translateAndRotate(pose);
                                    pose.translate(1/16f, -2/16f, 0);
                              }
                              case R_ARM -> {
                                    this.getParentModel().rightArm.translateAndRotate(pose);
                                    pose.translate(-1/16f, -2/16f, 0);
                              }
                              case L_LEG -> this.getParentModel().leftLeg.translateAndRotate(pose);
                              case R_LEG -> this.getParentModel().rightLeg.translateAndRotate(pose);
                        }

                        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
                        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
                        pose.translate(-8 / 16f, -12 / 16f, -8 / 16f - 0.001f);

                        ModelManager modelmanager = this.itemRenderer().getItemModelShaper().getModelManager();
                        ModelResourceLocation modelLocation = Services.PLATFORM.getModelVariant(location);
                        BakedModel bakedModel = modelmanager.getModel(modelLocation);
                        BakedModel backpackModel = bakedModel.getOverrides().resolve(bakedModel, itemStack, (ClientLevel) player.level(), player, player.getId());

                        if (backpackModel != null) {
                              VertexConsumer buffer = pBufferSource.getBuffer(Sheets.cutoutBlockSheet());
                              this.blockRenderer().getModelRenderer().renderModel(pose.last(), buffer, null, backpackModel, 1.0F, 1.0F, 1.0F, pCombinedLight, OverlayTexture.NO_OVERLAY);
//                        this.itemRenderer.render(itemStack, ItemDisplayContext.FIXED, false, pose, pBufferSource, pCombinedLight, OverlayTexture.NO_OVERLAY, backpackModel);
                        }

                        pose.popPose();
                  });
            });
      }

}
