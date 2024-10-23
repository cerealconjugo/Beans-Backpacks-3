package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.client.renderer.BackpackModel;
import com.beansgalaxy.backpacks.client.renderer.BackpackRender;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.traits.generic.BackpackEntity;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector3f;

import java.util.Optional;

public class BundleScreen extends AbstractContainerScreen<BundleMenu> implements BackpackRender {
      private static final ResourceLocation TEXTURE = ResourceLocation.parse(Constants.MOD_ID + ":textures/gui/backpack.png");
      private final BackpackModel<Entity> backpackModel;

      public BundleScreen(BundleMenu $$0, Inventory $$1, Component $$2) {
            super($$0, $$1, $$2);
            this.imageHeight = 256;
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            this.backpackModel = new BackpackModel<>(entityModels.bakeLayer(BACKPACK_MODEL));
      }

      @Override
      protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, TEXTURE);
            int j = BundleMenu.INV_OFFSET + topPos;
            context.blit(TEXTURE, leftPos, j - 123, 0, 0, 0, imageWidth, imageHeight, 256, 256);
            drawBackpack(context, width / 2, j, 202, menu.backpack, mouseX, mouseY);

            for (BundleSlots backpackSlot : menu.backpackSlots) {
                  if (BundleSlots.State.HIDDEN.equals(backpackSlot.state)) continue;

                  int x = backpackSlot.x + leftPos - 1;
                  int y = backpackSlot.y + topPos - 1;
                  int z = 200;
                  int color = 0x60111111;
                  context.fill(x, y, x + BundleSlots.SPACING, y + BundleSlots.SPACING, z, color);
            }
      }

      private void drawBackpack(GuiGraphics context, int x, int y, int scale, BackpackEntity backpack, int mouseX, int mouseY) {
            Optional<EquipableComponent> optional = backpack.getEquipable();
            if (optional.isEmpty())
                  return;

            PoseStack pose = context.pose();
            pose.pushPose();
            context.enableScissor(x - 80, y - 220, x + 80, y + 36);
            float windowWidth = ((float) context.guiWidth()) / imageWidth * 2;
            int center = leftPos + imageWidth / 2;
            int abs = (mouseX - center) * (mouseX - center);

            double pro = abs / 25000.0;

            double rot = pro == 0
                        ? 0
                        : pro == 1
                        ? 1
                        : pro < 0.5 ? Math.pow(2, 20 * pro - 10) / 2
                        : (2 - Math.pow(2, -20 * pro + 10)) / 2;

            boolean isNegative = mouseX - center < 0;
            double sign = isNegative ? -1 : 1;
            rot *= sign;

            pose.translate(x, y + 30, 80);
            pose.mulPose(Axis.XP.rotationDegrees((topPos - mouseY) / 25f + 170));
            float j = (mouseX - center) / windowWidth + 180;
            pose.mulPose(Axis.YN.rotationDegrees((float) rot * (isNegative ? 150 : 140) + (j)));
            pose.mulPose(Axis.ZP.rotationDegrees(180));
            pose.scale(scale, -scale, scale);

            RenderSystem.setupGui3DDiffuseLighting(new Vector3f(-1f, 0f, 0f), new Vector3f(1f, 1f, 0f));

            renderBackpack(backpack, 0, pose, context.bufferSource(), 0x000000FF);

            context.flush();
            pose.popPose();
            context.disableScissor();
            Lighting.setupFor3DItems();
      }

      @Override
      public BackpackModel<?> model() {
            return backpackModel;
      }

      @Override
      public ItemRenderer itemRenderer() {
            return Minecraft.getInstance().getItemRenderer();
      }

      @Override
      public BlockRenderDispatcher blockRenderer() {
            return Minecraft.getInstance().getBlockRenderer();
      }
}
