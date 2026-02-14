package com.yelf42.cropcritters.features;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import com.yelf42.cropcritters.blocks.SoulRoseBlockEntity;

public class SoulRoseHintFeature extends Feature<NoneFeatureConfiguration> {

    public SoulRoseHintFeature(Codec<NoneFeatureConfiguration> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        int level = random.nextInt(3) + 1;
        WorldGenLevel world = context.level();
        if (!world.getBlockState(context.origin()).is(Blocks.AIR)) return false;
        BlockPos pos = context.origin().below();

        // Stage 1
        if (!trySetBlock(world, pos.below().below(), random)) return false;
        for (Vec3i offset : SoulRoseBlockEntity.STAGE_1A) {
            if (!trySetBlock(world, pos.offset(offset), random)) return true;
        }
        for (int i = 0; i < 4; i++) {
            for (Vec3i offset : SoulRoseBlockEntity.STAGE_1B) {
                trySetBlock(world, pos.offset(rotate(offset, i)), random);
            }
        }

        // Stage 2
        if (level > 1) {
            for (int i = 0; i < 4; i++) {
                for (Vec3i offset : SoulRoseBlockEntity.STAGE_2) {
                    trySetBlock(world, pos.offset(rotate(offset, i)), random);
                }
            }
        }

        // Stage 3
        if (level > 2) {
            for (int i = 0; i < 4; i++) {
                for (Vec3i offset : SoulRoseBlockEntity.STAGE_3) {
                    trySetBlock(world, pos.offset(rotate(offset, i)), random);
                }
            }
        }

        return true;
    }

    private boolean trySetBlock(WorldGenLevel world, BlockPos pos, RandomSource random) {
        BlockState check = world.getBlockState(pos);
        if (!check.is(Blocks.AIR)) {
            this.setBlock(world, pos, chooseBlock(random));
            return true;
        }
        return false;
    }

    private static BlockState chooseBlock(RandomSource random) {
        //return Blocks.END_ROD.getDefaultState();
        return random.nextInt(5) != 0 ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : (random.nextInt(2) == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.RAW_GOLD_BLOCK.defaultBlockState());
    }

    private static Vec3i rotate(Vec3i v, int dir) {
        int x = v.getX();
        int y = v.getY();
        int z = v.getZ();

        return switch (dir) {
            case 0  -> new Vec3i( x, y,  z);
            case 1 -> new Vec3i(-z, y,  x);
            case 2  -> new Vec3i(-x, y, -z);
            case 3 -> new Vec3i( z, y, -x);
            default -> v;
        };
    }
}
