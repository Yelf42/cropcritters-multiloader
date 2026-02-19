package com.yelf42.cropcritters.client.renderer.entity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.math.Axis;
import com.yelf42.cropcritters.entity.PopperPodEntity;
import net.minecraft.world.item.ItemStack;

public class PopperPodEntityRenderer extends EntityRenderer<PopperPodEntity> {

    private final ItemRenderer itemRenderer;

    public PopperPodEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(PopperPodEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Billboard to camera
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        // Base rotation
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));

        // Shot-at-angle transform
        if (entity.wasShotAtAngle()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }

        // Render the item
        ItemStack stack = entity.getItem();
        this.itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PopperPodEntity popperPodEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}