package com.yelf42.cropcritters.entity;

import com.yelf42.cropcritters.config.TillingBlockMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
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
        return (TillingBlockMapping.BEETROOT_INSTANCE::canTill);
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        this.playSound(ModSounds.ENTITY_CRITTER_TILL, 1.0F, 1.0F);

        BlockState target = this.level().getBlockState(this.targetPos);
        BlockState tillTo = TillingBlockMapping.BEETROOT_INSTANCE.getTillingMapping(target).orElse(null);
        if (tillTo == null) return;
        Block.pushEntitiesUp(target, tillTo, this.level(), this.targetPos);

        this.level().setBlock(this.targetPos, tillTo, Block.UPDATE_ALL_IMMEDIATE);
        this.level().levelEvent(this, 2001, this.targetPos, Block.getId(this.level().getBlockState(this.targetPos)));
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
    public boolean isAttractive(BlockPos pos) {
        BlockState target = this.level().getBlockState(pos);
        BlockState above = this.level().getBlockState(pos.above());
        return this.getTargetBlockFilter().test(target) && (above.isAir() || above.getBlock() instanceof VegetationBlock);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(Mth.nextInt(this.random, 100, 200));
    }
}
