package com.yelf42.cropcritters.effects;

import com.yelf42.cropcritters.registry.ModParticles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.area_affectors.AffectorType;
import com.yelf42.cropcritters.area_affectors.TypedBlockArea;
import com.yelf42.cropcritters.config.CritterHelper;

import java.util.Collection;

public class SoulSiphonEffect extends MobEffect {

    public SoulSiphonEffect(MobEffectCategory statusEffectCategory, int i) {
        super(statusEffectCategory, i);
    }

    private void growCropsAndCritters(ServerLevel world, BlockPos pos, int amplifier) {
        Iterable<BlockPos> iterable = BlockPos.withinManhattan(pos, amplifier, 1, amplifier);
        for(BlockPos blockPos : iterable) {
            if (world.random.nextInt(2) == 0) continue;

            BlockState blockState = world.getBlockState(blockPos);
            if (!(blockState.getBlock() instanceof BushBlock)) continue;

            if (blockState.getBlock() instanceof BonemealableBlock fertilizable) {
                if (fertilizable.isValidBonemealTarget(world, blockPos, blockState, false)) {
                    if (world instanceof ServerLevel) {
                        if (fertilizable.isBonemealSuccess(world, world.random, blockPos, blockState)) {
                            fertilizable.performBonemeal(world, world.random, blockPos, blockState);
                        }
                    }
                } else {
                    if ((blockState.getBlock() instanceof CropBlock) || (blockState.getBlock() instanceof PitcherCropBlock)) {
                        CritterHelper.spawnCritter(world, blockState, world.random, blockPos);
                    }
                }
            } else {
                if (blockState.is(Blocks.NETHER_WART) && blockState.getOptionalValue(NetherWartBlock.AGE).orElse(0) == NetherWartBlock.MAX_AGE) {
                    CritterHelper.spawnCritter(world, blockState, world.random, blockPos);
                }
            }
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        int i = 42 / amplifier;
        boolean shouldApplyEffect = i <= 0 || entity.tickCount % i == 0;

        if (entity.level() instanceof ServerLevel serverLevel) {
            if (entity.getRandom().nextInt(5) == 0) {
                serverLevel.sendParticles(
                        ModParticles.SOUL_SIPHON,
                        entity.getRandomX(0.5),
                        entity.getRandomY(),
                        entity.getRandomZ(0.5),
                        1, 0, 0, 0, 0
                );
            }

            if (shouldApplyEffect) {
                if (entity.getType().is(CropCritters.UNDEAD) && entity.getHealth() <= 3.0f) {
                    Vec3 entityPos = entity.position();
                    BlockPos pos = new BlockPos((int) entityPos.x, (int) Math.floor(entityPos.y + 0.5F), (int) entityPos.z);
                    AffectorPositions affectorPositions = CropCritters.getAffectorPositions(serverLevel);
                    Collection<? extends TypedBlockArea> affectorsInSection = affectorPositions.getAffectorsInSection(pos);
                    if (!affectorsInSection.isEmpty()) {
                        for (TypedBlockArea typedBlockArea : affectorsInSection) {
                            AffectorType type = typedBlockArea.type();
                            if (type == AffectorType.SOUL_ROSE_GOLD_3 || type == AffectorType.SOUL_ROSE_GOLD_2 || type == AffectorType.SOUL_ROSE_GOLD_1) {
                                if (typedBlockArea.blockArea().isPositionInside(pos)) {
                                    growCropsAndCritters(serverLevel, pos, amplifier);
                                    break;
                                }
                            }
                        }
                    }
                    entity.kill();
                } else {
                    entity.hurt(entity.damageSources().magic(), 3.0F);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
