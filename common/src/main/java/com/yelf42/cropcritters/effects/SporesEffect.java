package com.yelf42.cropcritters.effects;

import com.yelf42.cropcritters.registry.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.blocks.StrangleFern;
import com.yelf42.cropcritters.registry.ModParticles;

public class SporesEffect extends MobEffect {
    public SporesEffect(MobEffectCategory category, int color) {
        super(category, color, ColorParticleOption.create(ModParticles.SPORES, ARGB.color(100, color)));
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        BlockState toPlant = ModBlocks.STRANGLE_FERN.defaultBlockState();
        if (entity instanceof LivingEntity) {
            BlockPos pos = BlockPos.containing(entity.position().add(0.0, 0.5, 0.0));

            boolean canPlant = StrangleFern.canInfest(world.getBlockState(pos));
            if (canPlant) {
                world.setBlockAndUpdate(pos, toPlant);
                reduceDuration(entity);
            }
            if (!world.getFluidState(pos).isEmpty()) {
                entity.removeEffect(ModEffects.SPORES);
            }
        }

        return super.applyEffectTick(world, entity, amplifier);
    }

    private void reduceDuration(LivingEntity entity) {
        int duration = entity.getEffect(ModEffects.SPORES).getDuration();
        entity.removeEffect(ModEffects.SPORES);
        if (duration > 1300) {
            entity.addEffect(new MobEffectInstance(ModEffects.SPORES, duration - 1200, 1, true, true, false));
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
