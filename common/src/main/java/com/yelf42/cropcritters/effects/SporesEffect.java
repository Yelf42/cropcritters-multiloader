package com.yelf42.cropcritters.effects;

import com.yelf42.cropcritters.registry.ModEffects;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.blocks.StrangleFern;
import com.yelf42.cropcritters.registry.ModParticles;

public class SporesEffect extends MobEffect {
    public SporesEffect(MobEffectCategory category, int color) {
        super(category, color, ColorParticleOption.create(ModParticles.SPORES, FastColor.ABGR32.color(100, color)));
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Level world = entity.level();
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

        return super.applyEffectTick(entity, amplifier);
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
