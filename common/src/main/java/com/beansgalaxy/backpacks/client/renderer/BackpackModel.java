package com.beansgalaxy.backpacks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class BackpackModel<T extends Entity>
		extends EntityModel<T> {
	private final ModelPart main;
	private final ModelPart body;
	private final ModelPart button;
	private final ModelPart body_mask;
	private final ModelPart head_mask;

	public BackpackModel(ModelPart root) {
		this.main = root.getChild("main");
		this.body = main.getChild("body");
		this.button = main.getChild("button");
		this.body_mask = main.getChild("body_mask");
		this.head_mask = main.getChild("head_mask");
	}

	public static LayerDefinition getTexturedModelData() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		main.addOrReplaceChild("body_mask", CubeListBuilder.create().texOffs(0, 20)
				.addBox(-4.0F, -6.0F, 2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(-0.01F)),
				PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 3.1416F, 3.1416F, 0.0F));
		main.addOrReplaceChild("button", CubeListBuilder.create().texOffs(26, 0)
						.addBox(-1.0F, 1.0F, -5.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -11.0F, 2.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition mask_head = main.addOrReplaceChild("head_mask", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -11.0F, 2.0F, 0.0F, 3.1416F, 0.0F));
		mask_head.addOrReplaceChild("spine_r1", CubeListBuilder.create().texOffs(8, 20)
				.addBox(-4.0F, 0.0F, -4.0F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 1.0F, -4.0F, -0.3229F, 3.1416F, 0.0F));
		mask_head.addOrReplaceChild("top_r1", CubeListBuilder.create().texOffs(11, 23)
				.addBox(-4.0F, -1.0F, -4.0F, 8.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 1.0F, -4.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition body = main.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));
		body.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 8)
				.addBox(-4.0F, 7.0F, -6.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 3.1416F, 0.0F));
		body.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 1)
				.addBox(-4.0F, -1.0F, -4.0F, 8.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 1.0F, 2.0F, 0.0F, 3.1416F, 0.0F));


		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {

	}

	@Override
	public void renderToBuffer(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		body.render(matrices, vertexConsumer, light, overlay, color);
		button.render(matrices, vertexConsumer, light, overlay, color);
	}

	public void renderMask(PoseStack pose, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		head_mask.render(pose, vertexConsumer, light, overlay, color);

		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(180));

		pose.scale(1, 1, -1);
		body_mask.render(pose, vertexConsumer, light, overlay, color);
		pose.popPose();
	}

	public void renderBody(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		body.render(matrices, vertexConsumer, light, overlay, color);
	}

	public void renderButton(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		button.render(matrices, vertexConsumer, light, overlay, color);
	}

	public void setOpenAngle(float headPitch) {
		ModelPart[] topParts = {body.getChild("top"), button, head_mask};
		for (ModelPart topPart : topParts) {
			topPart.xRot = headPitch;
		}
	}
}