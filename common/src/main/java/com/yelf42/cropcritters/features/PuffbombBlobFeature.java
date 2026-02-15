package com.yelf42.cropcritters.features;

import com.mojang.serialization.Codec;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.blocks.PuffbombPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import com.yelf42.cropcritters.registry.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class PuffbombBlobFeature extends Feature<NoneFeatureConfiguration> {
    private record Sphere(Vec3 pos, double radius) {
        private boolean isWithinDistance(BlockPos blockPos) {
            return blockPos.distToCenterSqr(pos) <= radius * radius;
        }
    }


    public PuffbombBlobFeature(Codec<NoneFeatureConfiguration> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        BlockPos centerPos = context.origin();
        RandomSource random = context.random();

        // Stop worldgen on leaves etc
        BlockState soil = world.getBlockState(centerPos.below());
        if ((!soil.is(BlockTags.MUSHROOM_GROW_BLOCK) && !soil.is(BlockTags.DIRT)) && (!soil.isSolidRender())) return false;

        // Seed center block
        BlockState toPlace = ModBlocks.PUFFBOMB_MUSHROOM_BLOCK.defaultBlockState();
        this.setBlock(world, centerPos, toPlace);

        // Generate spheres
        Vec3 trueCenter = centerPos.getBottomCenter();
        List<Sphere> spheres = new ArrayList<>();
        spheres.add(new Sphere(trueCenter, 2.0));
        for (int i = 0; i <= random.nextInt(3); i++) {
            double a = random.nextDouble() * 2.0 * Math.PI;
            double rad = 1.5 + random.nextDouble() * 2.0;
            double dist = random.nextDouble() * (3.0 / rad);
            Vec3 newCenter = new Vec3(Math.cos(a), 0.0 - (random.nextDouble() * (rad / 2.0)), Math.sin(a)).scale(dist).add(trueCenter);
            spheres.add(new Sphere(newCenter, rad));
        }

        // Try placing blocks and such
        Iterable<BlockPos> iterable = BlockPos.withinManhattan(centerPos, 4,4,4);
        for(BlockPos blockPos : iterable) {
            if (blockPos.getY() < centerPos.getY()
                    || !hasNeighbours(world, blockPos)
                    || !inSphere(blockPos, spheres)
                    || !isReplaceable(world, blockPos)) continue;
            this.setBlock(world, blockPos, toPlace);
        }
        return true;
    }

    private boolean hasNeighbours(LevelAccessor world, BlockPos pos) {
        if (world.getBlockState(pos.north()).is(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK)) return true;
        if (world.getBlockState(pos.east()).is(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK)) return true;
        if (world.getBlockState(pos.south()).is(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK)) return true;
        if (world.getBlockState(pos.west()).is(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK)) return true;
        if (world.getBlockState(pos.above()).is(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK)) return true;
        return (world.getBlockState(pos.below()).is(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK));
    }

    private boolean inSphere(BlockPos pos, List<Sphere> spheres) {
        for (Sphere sphere : spheres) {
            if (sphere.isWithinDistance(pos)) return true;
        }
        return false;
    }

    private static boolean isReplaceable(WorldGenLevel world, BlockPos pos) {
        if (world.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::canBeReplaced)) return true;
        return (world.getBlockState(pos).getBlock() instanceof VegetationBlock);
    }
}