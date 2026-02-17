package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import com.yelf42.cropcritters.config.WeedHelper;


public class LiverwortBlock extends MultifaceBlock implements BonemealableBlock {
    public static final MapCodec<LiverwortBlock> CODEC = simpleCodec(LiverwortBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final MultifaceSpreader grower = new MultifaceSpreader(new LiverwortGrowChecker(this));

    public static final BooleanProperty CAN_SPREAD = BooleanProperty.create("can_spread");

    public MapCodec<LiverwortBlock> codec() {
        return CODEC;
    }

    public LiverwortBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (super.canSurvive(state, world, pos)) return true;

        boolean bl = false;
        for(Direction direction : DIRECTIONS) {
            if (hasFace(state, direction)) {
                if (!world.getBlockState(pos.relative(direction)).is(BlockTags.DIRT)) {
                    return false;
                }
                bl = true;
            }
        }
        return bl;
    }

    @Override
    public boolean isValidStateForPlacement(BlockGetter world, BlockState state, BlockPos pos, Direction direction) {
        if (super.isValidStateForPlacement(world, state, pos, direction)) return true;
        if (this.isFaceSupported(direction) && (!state.is(this) || !hasFace(state, direction))) {
            BlockPos blockPos = pos.relative(direction);
            return world.getBlockState(blockPos).is(BlockTags.DIRT);
        } else {
            return false;
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state.setValue(CAN_SPREAD, state.getValue(CAN_SPREAD) || !facingState.is(ModBlocks.LIVERWORT)), facing, facingState, world, currentPos, facingPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        // Don't grow if unsuitable temperatures
        float temp = world.getBiome(pos).value().getBaseTemperature();
        if (temp > 0.81 || temp < 0.79) return;

        // Dry out in sunlight
        long time = world.getDayTime() % 24000;
        if (state.getFluidState().isEmpty()
                && (time <= 8000 && time >= 4000)
                && world.getBrightness(LightLayer.SKY, pos) >= 15
                && !world.isRainingAt(pos)) {
            world.blockEvent(pos, this, 0, 0);
            return;
        }

        // Dry out in nether (any dimension where water evaporates)
        if (world.dimensionType().ultraWarm()) {
            world.blockEvent(pos, this, 0, 0);
            return;
        }

        if (state.getOptionalValue(CAN_SPREAD).isEmpty()) {
            if (world.isRaining() && random.nextInt(6) == 1) world.setBlockAndUpdate(pos, state.setValue(CAN_SPREAD, true));
            return;
        }

        // Calculate neighbours, return if too many
        int neighbouringWeeds = -1;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                BlockPos checkPos = pos.offset(i, 0, j);
                BlockState checkState = world.getBlockState(checkPos);
                if (checkState.is(this)) neighbouringWeeds++;
            }
        }
        if (neighbouringWeeds > 3 || random.nextInt(16) == 0) {
            world.setBlockAndUpdate(pos, state.setValue(CAN_SPREAD, false));
            return;
        }

        // Rain growth
        if (world.isRainingAt(pos) || world.isRainingAt(pos.relative(Direction.getRandom(random)))) {
            if (random.nextInt(2) == 0 && isValidBonemealTarget(world, pos, state)) this.grower.spreadFromRandomFaceTowardRandomDirection(state, world, pos, random);
            return;
        }

        // Waterlogged growth
        if (!state.getFluidState().isEmpty() && world.getBrightness(LightLayer.SKY, pos) >= 14) {
            if (random.nextInt(4) == 0 && isValidBonemealTarget(world, pos, state)) this.grower.spreadFromRandomFaceTowardRandomDirection(state, world, pos, random);
            return;
        }

        // Moist farmland
        BlockState soil = world.getBlockState(pos.below());
        if (soil.is(ModBlocks.SOUL_FARMLAND) || (soil.is(Blocks.FARMLAND) && soil.getOptionalValue(FarmBlock.MOISTURE).orElse(0) > 5)) {
            if (isValidBonemealTarget(world, pos, state)) this.grower.spreadFromRandomFaceTowardRandomDirection(state, world, pos, random);
            return;
        }

        // Dripping growth / death
        BlockPos blockPos = PointedDripstoneBlock.findStalactiteTipAboveCauldron(world, pos);
        if (blockPos != null) {
            Fluid fluid = PointedDripstoneBlock.getCauldronFillFluidType(world, blockPos);
            if (fluid == Fluids.WATER && isValidBonemealTarget(world, pos, state)) {
                this.grower.spreadFromRandomFaceTowardRandomDirection(state, world, pos, random);
            } else if (fluid == Fluids.LAVA) {
                world.blockEvent(pos, this, 0, 0);
            }
        }

    }

    @Override
    protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int type, int data) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        for(int l = 0; l < 8; ++l) {
            world.addParticle(ParticleTypes.WHITE_SMOKE, (double)((float)i + world.random.nextFloat()), (double)((float)j + world.random.nextFloat()), (double)((float)k + world.random.nextFloat()), (double)0.0F, (double)0.0F, (double)0.0F);
        }
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
        builder.add(CAN_SPREAD);
    }

    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return Direction.stream().anyMatch((direction) -> this.grower.canSpreadInAnyDirection(state, world, pos, direction.getOpposite()));
    }

    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        this.grower.spreadFromRandomFaceTowardRandomDirection(state, world, pos, random);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    public MultifaceSpreader getSpreader() {
        return this.grower;
    }

    static class LiverwortGrowChecker extends MultifaceSpreader.DefaultSpreaderConfig {

        public LiverwortGrowChecker(MultifaceBlock lichen) {
            super(lichen);
        }

        @Override
        protected boolean stateCanBeReplaced(BlockGetter world, BlockPos pos, BlockPos growPos, Direction direction, BlockState state) {
            return super.stateCanBeReplaced(world, pos, growPos, direction, state) || WeedHelper.canWeedsReplace(state);
        }
    }

}
