package com.yelf42.cropcritters.blocks;

import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.CropCritters;

public class WitheringSpiteweed extends SpreadingWeedBlock {

    public WitheringSpiteweed(Properties settings) {
        super(settings);
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        return super.mayPlaceOn(floor, world, pos)
                || floor.is(Blocks.SOUL_SOIL)
                || floor.is(Blocks.SOUL_SAND)
                || floor.is(Blocks.BLACKSTONE)
                || floor.is(Blocks.CRIMSON_NYLIUM)
                || floor.is(Blocks.WARPED_NYLIUM)
                || floor.is(Blocks.NETHERRACK);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockState soilCheck = world.getBlockState(pos.below());
        if (!soilCheck.is(Blocks.BLACKSTONE) && (soilCheck.is(Blocks.SOUL_SAND) || soilCheck.is(Blocks.SOUL_SOIL) || soilCheck.is(ModBlocks.SOUL_FARMLAND))) {
            world.setBlock(pos.below(), Blocks.BLACKSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        super.randomTick(state, world, pos, random);
    }

    @Override
    public int getMaxNeighbours() { return 3; }

    // Also turn the block below in blackstone if possible
    @Override
    public void setToWeed(Level world, BlockPos pos) {
        super.setToWeed(world, pos);
        BlockState soilCheck = world.getBlockState(pos.below());
        if (soilCheck.is(Blocks.SOUL_SAND) || soilCheck.is(Blocks.SOUL_SOIL) || soilCheck.is(ModBlocks.SOUL_FARMLAND)) {
            world.setBlock(pos.below(), Blocks.BLACKSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler) {
        if (entity instanceof LivingEntity livingEntity
                && !(livingEntity.getType().is(CropCritters.WEED_IMMUNE))) {
            Vec3 vec3d = new Vec3(0.9, 0.9F, 0.9);
            livingEntity.makeStuckInBlock(state, vec3d);
            if (world instanceof ServerLevel serverWorld && !livingEntity.isInvulnerableTo(serverWorld, world.damageSources().wither())) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 40));
            }
        }
    }
}
