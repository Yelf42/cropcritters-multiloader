package com.yelf42.cropcritters.entity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.Tuple;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.function.Predicate;

public class BeetrootCritterEntity extends AbstractCropCritterEntity {
    public BeetrootCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.is(Blocks.ROOTED_DIRT));
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        this.playSound(ModSounds.ENTITY_CRITTER_TILL, 1.0F, 1.0F);
        this.level().setBlock(this.targetPos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        this.level().levelEvent(null, 2001, this.targetPos, Block.getId(this.level().getBlockState(this.targetPos)));
    }

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.BEETROOT, 5);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.is(Items.BEETROOT) || itemStack.is(Items.BEETROOT_SEEDS);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(Mth.nextInt(this.random, 100, 200));
    }
}
