package com.yelf42.cropcritters.entity;

import com.yelf42.cropcritters.registry.ModEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.config.WeedHelper;

public class PopperSeedEntity extends ThrowableItemProjectile {
    private int lifespan = 0;

    public PopperSeedEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public PopperSeedEntity(Vec3 pos, Level world) {
        super(ModEntities.POPPER_SEED_PROJECTILE, pos.x, pos.y, pos.z, world, new ItemStack(ModBlocks.POPPER_PLANT));
    }

    @Override
    protected Item getDefaultItem() {
        return ModBlocks.POPPER_PLANT.asItem();
    }

    @Override
    public void tick() {
        super.tick();
        ++this.lifespan;
        if (this.lifespan > 80 && !this.level().isClientSide()) this.discard();
    }

    @Override
    protected void onInsideBlock(BlockState state) {
        if (this.level().getBlockState(this.blockPosition()).is(BlockTags.DIRT)) {
            BlockState toCheckUp = this.level().getBlockState(this.blockPosition().above());
            if (WeedHelper.canWeedsReplace(toCheckUp)) {
                this.level().setBlockAndUpdate(this.blockPosition().above(), ModBlocks.POPPER_PLANT.defaultBlockState());
                this.discard();
            }
        } else if (this.level().getBlockState(this.blockPosition().below()).is(BlockTags.DIRT)) {
            BlockState toCheck = this.level().getBlockState(this.blockPosition());
            if (WeedHelper.canWeedsReplace(toCheck)) {
                this.level().setBlockAndUpdate(this.blockPosition(), ModBlocks.POPPER_PLANT.defaultBlockState());
                this.discard();
            }
        }
    }
}
