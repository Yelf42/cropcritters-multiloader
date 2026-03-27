package com.yelf42.cropcritters.effects;

import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.registry.ModEffects;
import com.yelf42.cropcritters.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.blocks.StrangleFern;

public class SporesEffect extends MobEffect {

    public SporesEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level world = entity.level();
        if (world instanceof ServerLevel serverLevel) {
            if (entity.getRandom().nextInt(5) == 0) {
                serverLevel.sendParticles(
                        ModParticles.SPORES,
                        entity.getX(),
                        entity.getY() + 1,
                        entity.getZ(),
                        1, 0.5, 0.5, 0.5, 0
                );
            }
        }

        if (entity.tickCount % 10 == 0) {
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
        }


    }

    private void reduceDuration(LivingEntity entity) {
        if (entity.getEffect(ModEffects.SPORES) == null) {
            CropCritters.LOGGER.info("WARNING: Spores entity is null");
            return;
        }
        int duration = entity.getEffect(ModEffects.SPORES).getDuration();
        entity.removeEffect(ModEffects.SPORES);
        if (duration > 1300) {
            entity.addEffect(new MobEffectInstance(ModEffects.SPORES, duration - 1200, 1, true, true, false));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
