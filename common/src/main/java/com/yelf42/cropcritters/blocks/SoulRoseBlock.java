package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModBlockEntities;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
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
import net.minecraft.world.level.ScheduledTickAccess;
import org.jspecify.annotations.Nullable;

public class SoulRoseBlock extends BaseEntityBlock {
    public static final MapCodec<SoulRoseBlock> CODEC = simpleCodec(SoulRoseBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    public static final EnumProperty<SoulRoseType> TYPE = EnumProperty.create("type", SoulRoseType.class);

    private static final VoxelShape SMALL_0_SHAPE = Block.column((double)6.0F, (double)0.0F, (double)11.0F);
    private static final VoxelShape SMALL_1_SHAPE = Block.column((double)10.0F, (double)0.0F, (double)16.0F);
    private static final VoxelShape LARGE_SHAPE = Block.column((double)14.0F, (double)0.0F, (double)16.0F);

    public SoulRoseBlock(Properties settings) {
        super(settings);
        this.registerDefaultState((this.stateDefinition.any()).setValue(HALF, DoubleBlockHalf.LOWER).setValue(TYPE, SoulRoseType.NONE));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValueOrElse(HALF, DoubleBlockHalf.UPPER) == DoubleBlockHalf.LOWER) {
            return new SoulRoseBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.SOUL_ROSE, world.isClientSide() ? SoulRoseBlockEntity::clientTick : SoulRoseBlockEntity::serverTick);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch(state.getValueOrElse(LEVEL, 0)) {
            case 0 -> SMALL_0_SHAPE;
            case 1 -> SMALL_1_SHAPE;
            default -> LARGE_SHAPE;
        };
    }

    public static boolean isDoubleTallAtLevel(int level) {
        return level >= 2;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos blockPos = ctx.getClickedPos();
        Level world = ctx.getLevel();
        return blockPos.getY() < world.getMaxY() && world.getBlockState(blockPos.above()).canBeReplaced(ctx) ? super.getStateForPlacement(ctx) : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (isDoubleTallAtLevel(state.getValue(LEVEL))) {
            DoubleBlockHalf doubleBlockHalf = state.getValue(HALF);
            if (direction.getAxis() != Direction.Axis.Y || doubleBlockHalf == DoubleBlockHalf.LOWER != (direction == Direction.UP) || neighborState.is(this) && neighborState.getValue(HALF) != doubleBlockHalf) {
                return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
            } else {
                return Blocks.AIR.defaultBlockState();
            }
        } else {
            return state.canSurvive(world, pos) ? state : Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.below());
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return below.is(Blocks.SOUL_SOIL) || below.is(Blocks.SOUL_SAND);
        } else {
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
        builder.add(LEVEL);
        builder.add(TYPE);
    }
}
