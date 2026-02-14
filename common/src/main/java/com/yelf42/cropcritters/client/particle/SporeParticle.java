package com.yelf42.cropcritters.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SporeParticle extends SingleQuadParticle {
    private static final RandomSource RANDOM = RandomSource.create();
    private final SpriteSet spriteProvider;
    private float defaultAlpha = 1.0F;

    SporeParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, (double) 0.5F - RANDOM.nextDouble(), velocityY, (double) 0.5F - RANDOM.nextDouble(), spriteProvider.first());
        this.friction = 0.96F;
        this.gravity = 0.1F;
        this.speedUpWhenYMotionIsBlocked = false;
        this.spriteProvider = spriteProvider;
        this.yd *= (double) -0.1F;
        if (velocityX == (double) 0.0F && velocityZ == (double) 0.0F) {
            this.xd *= (double) 0.1F;
            this.zd *= (double) 0.1F;
        }

        this.quadSize *= 0.75F;
        this.lifetime = (int) ((double) 5.0F / ((double) this.random.nextFloat() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteProvider);
        if (this.isInvisible()) {
            this.setAlpha(0.0F);
        }

    }

    public Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteProvider);
        if (this.isInvisible()) {
            this.alpha = 0.0F;
        } else {
            this.alpha = Mth.lerp(0.05F, this.alpha, this.defaultAlpha);
        }

    }

    protected void setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.defaultAlpha = alpha;
    }

    private boolean isInvisible() {
        Minecraft minecraftClient = Minecraft.getInstance();
        LocalPlayer clientPlayerEntity = minecraftClient.player;
        return clientPlayerEntity != null && clientPlayerEntity.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= (double) 9.0F && minecraftClient.options.getCameraType().isFirstPerson() && clientPlayerEntity.isScoping();
    }

    public static class Factory implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(ColorParticleOption tintedParticleEffect, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, RandomSource random) {
            SporeParticle sporeParticle = new SporeParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            sporeParticle.setColor(tintedParticleEffect.getRed(), tintedParticleEffect.getGreen(), tintedParticleEffect.getBlue());
            sporeParticle.setAlpha(tintedParticleEffect.getAlpha());
            return sporeParticle;
        }
    }
}