package com.yelf42.cropcritters.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.CropCritters;

public class Waftgrass extends SpreadingWeedBlock {

    public Waftgrass(Properties settings) {
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
        if (world instanceof ServerLevel
                && entity instanceof LivingEntity livingEntity
                && !(livingEntity.getType().is(CropCritters.WEED_IMMUNE))) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 120));
        }
    }
}
