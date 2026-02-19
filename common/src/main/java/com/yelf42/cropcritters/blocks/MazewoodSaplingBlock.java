package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;
import com.yelf42.cropcritters.config.ConfigManager;

public class MazewoodSaplingBlock extends BaseEntityBlock implements BonemealableBlock {
    public static final MapCodec<MazewoodSaplingBlock> CODEC = simpleCodec(MazewoodSaplingBlock::new);
    private static final VoxelShape SHAPE = ModBlocks.column(8.0F, 0.0F,8.0F);;
    public static final IntegerProperty SPREAD = IntegerProperty.create("spread", 0, 128);

    public MazewoodSaplingBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(this.getSpreadProperty(), ConfigManager.CONFIG.mazewoodSpread));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MazewoodSaplingBlockEntity(pos, state);
    }

    @Override
    public MapCodec<? extends MazewoodSaplingBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected IntegerProperty getSpreadProperty() {
        return SPREAD;
    }

    public int getSpread(BlockState state) {
        return (Integer)state.getValue(this.getSpreadProperty());
    }

    public BlockState withSpread(int spread) {
        return this.defaultBlockState().setValue(this.getSpreadProperty(), spread);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MazewoodSaplingBlockEntity spreader) {
            spreader.tickScheduled();
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MazewoodSaplingBlockEntity spreader) {
            spreader.tickRandom(random);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SPREAD);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.below()).is(BlockTags.DIRT);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return (double)world.random.nextFloat() < 0.45;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MazewoodSaplingBlockEntity spreader) {
            spreader.tickRandom(random);
        }
    }
}
