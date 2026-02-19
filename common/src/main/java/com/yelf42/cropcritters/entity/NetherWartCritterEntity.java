package com.yelf42.cropcritters.entity;

import com.yelf42.cropcritters.CropCritters;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.registry.ModParticles;

import java.util.function.Predicate;

public class NetherWartCritterEntity extends AbstractCropCritterEntity {

    private static final ColorParticleOption PARTICLE_EFFECT = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ABGR32.color(255, 16073282));
    private static final int GO_CRAZY = 400;
    private static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(NetherWartCritterEntity.class, EntityDataSerializers.INT);

    private static final ExplosionDamageCalculator BURST = new ExplosionDamageCalculator() {
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float power) {
            return state.is(ModBlocks.WITHERING_SPITEWEED) || state.is(Blocks.WITHER_ROSE);
        }
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }
    };


    public NetherWartCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.FOLLOW_RANGE, 10);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LIFESPAN, 1200);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.is(Blocks.SOUL_SOIL)
                                        || blockState.is((Blocks.SOUL_SAND))
                                        || blockState.is(ModBlocks.SOUL_FARMLAND)
                                        || blockState.is(Blocks.BLACKSTONE));
    }

    @Override
    public boolean isAttractive(BlockPos pos) {
        BlockState target = this.level().getBlockState(pos);
        BlockState above = this.level().getBlockState(pos.above());
        return this.getTargetBlockFilter().test(target) && (above.isAir() || (above.getBlock() instanceof BushBlock && !above.hasProperty(DoublePlantBlock.HALF) && !(above.getBlock() instanceof NetherWartBlock)));
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        BlockState target = this.level().getBlockState(this.targetPos);
        if (target.is(Blocks.SOUL_SAND) || target.is(Blocks.SOUL_SOIL) || target.is(ModBlocks.SOUL_FARMLAND)) {
            this.playSound(SoundEvents.SOUL_SAND_PLACE, 1.0F, 1.0F);
            this.level().setBlock(this.targetPos.above(), Blocks.NETHER_WART.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
            ((ServerLevel)this.level()).sendParticles(ModParticles.SOUL_GLINT, this.targetPos.getX() + 0.5, this.targetPos.getY() + 1.0, this.targetPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.0);
            this.discard();
        } else {
            explode();
        }
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.is(Items.NETHER_WART);
    }

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.NETHER_WART, 3);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return 10;
    }

    @Override
    protected boolean canWork() {
        return !this.isTrusting();
    }

    @Override
    public boolean isShaking() {
        return this.entityData.get(LIFESPAN) < GO_CRAZY;
    }

    @Override
    protected void tryTame(Player player) {
        if (!isShaking()) super.tryTame(player);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (!this.isTrusting()) this.entityData.set(LIFESPAN, this.entityData.get(LIFESPAN) - 1);
            if (this.entityData.get(LIFESPAN) <= 0) explode();
        } else if (this.isShaking()) {
            if (this.level().random.nextInt(8) != 0) return;
            double x = this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth();
            double y = this.getY() + this.getBbHeight() * 0.5;
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth();
            this.level().addParticle(PARTICLE_EFFECT, x, y, z, 0, 0, 0);
        }
    }

    private void explode() {
        if (this.level() instanceof ServerLevel serverWorld) {
            Vec3 p = this.position();
            serverWorld.explode(this, Explosion.getDefaultDamageSource(this.level(), this), BURST, p.x, p.y, p.z, 1.5F, false, Level.ExplosionInteraction.MOB);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        BlockPos pos = this.blockPosition().offset(i, j, k);
                        if (serverWorld.getBlockState(pos).is(Blocks.BLACKSTONE)) {
                            serverWorld.setBlock(pos, Blocks.SOUL_SAND.defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                        if (serverWorld.getBlockState(pos).is(ModBlocks.WITHERING_SPITEWEED)) {
                            this.level().levelEvent(null, 2001, this.targetPos, Block.getId(this.level().getBlockState(this.targetPos)));
                            serverWorld.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
            this.dead = true;
            this.triggerOnDeathMobEffects(RemovalReason.KILLED);
            this.discard();
        }
    }
}
