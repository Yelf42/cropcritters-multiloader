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

public class PotatoCritterEntity extends AbstractCropCritterEntity {
    public PotatoCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.is(Blocks.COARSE_DIRT));
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        this.playSound(ModSounds.ENTITY_CRITTER_TILL, 1.0F, 1.0F);
        this.level().setBlock(this.targetPos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        this.level().levelEvent(this, 2001, this.targetPos, Block.getId(this.level().getBlockState(this.targetPos)));
    }

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.POTATO, 6);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.is(Items.POTATO) || itemStack.is(Items.BAKED_POTATO);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(Mth.nextInt(this.random, 100, 200));
    }
}
