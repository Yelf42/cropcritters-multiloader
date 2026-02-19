package com.yelf42.cropcritters.entity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.function.Predicate;

public class CarrotCritterEntity extends AbstractCropCritterEntity {
    public CarrotCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.is(Blocks.DIRT) || blockState.is(Blocks.GRASS_BLOCK)
                                        || blockState.is(Blocks.SOUL_SOIL) || blockState.is(Blocks.SOUL_SAND));
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        this.playSound(ModSounds.ENTITY_CRITTER_TILL, 1.0F, 1.0F);
        BlockState target = this.level().getBlockState(this.targetPos);
        BlockState farmland = (target.is(Blocks.DIRT) || target.is(Blocks.GRASS_BLOCK)) ? Blocks.FARMLAND.defaultBlockState() : (target.is(Blocks.SOUL_SAND) || target.is(Blocks.SOUL_SOIL)) ? ModBlocks.SOUL_FARMLAND.defaultBlockState() : null;
        if (farmland == null) return;
        this.level().setBlock(this.targetPos, farmland, Block.UPDATE_ALL_IMMEDIATE);
        this.level().levelEvent(null, 2001, this.targetPos, Block.getId(this.level().getBlockState(this.targetPos)));
    }

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.CARROT, 6);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.is(Items.CARROT) || itemStack.is(Items.GOLDEN_CARROT);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(Mth.nextInt(this.random, 100, 200));
    }

    @Override
    public boolean isAttractive(BlockPos pos) {
        BlockState target = this.level().getBlockState(pos);
        BlockState above = this.level().getBlockState(pos.above());
        return this.getTargetBlockFilter().test(target) && (above.isAir() || above.getBlock() instanceof BushBlock);
    }
}
