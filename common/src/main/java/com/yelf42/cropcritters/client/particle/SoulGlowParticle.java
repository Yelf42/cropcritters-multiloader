package com.yelf42.cropcritters.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SoulGlowParticle extends TextureSheetParticle {

    private final SpriteSet spriteProvider;
    static final RandomSource RANDOM = RandomSource.create();

    SoulGlowParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.spriteProvider = spriteProvider;
        this.quadSize *= 0.75F;
        this.hasPhysics = false;
        this.lifetime *= 2;
        this.setSpriteFromAge(spriteProvider);
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getLightColor(float tint) {
        return 240;
    }

    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteProvider);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SoulGlowParticle glowParticle = new SoulGlowParticle(level, x,y,z, (double)0.5F - RANDOM.nextDouble(), ySpeed, (double)0.5F - RANDOM.nextDouble(), this.spriteProvider);

            glowParticle.yd *= (double)0.2F;
            if (xSpeed == (double)0.0F && zSpeed == (double)0.0F) {
                glowParticle.xd *= (double)0.1F;
                glowParticle.zd *= (double)0.1F;
            }

            glowParticle.setLifetime((int)((double)8.0F / (RANDOM.nextDouble() * 0.8 + 0.2)));
            return glowParticle;
        }
    }

}
