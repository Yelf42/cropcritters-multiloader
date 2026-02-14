package com.yelf42.cropcritters.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
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
import com.yelf42.cropcritters.config.AffectorsHelper;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.config.ConfigManager;
import com.yelf42.cropcritters.config.CritterHelper;
import com.yelf42.cropcritters.config.WeedHelper;

@Mixin(NetherWartBlock.class)
public abstract class NetherWartBlockMixin {
    @Shadow @Final public static IntegerProperty AGE;

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulStuff(BlockState floor, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.is(Blocks.SOUL_SAND) || floor.is(ModBlocks.SOUL_FARMLAND) || floor.is(Blocks.SOUL_SOIL)) {
            cir.setReturnValue(true);
            return;
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void injectIntoRandomTicksHead(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (random.nextInt(100) < ConfigManager.CONFIG.goldSoulRoseSlowdown && AffectorsHelper.copperSoulRoseCheck(world, pos)) ci.cancel();
    }

    // Inject into randomTicks to turn into weed if mature
    // Grow faster on soul farmland
    @Inject(method = "randomTick", at = @At("TAIL"))
    private void injectIntoRandomTicksTail(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        // Grow faster on soul farmland
        BlockState soilCheck = world.getBlockState(pos.below());
        int wartAge = state.getValueOrElse(AGE, 0);
        if (wartAge < NetherWartBlock.MAX_AGE) {
            if (soilCheck.is(ModBlocks.SOUL_FARMLAND) && (random.nextInt(7) == 0)) {
                state = state.setValue(AGE, wartAge + 1);
                world.setBlock(pos, state, 2);
            }
            return;
        }

        if (CritterHelper.spawnCritter(world, state, random, pos)) return;

        WeedHelper.generateWeed(state, world, pos, random, true);
    }
}
