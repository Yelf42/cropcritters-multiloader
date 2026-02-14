package com.yelf42.cropcritters.blocks;

import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.ticks.TickPriority;
import com.yelf42.cropcritters.entity.TorchflowerCritterEntity;

import java.util.List;

public class TorchflowerSparkBlock extends AirBlock {

    public TorchflowerSparkBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        List<TorchflowerCritterEntity> list = world.getEntitiesOfClass(TorchflowerCritterEntity.class, new AABB(pos).inflate(2F), (torchflowerCritterEntity -> true));
        if (list.isEmpty()) {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        } else {
            world.scheduleTick(pos, ModBlocks.TORCHFLOWER_SPARK, 200, TickPriority.EXTREMELY_LOW);
        }
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);
        if (world.isClientSide()) {
            Vec3 p = pos.getCenter();
            world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 0F, 0F, 0F);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        world.scheduleTick(pos, ModBlocks.TORCHFLOWER_SPARK, 1, TickPriority.EXTREMELY_LOW);
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleTick(pos, ModBlocks.TORCHFLOWER_SPARK, 200, TickPriority.EXTREMELY_LOW);
        super.onPlace(state, world, pos, oldState, notify);
    }
}
