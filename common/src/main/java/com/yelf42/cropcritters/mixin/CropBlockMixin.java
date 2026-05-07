package com.yelf42.cropcritters.mixin;

import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.config.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
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
import com.yelf42.cropcritters.registry.ModBlocks;

import static net.minecraft.world.level.block.Block.pushEntitiesUp;

// TODO inject into performBonemeal to catch mods that use that to grow via custom farmland
@Mixin(CropBlock.class)
public abstract class CropBlockMixin {

    // Allows plants to be planted on SOUL_FARMLAND
    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulAndDirt(BlockState floor, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.is(BlockTags.SUPPORTS_VEGETATION) || floor.getBlock() instanceof FarmlandBlock) cir.setReturnValue(true);
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
            BlockState degradeTo = FarmlandDegradationMapping.INSTANCE.getFarmlandDegradationMapping(soilCheck).orElse(null);

            if (degradeTo != null && ((CropBlockAccessor)cropBlock).invokeMayPlaceOn(degradeTo, world, pos)) {
                pushEntitiesUp(soilCheck, degradeTo, world, pos.below());
                world.setBlock(pos.below(), degradeTo, Block.UPDATE_ALL);
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
        if (random.nextInt(100) < ConfigManager.CONFIG.copperSoulRoseSlowdown && AffectorsHelper.copperSoulRoseCheck(world, pos)) {
            ci.cancel();
            return;
        }

        BlockState soilCheck = world.getBlockState(pos.below());
        if (!FarmlandDegradationMapping.INSTANCE.growingMedium(soilCheck)) {
            ci.cancel();
            return;
        }

        if (state.getBlock() instanceof CropBlock cropBlock) {
            if (world.getBiome(pos).is(Biomes.SOUL_SAND_VALLEY) && cropBlock.isMaxAge(state)) {
                if (CritterHelper.spawnCritter(world, state, random, pos)) return;
            }
            if (random.nextDouble() < 0.03 * ((double) cropBlock.getAge(state) / (cropBlock.getMaxAge() - 1))) {
                WeedHelper.generateWeed(state, world, pos, random, soilCheck.is(CropCritters.GROWS_NETHER_WEEDS));
            }
        }
    }


}
