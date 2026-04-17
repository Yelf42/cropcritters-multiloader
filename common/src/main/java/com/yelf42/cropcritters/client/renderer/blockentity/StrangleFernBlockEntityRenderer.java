package com.yelf42.cropcritters.client.renderer.blockentity;

import com.yelf42.cropcritters.blocks.StrangleFernBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
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

        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;

        MovingBlockRenderState movingBlock = new MovingBlockRenderState();
        movingBlock.blockState = state.infestedBlock;
        movingBlock.blockPos = state.blockPos;
        movingBlock.randomSeedPos = state.blockPos;
        movingBlock.lightEngine = level.getLightEngine();
        movingBlock.biome = level.getBiome(state.blockPos);
        movingBlock.cardinalLighting = CardinalLighting.DEFAULT;

        queue.submitMovingBlock(matrices, movingBlock);
    }


}
