package com.yelf42.cropcritters.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.CropCritters;

public class CrawlThistle extends SpreadingWeedBlock {

    public CrawlThistle(Properties settings) {
        super(settings);
    }

    @Override
    public int getMaxNeighbours() { return 2; }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler) {
        if (entity instanceof LivingEntity livingEntity && !(livingEntity.getType().is(CropCritters.WEED_IMMUNE))) {
            Vec3 vec3d = new Vec3(0.9, 0.9F, 0.9);
            livingEntity.makeStuckInBlock(state, vec3d);
        }
    }


}
