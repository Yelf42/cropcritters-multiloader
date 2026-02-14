package com.yelf42.cropcritters.entity;

import com.yelf42.cropcritters.registry.ModEntities;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.registry.ModItems;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.List;
import java.util.OptionalInt;

public class PopperPodEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> ITEM;
    private static final EntityDataAccessor<OptionalInt> SHOOTER_ENTITY_ID;
    private static final EntityDataAccessor<Boolean> SHOT_AT_ANGLE;
    private int life;
    private int lifeTime;
    private boolean shouldExplode = true;
    private @Nullable LivingEntity shooter;

    public PopperPodEntity(EntityType<? extends PopperPodEntity> entityType, Level world) {
        super(entityType, world);
        this.life = 0;
        this.lifeTime = 0;
    }

    public PopperPodEntity(Level world, double x, double y, double z, ItemStack stack) {
        super(ModEntities.POPPER_POD_PROJECTILE, world);
        this.life = 0;
        this.lifeTime = 0;
        this.life = 0;
        this.setPos(x, y, z);
        this.entityData.set(ITEM, stack.copy());
        this.setDeltaMovement(this.random.triangle((double)0.0F, 0.002297), 0.05, this.random.triangle((double)0.0F, 0.002297));
        this.lifeTime = 15 + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public PopperPodEntity(Level world, @Nullable Entity entity, double x, double y, double z, ItemStack stack) {
        this(world, x, y, z, stack);
        this.setOwner(entity);
    }

    public PopperPodEntity(Level world, ItemStack stack, LivingEntity shooter) {
        this(world, shooter, shooter.getX(), shooter.getY(), shooter.getZ(), stack);
        this.entityData.set(SHOOTER_ENTITY_ID, OptionalInt.of(shooter.getId()));
        this.shooter = shooter;
        this.shouldExplode = false;
    }

    public PopperPodEntity(Level world, ItemStack stack, double x, double y, double z, boolean shotAtAngle) {
        this(world, x, y, z, stack);
        this.entityData.set(SHOT_AT_ANGLE, shotAtAngle);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ITEM, getDefaultStack());
        builder.define(SHOOTER_ENTITY_ID, OptionalInt.empty());
        builder.define(SHOT_AT_ANGLE, false);
    }

    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < (double)4096.0F && !this.wasShotByEntity();
    }

    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return super.shouldRender(cameraX, cameraY, cameraZ) && !this.wasShotByEntity();
    }

    public void tick() {
        super.tick();
        HitResult hitResult;
        if (this.wasShotByEntity()) {
            if (this.shooter == null) {
                ((OptionalInt)this.entityData.get(SHOOTER_ENTITY_ID)).ifPresent((id) -> {
                    Entity entity = this.level().getEntity(id);
                    if (entity instanceof LivingEntity) {
                        this.shooter = (LivingEntity)entity;
                    }

                });
            }

            if (this.shooter != null) {
                Vec3 vec3d3;
                if (this.shooter.isFallFlying()) {
                    Vec3 vec3d = this.shooter.getLookAngle();
                    double d = (double)1.5F;
                    double e = 0.1;
                    Vec3 vec3d2 = this.shooter.getDeltaMovement();
                    this.shooter.setDeltaMovement(vec3d2.add(vec3d.x * 0.1 + (vec3d.x * (double)1.5F - vec3d2.x) * (double)0.5F, vec3d.y * 0.1 + (vec3d.y * (double)1.5F - vec3d2.y) * (double)0.5F, vec3d.z * 0.1 + (vec3d.z * (double)1.5F - vec3d2.z) * (double)0.5F));
                    vec3d3 = this.shooter.getHandHoldingItemAngle(ModItems.POPPER_POD);
                } else {
                    vec3d3 = Vec3.ZERO;
                }

                this.setPos(this.shooter.getX() + vec3d3.x, this.shooter.getY() + vec3d3.y, this.shooter.getZ() + vec3d3.z);
                this.setDeltaMovement(this.shooter.getDeltaMovement());
            }

            hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        } else {
            if (!this.wasShotAtAngle()) {
                double f = this.horizontalCollision ? (double)1.0F : 1.15;
                this.setDeltaMovement(this.getDeltaMovement().multiply(f, (double)1.0F, f).add((double)0.0F, 0.04, (double)0.0F));
            }

            Vec3 vec3d3 = this.getDeltaMovement();
            hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            this.move(MoverType.SELF, vec3d3);
            this.applyEffectsFromBlocks();
            this.setDeltaMovement(vec3d3);
        }

        if (!this.noPhysics && this.isAlive() && hitResult.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf(hitResult);
            this.needsSync = true;
        }

        this.updateRotation();
        if (this.life == 0 && !this.isSilent()) {
            // Launch sfx
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.POPPER_POD_LAUNCH, SoundSource.AMBIENT, 2.0F, 1.0F + (random.nextFloat() * 0.8F - 0.4F));
        }

        ++this.life;
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, -this.getDeltaMovement().y * (double)0.5F, this.random.nextGaussian() * 0.05);
        }

        if (this.life > this.lifeTime) {
            Level var12 = this.level();
            if (var12 instanceof ServerLevel serverWorld) {
                this.explodeAndRemove(serverWorld);
            }
        }

    }

    private void explodeAndRemove(ServerLevel world) {
        world.broadcastEntityEvent(this, (byte)17);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner()); // IDK what this does
        this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), ModSounds.POPPER_POD_POP, SoundSource.AMBIENT, 2.0F, 1.0F + (random.nextFloat() * 0.8F - 0.4F));
        if (this.shouldExplode) this.explode(world);
        this.discard();
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Level var3 = this.level();
        if (var3 instanceof ServerLevel serverWorld) {
            this.explodeAndRemove(serverWorld);
        }

    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockPos blockPos = new BlockPos(blockHitResult.getBlockPos());
        this.level().getBlockState(blockPos).entityInside(this.level(), blockPos, this, InsideBlockEffectApplier.NOOP, true);
        Level var4 = this.level();
        if (var4 instanceof ServerLevel serverWorld) {
            this.explodeAndRemove(serverWorld);
        }

        super.onHitBlock(blockHitResult);
    }

    private void explode(ServerLevel world) {
        int count = (random.nextInt(3) != 0) ? 1 : (random.nextInt(3) != 0 ? 2 : 3);

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0f;
            Projectile.spawnProjectile(new PopperSeedEntity(this.position(), world),
                    world,
                    new ItemStack(ModBlocks.POPPER_PLANT.asItem()),
                    (entity) -> entity.shoot(Math.sin(angle), 0.0f, Math.cos(angle), random.nextFloat() * 0.3f + 0.1f, 10.0F));
        }
    }

    private boolean wasShotByEntity() {
        return ((OptionalInt)this.entityData.get(SHOOTER_ENTITY_ID)).isPresent();
    }

    public boolean wasShotAtAngle() {
        return (Boolean)this.entityData.get(SHOT_AT_ANGLE);
    }

    public void handleEntityEvent(byte status) {
        if (status == 17 && this.level().isClientSide()) {
            Vec3 vec3d = this.getDeltaMovement();
            this.level().createFireworks(this.getX(), this.getY(), this.getZ(), vec3d.x, vec3d.y, vec3d.z, List.of());
        }
        super.handleEntityEvent(status);
    }

    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putInt("Life", this.life);
        view.putInt("LifeTime", this.lifeTime);
        view.store("PopperPodItem", ItemStack.CODEC, this.getItem());
        view.putBoolean("ShotAtAngle", (Boolean)this.entityData.get(SHOT_AT_ANGLE));
    }

    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.life = view.getIntOr("Life", 0);
        this.lifeTime = view.getIntOr("LifeTime", 0);
        this.entityData.set(ITEM, (ItemStack)view.read("PopperPodItem", ItemStack.CODEC).orElse(getDefaultStack()));
        this.entityData.set(SHOT_AT_ANGLE, view.getBooleanOr("ShotAtAngle", false));
    }

    public ItemStack getItem() {
        return (ItemStack)this.entityData.get(ITEM);
    }

    public boolean isAttackable() {
        return false;
    }

    private static ItemStack getDefaultStack() {
        return new ItemStack(ModItems.POPPER_POD);
    }

    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity target, DamageSource source) {
        double d = target.position().x - this.position().x;
        double e = target.position().z - this.position().z;
        return DoubleDoubleImmutablePair.of(d, e);
    }

    static {
        ITEM = SynchedEntityData.defineId(PopperPodEntity.class, EntityDataSerializers.ITEM_STACK);
        SHOOTER_ENTITY_ID = SynchedEntityData.defineId(PopperPodEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
        SHOT_AT_ANGLE = SynchedEntityData.defineId(PopperPodEntity.class, EntityDataSerializers.BOOLEAN);
    }
}
