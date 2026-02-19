package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.*;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerPlayer;
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
import org.jetbrains.annotations.Nullable;

public class SoulPotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<SoulPotBlock> CODEC = simpleCodec(SoulPotBlock::new);
    public static final BooleanProperty CRACKED;
    public static final BooleanProperty WATERLOGGED;
    public static final IntegerProperty LEVEL;
    private static final VoxelShape SHAPE;

    public MapCodec<SoulPotBlock> codec() {
        return CODEC;
    }

    public SoulPotBlock(Properties settings) {
        super(settings);
        this.registerDefaultState((((this.stateDefinition.any())).setValue(WATERLOGGED, false)).setValue(CRACKED, false).setValue(LEVEL, 0));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
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
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
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
                    ModPackets.ParticleRingS2CPayload payload = new ModPackets.ParticleRingS2CPayload(center, 0.5F, 10, ModParticles.SOUL_GLOW);
                    ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);

                    for (ServerPlayer player : world.players()) {
                        if (center.closerThan(player.position(), 64)) {
                            player.connection.send(packet);
                        }
                    }
                }
            }
        }
    }



    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SoulPotBlockEntity soulPotBlockEntity) {
            if (world.isClientSide() || !stack.is(ModItems.LOST_SOUL)) {
                return ItemInteractionResult.SUCCESS;
            } else {
                ItemStack itemStack = soulPotBlockEntity.getTheItem();
                if (!stack.isEmpty() && (itemStack.isEmpty() || ItemStack.isSameItemSameComponents(itemStack, stack) && itemStack.getCount() < itemStack.getMaxStackSize())) {
                    soulPotBlockEntity.wobble(SoulPotBlockEntity.WobbleType.POSITIVE);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    ItemStack itemStack2 = stack.consumeAndReturn(1, player);
                    float f;
                    if (soulPotBlockEntity.isEmpty()) {
                        soulPotBlockEntity.setTheItem(itemStack2);
                        f = (float)itemStack2.getCount() / (float)itemStack2.getMaxStackSize();
                    } else {
                        soulPotBlockEntity.increaseStack();
                        f = (float)itemStack.getCount() / (float)itemStack.getMaxStackSize();
                    }

                    world.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * f);
                    if (world instanceof ServerLevel serverWorld) {
                        serverWorld.sendParticles(ModParticles.SOUL_GLINT_PLUME, (double)pos.getX() + (double)0.5F, (double)pos.getY() + 1.2, (double)pos.getZ() + (double)0.5F, 5, 0.0F, 0.0F, 0.0F, 0.0F);
                    }

                    soulPotBlockEntity.setChanged();
                    world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                    world.setBlock(pos, state.setValue(LEVEL, Math.clamp(soulPotBlockEntity.count(), 0, 12)), 3);
                    return ItemInteractionResult.SUCCESS;
                } else {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
            }
        } else {
            return ItemInteractionResult .PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }

    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity var7 = world.getBlockEntity(pos);
        if (var7 instanceof SoulPotBlockEntity soulPotBlockEntity) {
            world.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
            soulPotBlockEntity.wobble(SoulPotBlockEntity.WobbleType.NEGATIVE);
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, WATERLOGGED, CRACKED);
    }

    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulPotBlockEntity(pos, state);
    }

    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        Containers.dropContentsOnDestroy(state, newState, world, pos);
        super.onRemove(state, world, pos, newState, moved);
    }

    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        ItemStack itemStack = player.getMainHandItem();
        BlockState blockState = state;
        if (itemStack.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasTag(itemStack, EnchantmentTags.PREVENTS_DECORATED_POT_SHATTERING)) {
            blockState = state.setValue(CRACKED, true);
            world.setBlock(pos, blockState, 260);
        }

        return super.playerWillDestroy(world, pos, blockState, player);
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected SoundType getSoundType(BlockState state) {
        return state.getValue(CRACKED) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
    }

    protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (world instanceof ServerLevel serverWorld) {
            if (projectile.mayInteract(serverWorld, blockPos) && projectile.mayBreak(serverWorld)) {
                world.setBlock(blockPos, state.setValue(CRACKED, true), 260);
                world.destroyBlock(blockPos, true, projectile);
            }
        }

    }

    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    static {
        LEVEL = IntegerProperty.create("level", 0, 12);
        CRACKED = BlockStateProperties.CRACKED;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        SHAPE = ModBlocks.column(14.0F, 0.0F, 16.0F);
    }
}
