package com.yelf42.cropcritters.client.renderer.blockentity;

import com.yelf42.cropcritters.blocks.StrangleFernBlockEntity;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;


public class StrangleFernBlockEntityRenderer implements BlockEntityRenderer<StrangleFernBlockEntity, StrangleFernBlockEntityRenderState> {

    public StrangleFernBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public StrangleFernBlockEntityRenderState createRenderState() {
        return new StrangleFernBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(StrangleFernBlockEntity blockEntity, StrangleFernBlockEntityRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.infestedBlock = blockEntity.getInfestedState();
    }

    @Override
    public void submit(StrangleFernBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.infestedBlock == null || state.infestedBlock.is(Blocks.AIR)) return;
        //queue.submitBlock(matrices, state.infestedBlock, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);

        Minecraft client = Minecraft.getInstance();

        int tint = client.getBlockColors().getColor(state.infestedBlock, client.level, state.blockPos, 0);
        float r = (float)((tint >> 16) & 0xFF) / 255f;
        float g = (float)((tint >> 8) & 0xFF) / 255f;
        float b = (float)(tint & 0xFF) / 255f;

        BlockRenderDispatcher blockRenderManager = client.getBlockRenderer();
        BlockStateModel model = blockRenderManager.getBlockModel(state.infestedBlock);

        ModelBlockRenderer.renderModel(
                matrices.last(),
                client.renderBuffers().bufferSource().getBuffer(RenderTypes.cutoutMovingBlock()),
                model,
                r, g, b,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY
        );
    }
}
