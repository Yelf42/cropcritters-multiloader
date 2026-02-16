package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.ticks.TickPriority;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.events.WeedGrowNotifier;
import com.yelf42.cropcritters.registry.ModSounds;

public class BoneTrapBlock extends VegetationBlock {
    public static final MapCodec<BoneTrapBlock> CODEC = simpleCodec(BoneTrapBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    // 0 = open, 2 = closed, 1 = recharging
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 2);

    private static final VoxelShape[] SHAPES_BY_STAGE = new VoxelShape[] {
            Block.column(14,-1,2),
            Block.column(13,-1,4),
            Block.column(9,-1,9)
    };

    public BoneTrapBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES_BY_STAGE[this.getStage(state)];
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getClockWise());
    }

    public int getStage(BlockState state) {
        return (int)state.getValue(STAGE);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (this.getStage(state) == 2) {
            world.setBlockAndUpdate(pos, state.setValue(STAGE, 1));
            world.playSound(null, pos, ModSounds.BONE_TRAP_OPEN, SoundSource.BLOCKS, 0.5F, 1.0F + (world.random.nextFloat() * 0.6F - 0.3F));
            world.scheduleTick(pos, this, 100 + world.random.nextInt(60), TickPriority.EXTREMELY_LOW);
        } else {
            world.playSound(null, pos, ModSounds.BONE_TRAP_OPEN, SoundSource.BLOCKS, 0.5F, 1.0F + (world.random.nextFloat() * 0.6F - 0.3F));
            world.setBlockAndUpdate(pos, state.setValue(STAGE, 0));
        }
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler) {
        if (entity instanceof LivingEntity livingEntity) {
            double dist = livingEntity.position().distanceTo(pos.getBottomCenter());
            if (this.getStage(state) == 0 && dist <= 0.2F) {

                world.playSound(null, pos, ModSounds.BONE_TRAP_CLOSE, SoundSource.BLOCKS, 0.5F, 1.0F + (world.random.nextFloat() * 0.6F - 0.3F));

                if (world instanceof ServerLevel serverWorld) {
                    if (!(livingEntity.getType().is(CropCritters.WEED_IMMUNE))) livingEntity.hurtServer(serverWorld, world.damageSources().sweetBerryBush(), 4.0F);
                    serverWorld.scheduleTick(pos, this, 40 + serverWorld.random.nextInt(20), TickPriority.EXTREMELY_LOW);
                    serverWorld.setBlockAndUpdate(pos, state.setValue(STAGE, 2));
                }
            } else if (this.getStage(state) == 2) {
                Vec3 vec3d = new Vec3(0.01, 0.01, 0.01);
                livingEntity.makeStuckInBlock(state, vec3d);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        WeedGrowNotifier.notifyEvent(world, pos);
        super.onPlace(state, world, pos, oldState, notify);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        WeedGrowNotifier.notifyRemoval(world, pos);
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
        builder.add(FACING);
    }
}
