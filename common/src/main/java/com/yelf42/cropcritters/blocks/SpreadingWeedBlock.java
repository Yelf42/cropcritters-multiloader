package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
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
import net.minecraft.world.level.ScheduledTickAccess;
import com.yelf42.cropcritters.config.WeedHelper;
import com.yelf42.cropcritters.events.WeedGrowNotifier;

import java.util.ArrayList;
import java.util.List;


public class SpreadingWeedBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<SpreadingWeedBlock> CODEC = simpleCodec(SpreadingWeedBlock::new);
    public static final int MAX_AGE = 1;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    private static final VoxelShape[] SHAPES_BY_AGE = Block.boxes(2, age -> Block.column(8 + age * 4, 0.0, 8 + age * 4));
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
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return super.updateShape(state.setValue(CAN_SPREAD, state.getValue(CAN_SPREAD) || !neighborState.is(this)), world, tickView, pos, direction, neighborPos, neighborState, random);
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
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        WeedGrowNotifier.notifyEvent(world, pos);
        super.onPlace(state, world, pos, oldState, notify);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        WeedGrowNotifier.notifyRemoval(world, pos);
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        builder.add(CAN_SPREAD);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return !isMature(state) || BonemealableBlock.hasSpreadableNeighbourPos(world, pos, state);
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
            BonemealableBlock.findSpreadableNeighbourPos(world, pos, state).ifPresent((posx) -> setToWeed(world,posx));
        }
    }
}
