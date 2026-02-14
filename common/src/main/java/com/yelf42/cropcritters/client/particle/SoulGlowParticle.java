package com.yelf42.cropcritters.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SoulGlowParticle extends SingleQuadParticle {

    private final SpriteSet spriteProvider;

    SoulGlowParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.first());
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.spriteProvider = spriteProvider;
        this.quadSize *= 0.75F;
        this.hasPhysics = false;
        this.lifetime *= 2;
        this.setSpriteFromAge(spriteProvider);
    }

    public Layer getLayer() {
        return Layer.TRANSLUCENT;
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

    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteProvider);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, RandomSource random) {
            SoulGlowParticle glowParticle = new SoulGlowParticle(clientWorld, d, e, f, (double)0.5F - random.nextDouble(), h, (double)0.5F - random.nextDouble(), this.spriteProvider);

            glowParticle.yd *= (double)0.2F;
            if (g == (double)0.0F && i == (double)0.0F) {
                glowParticle.xd *= (double)0.1F;
                glowParticle.zd *= (double)0.1F;
            }

            glowParticle.setLifetime((int)((double)8.0F / (random.nextDouble() * 0.8 + 0.2)));
            return glowParticle;
        }
    }

}
