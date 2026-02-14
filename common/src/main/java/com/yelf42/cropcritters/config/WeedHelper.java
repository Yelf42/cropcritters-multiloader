package com.yelf42.cropcritters.config;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.gameevent.GameEvent;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.registry.ModBlocks;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.level.block.Block.pushEntitiesUp;

public class WeedHelper {

    public static boolean canWeedsReplace(BlockState state) {
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) return false;
        return state.is(Blocks.AIR)
                || (state.getBlock() instanceof VegetationBlock && !state.is(CropCritters.WEEDS) && !state.is(CropCritters.IMMUNE_PLANTS));
    }

    // List of weeds not in the weighted lists:
    // Withering Spiteweed (only generates in soul sand valley)
    // Liverwort (only generates when raining)
    // Mazewood sapling (generates from lightning, not this)

    private static final List<Tuple<BlockState, Double>> WEIGHTED_NETHER_WEEDS = new ArrayList<>(List.of(
            new Tuple<>(ModBlocks.WITHERING_SPITEWEED.defaultBlockState(), 0.2),
            new Tuple<>(ModBlocks.WAFTGRASS.defaultBlockState(), 0.25),
            new Tuple<>(ModBlocks.BONE_TRAP.defaultBlockState(), 0.25),
            new Tuple<>(ModBlocks.CRIMSON_THORNWEED.defaultBlockState(), 0.3)
    ));

    private static final List<Tuple<BlockState, Double>> WEIGHTED_OVERWORLD_WEEDS = new ArrayList<>(List.of(
            new Tuple<>(ModBlocks.POPPER_PLANT.defaultBlockState(), 0.15),
            new Tuple<>(ModBlocks.PUFFBOMB_MUSHROOM.defaultBlockState(), 0.15),
            new Tuple<>(ModBlocks.STRANGLE_FERN.defaultBlockState(), 0.3),
            new Tuple<>(ModBlocks.CRAWL_THISTLE.defaultBlockState(), 0.4)
    ));

    // Assumes weights add up to 1
    public static <A> A getFromWeightedList(List<Tuple<A, Double>> list) {
        double r = Math.random();
        for (Tuple<A, Double> pair : list) {
            r -= pair.getB();
            if (r <= 0) return pair.getA();
        }
        return list.getLast().getA();
    }

    public static void generateWeed(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, boolean nether) {
        // Cancel if in range of a gold Soul Rose
        if (AffectorsHelper.copperSoulRoseCheck(world, pos)) return;

        // Count how many neighbours are the same type of crop
        // More identical crops increases chance of weed growth
        float monoCount = 1F;
        if (ConfigManager.CONFIG.monoculturePenalize) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == j && j == 0) continue;
                    BlockState cropToCheck = world.getBlockState(pos.offset(i,0, j));
                    monoCount += cropToCheck.is(state.getBlock()) ? 1F : 0F;
                }
            }
            // Quadratic penalty increase for monocultural practices
            monoCount = (monoCount * monoCount) / 8F;
        }
        boolean growOverworldWeed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.regularWeedChance + (monoCount);
        boolean growNetherWeed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.netherWeedChance + (monoCount);

        // Break early, no weeds can grow
        if (!growOverworldWeed && !growNetherWeed) return;

        if (nether && growNetherWeed) {
            // Special case checks
            if (world.getBiome(pos).is(Biomes.SOUL_SAND_VALLEY)) {
                placeUniqueWeed(ModBlocks.WITHERING_SPITEWEED.defaultBlockState(), world, pos, Blocks.BLACKSTONE.defaultBlockState());
                return;
            }

            // Default
            placeNetherWeed(getFromWeightedList(WEIGHTED_NETHER_WEEDS), world, pos, random);
        } else if (!nether && growOverworldWeed) {
            // Special case checks
            if (world.isRainingAt(pos)) {
                placeOverworldWeed(ModBlocks.LIVERWORT.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true), world, pos, random);
                return;
            }

            BlockState toPlace = getFromWeightedList(WEIGHTED_OVERWORLD_WEEDS);

            float temp = world.getBiome(pos).value().getBaseTemperature();
            if (toPlace.is(ModBlocks.POPPER_PLANT) && (temp >= 1.0 || temp < 0.5)) toPlace = ModBlocks.CRAWL_THISTLE.defaultBlockState();

            // Default
            placeOverworldWeed(toPlace, world, pos, random);
        }
    }

    private static void placeNetherWeed(BlockState weedState, ServerLevel world, BlockPos pos, RandomSource random) {
        pushEntitiesUp(ModBlocks.SOUL_FARMLAND.defaultBlockState(), Blocks.SOUL_SOIL.defaultBlockState(), world, pos.below());
        world.setBlock(pos.below(), (random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.defaultBlockState() : Blocks.SOUL_SAND.defaultBlockState(), Block.UPDATE_CLIENTS);
        world.setBlockAndUpdate(pos, weedState);
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(null, weedState));
    }

    private static void placeOverworldWeed(BlockState weedState, ServerLevel world, BlockPos pos, RandomSource random) {
        pushEntitiesUp(Blocks.FARMLAND.defaultBlockState(), Blocks.COARSE_DIRT.defaultBlockState(), world, pos.below());
        world.setBlock(pos.below(), (random.nextInt(2) == 0) ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.ROOTED_DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
        world.setBlockAndUpdate(pos, weedState);
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(null, weedState));
    }

    private static void placeUniqueWeed(BlockState weedState, ServerLevel world, BlockPos pos, BlockState below) {
        pushEntitiesUp(ModBlocks.SOUL_FARMLAND.defaultBlockState(), below, world, pos.below());
        world.setBlock(pos.below(), below, Block.UPDATE_CLIENTS);
        world.setBlockAndUpdate(pos, weedState);
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(null, weedState));
    }
}
