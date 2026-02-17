package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.items.StrangeFertilizerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ModDispenserBehaviours {
    public static final List<Runnable> DISPENSER_BEHAVIORS = new ArrayList<>();

    public static void registerDispenserBehavior() {
        DISPENSER_BEHAVIORS.add(() ->
                DispenserBlock.registerProjectileBehavior(ModItems.SEED_BALL)
        );
        DISPENSER_BEHAVIORS.add(() ->
                DispenserBlock.registerBehavior(ModItems.STRANGE_FERTILIZER, new OptionalDispenseItemBehavior() {
                    protected ItemStack execute(BlockSource pointer, ItemStack stack) {
                        this.setSuccess(true);
                        Level world = pointer.level();
                        Direction facing = pointer.state().getValue(DispenserBlock.FACING);
                        BlockPos blockPos = pointer.pos().relative(facing);
                        BlockState state = world.getBlockState(blockPos);
                        if (!StrangeFertilizerItem.tryReviveCoral(stack, world, blockPos, state)
                                && !StrangeFertilizerItem.growCrop(stack, world, blockPos)
                                && !StrangeFertilizerItem.useOnGround(stack, world, blockPos, blockPos, facing)) {
                            this.setSuccess(false);
                        } else if (!world.isClientSide()) {
                            world.levelEvent(1505, blockPos, 15);
                        }
                        return stack;
                    }
                })
        );
    }

    public static void runDispenserRegistration() {
        DISPENSER_BEHAVIORS.forEach(Runnable::run);
    }
}
