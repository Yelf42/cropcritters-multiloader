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
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.blocks.StrangleFern;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.function.Predicate;

public class WheatCritterEntity extends AbstractCropCritterEntity {
    public WheatCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> (blockState.is(CropCritters.WEEDS) && !blockState.is(ModBlocks.STRANGLE_FERN))
                || (blockState.is(ModBlocks.STRANGLE_FERN) && blockState.getValueOrElse(StrangleFern.AGE, 0) > 1)
                || blockState.is(Blocks.DEAD_BUSH));
    }

    @Override
    protected  int getTargetOffset() {return 0;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        this.playSound(ModSounds.ENTITY_CRITTER_SHEAR, 1.0F, 1.0F);
        this.level().levelEvent(this, 2001, this.targetPos, Block.getId(this.level().getBlockState(this.targetPos)));
        this.level().setBlock(this.targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
    }

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.WHEAT, 4);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.is(Items.WHEAT) || itemStack.is(Items.WHEAT_SEEDS);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(Mth.nextInt(this.random, 100, 200));
    }
}
