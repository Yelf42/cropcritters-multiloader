package com.yelf42.cropcritters.mixin;

import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmBlock.class)
public abstract class FarmBlockMixin {

    @Inject(method = "isNearWater", at = @At("HEAD"), cancellable = true)
    private static void isWaterNearby(LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!world.getBiome(pos).value().hasPrecipitation()) {
            for(BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 1, 2))) {
                if (world.getFluidState(blockPos).is(FluidTags.WATER)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
            cir.setReturnValue(false);
        }
    }

}
