package com.yelf42.cropcritters.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.yelf42.cropcritters.CropCritters;

@Mixin(Biome.class)
public abstract class BiomeMixin {

    @Inject(method = "shouldSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBrightness(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I", shift = At.Shift.AFTER), cancellable = true)
    public void injectSnowOnCrops(LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.is(CropCritters.SNOW_FALL_KILLS) || (blockState.getBlock() instanceof CropBlock cropBlock && !world.getBlockState(pos.above()).is(cropBlock))) {
            cir.setReturnValue(true);
        }
    }

}
