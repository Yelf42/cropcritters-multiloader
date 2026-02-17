package com.yelf42.cropcritters.entity;

import com.yelf42.cropcritters.blocks.StrangleFernBlockEntity;
import com.yelf42.cropcritters.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.registry.ModItems;

public class HerbicideEntity extends ThrowableItemProjectile {

    public HerbicideEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public HerbicideEntity(double x, double y, double z, Level world, ItemStack stack) {
        super(ModEntities.HERBICIDE_PROJECTILE, x, y, z, world);
    }

    public HerbicideEntity(ServerLevel serverWorld, LivingEntity livingEntity) {
        super(ModEntities.HERBICIDE_PROJECTILE, livingEntity, serverWorld);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        this.level().broadcastEntityEvent(this, (byte)3);
        if (this.level() instanceof ServerLevel serverWorld) {
            killWeeds(serverWorld, this.blockPosition());
            serverWorld.levelEvent(2002, this.blockPosition(), CommonColors.GREEN);
            this.discard();
        }
    }

    private void killWeeds(ServerLevel world, BlockPos blockPos) {
        Iterable<BlockPos> iterable = BlockPos.withinManhattan(blockPos, 4, 4, 4);

        for (BlockPos pos : iterable) {
            if (!pos.closerToCenterThan(this.position(), 4)) continue;

            BlockState check = world.getBlockState(pos);
            boolean airState = check.getFluidState().isEmpty();
            if (check.is(CropCritters.WEEDS) && !check.is(ModBlocks.PUFFBOMB_MUSHROOM)) {
                if (check.is(ModBlocks.STRANGLE_FERN)) {
                    StrangleFernBlockEntity sfbe = (StrangleFernBlockEntity) world.getBlockEntity(pos);
                    BlockState infested = Blocks.DEAD_BUSH.defaultBlockState();
                    if (sfbe != null) {
                        infested = sfbe.getInfestedState();
                    }
                    world.setBlock(pos, infested, 3);
                } else {
                    world.setBlock(pos, airState ? Blocks.AIR.defaultBlockState() : Blocks.WATER.defaultBlockState(), 3);
                }
            }
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.HERBICIDE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }
}
