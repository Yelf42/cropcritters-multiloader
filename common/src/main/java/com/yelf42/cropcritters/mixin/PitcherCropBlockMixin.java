package com.yelf42.cropcritters.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.config.CritterHelper;

import static net.minecraft.world.level.block.Block.pushEntitiesUp;

@Mixin(PitcherCropBlock.class)
public abstract class PitcherCropBlockMixin {

    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final public static EnumProperty<DoubleBlockHalf> HALF;

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulAndDirt(BlockState floor, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.is(BlockTags.DIRT)) cir.setReturnValue(true);
    }

    // Stop growth on non-farmland
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void cancelGrowth(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!(world.getBlockState(pos.below()).is(Blocks.FARMLAND) || world.getBlockState(pos.below()).is(ModBlocks.SOUL_FARMLAND))) ci.cancel();
    }

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/PitcherCropBlock;grow(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;I)V", shift = At.Shift.AFTER))
    private void removeNutrientsAndSpawnCritters(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (state.getValueOrElse(AGE, 0) <= 3) return;

        BlockState soilCheck = world.getBlockState(pos.below());
        if (soilCheck.is(Blocks.FARMLAND)) {
            pushEntitiesUp(Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT.defaultBlockState(), world, pos.below());
            BlockState toDirt = (world.random.nextInt(4) == 0) ? Blocks.DIRT.defaultBlockState() : (world.random.nextInt(2) == 0) ? Blocks.ROOTED_DIRT.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
            world.setBlock(pos.below(), toDirt, Block.UPDATE_CLIENTS);
        } else if (soilCheck.is(ModBlocks.SOUL_FARMLAND)){
            pushEntitiesUp(ModBlocks.SOUL_FARMLAND.defaultBlockState(), Blocks.SOUL_SOIL.defaultBlockState(), world, pos.below());
            BlockState toDirt = (world.random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.defaultBlockState() : Blocks.SOUL_SAND.defaultBlockState();
            world.setBlock(pos.below(), toDirt, Block.UPDATE_CLIENTS);
        } else {
            return;
        }

        if (CritterHelper.spawnCritter(world, state, world.random, pos)) return;
    }

}
