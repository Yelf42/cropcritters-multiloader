package com.yelf42.cropcritters.effects;

import com.yelf42.cropcritters.registry.ModEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import com.yelf42.cropcritters.registry.ModSounds;

public class PuffbombPoisoningEffect extends MobEffect {

    private static final ExplosionDamageCalculator POP = new ExplosionDamageCalculator() {
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float power) {
            return false;
        }
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return true;
        }
    };

    public PuffbombPoisoningEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        int duration = entity.getEffect(ModEffects.PUFFBOMB_POISONING).getDuration();
        if (duration <= 20) {
            BlockPos pos = BlockPos.containing(entity.position());
            world.explode(null, null, POP, pos.getX(), pos.getY(), pos.getZ(), 4F, false, Level.ExplosionInteraction.BLOCK, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, WeightedList.of(), ModSounds.PUFFBOMB_EXPLODE);
            entity.removeEffect(ModEffects.PUFFBOMB_POISONING);
        } else {
            world.playSound(null, entity.blockPosition(), ModSounds.TICKING, SoundSource.HOSTILE, 0.5f, 0.8f + 0.05f * (float)world.random.nextInt(8));
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }


}
