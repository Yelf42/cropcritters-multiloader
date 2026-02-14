package com.yelf42.cropcritters.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.CropCritters;

public class CrimsonThornweed extends SpreadingWeedBlock {

    public CrimsonThornweed(Properties settings) {
        super(settings);
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        return super.mayPlaceOn(floor, world, pos) || floor.is(Blocks.CRIMSON_NYLIUM) || floor.is(Blocks.WARPED_NYLIUM) || floor.is(Blocks.NETHERRACK);
    }

    @Override
    public int getMaxNeighbours() { return 3; }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean bl) {
        // Apply damage, avoid critters and nether mobs
        if (entity instanceof LivingEntity livingEntity
                && !(livingEntity.getType().is(CropCritters.WEED_IMMUNE))) {
            Vec3 vec3d = new Vec3(0.9, 0.9F, 0.9);
            livingEntity.makeStuckInBlock(state, vec3d);
            if (world instanceof ServerLevel serverWorld) livingEntity.hurtServer(serverWorld, world.damageSources().sweetBerryBush(), 2.0F);
        }
    }
}
