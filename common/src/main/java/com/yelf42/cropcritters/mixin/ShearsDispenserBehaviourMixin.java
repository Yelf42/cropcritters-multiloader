package com.yelf42.cropcritters.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.blocks.PopperPlantBlock;
import com.yelf42.cropcritters.registry.ModItems;

@Mixin(ShearsDispenseItemBehavior.class)
public abstract class ShearsDispenserBehaviourMixin {

    // TODO tryShearBeehive is a weird remapping, but should work
    @Inject(method = "tryShearBeehive", at = @At("TAIL"), cancellable = true)
    private static void shearPopperPlant(ServerLevel world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.is(ModBlocks.POPPER_PLANT) && (blockState.getOptionalValue(PopperPlantBlock.AGE).orElse(0) == PopperPlantBlock.MAX_AGE)) {
            world.gameEvent((Entity)null, GameEvent.SHEAR, pos);
            world.playSound(null, pos, SoundEvents.BEEHIVE_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);

            Vec3 center = pos.getCenter();
            ItemStack itemStack = new ItemStack(ModItems.POPPER_POD);
            ItemEntity itemEntity = new ItemEntity(world, center.x, center.y, center.z, itemStack);
            itemEntity.setDefaultPickUpDelay();
            world.addFreshEntity(itemEntity);

            world.setBlockAndUpdate(pos, blockState.setValue(PopperPlantBlock.AGE, 0));

            cir.setReturnValue(true);
        }
    }

}
