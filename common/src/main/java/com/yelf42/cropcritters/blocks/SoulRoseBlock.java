package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModBlockEntities;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

public class SoulRoseBlock extends BaseEntityBlock {
    public static final MapCodec<SoulRoseBlock> CODEC = simpleCodec(SoulRoseBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    public static final EnumProperty<SoulRoseType> TYPE = EnumProperty.create("type", SoulRoseType.class);

    private static final VoxelShape SMALL_0_SHAPE = ModBlocks.column((double)6.0F, (double)0.0F, (double)11.0F);
    private static final VoxelShape SMALL_1_SHAPE = ModBlocks.column((double)10.0F, (double)0.0F, (double)16.0F);
    private static final VoxelShape LARGE_SHAPE = ModBlocks.column((double)14.0F, (double)0.0F, (double)16.0F);

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
        if (state.getOptionalValue(HALF).orElse(DoubleBlockHalf.UPPER) == DoubleBlockHalf.LOWER) {
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
        return switch(state.getOptionalValue(LEVEL).orElse(0)) {
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
        return blockPos.getY() < world.getMaxBuildHeight() - 1 && world.getBlockState(blockPos.above()).canBeReplaced(ctx) ? super.getStateForPlacement(ctx) : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (isDoubleTallAtLevel(state.getValue(LEVEL))) {
            DoubleBlockHalf doubleBlockHalf = state.getValue(HALF);
            if (facing.getAxis() != Direction.Axis.Y || doubleBlockHalf == DoubleBlockHalf.LOWER != (facing == Direction.UP) || facingState.is(this) && facingState.getValue(HALF) != doubleBlockHalf) {
                return doubleBlockHalf == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(world, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, world, currentPos, facingPos);
            } else {
                return Blocks.AIR.defaultBlockState();
            }
        } else {
            return state.canSurvive(world, currentPos) ? state : Blocks.AIR.defaultBlockState();
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
