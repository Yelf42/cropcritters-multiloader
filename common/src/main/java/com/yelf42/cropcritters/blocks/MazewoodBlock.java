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
    public static final MapCodec<MazewoodBlock> CODEC = simpleCodec(MazewoodBlock::new);
    public static final BooleanProperty UP;
    public static final EnumProperty<MazewoodShape> EAST_WALL_SHAPE;
    public static final EnumProperty<MazewoodShape> NORTH_WALL_SHAPE;
    public static final EnumProperty<MazewoodShape> SOUTH_WALL_SHAPE;
    public static final EnumProperty<MazewoodShape> WEST_WALL_SHAPE;
    public static final Map<Direction, EnumProperty<MazewoodShape>> WALL_SHAPE_PROPERTIES_BY_DIRECTION;
    private final Map<BlockState, VoxelShape> outlineShapeFunction;
    private final Map<BlockState, VoxelShape> collisionShapeFunction;
    private static final Map<Direction, VoxelShape> WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION;

    public MapCodec<MazewoodBlock> codec() {
        return CODEC;
    }

    public MazewoodBlock(Properties settings) {
        super(settings);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(UP, true)).setValue(NORTH_WALL_SHAPE, MazewoodShape.NONE)).setValue(EAST_WALL_SHAPE, MazewoodShape.NONE)).setValue(SOUTH_WALL_SHAPE, MazewoodShape.NONE)).setValue(WEST_WALL_SHAPE, MazewoodShape.NONE)));
        this.outlineShapeFunction = this.createShapeFunction(16.0F);
        this.collisionShapeFunction = this.createShapeFunction(24.0F);
    }

    // TODO
    private Map<BlockState, VoxelShape> createShapeFunction(float tallHeight) {
        VoxelShape voxelShape = ModBlocks.column((double)8.0F, (double)0.0F, (double)tallHeight);

        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        VoxelShape northShape = ModBlocks.boxZ((double)8.0F, (double)0.0F, (double)tallHeight, (double)0.0F, (double)11.0F);
        map.put(Direction.NORTH, northShape);
        map.put(Direction.SOUTH, northShape); // Same as north, opposite side
        map.put(Direction.EAST, Block.box(5.0D, 0.0D, 0.0D, 11.0D, tallHeight, 16.0D)); // Rotated 90°
        map.put(Direction.WEST, Block.box(5.0D, 0.0D, 0.0D, 11.0D, tallHeight, 16.0D)); // Same as east

        return this.getShapeForEachState((state) -> {
            VoxelShape voxelShape2 = state.getValue(UP) ? voxelShape : Shapes.empty();

            for(Map.Entry<Direction, EnumProperty<MazewoodShape>> entry : WALL_SHAPE_PROPERTIES_BY_DIRECTION.entrySet()) {
                VoxelShape shapeToAdd = switch (state.getValue(entry.getValue())) {
                    case NONE -> Shapes.empty();
                    case TALL -> map.get(entry.getKey());
                };

                voxelShape2 = Shapes.or(voxelShape2, shapeToAdd);
            }

            return voxelShape2;
        });
    }

    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return (VoxelShape)this.outlineShapeFunction.get(state);
    }

    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return (VoxelShape)this.collisionShapeFunction.get(state);
    }

    protected boolean isPathfindable(BlockState state, PathComputationType type) {
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
        BlockState blockState6 = (BlockState)this.defaultBlockState();
        return this.getStateWith(worldView, blockState6, blockPos6, blockState5, bl, bl2, bl3, bl4);
    }

    protected BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
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
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState floor = world.getBlockState(pos.below());
        return floor.is(BlockTags.DIRT) || floor.is(Blocks.FARMLAND) || (floor.getBlock() instanceof MazewoodBlock);
    }

    private static boolean isConnected(BlockState state, Property<MazewoodShape> property) {
        return state.getValue(property) != MazewoodShape.NONE;
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
        BlockState blockState = this.getStateWith(state, north, east, south, west, voxelShape);
        return (BlockState)blockState.setValue(UP, true);
    }

    private BlockState getStateWith(BlockState state, boolean north, boolean east, boolean south, boolean west, VoxelShape aboveShape) {
        return (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(NORTH_WALL_SHAPE, this.getMazewoodShape(north, aboveShape, (VoxelShape)WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.NORTH)))).setValue(EAST_WALL_SHAPE, this.getMazewoodShape(east, aboveShape, (VoxelShape)WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.EAST)))).setValue(SOUTH_WALL_SHAPE, this.getMazewoodShape(south, aboveShape, (VoxelShape)WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.SOUTH)))).setValue(WEST_WALL_SHAPE, this.getMazewoodShape(west, aboveShape, (VoxelShape)WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.WEST)));
    }

    private MazewoodShape getMazewoodShape(boolean connected, VoxelShape aboveShape, VoxelShape tallShape) {
        if (connected) {
            return MazewoodShape.TALL;
        } else {
            return MazewoodShape.NONE;
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{UP, NORTH_WALL_SHAPE, EAST_WALL_SHAPE, WEST_WALL_SHAPE, SOUTH_WALL_SHAPE});
    }

    protected BlockState rotate(BlockState state, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(NORTH_WALL_SHAPE, (MazewoodShape)state.getValue(SOUTH_WALL_SHAPE))).setValue(EAST_WALL_SHAPE, (MazewoodShape)state.getValue(WEST_WALL_SHAPE))).setValue(SOUTH_WALL_SHAPE, (MazewoodShape)state.getValue(NORTH_WALL_SHAPE))).setValue(WEST_WALL_SHAPE, (MazewoodShape)state.getValue(EAST_WALL_SHAPE));
            }
            case COUNTERCLOCKWISE_90 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(NORTH_WALL_SHAPE, (MazewoodShape)state.getValue(EAST_WALL_SHAPE))).setValue(EAST_WALL_SHAPE, (MazewoodShape)state.getValue(SOUTH_WALL_SHAPE))).setValue(SOUTH_WALL_SHAPE, (MazewoodShape)state.getValue(WEST_WALL_SHAPE))).setValue(WEST_WALL_SHAPE, (MazewoodShape)state.getValue(NORTH_WALL_SHAPE));
            }
            case CLOCKWISE_90 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(NORTH_WALL_SHAPE, (MazewoodShape)state.getValue(WEST_WALL_SHAPE))).setValue(EAST_WALL_SHAPE, (MazewoodShape)state.getValue(NORTH_WALL_SHAPE))).setValue(SOUTH_WALL_SHAPE, (MazewoodShape)state.getValue(EAST_WALL_SHAPE))).setValue(WEST_WALL_SHAPE, (MazewoodShape)state.getValue(SOUTH_WALL_SHAPE));
            }
            default -> {
                return state;
            }
        }
    }

    protected BlockState mirror(BlockState state, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT -> {
                return (BlockState)((BlockState)state.setValue(NORTH_WALL_SHAPE, (MazewoodShape)state.getValue(SOUTH_WALL_SHAPE))).setValue(SOUTH_WALL_SHAPE, (MazewoodShape)state.getValue(NORTH_WALL_SHAPE));
            }
            case FRONT_BACK -> {
                return (BlockState)((BlockState)state.setValue(EAST_WALL_SHAPE, (MazewoodShape)state.getValue(WEST_WALL_SHAPE))).setValue(WEST_WALL_SHAPE, (MazewoodShape)state.getValue(EAST_WALL_SHAPE));
            }
            default -> {
                return super.mirror(state, mirror);
            }
        }
    }

    static {
        UP = BlockStateProperties.UP;
        EAST_WALL_SHAPE = EnumProperty.create("east", MazewoodShape.class);
        NORTH_WALL_SHAPE = EnumProperty.create("north", MazewoodShape.class);
        SOUTH_WALL_SHAPE = EnumProperty.create("south", MazewoodShape.class);
        WEST_WALL_SHAPE = EnumProperty.create("west", MazewoodShape.class);
        WALL_SHAPE_PROPERTIES_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(Direction.NORTH, NORTH_WALL_SHAPE, Direction.EAST, EAST_WALL_SHAPE, Direction.SOUTH, SOUTH_WALL_SHAPE, Direction.WEST, WEST_WALL_SHAPE)));
        // TODO
        WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION = Map.of(
                Direction.NORTH, Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 9.0D),
                Direction.SOUTH, Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 9.0D),
                Direction.EAST, Block.box(0.0D, 0.0D, 6.0D, 9.0D, 16.0D, 10.0D),
                Direction.WEST, Block.box(0.0D, 0.0D, 6.0D, 9.0D, 16.0D, 10.0D)
        );
    }
}