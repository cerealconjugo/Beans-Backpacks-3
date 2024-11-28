package com.beansgalaxy.backpacks.client.renderer;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipmentModel;
import com.beansgalaxy.backpacks.shorthand.Shorthand;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
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
      public void render(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, AbstractClientPlayer player, float limbAngle, float limbDistance, float tick, float animationProgress, float playerHeadYaw, float playerHeadPitch) {
            EquipableComponent.runIfPresent(player, (equipable, slot) -> {
                  if (!equipable.slots().test(slot))
                        return;

                  ItemStack itemStack = player.getItemBySlot(slot);
                  ResourceLocation texture = equipable.backpackTexture();
                  EquipmentModel model = equipable.customModel();

                  if (texture != null) {
                        pose.pushPose();
                        this.getParentModel().body.translateAndRotate(pose);

                        pose.translate(0.0F, (player.isCrouching() ? 1 / 16f : 0), 0.0F);
                        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
                              pose.translate(0.0F, -1 / 16f, 1 / 16f);

                        ViewableBackpack viewable = ViewableBackpack.get(player);
                        if (viewable.lastDelta > tick)
                              viewable.updateOpen();

                        float headPitch = Mth.lerp(tick, viewable.lastPitch, viewable.headPitch) * 0.25f;
                        model().setOpenAngle(headPitch);
                        viewable.lastDelta = tick;

                        renderTexture(pose, pBufferSource, pCombinedLight, texture, itemStack);
                        pose.popPose();
                  }
                  else if (model != null)
                        renderModel(pose, pBufferSource, pCombinedLight, player, slot, model, itemStack);
            });

            Shorthand shorthand = Shorthand.get(player);
            int selectedWeapon = shorthand.getSelectedWeapon();
            ItemStack stack = shorthand.weapons.getItem(selectedWeapon);
            if (stack.isEmpty())
                  return;

            Inventory inventory = player.getInventory();
            int selected = inventory.selected - inventory.items.size() - shorthand.tools.getContainerSize();
            boolean mainHand = selectedWeapon != selected;

            if (mainHand && !stack.isEmpty()) {
                  pose.pushPose();
                  this.getParentModel().body.translateAndRotate(pose);
                  pose.translate(0, player.isCrouching() ? 6/16f : 5/16f, 5/32f);
                  if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
                        pose.translate(0.0F, -1/16f, 1 / 16f);

                  pose.mulPose(Axis.ZN.rotationDegrees(90));
                  pose.translate(0.001, -0.001, 0);

                  BakedModel model = itemRenderer.getModel(stack, player.level(), player, player.getId());
                  itemRenderer().render(stack, ItemDisplayContext.FIXED, false, pose, pBufferSource, pCombinedLight, OverlayTexture.NO_OVERLAY, model);
                  pose.popPose();
            }
      }

      private void renderModel(PoseStack pose, MultiBufferSource pBufferSource, int pCombinedLight, AbstractClientPlayer player, EquipmentSlot slot, EquipmentModel model, ItemStack itemStack) {
            model.attachments().forEach((attachment, location) -> {
                  pose.pushPose();
                  switch (attachment) {
                        case HEAD -> {
                              this.getParentModel().head.translateAndRotate(pose);
                              pose.translate(0, -12/16f, 0);
                        }
                        case BODY ->
                              this.getParentModel().body.translateAndRotate(pose);
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

                  renderBackpack(pose, pBufferSource, pCombinedLight, location, itemStack, player, player.clientLevel, player.getId());

                  pose.popPose();
            });
      }

}
