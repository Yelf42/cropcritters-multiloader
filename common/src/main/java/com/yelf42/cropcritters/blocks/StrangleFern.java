package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
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
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.registry.ModEffects;
import org.jetbrains.annotations.Nullable;

public class StrangleFern extends BaseEntityBlock implements BonemealableBlock {

    public static final MapCodec<StrangleFern> CODEC = simpleCodec(StrangleFern::new);
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public StrangleFern(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), 0));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public MapCodec<? extends StrangleFern> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return ModBlocks.column(12, -1, 5 * Math.min(this.getAge(state), 2) + 4);
    }

    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return MAX_AGE;
    }

    public int getAge(BlockState state) {
        return state.getValue(this.getAgeProperty());
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    public static boolean canInfest(BlockState toCheck) {
        boolean tall = toCheck.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF);
        boolean crop = toCheck.getBlock() instanceof CropBlock;
        boolean canSpread = toCheck.is(CropCritters.SPORES_INFECT);
        boolean weed = toCheck.is(CropCritters.WEEDS);
        return ((!tall && !weed) && (crop || canSpread));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState floor = world.getBlockState(pos.below());
        return floor.is(BlockTags.DIRT);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
        return !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, world, pos, facingPos);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        if (canInfest(ctx.getLevel().getBlockState(ctx.getClickedPos()))) {
            return super.getStateForPlacement(ctx);
        }
        return null;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !isMature(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!isMature(state) && random.nextInt(2) == 0) {
            ageUp(state,world,pos);
        }
    }

    // Increase age, kill host if matured (maybe replace dead_bush with smth smaller)
    private void ageUp(BlockState state, ServerLevel world, BlockPos pos) {
        int newAge = this.getAge(state) + 1;
        world.setBlock(pos, state.setValue(AGE, newAge), 3);
        if (newAge == this.getMaxAge() && world.random.nextInt(3) == 0) {
            StrangleFernBlockEntity sfbe = (StrangleFernBlockEntity) world.getBlockEntity(pos);
            if (sfbe != null) sfbe.setInfestedState(Blocks.DEAD_BUSH.defaultBlockState());
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level instanceof ServerLevel
                && isMature(state)
                && entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(ModEffects.NATURAL_SPORES.get());
            level.setBlock(pos, state.setValue(AGE, this.getMaxAge() - 1), 3);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        StrangleFernBlockEntity sfbe = (StrangleFernBlockEntity) world.getBlockEntity(pos);
        if (sfbe != null && !oldState.is(this)) {
            if (!canInfest(oldState)) oldState = Blocks.SHORT_GRASS.defaultBlockState();
            sfbe.setInfestedState(oldState);
        }
        super.onPlace(state, world, pos, oldState, notify);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StrangleFernBlockEntity(pos, state);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return !isMature(state);
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return (double)random.nextFloat() < 0.4;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        ageUp(state,world,pos);
    }
}
