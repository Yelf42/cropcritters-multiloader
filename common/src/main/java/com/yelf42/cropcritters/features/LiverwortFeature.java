package com.yelf42.cropcritters.features;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import com.yelf42.cropcritters.blocks.LiverwortBlock;
import com.yelf42.cropcritters.registry.ModBlocks;

public class LiverwortFeature extends Feature<CountConfiguration> {
    public LiverwortFeature(Codec<CountConfiguration> codec) {
        super(codec);
    }

    public boolean place(FeaturePlaceContext<CountConfiguration> context) {
        int i = 0;
        RandomSource random = context.random();
        WorldGenLevel structureWorldAccess = context.level();
        BlockPos blockPos = context.origin();
        int j = ((CountConfiguration)context.config()).count().sample(random);

        for(int k = 0; k < j; ++k) {
            int l = random.nextInt(8) - random.nextInt(8);
            int m = random.nextInt(8) - random.nextInt(8);
            int n = structureWorldAccess.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + l, blockPos.getZ() + m);
            BlockPos blockPos2 = new BlockPos(blockPos.getX() + l, n, blockPos.getZ() + m);
            BlockState blockState = (BlockState) ModBlocks.LIVERWORT.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true).setValue(LiverwortBlock.WATERLOGGED, true);
            if (structureWorldAccess.getBlockState(blockPos2).is(Blocks.WATER) && blockState.canSurvive(structureWorldAccess, blockPos2)) {
                structureWorldAccess.setBlock(blockPos2, blockState, 2);
                ++i;
            }
        }

        return i > 0;
    }
}
