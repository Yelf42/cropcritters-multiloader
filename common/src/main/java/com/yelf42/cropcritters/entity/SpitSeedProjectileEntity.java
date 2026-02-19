package com.yelf42.cropcritters.entity;

import com.yelf42.cropcritters.registry.ModEntities;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;

public class SpitSeedProjectileEntity extends ThrowableItemProjectile {

    public SpitSeedProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public SpitSeedProjectileEntity(ServerLevel serverWorld, LivingEntity livingEntity, ItemStack itemStack) {
        super(ModEntities.SPIT_SEED_PROJECTILE, livingEntity, serverWorld);
        //this.spatItem = itemStack.getItem();
    }

    @Override
    protected Item getDefaultItem() {
        return Items.PUMPKIN_SEEDS;
    }

    @Override
    public void tick() {
        super.tick();
        this.level().broadcastEntityEvent(this, (byte)4);
    }

    private ParticleOptions getParticleParameters() {
        //ItemStack itemStack = this.getStack();
        return ParticleTypes.SPLASH;
    }

    public void handleEntityEvent(byte status) {
        ParticleOptions particleEffect = this.getParticleParameters();
        if (status == 3) {
            for(int i = 0; i < 8; ++i) {
                this.level().addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
        if (status == 4) {
            this.level().addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }



    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) { // called on entity hit.
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity(); // sets a new Entity instance as the EntityHitResult (victim)
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), 1F);
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }

    @Override
    protected void onInsideBlock(BlockState state) {
        Level world = this.level();
        if (!world.isClientSide()) {
            world.broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}
