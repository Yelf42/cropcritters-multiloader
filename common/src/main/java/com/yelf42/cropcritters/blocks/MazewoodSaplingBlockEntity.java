package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.Codec;
import com.yelf42.cropcritters.registry.ModBlockEntities;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.ticks.TickPriority;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.ArrayDeque;

public class MazewoodSaplingBlockEntity extends BlockEntity {

    private final ArrayDeque<Long> growInto = new ArrayDeque<>();
    public static final long[] MAZE_TILES = new long[] {
            0b1111011110100000101011101000101000001010100010001010101110100000L,
            0b1111011110100000101011101010100000111010100000101111111010000000L,
            0b1111011110100000101111101010001000101010101010001010111010000000L,
            0b1111011110100010101010101000101000001010101000101010111010000000L,
            0b1111011110000000111000101010001000101010101000101011111010000000L,
            0b1111011110000000111001101010001000111010100010101010101010100000L,
            0b1111011110000000111001101010001000111010100010001011111010000000L,
            0b1111011110000000111000101000001000111010100010101110111010000000L,
            0b1111011110000000111000111000000000111010101000101011111010000000L,
            0b1111011110000000111000111000000000111110101000101010101010000000L,
            0b1111011110000000111111101000100000101010101010001011111010000000L,
            0b1111011110000000111111101010000000101110101010001010101010000010L,
            0b1111011110000000111111101010001000101010101010001010111110000000L,
            0b1111011110000010111110101000001000111110100010001110111010000000L,
            0b1111011110000010111110101000100000101110101010001011101110000000L,
            0b1111011110000010111110101000100000111110100000101011101010000000L
    };

    public MazewoodSaplingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAZEWOOD_SAPLING, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);

        ValueOutput.TypedOutputList<Long> appender = view.list("GrowInto", Codec.LONG);
        for (Long pos : growInto) {
            appender.add(pos);
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);

        growInto.clear();
        ValueInput.TypedInputList<Long> listView = view.listOrEmpty("GrowInto", Codec.LONG);
        for (Long posLong : listView) {
            growInto.add(posLong);
        }
    }

    public void tickScheduled() {
        if (level == null || level.isClientSide()) return;

        if (growInto.isEmpty()) {
            BlockState matureMazewood = ModBlocks.MAZEWOOD.defaultBlockState();
            level.playSound(null, worldPosition, ModSounds.MAZEWOOD_MATURE, SoundSource.BLOCKS);

            level.setBlockAndUpdate(worldPosition, matureMazewood);
            level.gameEvent(GameEvent.BLOCK_CHANGE, worldPosition, GameEvent.Context.of(null, matureMazewood));

            if (level.getBlockState(worldPosition.above()).isAir()) {
                level.setBlockAndUpdate(worldPosition.above(), matureMazewood);
                level.gameEvent(GameEvent.BLOCK_CHANGE, worldPosition.above(), GameEvent.Context.of(null, matureMazewood));

                if (level.getBlockState(worldPosition.above().above()).isAir()) {
                    level.setBlockAndUpdate(worldPosition.above().above(), matureMazewood);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, worldPosition.above().above(), GameEvent.Context.of(null, matureMazewood));
                }
            }
            return;
        }

        BlockPos growPos = BlockPos.of(growInto.removeFirst());
        BlockState checkState = level.getBlockState(growPos);
        BlockState checkBelowState = level.getBlockState(growPos.below());
        boolean planted = false;
        if (canPlantAt(checkState, checkBelowState)) {
            level.playSound(null, growPos, ModSounds.MAZEWOOD_GROW, SoundSource.BLOCKS);
            BlockState blockState = getBlockState().setValue(MazewoodSaplingBlock.SPREAD, getBlockState().getValue(MazewoodSaplingBlock.SPREAD) - 1);
            level.setBlockAndUpdate(growPos, blockState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, growPos, GameEvent.Context.of(null, blockState));
            planted = true;
        }

        level.scheduleTick(worldPosition, getBlockState().getBlock(), planted ? 15 + level.random.nextInt(30) : 1, TickPriority.EXTREMELY_LOW);
    }

    private boolean canPlantAt(BlockState checkState, BlockState checkBelowState) {
        return (checkBelowState.is(BlockTags.DIRT))
                && (checkState.isAir()
                || (!(checkState.getBlock() instanceof MazewoodSaplingBlock)
                    && (checkState.getBlock() instanceof VegetationBlock)));
    }

    public void tickRandom(RandomSource random) {
        if (level == null || level.isClientSide() || !this.growInto.isEmpty()) return;

        BlockState soil = level.getBlockState(worldPosition.below());
        if (soil.is(Blocks.FARMLAND)) {
            BlockState to = switch (random.nextInt(3)) {
                case 0 -> Blocks.DIRT.defaultBlockState();
                case 1 -> Blocks.ROOTED_DIRT.defaultBlockState();
                default -> Blocks.COARSE_DIRT.defaultBlockState();
            };
            level.setBlockAndUpdate(worldPosition.below(), to);
        } else if (soil.is(ModBlocks.SOUL_FARMLAND)) {
            level.setBlockAndUpdate(worldPosition.below(), random.nextBoolean()
                    ? Blocks.SOUL_SOIL.defaultBlockState()
                    : Blocks.SOUL_SAND.defaultBlockState());
        }

        mature();
    }

    private int getSpread() {
        return getBlockState().getValue(MazewoodSaplingBlock.SPREAD);
    }

    private void mature() {
        if (level == null || level.isClientSide()) return;
        if (!isWall(worldPosition)) {
            level.setBlockAndUpdate(worldPosition, Blocks.DEAD_BUSH.defaultBlockState());
            return;
        }

        if (getSpread() > 0) {
            for (BlockPos p : BlockPos.withinManhattan(worldPosition, 2, 1, 2)) {
                if (isWall(p)) growInto.addLast(p.asLong());
            }
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 15, TickPriority.EXTREMELY_LOW);
        } else {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 45, TickPriority.EXTREMELY_LOW);
        }

    }

    public static boolean isWall(BlockPos blockPos) {
        int tileSize = 8;

        int tileX = Math.floorDiv(blockPos.getX(), tileSize);
        int tileZ = Math.floorDiv(blockPos.getZ(), tileSize);
        int mazeTile = Math.floorMod((int)(((long)tileX * 7342871L) ^ ((long)tileZ * 912783L)), 16);

        int localX = Math.floorMod(blockPos.getX(), tileSize);
        int localZ = Math.floorMod(blockPos.getZ(), tileSize);
        int bitIndex = localZ * tileSize + localX;

        return ((MAZE_TILES[mazeTile] >> bitIndex) & 1L) != 0;
    }
}
