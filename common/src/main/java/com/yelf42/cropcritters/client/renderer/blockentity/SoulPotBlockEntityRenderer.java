package com.yelf42.cropcritters.client.renderer.blockentity;

import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import com.yelf42.cropcritters.blocks.SoulPotBlockEntity;

public class SoulPotBlockEntityRenderer implements BlockEntityRenderer<SoulPotBlockEntity> {

    public SoulPotBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    public void render(SoulPotBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate((double)0.5F, (double)0.0F, (double)0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate((double)-0.5F, (double)0.0F, (double)-0.5F);
        SoulPotBlockEntity.WobbleType decoratedpotblockentity$wobblestyle = blockEntity.lastWobbleType;
        if (decoratedpotblockentity$wobblestyle != null && blockEntity.getLevel() != null) {
            float f = ((float)(blockEntity.getLevel().getGameTime() - blockEntity.lastWobbleTime) + partialTick) / (float)decoratedpotblockentity$wobblestyle.lengthInTicks;
            if (f >= 0.0F && f <= 1.0F) {
                if (decoratedpotblockentity$wobblestyle == SoulPotBlockEntity.WobbleType.POSITIVE) {
                    float f1 = 0.015625F;
                    float f2 = f * ((float)Math.PI * 2F);
                    float f3 = -1.5F * (Mth.cos(f2) + 0.5F) * Mth.sin(f2 / 2.0F);
                    poseStack.rotateAround(Axis.XP.rotation(f3 * 0.015625F), 0.5F, 0.0F, 0.5F);
                    float f4 = Mth.sin(f2);
                    poseStack.rotateAround(Axis.ZP.rotation(f4 * 0.015625F), 0.5F, 0.0F, 0.5F);
                } else {
                    float f5 = Mth.sin(-f * 3.0F * (float)Math.PI) * 0.125F;
                    float f6 = 1.0F - f;
                    poseStack.rotateAround(Axis.YP.rotation(f5 * f6), 0.5F, 0.0F, 0.5F);
                }
            }
        }

        poseStack.popPose();
    }
}