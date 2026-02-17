package com.yelf42.cropcritters.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import com.google.common.collect.ImmutableMap.Builder;
import org.jetbrains.annotations.Nullable;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.blocks.SoulRoseBlock;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.Map;
import java.util.Optional;

public class StrangeFertilizerItem extends BoneMealItem {
    protected static final Map<Block, Block> REVIVE_CORAL = new Builder<Block, Block>()
            .put(Blocks.DEAD_TUBE_CORAL_BLOCK, Blocks.TUBE_CORAL_BLOCK)
            .put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, Blocks.TUBE_CORAL_WALL_FAN)
            .put(Blocks.DEAD_TUBE_CORAL_FAN, Blocks.TUBE_CORAL_FAN)
            .put(Blocks.DEAD_TUBE_CORAL, Blocks.TUBE_CORAL)
            .put(Blocks.DEAD_BRAIN_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK)
            .put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN)
            .put(Blocks.DEAD_BRAIN_CORAL_FAN, Blocks.BRAIN_CORAL_FAN)
            .put(Blocks.DEAD_BRAIN_CORAL, Blocks.BRAIN_CORAL)
            .put(Blocks.DEAD_BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK)
            .put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN)
            .put(Blocks.DEAD_BUBBLE_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN)
            .put(Blocks.DEAD_BUBBLE_CORAL, Blocks.BUBBLE_CORAL)
            .put(Blocks.DEAD_FIRE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK)
            .put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN)
            .put(Blocks.DEAD_FIRE_CORAL_FAN, Blocks.FIRE_CORAL_FAN)
            .put(Blocks.DEAD_FIRE_CORAL, Blocks.FIRE_CORAL)
            .put(Blocks.DEAD_HORN_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK)
            .put(Blocks.DEAD_HORN_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN)
            .put(Blocks.DEAD_HORN_CORAL_FAN, Blocks.HORN_CORAL_FAN)
            .put(Blocks.DEAD_HORN_CORAL, Blocks.HORN_CORAL)
            .build();


    public StrangeFertilizerItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockPos blockPos2 = blockPos.relative(context.getClickedFace());
        BlockState blockState = world.getBlockState(blockPos);
        Player playerEntity = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        // Use on the ground
        boolean bl = blockState.isFaceSturdy(world, blockPos, context.getClickedFace());
        if (bl && useOnGround(context.getItemInHand(), world, blockPos, blockPos2, context.getClickedFace())) {
            if (!world.isClientSide()) {
                if (playerEntity != null) playerEntity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                world.levelEvent(1505, blockPos2, 15);
            }

            return InteractionResult.SUCCESS;
        }

        // Use on fertilizable things
        if (growCrop(context.getItemInHand(), world, blockPos)) {
            if (!world.isClientSide()) {
                if (playerEntity != null) playerEntity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                world.levelEvent(1505, blockPos, 15);
            }

            return InteractionResult.SUCCESS;
        }

        // Revive Coral
        if (tryReviveCoral(context.getItemInHand(), world, blockPos, world.getBlockState(blockPos))) {
            if (playerEntity instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) playerEntity, blockPos, itemStack);
            }
            return InteractionResult.SUCCESS;
        }

        // Trimmed Soul Rose
        if (blockState.is(ModBlocks.SOUL_ROSE) && blockState.getOptionalValue(SoulRoseBlock.LEVEL).orElse(0) > 1) {
            if (!world.isClientSide()) {
                if (playerEntity != null) playerEntity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                world.levelEvent(1505, blockPos, 15);
                context.getItemInHand().shrink(1);
                Block.popResource(world, blockPos, new ItemStack(ModBlocks.TRIMMED_SOUL_ROSE));
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static boolean useOnGround(ItemStack stack, Level world, BlockPos blockPos, BlockPos underwaterPos, @Nullable Direction facing) {
        BlockState state = world.getBlockState(blockPos);
        BlockState underwaterState = world.getBlockState(underwaterPos);
        if (underwaterState.is(Blocks.WATER) && world.getFluidState(underwaterPos).getAmount() == 8) {
            useUnderwater(stack, world, underwaterPos, facing);
            return true;
        } else if (state.is(BlockTags.DIRT) || state.getBlock() instanceof BonemealableBlock fertilizable && fertilizable.isValidBonemealTarget(world, blockPos, state)) {
            useOnLand(stack, world, blockPos);
            return true;
        } else {
            return false;
        }
    }

    private static void useUnderwater(ItemStack stack, Level world, BlockPos blockPos, @Nullable Direction facing) {
        if (!(world instanceof ServerLevel)) return;

        blockPos = blockPos.relative(facing);
        RandomSource random = world.getRandom();

        label80:
        for (int i = 0; i < 128; i++) {
            BlockPos blockPos2 = blockPos;
            BlockState blockState = Blocks.SEAGRASS.defaultBlockState();

            for (int j = 0; j < i / 16; j++) {
                blockPos2 = blockPos2.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (world.getBlockState(blockPos2).isCollisionShapeFullBlock(world, blockPos2)) {
                    continue label80;
                }
            }

            if (i == 0 && facing != null && facing.getAxis().isHorizontal()) {
                blockState = (BlockState) BuiltInRegistries.BLOCK
                        .getRandomElementOf(BlockTags.WALL_CORALS, world.random)
                        .map(blockEntry -> ((Block)blockEntry.value()).defaultBlockState())
                        .orElse(blockState);
                if (blockState.hasProperty(BaseCoralWallFanBlock.FACING)) {
                    blockState = blockState.setValue(BaseCoralWallFanBlock.FACING, facing);
                }
            } else if (random.nextInt(2) == 0) {
                blockState = (BlockState) BuiltInRegistries.BLOCK
                        .getRandomElementOf(CropCritters.UNDERWATER_STRANGE_FERTILIZERS, world.random)
                        .map(blockEntry -> ((Block)blockEntry.value()).defaultBlockState())
                        .orElse(blockState);
            }

            if (blockState.is(BlockTags.WALL_CORALS, state -> state.hasProperty(BaseCoralWallFanBlock.FACING))) {
                for (int k = 0; !blockState.canSurvive(world, blockPos2) && k < 4; k++) {
                    blockState = blockState.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
                }
            }

            if (!blockState.is(CropCritters.IGNORE_STRANGE_FERTILIZERS) && blockState.canSurvive(world, blockPos2)) {
                BlockState blockState2 = world.getBlockState(blockPos2);
                if (blockState2.is(Blocks.WATER) && world.getFluidState(blockPos2).getAmount() == 8) {
                    world.setBlock(blockPos2, blockState, Block.UPDATE_ALL);
                } else if (random.nextInt(2) == 0 && !blockState2.is(BlockTags.SAPLINGS)) {
                    growCrop(stack, world, blockPos2);
                }
            }
        }

        stack.shrink(1);
    }

    private static void useOnLand(ItemStack stack, Level world, BlockPos blockPos) {
        if (!(world instanceof ServerLevel)) return;

        RandomSource random = world.getRandom();

        int successfulPlacements = 0;
        int maxAttempts = 128;
        int targetPlacements = 32;

        for (int attempt = 0; attempt < maxAttempts && successfulPlacements < targetPlacements; attempt++) {
            BlockPos blockPos2 = blockPos;

            int walkSteps = random.nextInt(5) + 1;

            for (int j = 0; j < walkSteps; j++) {
                blockPos2 = blockPos2.offset(
                        random.nextInt(3) - 1,
                        (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1
                );

                if (world.getBlockState(blockPos2).isCollisionShapeFullBlock(world, blockPos2)) {
                    blockPos2 = null;
                    break;
                }
            }

            if (blockPos2 == null) continue;

            BlockState blockState2 = world.getBlockState(blockPos2);
            if (blockState2.is(Blocks.AIR)) {
                BlockState toPlace;
                BlockState floor = world.getBlockState(blockPos2.below());

                if (floor.is(Blocks.CRIMSON_NYLIUM) || floor.is(Blocks.WARPED_NYLIUM)) {
                    toPlace = BuiltInRegistries.BLOCK
                            .getRandomElementOf(CropCritters.ON_NYLIUM_STRANGE_FERTILIZERS, world.random)
                            .map(blockEntry -> ((Block)blockEntry.value()).defaultBlockState())
                            .orElse(Blocks.NETHER_SPROUTS.defaultBlockState());
                } else if (floor.is(Blocks.MYCELIUM)) {
                    toPlace = BuiltInRegistries.BLOCK
                            .getRandomElementOf(CropCritters.ON_MYCELIUM_STRANGE_FERTILIZERS, world.random)
                            .map(blockEntry -> ((Block)blockEntry.value()).defaultBlockState())
                            .orElse(Blocks.BROWN_MUSHROOM.defaultBlockState());
                } else {
                    toPlace = BuiltInRegistries.BLOCK
                            .getRandomElementOf((random.nextInt(2) == 0) ? CropCritters.ON_LAND_RARE_STRANGE_FERTILIZERS : CropCritters.ON_LAND_COMMON_STRANGE_FERTILIZERS, world.random)
                            .map(blockEntry -> ((Block)blockEntry.value()).defaultBlockState())
                            .orElse(Blocks.SHORT_GRASS.defaultBlockState());
                }

                if (!toPlace.is(CropCritters.IGNORE_STRANGE_FERTILIZERS) && toPlace.canSurvive(world, blockPos2)) {
                    if (toPlace.getBlock() instanceof DoublePlantBlock) {
                        if (world.getBlockState(blockPos2.above()).is(Blocks.AIR)) {
                            DoublePlantBlock.placeAt(world, toPlace, blockPos2, 3);
                            successfulPlacements++;
                        }
                    } else {
                        world.setBlock(blockPos2, toPlace, Block.UPDATE_ALL);
                        successfulPlacements++;
                    }
                }
            }
        }

        stack.shrink(1);
    }

    public static boolean tryReviveCoral(ItemStack stack, Level world, BlockPos pos, BlockState state) {
        Optional<BlockState> optional = getRevivedState(state);
        if (optional.isPresent()) {
            world.setBlock(pos, (BlockState) optional.get(), Block.UPDATE_ALL_IMMEDIATE);
            world.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            world.playSound(null, pos, ModSounds.REVIVE_CORAL, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.shrink(1);
            return true;
        }
        return false;
    }

    private static Optional<BlockState> getRevivedState(BlockState state) {
        return Optional.ofNullable((Block)REVIVE_CORAL.get(state.getBlock()))
                .map(block -> {
                    BlockState revivedState = block.defaultBlockState();
                    //block.getDefaultState().with(Properties.WATERLOGGED, state.get(Properties.WATERLOGGED))
                    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                        revivedState = revivedState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
                    }

                    if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                        revivedState = revivedState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                    }

                    return revivedState;
                });
    }


}
