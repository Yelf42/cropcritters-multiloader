package com.yelf42.cropcritters.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.yelf42.cropcritters.config.AffectorsHelper;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.config.ConfigManager;
import com.yelf42.cropcritters.config.CritterHelper;
import com.yelf42.cropcritters.config.WeedHelper;

import static net.minecraft.world.level.block.Block.pushEntitiesUp;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin {

    // Allows plants to be planted on SOUL_FARMLAND
    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulAndDirt(BlockState floor, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.is(BlockTags.DIRT)) cir.setReturnValue(true);
    }

    /// getGrowthSpeed is different in fabric v neoforge
//    // If SOUL_FARMLAND, ignore vanilla moisture stuff and just return 18.f
//    @Inject(method = "getGrowthSpeed", at = @At("HEAD"), cancellable = true)
//    private static void soulBasedMoisture(Block block, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
//        BlockState blockState = world.getBlockState(pos.below());
//        if (blockState.is(ModBlocks.SOUL_FARMLAND)) {
//            cir.setReturnValue(18.f);
//        }
//    }

    // Inject into hasRandomTicks to make always true
    @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
    private void overrideMatureStoppingRandomTicks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    // Replace farmland with a dirt if just matured
    // Chance to spawn critter if just matured
    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.AFTER))
    private void removeNutrientsAndSpawnCritters(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            if (cropBlock.getAge(state) + 1 != cropBlock.getMaxAge()) return;
            BlockState soilCheck = world.getBlockState(pos.below());
            if (soilCheck.is(Blocks.FARMLAND)) {
                // Farmland to dirt
                pushEntitiesUp(Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT.defaultBlockState(), world, pos.below());
                BlockState toDirt = (random.nextInt(4) == 0) ? Blocks.DIRT.defaultBlockState() : (random.nextInt(2) == 0) ? Blocks.ROOTED_DIRT.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
                world.setBlock(pos.below(), toDirt, Block.UPDATE_CLIENTS);
            } else if (soilCheck.is(ModBlocks.SOUL_FARMLAND)){
                // Soul farmland to soul blocks
                pushEntitiesUp(Blocks.FARMLAND.defaultBlockState(), Blocks.SOUL_SOIL.defaultBlockState(), world, pos.below());
                BlockState toDirt = (random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.defaultBlockState() : Blocks.SOUL_SAND.defaultBlockState();
                world.setBlock(pos.below(), toDirt, Block.UPDATE_CLIENTS);
            } else {
                return;
            }

            if (CritterHelper.spawnCritter(world, state, random, pos)) return;
        }
    }

    // Stop ticking / aging if not on farmland
    // Percent chance to cancel if in gold Soul Rose area
    // Try spawn critter if soul_sand_valley
    // Try to generate weed if on farmland (scale chance with age)
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void stopGrowthAndSpawnSoulSandValleyCrittersOrWeeds(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (random.nextInt(100) < ConfigManager.CONFIG.goldSoulRoseSlowdown && AffectorsHelper.copperSoulRoseCheck(world, pos)) {
            ci.cancel();
            return;
        }

        BlockState soilCheck = world.getBlockState(pos.below());
        if (!(soilCheck.is(Blocks.FARMLAND) || soilCheck.is(ModBlocks.SOUL_FARMLAND))) {
            ci.cancel();
            return;
        }

        if (state.getBlock() instanceof CropBlock cropBlock) {
            if (world.getBiome(pos).is(Biomes.SOUL_SAND_VALLEY) && cropBlock.isMaxAge(state)) {
                if (CritterHelper.spawnCritter(world, state, random, pos)) return;
            }
            if (random.nextDouble() < 0.03 * ((double) cropBlock.getAge(state) / (cropBlock.getMaxAge() - 1))) {
                WeedHelper.generateWeed(state, world, pos, random, soilCheck.is(ModBlocks.SOUL_FARMLAND));
            }
        }
    }


}
