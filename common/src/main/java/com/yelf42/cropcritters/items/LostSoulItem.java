package com.yelf42.cropcritters.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import com.yelf42.cropcritters.config.CritterHelper;
import com.yelf42.cropcritters.entity.AbstractCropCritterEntity;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.Optional;

public class LostSoulItem extends Item {

    public LostSoulItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) return InteractionResult.PASS;
        ServerLevel world = (ServerLevel) context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState state = world.getBlockState(blockPos);
        ItemStack itemStack = context.getItemInHand();
        Player playerEntity = context.getPlayer();

        // Create slime
        if (state.is(Blocks.SLIME_BLOCK)) {
            if (world.random.nextInt(2) == 0) {
                world.sendParticles(ParticleTypes.SMOKE, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            } else {
                world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                Slime slime = EntityType.SLIME.create(world);
                slime.setSize(2, true);
                slime.setPos(blockPos.getBottomCenter());
                world.addFreshEntity(slime);
                world.playSound(null, blockPos, ModSounds.SPAWN_SLIME, SoundSource.BLOCKS, 1F, 1F);
                world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            }
            if (playerEntity instanceof ServerPlayer serverPlayerEntity) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, blockPos, itemStack);
            }
            itemStack.consume(1, playerEntity);
            return InteractionResult.SUCCESS;
        }

        // Critter spawning logic
        Optional<AbstractCropCritterEntity> toSpawn = CritterHelper.spawnCritterWithItem(world, state);
        if (toSpawn.isEmpty()) return InteractionResult.PASS;
        AbstractCropCritterEntity critter = toSpawn.get();
        int failChance = (critter.getMaxHealth() > 12) ? 80 : 60;
        if (world.random.nextInt(100) + 1 <= failChance) {
            world.sendParticles(ParticleTypes.SMOKE, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
        } else {
            BlockPos toSpawnAt = blockPos;
            if (state.getOptionalValue(PitcherCropBlock.HALF).orElse(DoubleBlockHalf.LOWER) == DoubleBlockHalf.UPPER) {
                world.setBlock(blockPos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                toSpawnAt = blockPos.below();
            }
            world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            critter.setPos(toSpawnAt.getBottomCenter());
            world.addFreshEntity(critter);
            world.playSound(null, blockPos, ModSounds.SPAWN_CRITTER, SoundSource.BLOCKS, 1F, 1F);
            world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            if (playerEntity instanceof ServerPlayer serverPlayerEntity) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, blockPos, itemStack);
            }
            itemStack.consume(1, playerEntity);
        }
        return InteractionResult.SUCCESS;
    }
}
