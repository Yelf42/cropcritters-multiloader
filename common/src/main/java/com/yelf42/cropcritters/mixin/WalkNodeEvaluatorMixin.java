package com.yelf42.cropcritters.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.yelf42.cropcritters.CropCritters;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin {

    // PathNodeType.DAMAGE_OTHER is only used for CACTUS and SWEET_BERRY_BUSH, which is close enough to WEEDS
    // In the future, consider custom PathNodeType just for WEEDS
    @Inject(method= "getPathTypeFromState", at=@At("HEAD"), cancellable = true)
    private static void injectWeedPenalties(BlockGetter world, BlockPos pos, CallbackInfoReturnable<PathType> cir) {
        BlockState state = world.getBlockState(pos);
        if (state.is(CropCritters.PATH_PENALTY_WEEDS)) cir.setReturnValue(PathType.DAMAGE_OTHER);
    }

}
