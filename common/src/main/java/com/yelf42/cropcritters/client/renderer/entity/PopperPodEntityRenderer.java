package com.yelf42.cropcritters.client.renderer.entity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FireworkRocketRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.math.Axis;
import com.yelf42.cropcritters.entity.PopperPodEntity;

public class PopperPodEntityRenderer extends EntityRenderer<PopperPodEntity, FireworkRocketRenderState> {
    private final ItemModelResolver itemModelManager;

    public PopperPodEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelManager = context.getItemModelResolver();
    }

    @Override
    public void render(FireworkRocketRenderState fireworkRocketEntityRenderState, PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight) {
        matrixStack.pushPose();

        matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        matrixStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
        if (fireworkRocketEntityRenderState.isShotAtAngle) {
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }


        fireworkRocketEntityRenderState.item.render(matrixStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
        super.render(fireworkRocketEntityRenderState, matrixStack, bufferSource, packedLight);
    }

    public FireworkRocketRenderState createRenderState() {
        return new FireworkRocketRenderState();
    }

    public void extractRenderState(PopperPodEntity popperPodEntity, FireworkRocketRenderState fireworkRocketEntityRenderState, float f) {
        super.extractRenderState(popperPodEntity, fireworkRocketEntityRenderState, f);
        fireworkRocketEntityRenderState.isShotAtAngle = popperPodEntity.wasShotAtAngle();
        this.itemModelManager.updateForNonLiving(fireworkRocketEntityRenderState.item, popperPodEntity.getItem(), ItemDisplayContext.GROUND, popperPodEntity);
    }
}