package com.yelf42.cropcritters.blocks;

import com.yelf42.cropcritters.platform.Services;
import com.yelf42.cropcritters.registry.*;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class SoulPotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty CRACKED;
    public static final BooleanProperty WATERLOGGED;
    public static final IntegerProperty LEVEL;
    private static final VoxelShape SHAPE;

    public SoulPotBlock(Properties settings) {
        super(settings);
        this.registerDefaultState((((this.stateDefinition.any())).setValue(WATERLOGGED, false)).setValue(CRACKED, false).setValue(LEVEL, 0));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return ((this.defaultBlockState()).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)).setValue(CRACKED, false).setValue(LEVEL, 0);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (world.getGameTime() % 3L == 0L && world.getBlockState(pos.above()).is(Blocks.POTTED_WITHER_ROSE)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SoulPotBlockEntity soulPotBlockEntity && soulPotBlockEntity.getTheItem().getCount() > 0) {
                world.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        pos.getX() + 0.5,
                        pos.getY() + 1.4,
                        pos.getZ() + 0.5,
                        0.0, 0.05, 0.0);
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockState above = world.getBlockState(pos.above());
        if (above.is(Blocks.POTTED_WITHER_ROSE)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SoulPotBlockEntity soulPotBlockEntity) {
                ItemStack inv = soulPotBlockEntity.getTheItem();
                if (inv != null && inv.is(ModItems.LOST_SOUL) && inv.getCount() >= 24) {
                    soulPotBlockEntity.setTheItem(inv.copyWithCount(inv.getCount() - 24));
                    world.setBlockAndUpdate(pos.above(), ModBlocks.POTTED_SOUL_ROSE.defaultBlockState());

                    world.playSound(null, pos.above(), ModSounds.WITHER_ROSE_CONVERT, SoundSource.BLOCKS);
                    world.playSound(null, pos.above(), ModSounds.WITHER_ROSE_CONVERT_EXTRA, SoundSource.BLOCKS);

                    Vec3 center = pos.above().getCenter();
                    Services.PLATFORM.sendParticleRingToNearbyPlayers(world, center, 0.5F, 10);
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof SoulPotBlockEntity soulPotBlockEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);

        if (!stack.is(ModItems.LOST_SOUL)) {
            // useWithoutItem behaviour
            if (!world.isClientSide()) {
                world.playSound(null, pos, ModSounds.SOUL_POT_FAIL_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
                world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return InteractionResult.SUCCESS;
        }

        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack itemStack = soulPotBlockEntity.getTheItem();
        if (!stack.isEmpty() && (itemStack.isEmpty() || itemStack.getCount() < itemStack.getMaxStackSize())) {
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

            // consumeAndReturn equivalent
            ItemStack inserted = stack.copy();
            inserted.setCount(1);
            if (!player.isCreative()) {
                stack.shrink(1);
            }

            float f;
            if (soulPotBlockEntity.isEmpty()) {
                soulPotBlockEntity.setTheItem(inserted);
                f = (float) inserted.getCount() / (float) inserted.getMaxStackSize();
            } else {
                soulPotBlockEntity.increaseStack();
                f = (float) itemStack.getCount() / (float) itemStack.getMaxStackSize();
            }

            world.playSound(null, pos, ModSounds.SOUL_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * f);
            if (world instanceof ServerLevel serverWorld) {
                serverWorld.sendParticles(ModParticles.SOUL_GLINT_PLUME,
                        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                        5, 0.0, 0.0, 0.0, 0.0);
            }

            soulPotBlockEntity.setChanged();
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, WATERLOGGED, CRACKED);
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulPotBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SoulPotBlockEntity soulPotBlockEntity) {
                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), soulPotBlockEntity.getTheItem());
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        ItemStack itemStack = player.getMainHandItem();
        BlockState blockState = state;
        if (itemStack.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasSilkTouch(itemStack)) {
            blockState = state.setValue(CRACKED, true);
            level.setBlock(pos, blockState, 4);
        }

        super.playerWillDestroy(level, pos, blockState, player);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public SoundType getSoundType(BlockState state) {
        return state.getValue(CRACKED) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
    }

    public void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (world instanceof ServerLevel serverWorld) {
            if (projectile.mayInteract(serverWorld, blockPos)) {
                world.setBlock(blockPos, state.setValue(CRACKED, true), 260);
                world.destroyBlock(blockPos, true, projectile);
            }
        }

    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    static {
        LEVEL = IntegerProperty.create("level", 0, 12);
        CRACKED = BlockStateProperties.CRACKED;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        SHAPE = ModBlocks.column(14.0F, 0.0F, 16.0F);
    }
}
