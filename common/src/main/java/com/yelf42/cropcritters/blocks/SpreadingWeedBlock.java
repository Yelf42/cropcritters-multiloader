package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.gameevent.GameEvent;
import com.yelf42.cropcritters.config.WeedHelper;
import com.yelf42.cropcritters.events.WeedGrowNotifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class SpreadingWeedBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<SpreadingWeedBlock> CODEC = simpleCodec(SpreadingWeedBlock::new);
    public static final int MAX_AGE = 1;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    private static final VoxelShape[] SHAPES_BY_AGE = ModBlocks.boxes(2, age -> ModBlocks.column(8 + age * 4, 0.0, 8 + age * 4));
    public static final BooleanProperty CAN_SPREAD = BooleanProperty.create("can_spread");

    public SpreadingWeedBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public MapCodec<? extends SpreadingWeedBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES_BY_AGE[this.getAge(state)];
    }

    public int getMaxNeighbours() { return 0; }

    public int getMaxAge() {
        return MAX_AGE;
    }

    public int getAge(BlockState state) {
        return (Integer)state.getValue(AGE);
    }

    public BlockState withAge(int age) {
        return this.defaultBlockState().setValue(AGE, age);
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }


    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        return super.updateShape(state.setValue(CAN_SPREAD, state.getValue(CAN_SPREAD) || !facingState.is(this)), facing, facingState, world, currentPos, facingPos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(CAN_SPREAD);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {

        // Turn farmlands bad
        BlockState soilCheck = world.getBlockState(pos.below());
        if (soilCheck.is(Blocks.FARMLAND)) {
            BlockState toDirt = (random.nextInt(4) == 0) ? Blocks.DIRT.defaultBlockState() : (random.nextInt(2) == 0) ? Blocks.ROOTED_DIRT.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
            world.setBlock(pos.below(), toDirt, Block.UPDATE_CLIENTS);
        } else if (soilCheck.is(ModBlocks.SOUL_FARMLAND)){
            BlockState toDirt = (random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.defaultBlockState() : Blocks.SOUL_SAND.defaultBlockState();
            world.setBlock(pos.below(), toDirt, Block.UPDATE_CLIENTS);
        }

        if (this.isMature(state)) {
            // Count neighbouring weeds and finding spots to spread to
            List<BlockPos> canSpreadTo = new ArrayList<>();
            int neighbouringWeeds = -1;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        BlockPos checkPos = pos.offset(i, k, j);
                        BlockState checkState = world.getBlockState(checkPos);
                        if (checkState.is(this)) neighbouringWeeds++;
                        BlockState checkBelowState = world.getBlockState(checkPos.below());
                        if (mayPlaceOn(checkBelowState, world, checkPos.below()) && WeedHelper.canWeedsReplace(checkState)) {
                            canSpreadTo.add(checkPos);
                        }
                    }
                }
            }

            // Place new weed if mature and <2 neighbouring weeds and target is plantable
            if (neighbouringWeeds < getMaxNeighbours() && !canSpreadTo.isEmpty()) {
                BlockPos targetPos = canSpreadTo.get(random.nextInt(canSpreadTo.size()));
                setToWeed(world, targetPos);
            } else {
                world.setBlockAndUpdate(pos, state.setValue(CAN_SPREAD, false));
            }
        } else if (random.nextInt(2) == 0) {
            // 50% chance to mature per random tick
            world.setBlock(pos, this.withAge(this.getMaxAge()), Block.UPDATE_CLIENTS);
        }
    }

    public void setToWeed(Level world, BlockPos pos) {
        BlockState blockState = this.defaultBlockState();
        world.setBlockAndUpdate(pos, blockState);
        WeedGrowNotifier.notifyEvent(world, pos);
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(null, blockState));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        builder.add(CAN_SPREAD);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return !isMature(state) || hasSpreadableNeighbourPos(world, pos, state);
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextInt(2) == 0;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        if (!isMature(state)) {
            world.setBlock(pos, this.withAge(this.getMaxAge()), Block.UPDATE_CLIENTS);
        } else {
            getSpreadableNeighbourPos(world, pos, state).ifPresent((posx) -> setToWeed(world,posx));
        }
    }

    public static boolean hasSpreadableNeighbourPos(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return getSpreadableNeighbourPos(levelReader, blockPos, blockState).isPresent();
    }
    public static Optional<BlockPos> getSpreadableNeighbourPos(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        for(Direction direction : Direction.Plane.HORIZONTAL.stream().toList()) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (levelReader.isEmptyBlock(blockPos2) && blockState.canSurvive(levelReader, blockPos2)) {
                return Optional.of(blockPos2);
            }
        }

        return Optional.empty();
    }
}
