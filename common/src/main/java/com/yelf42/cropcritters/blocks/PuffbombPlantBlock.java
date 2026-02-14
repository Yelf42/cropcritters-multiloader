package com.yelf42.cropcritters.blocks;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.util.random.WeightedList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.ticks.TickPriority;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.events.WeedGrowNotifier;
import com.yelf42.cropcritters.registry.ModFeatures;
import com.yelf42.cropcritters.registry.ModSounds;

public class PuffbombPlantBlock extends MushroomBlock {

    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    private static final VoxelShape[] SHAPES_BY_AGE = Block.boxes(2, age -> Block.column(5 + age * 4, -1.0, 5 + age * 4));

    private static final ResourceKey<ConfiguredFeature<?, ?>> FEATURE_KEY = ModFeatures.PUFFBOMB_BLOB_CONFIGURED_FEATURE;

    private static final ExplosionDamageCalculator BURST = new ExplosionDamageCalculator() {
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float power) {
            return false;
        }
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }
    };

    public PuffbombPlantBlock(Properties settings) {
        super(FEATURE_KEY, settings);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES_BY_AGE[this.getAge(state)];
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos blockPos = pos.below();
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.is(BlockTags.MUSHROOM_GROW_BLOCK) || blockState.is(BlockTags.DIRT)) {
            return true;
        } else {
            return world.getRawBrightness(pos, 0) < 13 && this.mayPlaceOn(blockState, world, blockPos);
        }
    }

    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return MAX_AGE;
    }

    public int getAge(BlockState state) {
        return (Integer)state.getValue(this.getAgeProperty());
    }

    public BlockState withAge(int age) {
        return this.defaultBlockState().setValue(this.getAgeProperty(), age);
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        switch(getAge(state)) {
            case 0:
                world.scheduleTick(pos, state.getBlock(), 40, TickPriority.EXTREMELY_LOW);
                break;
            case 1:
                world.playSound(null, pos, ModSounds.TICKING, SoundSource.BLOCKS, 0.2f, 0.8f + 0.05f * (float)random.nextInt(8));
                world.scheduleTick(pos, state.getBlock(), 20, TickPriority.EXTREMELY_LOW);
                break;
            case 2:
                world.playSound(null, pos, ModSounds.TICKING, SoundSource.BLOCKS, 0.2f, 0.8f + 0.05f * (float)random.nextInt(8));
                world.scheduleTick(pos, state.getBlock(), 10, TickPriority.EXTREMELY_LOW);
                break;
            default:
                CropCritters.LOGGER.info("Puffbomb age is returning a weird value");
                break;
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) return;
        if (isMature(state)) {
            performBonemeal(world, random, pos, state);
        } else {
            world.setBlock(pos, this.withAge(this.getAge(state) + 1), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        if (isMature(state)) {
            world.explode(null, null, BURST, pos.getX(), pos.getY(), pos.getZ(), 3F, false, Level.ExplosionInteraction.BLOCK, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, WeightedList.of(), ModSounds.PUFFBOMB_EXPLODE);
            super.performBonemeal(world, random, pos, state);
            return;
        }
        world.setBlock(pos, this.withAge(this.getAge(state) + 1), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        WeedGrowNotifier.notifyEvent(world, pos);
        world.scheduleTick(pos, state.getBlock(), 40, TickPriority.EXTREMELY_LOW);
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
    }
}
