package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.ticks.TickPriority;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.area_affectors.TypedBlockArea;

import java.util.Collection;

public class TrimmedSoulRoseBlock extends BushBlock {
    public static final MapCodec<TrimmedSoulRoseBlock> CODEC = simpleCodec(TrimmedSoulRoseBlock::new);
    private static final VoxelShape SHAPE = ModBlocks.column(8.0F, 0.0F, 8.0F);

    public MapCodec<? extends TrimmedSoulRoseBlock> codec() {
        return CODEC;
    }

    public TrimmedSoulRoseBlock(Properties settings) {
        super(settings);
    }

    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.is(BlockTags.DIRT) || blockState.isFaceSturdy(world, pos, Direction.UP, SupportType.CENTER);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        AffectorPositions affectorPositions = CropCritters.getAffectorPositions(world);
        Collection<? extends TypedBlockArea> affectorsInSection = affectorPositions.getAffectorsInSection(pos);
        if (!affectorsInSection.isEmpty()) {
            for (TypedBlockArea typedBlockArea : affectorsInSection) {
                if (typedBlockArea.blockArea().isPositionInside(pos)) {
                    world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            pos.getX() + 0.5,
                            pos.getY() + 0.55,
                            pos.getZ() + 0.5,
                            1,
                            0.0, 0.0, 0.0,
                            0.0
                    );
                    world.scheduleTick(pos, ModBlocks.TRIMMED_SOUL_ROSE, 20 + random.nextInt(60), TickPriority.EXTREMELY_LOW);
                }
            }
        } else {
            world.scheduleTick(pos, ModBlocks.TRIMMED_SOUL_ROSE, 400, TickPriority.EXTREMELY_LOW);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleTick(pos, ModBlocks.TRIMMED_SOUL_ROSE, 40, TickPriority.EXTREMELY_LOW);
        super.onPlace(state, world, pos, oldState, notify);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        world.scheduleTick(pos, ModBlocks.TRIMMED_SOUL_ROSE, 40, TickPriority.EXTREMELY_LOW);
    }

}