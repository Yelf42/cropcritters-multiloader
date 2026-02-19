package com.yelf42.cropcritters.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;
import com.yelf42.cropcritters.events.WeedGrowNotifier;
import com.yelf42.cropcritters.registry.ModParticles;
import com.yelf42.cropcritters.registry.ModSounds;

public class LostSoulInAJarBlock extends LanternBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LostSoulInAJarBlock(Properties settings) {
        super(settings);
        this.registerDefaultState((this.defaultBlockState().setValue(POWERED, false)));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        world.scheduleTick(pos, state.getBlock(), 15, TickPriority.EXTREMELY_LOW);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) world.setBlock(pos, state.setValue(POWERED, false), 3);
        if (WeedGrowNotifier.checkWeedsToRing(world, pos)) {
            world.setBlock(pos, state.setValue(POWERED, true), 3);
            ring(world, pos, random);
        }
        world.scheduleTick(pos, state.getBlock(), 15 + world.random.nextInt(30), TickPriority.EXTREMELY_LOW);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        double d = (double)pos.getX() + random.nextDouble() * (double)10.0F - (double)5.0F;
        double e = (double)pos.getY() - random.nextDouble() * (double)3.0F;
        double f = (double)pos.getZ() + random.nextDouble() * (double)10.0F - (double)5.0F;
        //world.addParticleClient(ParticleTypes.GLOW, d, world.getTopY(Heightmap.Type.WORLD_SURFACE, (int)d, (int)f), f, 0.0F, 1.0F, 0.0F);
        world.addParticle(ModParticles.LOST_SOUL_GLOW, d, e, f, 0.0F, 1.0F, 0.0F);
    }

    public void ring(Level world, BlockPos pos, RandomSource random) {
        world.playSound(null, pos, ModSounds.LOST_SOUL_JAR_CHIME, SoundSource.BLOCKS, 2.0f, 1.0f + 0.5f * (float)random.nextInt(7));

        world.blockEvent(pos, this, 0, random.nextInt(360));
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int type, int data) {
        Vec3 cPos = pos.getCenter();
        double angle = Math.toRadians((double)data);
        double planeOffset = 0.8f;
        double heightOffset = ((double)data / 360F) - 1.f;
        world.addParticle(ParticleTypes.NOTE, cPos.x() + Math.cos(angle) * planeOffset, cPos.y() + heightOffset, cPos.z() + Math.sin(angle) * planeOffset, 0.73f, 0.0F, 0.0F);

        return true;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return (Boolean)world.getBlockState(pos).getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

}
