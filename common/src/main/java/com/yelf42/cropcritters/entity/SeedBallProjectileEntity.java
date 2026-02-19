package com.yelf42.cropcritters.entity;

import com.yelf42.cropcritters.registry.ModEntities;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.registry.ModComponents;
import com.yelf42.cropcritters.registry.ModItems;

import java.util.Arrays;
import java.util.List;

public class SeedBallProjectileEntity extends ThrowableItemProjectile {

    private static final List<ResourceLocation> DefaultSeedTypes = Arrays.asList(BuiltInRegistries.BLOCK.getKey(Blocks.WHEAT), BuiltInRegistries.BLOCK.getKey(Blocks.CARROTS), BuiltInRegistries.BLOCK.getKey(Blocks.POTATOES), BuiltInRegistries.BLOCK.getKey(Blocks.BEETROOTS));


    public SeedBallProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public SeedBallProjectileEntity(double x, double y, double z, Level world, ItemStack itemStack) {
        super(ModEntities.SEED_BALL_PROJECTILE, x, y, z, world);
        this.setItem(itemStack);
    }

    public SeedBallProjectileEntity(ServerLevel serverWorld, LivingEntity livingEntity, ItemStack itemStack) {
        super(ModEntities.SEED_BALL_PROJECTILE, livingEntity, serverWorld);
        this.setItem(itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SEED_BALL;
    }

    private ParticleOptions getParticleParameters() {
        ItemStack itemStack = this.getItem();
        return (itemStack.isEmpty() ? ParticleTypes.SPLASH : new ItemParticleOption(ParticleTypes.ITEM, itemStack));
    }

    public void handleEntityEvent(byte status) {
        if (status == 3) {
            ParticleOptions particleEffect = this.getParticleParameters();
            for(int i = 0; i < 8; ++i) {
                this.level().addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }



    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof Player player) player.addEffect((new MobEffectInstance(MobEffects.BLINDNESS, 20 * 4, 0)));
        if (entity instanceof LivingEntity livingEntity) {
            int p = this.getItem().getOrDefault(ModComponents.POISONOUS_SEED_BALL, new ModComponents.PoisonousComponent(0)).poisonStacks();
            livingEntity.addEffect((new MobEffectInstance(MobEffects.POISON, 20 * 6 * p, 0)));
        }
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }

    @Override
    protected void onInsideBlock(BlockState state) {
        Level world = this.level();
        if (!world.isClientSide()) {
            if (!state.isSolid()) return;

            List<ResourceLocation> crops = this.getItem().getOrDefault(ModComponents.SEED_TYPES, new ModComponents.SeedTypesComponent(DefaultSeedTypes)).seedTypes();
            if (crops.isEmpty()) {
                this.discard();
                return;
            }

            Iterable<BlockPos> iterable = BlockPos.withinManhattan(this.blockPosition(), 2, 3, 2);
            for(BlockPos blockPos : iterable) {
                BlockState blockState = BuiltInRegistries.BLOCK.get(crops.get(this.random.nextInt(crops.size()))).defaultBlockState();
                if ((world.random.nextInt(2) == 0 || blockPos == this.blockPosition()) && blockState.canSurvive(world, blockPos) && world.getBlockState(blockPos).isAir()) {
                    world.setBlockAndUpdate(blockPos, blockState);
                }
            }
            world.broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}
