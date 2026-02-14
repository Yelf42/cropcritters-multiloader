package com.yelf42.cropcritters.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.registry.ModBlocks;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

    public ServerLevelMixin(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "updatePOIOnBlockStateChange", at = @At("HEAD"))
    public void injectAreaAffectorCheck(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        AffectorPositions.onBlockStateChange(ServerLevel.class.cast(this), pos, oldState, newState);
    }

    @Inject(method = "tickPrecipitation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", shift = At.Shift.AFTER))
    public void injectFarmlandSnowFall(BlockPos pos, CallbackInfo ci) {
        BlockPos blockPos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).below();
        BlockState blockState = this.getBlockState(blockPos);
        if (blockState.is(Blocks.FARMLAND)) {
            Block.pushEntitiesUp(blockState, Blocks.DIRT.defaultBlockState(), this, blockPos);
            this.setBlockAndUpdate(blockPos, Blocks.DIRT.defaultBlockState());
        } else if (blockState.is(ModBlocks.SOUL_FARMLAND)) {
            Block.pushEntitiesUp(blockState, Blocks.SOUL_SOIL.defaultBlockState(), this, blockPos);
            this.setBlockAndUpdate(blockPos, Blocks.SOUL_SOIL.defaultBlockState());
        }
    }

}
