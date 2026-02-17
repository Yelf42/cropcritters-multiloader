package com.yelf42.cropcritters.config;

import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.entity.AbstractCropCritterEntity;
import com.yelf42.cropcritters.registry.ModEntities;

import java.util.Optional;

// TODO
public class CritterHelper {

    public static Optional<AbstractCropCritterEntity> spawnCritterWithItem(ServerLevel world, BlockState state) {
        AbstractCropCritterEntity output = null;
        if (state.is(Blocks.PUMPKIN)) {
            output = ModEntities.PUMPKIN_CRITTER.create(world);
        } else if (state.is(Blocks.MELON)) {
            output = ModEntities.MELON_CRITTER.create(world);
        } else if (state.is(Blocks.COCOA) && state.getOptionalValue(CocoaBlock.AGE).orElse(0) >= 2) {
            output = ModEntities.COCOA_CRITTER.create(world);
        } else if (state.getBlock() instanceof BushBlock) {
            if (state.is(Blocks.PITCHER_PLANT) || (state.is(Blocks.PITCHER_CROP) && state.getOptionalValue(PitcherCropBlock.AGE).orElse(0) >= 4)) {
                output = ModEntities.PITCHER_CRITTER.create(world);
            } else if (state.is(Blocks.TORCHFLOWER)) {
                output = ModEntities.TORCHFLOWER_CRITTER.create(world);
            } else if (state.is(Blocks.NETHER_WART)) {
                for (int i = 0; i <= world.random.nextInt(3); i++) {
                    output = ModEntities.NETHER_WART_CRITTER.create(world);
                }
            } else if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
                if (state.is(Blocks.WHEAT)) {
                    output = ModEntities.WHEAT_CRITTER.create(world);
                } else if (state.is(Blocks.CARROTS)) {
                    output = ModEntities.CARROT_CRITTER.create(world);
                } else if (state.is(Blocks.POTATOES)) {
                    if (world.random.nextInt(100) + 1 < world.getDifficulty().getId() * 5) {
                        output = ModEntities.POISONOUS_POTATO_CRITTER.create(world);
                    } else {
                        output = ModEntities.POTATO_CRITTER.create(world);
                    }
                } else if (state.is(Blocks.BEETROOTS)) {
                    output = ModEntities.BEETROOT_CRITTER.create(world);
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        if (output == null) return Optional.empty();
        return Optional.of(output);
    }


    public static boolean spawnCritter(ServerLevel world, BlockState state, RandomSource random, BlockPos pos) {
        boolean bottomHalf = state.getOptionalValue(DoublePlantBlock.HALF).orElse(DoubleBlockHalf.LOWER) == DoubleBlockHalf.LOWER;
        if (!bottomHalf) return false;

        BlockState soulCheck = world.getBlockState(pos.below());
        boolean soulCheckBl = soulCheck.is(Blocks.SOUL_SOIL) || soulCheck.is(Blocks.SOUL_SAND) || soulCheck.is(ModBlocks.SOUL_FARMLAND);
        boolean airCheck = world.getBlockState(pos.above()).isAir();
        int spawnChance = ConfigManager.CONFIG.critterSpawnChance;
        spawnChance *= (soulCheckBl) ? 2 : 1;
        if (airCheck && random.nextInt(100) + 1 < spawnChance) {
            if (state.is(Blocks.WHEAT)) {
                ModEntities.WHEAT_CRITTER.spawn(world, pos, MobSpawnType.NATURAL);
            } else if (state.is(Blocks.CARROTS)) {
                ModEntities.CARROT_CRITTER.spawn(world, pos, MobSpawnType.NATURAL);
            } else if (state.is(Blocks.POTATOES)) {
                if (random.nextInt(100) + 1 < world.getDifficulty().getId() * 5) {
                    ModEntities.POISONOUS_POTATO_CRITTER.spawn(world, pos, MobSpawnType.NATURAL);
                } else {
                    ModEntities.POTATO_CRITTER.spawn(world, pos, MobSpawnType.NATURAL);
                }
            } else if (state.is(Blocks.NETHER_WART)) {
                for (int i = 0; i <= world.random.nextInt(3); i++) {
                    ModEntities.NETHER_WART_CRITTER.spawn(world, pos, MobSpawnType.NATURAL);
                }
            } else if (state.is(Blocks.BEETROOTS)) {
                ModEntities.BEETROOT_CRITTER.spawn(world, pos, MobSpawnType.NATURAL);
            } else if (state.is(Blocks.NETHER_WART)) {
                for (int i = 0; i <= world.random.nextInt(3); i++) {
                    ModEntities.NETHER_WART_CRITTER.spawn(world, pos, MobSpawnType.NATURAL);
                }
            } else if (state.is(Blocks.TORCHFLOWER_CROP)) {
                ModEntities.TORCHFLOWER_CRITTER.spawn(world, pos, MobSpawnType.NATURAL);
            } else if (state.is(Blocks.PITCHER_CROP)) {
                ModEntities.PITCHER_CRITTER.spawn(world, pos, MobSpawnType.NATURAL);
            }  else {
                return false;
            }
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            world.playSound(null, pos, SoundEvents.ALLAY_AMBIENT_WITH_ITEM, SoundSource.BLOCKS, 1F, 1F);
            world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);

            return true;
        }
        return false;
    }

}
