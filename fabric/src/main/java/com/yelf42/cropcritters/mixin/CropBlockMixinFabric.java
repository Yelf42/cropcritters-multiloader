package com.yelf42.cropcritters.mixin;

import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CropBlock.class)
public abstract class CropBlockMixinFabric {

    // If SOUL_FARMLAND, ignore vanilla moisture stuff and just return 18.f
    @Inject(method = "getGrowthSpeed", at = @At("HEAD"), cancellable = true)
    private static void soulBasedMoisture(Block block, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        BlockState blockState = world.getBlockState(pos.below());
        if (blockState.is(ModBlocks.SOUL_FARMLAND)) {
            cir.setReturnValue(18.f);
        }
    }

}
