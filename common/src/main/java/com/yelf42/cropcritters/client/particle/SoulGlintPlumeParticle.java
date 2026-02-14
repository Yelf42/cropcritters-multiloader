package com.yelf42.cropcritters.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SingleQuadParticle.Layer;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SoulGlintPlumeParticle extends SingleQuadParticle {
    private final SpriteSet spriteProvider;

    protected SoulGlintPlumeParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteSet spriteProvider) {
        super(world, x, y, z, 0.0F, 0.0F, 0.0F, spriteProvider.first());
        this.friction = 0.96F;
        this.gravity = 0.5F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.spriteProvider = spriteProvider;
        this.xd *= 0.7F;
        this.yd += 0.15F;
        this.yd *= 0.6F;
        this.zd *= 0.7F;
        this.xd += velocityX;
        this.yd += velocityY;
        this.zd += velocityZ;
        this.quadSize *= 0.75F * scaleMultiplier;
        this.lifetime = (int)((double)7 / ((double)this.random.nextFloat() * 0.8 + 0.2) * (double)scaleMultiplier);
        this.lifetime = Math.max(this.lifetime, 1);
        this.setSpriteFromAge(spriteProvider);
        this.hasPhysics = false;
    }

    public Layer getLayer() {
        return Layer.OPAQUE;
    }

    public float getQuadSize(float tickProgress) {
        return this.quadSize * Mth.clamp(((float)this.age + tickProgress) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    public void tick() {
        this.gravity = 0.88F * this.gravity;
        this.friction = 0.92F * this.friction;
        this.setSpriteFromAge(this.spriteProvider);
        super.tick();
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, RandomSource random) {
            return new SoulGlintPlumeParticle(clientWorld, d, e, f, g, h, i, 1.0F, this.spriteProvider);
        }
    }
}