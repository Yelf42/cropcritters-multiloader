package com.yelf42.cropcritters.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.EnumMap;
import java.util.Map;

public class MazewoodBlock extends Block {
    public static final BooleanProperty EAST_WALL_SHAPE;
    public static final BooleanProperty NORTH_WALL_SHAPE;
    public static final BooleanProperty SOUTH_WALL_SHAPE;
    public static final BooleanProperty WEST_WALL_SHAPE;
    private final Map<BlockState, VoxelShape> outlineShapeFunction;
    private final Map<BlockState, VoxelShape> collisionShapeFunction;

    public MazewoodBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH_WALL_SHAPE, false).setValue(EAST_WALL_SHAPE, false).setValue(SOUTH_WALL_SHAPE, false).setValue(WEST_WALL_SHAPE, false));
        this.outlineShapeFunction = this.createShapeFunction(16.0F);
        this.collisionShapeFunction = this.createShapeFunction(24.0F);
    }

    private Map<BlockState, VoxelShape> createShapeFunction(float tallHeight) {
        VoxelShape voxelShape = ModBlocks.column(8.0F, 0.0F, tallHeight);

        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        map.put(Direction.NORTH, Block.box(4.0D, 0.0D, 0.0D, 12.0D, tallHeight, 4.0D));
        map.put(Direction.SOUTH, Block.box(4.0D, 0.0D, 12.0D, 12.0D, tallHeight, 16.0D));
        map.put(Direction.EAST, Block.box(13.0D, 0.0D, 5.0D, 16.0D, tallHeight, 12.0D)); // Rotated 90°
        map.put(Direction.WEST, Block.box(0.0D, 0.0D, 5.0D, 4.0D, tallHeight, 12.0D)); // Same as east

        return this.getShapeForEachState((state) -> {
            VoxelShape voxelShape2 = voxelShape;

            if (state.getValue(NORTH_WALL_SHAPE)) voxelShape2 = Shapes.or(voxelShape2, map.get(Direction.NORTH));
            if (state.getValue(EAST_WALL_SHAPE)) voxelShape2 = Shapes.or(voxelShape2, map.get(Direction.EAST));
            if (state.getValue(SOUTH_WALL_SHAPE)) voxelShape2 = Shapes.or(voxelShape2, map.get(Direction.SOUTH));
            if (state.getValue(WEST_WALL_SHAPE)) voxelShape2 = Shapes.or(voxelShape2, map.get(Direction.WEST));

            return voxelShape2;
        });
    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return this.outlineShapeFunction.get(state);
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return this.collisionShapeFunction.get(state);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    private boolean shouldConnectTo(BlockState state, boolean faceFullSquare, Direction side) {
        return (state.getBlock() instanceof MazewoodBlock) || !isExceptionForConnection(state) && faceFullSquare;
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        LevelReader worldView = ctx.getLevel();
        BlockPos blockPos = ctx.getClickedPos();
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.east();
        BlockPos blockPos4 = blockPos.south();
        BlockPos blockPos5 = blockPos.west();
        BlockPos blockPos6 = blockPos.above();
        BlockState blockState = worldView.getBlockState(blockPos2);
        BlockState blockState2 = worldView.getBlockState(blockPos3);
        BlockState blockState3 = worldView.getBlockState(blockPos4);
        BlockState blockState4 = worldView.getBlockState(blockPos5);
        BlockState blockState5 = worldView.getBlockState(blockPos6);
        boolean bl = this.shouldConnectTo(blockState, blockState.isFaceSturdy(worldView, blockPos2, Direction.SOUTH), Direction.SOUTH);
        boolean bl2 = this.shouldConnectTo(blockState2, blockState2.isFaceSturdy(worldView, blockPos3, Direction.WEST), Direction.WEST);
        boolean bl3 = this.shouldConnectTo(blockState3, blockState3.isFaceSturdy(worldView, blockPos4, Direction.NORTH), Direction.NORTH);
        boolean bl4 = this.shouldConnectTo(blockState4, blockState4.isFaceSturdy(worldView, blockPos5, Direction.EAST), Direction.EAST);
        BlockState blockState6 = this.defaultBlockState();
        return this.getStateWith(worldView, blockState6, blockPos6, blockState5, bl, bl2, bl3, bl4);
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (!this.canSurvive(state, world, currentPos)) {
            if (world instanceof Level w) {
                w.scheduleTick(currentPos, this, 1);
            }
            return Blocks.AIR.defaultBlockState();
        }

        if (direction == Direction.DOWN) {
            return super.updateShape(state, direction, facingState, world, currentPos, facingPos);
        } else {
            return direction == Direction.UP ? this.getStateAt(world, state, facingPos, facingState) : this.getStateWithNeighbor(world, currentPos, state, facingPos, facingState, direction);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState floor = world.getBlockState(pos.below());
        return floor.is(BlockTags.DIRT) || floor.is(Blocks.FARMLAND) || (floor.getBlock() instanceof MazewoodBlock);
    }

    private static boolean isConnected(BlockState state, BooleanProperty property) {
        return state.getValue(property);
    }

    private BlockState getStateAt(LevelReader world, BlockState state, BlockPos pos, BlockState aboveState) {
        boolean bl = isConnected(state, NORTH_WALL_SHAPE);
        boolean bl2 = isConnected(state, EAST_WALL_SHAPE);
        boolean bl3 = isConnected(state, SOUTH_WALL_SHAPE);
        boolean bl4 = isConnected(state, WEST_WALL_SHAPE);
        return this.getStateWith(world, state, pos, aboveState, bl, bl2, bl3, bl4);
    }

    private BlockState getStateWithNeighbor(LevelReader world, BlockPos pos, BlockState state, BlockPos neighborPos, BlockState neighborState, Direction direction) {
        Direction direction2 = direction.getOpposite();
        boolean bl = direction == Direction.NORTH ? this.shouldConnectTo(neighborState, neighborState.isFaceSturdy(world, neighborPos, direction2), direction2) : isConnected(state, NORTH_WALL_SHAPE);
        boolean bl2 = direction == Direction.EAST ? this.shouldConnectTo(neighborState, neighborState.isFaceSturdy(world, neighborPos, direction2), direction2) : isConnected(state, EAST_WALL_SHAPE);
        boolean bl3 = direction == Direction.SOUTH ? this.shouldConnectTo(neighborState, neighborState.isFaceSturdy(world, neighborPos, direction2), direction2) : isConnected(state, SOUTH_WALL_SHAPE);
        boolean bl4 = direction == Direction.WEST ? this.shouldConnectTo(neighborState, neighborState.isFaceSturdy(world, neighborPos, direction2), direction2) : isConnected(state, WEST_WALL_SHAPE);
        BlockPos blockPos = pos.above();
        BlockState blockState = world.getBlockState(blockPos);
        return this.getStateWith(world, state, blockPos, blockState, bl, bl2, bl3, bl4);
    }

    private BlockState getStateWith(LevelReader world, BlockState state, BlockPos pos, BlockState aboveState, boolean north, boolean east, boolean south, boolean west) {
        VoxelShape voxelShape = aboveState.getCollisionShape(world, pos).getFaceShape(Direction.DOWN);
        return this.getStateWith(state, north, east, south, west, voxelShape);
    }

    private BlockState getStateWith(BlockState state, boolean north, boolean east, boolean south, boolean west, VoxelShape aboveShape) {
        return state.setValue(NORTH_WALL_SHAPE, north).setValue(EAST_WALL_SHAPE, east).setValue(SOUTH_WALL_SHAPE, south).setValue(WEST_WALL_SHAPE, west);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH_WALL_SHAPE, EAST_WALL_SHAPE, WEST_WALL_SHAPE, SOUTH_WALL_SHAPE);
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180 -> {
                return state.setValue(NORTH_WALL_SHAPE, state.getValue(SOUTH_WALL_SHAPE)).setValue(EAST_WALL_SHAPE, state.getValue(WEST_WALL_SHAPE)).setValue(SOUTH_WALL_SHAPE, state.getValue(NORTH_WALL_SHAPE)).setValue(WEST_WALL_SHAPE, state.getValue(EAST_WALL_SHAPE));
            }
            case COUNTERCLOCKWISE_90 -> {
                return state.setValue(NORTH_WALL_SHAPE, state.getValue(EAST_WALL_SHAPE)).setValue(EAST_WALL_SHAPE, state.getValue(SOUTH_WALL_SHAPE)).setValue(SOUTH_WALL_SHAPE, state.getValue(WEST_WALL_SHAPE)).setValue(WEST_WALL_SHAPE, state.getValue(NORTH_WALL_SHAPE));
            }
            case CLOCKWISE_90 -> {
                return state.setValue(NORTH_WALL_SHAPE, state.getValue(WEST_WALL_SHAPE)).setValue(EAST_WALL_SHAPE, state.getValue(NORTH_WALL_SHAPE)).setValue(SOUTH_WALL_SHAPE, state.getValue(EAST_WALL_SHAPE)).setValue(WEST_WALL_SHAPE, state.getValue(SOUTH_WALL_SHAPE));
            }
            default -> {
                return state;
            }
        }
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT -> {
                return state.setValue(NORTH_WALL_SHAPE, state.getValue(SOUTH_WALL_SHAPE)).setValue(SOUTH_WALL_SHAPE, state.getValue(NORTH_WALL_SHAPE));
            }
            case FRONT_BACK -> {
                return state.setValue(EAST_WALL_SHAPE, state.getValue(WEST_WALL_SHAPE)).setValue(WEST_WALL_SHAPE, state.getValue(EAST_WALL_SHAPE));
            }
            default -> {
                return super.mirror(state, mirror);
            }
        }
    }

    static {
        EAST_WALL_SHAPE = BooleanProperty.create("east");
        NORTH_WALL_SHAPE = BooleanProperty.create("north");
        SOUTH_WALL_SHAPE = BooleanProperty.create("south");
        WEST_WALL_SHAPE = BooleanProperty.create("west");
    }
}