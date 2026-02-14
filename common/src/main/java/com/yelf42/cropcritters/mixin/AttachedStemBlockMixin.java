package com.yelf42.cropcritters.mixin;

import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.yelf42.cropcritters.registry.ModBlocks;


@Mixin(AttachedStemBlock.class)
public abstract class AttachedStemBlockMixin {

    // Allow planting on same blocks as other crops
    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulAndDirt(BlockState floor, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.is(ModBlocks.SOUL_FARMLAND)) cir.setReturnValue(true);
    }

}
