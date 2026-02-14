package com.yelf42.cropcritters.effects;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.area_affectors.AffectorType;
import com.yelf42.cropcritters.area_affectors.TypedBlockArea;
import com.yelf42.cropcritters.config.CritterHelper;
import com.yelf42.cropcritters.registry.ModParticles;

import java.util.Collection;

public class SoulSiphonEffect extends MobEffect {

    public SoulSiphonEffect(MobEffectCategory statusEffectCategory, int i) {
        super(statusEffectCategory, i, ModParticles.SOUL_SIPHON);
    }

    public void onMobRemoved(ServerLevel world, LivingEntity entity, int amplifier, Entity.RemovalReason reason) {
        if (reason == Entity.RemovalReason.KILLED && entity.getType().is(EntityTypeTags.UNDEAD)) {
            Vec3 entityPos = entity.position();
            BlockPos pos = new BlockPos((int)entityPos.x, (int) Math.floor(entityPos.y + 0.5F), (int)entityPos.z);
            AffectorPositions affectorPositions = CropCritters.getAffectorPositions(world);
            Collection<? extends TypedBlockArea> affectorsInSection = affectorPositions.getAffectorsInSection(pos);
            if (!affectorsInSection.isEmpty()) {
                for (TypedBlockArea typedBlockArea : affectorsInSection) {
                    AffectorType type = typedBlockArea.type();
                    if (type == AffectorType.SOUL_ROSE_GOLD_3 || type == AffectorType.SOUL_ROSE_GOLD_2 || type == AffectorType.SOUL_ROSE_GOLD_1) {
                        if (typedBlockArea.blockArea().isPositionInside(pos)) {
                            growCropsAndCritters(world, pos, amplifier);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void growCropsAndCritters(ServerLevel world, BlockPos pos, int amplifier) {
        Iterable<BlockPos> iterable = BlockPos.withinManhattan(pos, amplifier, 1, amplifier);
        for(BlockPos blockPos : iterable) {
            if (world.random.nextInt(2) == 0) continue;

            BlockState blockState = world.getBlockState(blockPos);
            if (!(blockState.getBlock() instanceof VegetationBlock)) continue;

            if (blockState.getBlock() instanceof BonemealableBlock fertilizable) {
                if (fertilizable.isValidBonemealTarget(world, blockPos, blockState)) {
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
                if (blockState.is(Blocks.NETHER_WART) && blockState.getValueOrElse(NetherWartBlock.AGE, 0) == NetherWartBlock.MAX_AGE) {
                    CritterHelper.spawnCritter(world, blockState, world.random, blockPos);
                }
            }
        }
    }

    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        entity.hurtServer(world, entity.damageSources().magic(), 3.0F);
        return true;
    }

    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int i = 42 / amplifier;
        if (i > 0) {
            return duration % i == 0;
        } else {
            return true;
        }
    }
}
