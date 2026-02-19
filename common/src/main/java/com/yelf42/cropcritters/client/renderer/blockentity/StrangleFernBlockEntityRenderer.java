package com.yelf42.cropcritters.client.renderer.blockentity;

import com.yelf42.cropcritters.blocks.StrangleFernBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.state.BlockState;

public class StrangleFernBlockEntityRenderer implements BlockEntityRenderer<StrangleFernBlockEntity> {

    public StrangleFernBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(StrangleFernBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState infestedBlock = blockEntity.getInfestedState();
        if (infestedBlock == null || infestedBlock.is(Blocks.AIR)) return;

        Minecraft mc = Minecraft.getInstance();
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();

        int tint = mc.getBlockColors().getColor(infestedBlock, level, pos, 0);
        float r = ((tint >> 16) & 0xFF) / 255f;
        float g = ((tint >> 8) & 0xFF) / 255f;
        float b = (tint & 0xFF) / 255f;

        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(infestedBlock);

        int light = level != null ? LevelRenderer.getLightColor(level, pos) : 0;

        poseStack.pushPose();

        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.cutout()),
                infestedBlock,
                model,
                r, g, b,
                light,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}
