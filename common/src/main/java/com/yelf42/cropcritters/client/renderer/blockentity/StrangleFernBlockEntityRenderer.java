package com.yelf42.cropcritters.client.renderer.blockentity;

import com.yelf42.cropcritters.blocks.StrangleFernBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

// TODO mildly strange infestedState render
public class StrangleFernBlockEntityRenderer implements BlockEntityRenderer<StrangleFernBlockEntity> {

    public StrangleFernBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(StrangleFernBlockEntity strangleFernBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1, Vec3 vec3) {
        BlockState infestedBlock = strangleFernBlockEntity.getInfestedState();
        if (infestedBlock == null || infestedBlock.is(Blocks.AIR)) return;
        //queue.submitBlock(matrices, state.infestedBlock, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);

        Minecraft client = Minecraft.getInstance();

        int tint = client.getBlockColors().getColor(infestedBlock, client.level, strangleFernBlockEntity.getBlockPos(), 0);
        float r = (float)((tint >> 16) & 0xFF) / 255f;
        float g = (float)((tint >> 8) & 0xFF) / 255f;
        float b = (float)(tint & 0xFF) / 255f;

        BlockRenderDispatcher blockRenderManager = client.getBlockRenderer();
        BlockStateModel model = blockRenderManager.getBlockModel(infestedBlock);

        int lightCoords = strangleFernBlockEntity.getLevel() != null ? LevelRenderer.getLightColor(strangleFernBlockEntity.getLevel(), strangleFernBlockEntity.getBlockPos()) : 15728880;

        ModelBlockRenderer.renderModel(
                poseStack.last(),
                client.renderBuffers().bufferSource().getBuffer(RenderType.translucentMovingBlock()),
                model,
                r, g, b,
                lightCoords,
                OverlayTexture.NO_OVERLAY
        );
    }
}
