package com.yelf42.cropcritters.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class SoulGlintPlumeParticle extends BaseAshSmokeParticle {

    protected SoulGlintPlumeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float quadSizeMultiplier, SpriteSet sprites) {
        super(level, x, y, z, 0.7F, 0.6F, 0.7F, xSpeed, ySpeed + (double)0.15F, zSpeed, quadSizeMultiplier, sprites, 0.5F, 7, 0.5F, false);
        this.friction = 0.96F;
        this.gravity = 0.5F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.xd *= 0.7F;
        this.yd += 0.15F;
        this.yd *= 0.6F;
        this.zd *= 0.7F;
        this.xd += xSpeed;
        this.yd += ySpeed;
        this.zd += zSpeed;
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.quadSize *= 0.75F * quadSizeMultiplier;
        this.lifetime = (int)((double)7 / ((double)this.random.nextFloat() * 0.8 + 0.2) * (double)quadSizeMultiplier);
        this.lifetime = Math.max(this.lifetime, 1);
        this.hasPhysics = false;
    }

    public float getQuadSize(float tickProgress) {
        return this.quadSize * Mth.clamp(((float)this.age + tickProgress) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    public void tick() {
        this.gravity = 0.88F * this.gravity;
        this.friction = 0.92F * this.friction;
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getLightColor(float tint) {
        float f = ((float)this.age + tint) / (float)this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(tint);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int)(f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SoulGlintPlumeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, 1.0F, this.spriteProvider);
        }
    }
}