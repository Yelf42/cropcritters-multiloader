package com.yelf42.cropcritters.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class SoulSiphonParticle extends RisingParticle {
    private final SpriteSet sprites;

    public SoulSiphonParticle(ClientLevel world, double x, double y, double z, double vX, double vY, double vZ, SpriteSet sprites) {
        super(world, x, y, z, 0.0, 0.1, 0.0);
        this.sprites = sprites;
        this.scale(1.5F);
        this.setSpriteFromAge(sprites);
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
        this.setLocationFromBoundingbox();
    }

    public float getQuadSize(float tickProgress) {
        float f = ((float)this.age + tickProgress) / (float)this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
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

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SoulSiphonParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteProvider);
        }
    }
}