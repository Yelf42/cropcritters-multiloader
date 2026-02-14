package com.yelf42.cropcritters.client.renderer.blockentity;

import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class SoulPotBlockEntityRenderState extends BlockEntityRenderState {
    public DecoratedPotBlockEntity.@Nullable WobbleStyle wobbleType;
    public float wobbleAnimationProgress;
    public int level;
    public Direction facing;

    public SoulPotBlockEntityRenderState() {
        this.level = 0;
        this.facing = Direction.NORTH;
    }
}