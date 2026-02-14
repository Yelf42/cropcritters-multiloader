package com.yelf42.cropcritters.entity;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.tags.ItemTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.config.ConfigManager;
import com.yelf42.cropcritters.registry.ModItems;
import com.yelf42.cropcritters.registry.ModParticles;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractCropCritterEntity extends TamableAnimal implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Boolean> TRUSTING = SynchedEntityData.defineId(AbstractCropCritterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER = (entity) -> !entity.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity);
    private static final Predicate<Entity> FARM_ANIMALS_FILTER = (entity -> entity.getType().is(CropCritters.SCARE_CRITTERS));
    public static final RawAnimation SIT = RawAnimation.begin().thenLoop("animated.sit");

    // Override these methods
    protected abstract Predicate<BlockState> getTargetBlockFilter();
    protected abstract int getTargetOffset();
    protected abstract boolean isHealingItem(ItemStack itemStack);
    protected abstract int resetTicksUntilCanWork();
    public abstract void completeTargetGoal();
    protected abstract Tuple<Item, Integer> getLoot();

    protected  int resetTicksUntilCanWork(int work) {
        return (int) ((double)work * ConfigManager.CONFIG.critterWorkSpeedMultiplier);
    }

    // Override for more complex behaviours
    protected boolean canWork() {return this.isTrusting();}
    public boolean isShaking() {return false;}

    @Nullable
    BlockPos targetPos;
    TargetWorkGoal targetWorkGoal;

    int ticksUntilCanWork = 20 * 10;

    public AbstractCropCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
        setPathfindingMalus(PathType.DAMAGE_OTHER, 0.0f);
    }

    public void setTrusting(boolean trusting) {
        this.entityData.set(TRUSTING, trusting);
    }
    public boolean isTrusting() {
        return this.entityData.get(TRUSTING);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putBoolean("Trusting", this.isTrusting());
        view.putInt("TicksUntilCanWork", this.ticksUntilCanWork);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.setTrusting(view.getBooleanOr("Trusting", false));
        this.ticksUntilCanWork = view.getIntOr("TicksUntilCanWork", resetTicksUntilCanWork());
        this.targetPos = null;
    }


    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel world, AgeableMob entity) {return null;}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                new AnimationController<>("Sit", test -> {
                    if ((this.entityData.get(DATA_FLAGS_ID) & 0x01) != 0) {
                        return (test.setAndContinue(SIT));
                    }
                    test.controller().reset();
                    return PlayState.STOP;
                }),
                DefaultAnimations.genericWalkIdleController()
        );
    }

    protected void registerGoals() {
        net.minecraft.world.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.is(ModItems.LOST_SOUL), true);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, temptGoal);
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Animal.class, 10.0F, 1.2, 1.4, FARM_ANIMALS_FILTER::test));
        this.goalSelector.addGoal(6, new AvoidEntityGoal<>(this, Player.class, 10.0F, 1.2, 1.4, (entity) -> NOTICEABLE_PLAYER_FILTER.test(entity) && !this.isTrusting()));
        this.targetWorkGoal = new TargetWorkGoal();
        this.goalSelector.addGoal(8, this.targetWorkGoal);
        this.goalSelector.addGoal(12, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(20, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(20, new RandomLookAroundGoal(this));
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TRUSTING, false);
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.FOLLOW_RANGE, 10)
                .add(Attributes.TEMPT_RANGE, 10);
    }

    @Override
    public void playAmbientSound() {
        if (this.getBoundingBox().getXsize() > 0.51) {
            // Big
            playSound(ModSounds.ENTITY_CRITTER_AMBIENT, 1F, 0.6F);
        } else {
            // Smol
            makeSound(ModSounds.ENTITY_CRITTER_AMBIENT);
        }
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        if (this.getBoundingBox().getXsize() > 0.51) {
            // Big
            playSound(ModSounds.ENTITY_CRITTER_LARGE, 1F, 1.1F);
            playSound(ModSounds.ENTITY_CRITTER_HURT, 1F, 0.6F);
        } else {
            // Smol
            makeSound(ModSounds.ENTITY_CRITTER_HURT);
        }
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        if (this.getBoundingBox().getXsize() > 0.51) {
            // Big
            playSound(ModSounds.ENTITY_CRITTER_LARGE, 1F, 1.1F);
            return ModSounds.ENTITY_CRITTER_DEATH;
        } else {
            // Smol
            return ModSounds.ENTITY_CRITTER_DEATH;
        }
    }

    @Override
    protected void spawnTamingParticles(boolean positive) {
        ParticleOptions particleEffect = ModParticles.SOUL_HEART;
        if (!positive) {
            particleEffect = ParticleTypes.SMOKE;
        }

        for(int i = 0; i < 3; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particleEffect, this.getRandomX(1.0F), this.getRandomY() + (double)0.5F, this.getRandomZ(1.0F), d, e, f);
        }

    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        GroundPathNavigation mobNavigation = new GroundPathNavigation(this, world);
        mobNavigation.setCanFloat(true);
        return mobNavigation;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

/*
    @Override
    protected void mobTick(ServerWorld world) {
        for(PrioritizedGoal prioritizedGoal : this.goalSelector.getGoals()) {
            if (prioritizedGoal.isRunning()) {
                CropCritters.LOGGER.info(prioritizedGoal.getGoal().getClass().toString());
            }
        }
        super.mobTick(world);
    }
*/

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.SWEET_BERRY_BUSH)) {
            return false;
        } else {
            if (this.targetWorkGoal != null) this.targetWorkGoal.cancel();
            if (source.getEntity() instanceof Player && source.getWeaponItem() != null && source.getWeaponItem().is(ItemTags.HOES)) amount *= 3;
            return super.hurtServer(world, source, amount);
        }
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel world, DamageSource damageSource) {
        Tuple<Item, Integer> loot = getLoot();
        int quantity = 1;
        if (loot.getB() > 1 && damageSource.getEntity() instanceof Player) {
            boolean withHoe = damageSource.getWeaponItem() != null && damageSource.getWeaponItem().is(ItemTags.HOES);
            if (withHoe) quantity = world.random.nextInt(loot.getB()) + 1;
        }
        ItemStack toDrop = new ItemStack(loot.getA(), quantity);
        this.spawnAtLocation(world, toDrop);
        this.dropExperience(world, damageSource.getEntity());
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel world) {
        return super.getBaseExperienceReward(world) + (this.isTrusting() ? 3 : 0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        handleTicksUntilCanWork();
    }

    protected void handleTicksUntilCanWork() {
        if (this.ticksUntilCanWork > 0) {
            --this.ticksUntilCanWork;
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (itemStack.is(ModItems.LOST_SOUL) && !this.isTrusting()) {
                this.usePlayerItem(player, hand, itemStack);
                this.tryTame(player);
                this.setPersistenceRequired();
                return InteractionResult.SUCCESS;
            } else if (this.isHealingItem(itemStack) && (this.getHealth() < this.getMaxHealth())) {
                this.usePlayerItem(player, hand, itemStack);
                this.heal(4.f);
                this.level().broadcastEntityEvent(this, (byte)7);
                this.setPersistenceRequired();
                return InteractionResult.SUCCESS;
            } else if (itemStack.is(ModItems.SEED_BAR) && this.isTrusting()) {
                boolean didSomething = false;
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(4.f);
                    didSomething = true;
                }
                if (this.ticksUntilCanWork > 20) {
                    this.ticksUntilCanWork = 10;
                    didSomething = true;
                }
                if (didSomething) {
                    this.usePlayerItem(player, hand, itemStack);
                    this.level().broadcastEntityEvent(this, (byte)7);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
        }

        return InteractionResult.PASS;
    }

    protected void tryTame(Player player) {
        if (this.random.nextInt(2) == 0) {
            this.setTrusting(true);
            this.tame(player);
            this.level().broadcastEntityEvent(this, (byte)7);
            float newHealth = this.getMaxHealth() * 2F;
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newHealth);
            this.setHealth(newHealth);
        } else {
            this.level().broadcastEntityEvent(this, (byte)6);
        }
    }

    void clearTargetPos() {
        this.targetPos = null;
        this.ticksUntilCanWork = resetTicksUntilCanWork();
    }

    public boolean isAttractive(BlockPos pos) {
        BlockState target = this.level().getBlockState(pos);
        BlockState above = this.level().getBlockState(pos.above());
        return this.getTargetBlockFilter().test(target) && above.is(Blocks.AIR);
    }

    class TargetWorkGoal extends Goal {
        protected Long2LongOpenHashMap unreachableTargetsPosCache = new Long2LongOpenHashMap();
        protected boolean running;
        protected int ticks;
        protected Vec3 nextTarget;

        TargetWorkGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public void start() {
            this.running = true;
            this.ticks = 0;
        }

        public void stop() {
            this.running = false;
            AbstractCropCritterEntity.this.navigation.stop();
            AbstractCropCritterEntity.this.clearTargetPos();
        }

        @Override
        public boolean canUse() {
            if (AbstractCropCritterEntity.this.ticksUntilCanWork > 0) return false;
            if (!AbstractCropCritterEntity.this.canWork()) return false;
            Optional<BlockPos> optional = this.getTargetBlock();
            if (optional.isPresent()) {
                AbstractCropCritterEntity.this.targetPos = optional.get();
                return true;
            } else {
                AbstractCropCritterEntity.this.ticksUntilCanWork = 80;
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.running && (AbstractCropCritterEntity.this.targetPos != null);
        }

        public void tick() {
            if (AbstractCropCritterEntity.this.targetPos != null) {
                ++this.ticks;
                if (this.ticks > 600 || !(isAttractive(AbstractCropCritterEntity.this.targetPos))) {
                    AbstractCropCritterEntity.this.clearTargetPos();
                } else {
                    Vec3 vec3d = Vec3.atBottomCenterOf(AbstractCropCritterEntity.this.targetPos).add(0.0F, getTargetOffset(), 0.0F);
                    if (vec3d.distanceToSqr(AbstractCropCritterEntity.this.position()) > (double)1.0F) {
                        this.nextTarget = vec3d;
                        this.moveToNextTarget();
                    } else {
                        if (this.nextTarget == null) {
                            this.nextTarget = vec3d;
                        }

                        boolean bl = AbstractCropCritterEntity.this.position().distanceTo(this.nextTarget) <= 0.5;
                        if (!bl && this.ticks > 600) {
                            AbstractCropCritterEntity.this.clearTargetPos();
                        } else if (bl) {
                            // At target pos
                            AbstractCropCritterEntity.this.completeTargetGoal();
                            AbstractCropCritterEntity.this.clearTargetPos();
                        } else {
                            AbstractCropCritterEntity.this.getMoveControl().setWantedPosition(this.nextTarget.x(), this.nextTarget.y(), this.nextTarget.z(), 1.2F);
                        }
                    }
                }
            }
        }

        protected void moveToNextTarget() {
            AbstractCropCritterEntity.this.navigation.moveTo(AbstractCropCritterEntity.this.navigation.createPath(this.nextTarget.x(), this.nextTarget.y(), this.nextTarget.z(), 0), 1F);
        }

        void cancel() {
            this.running = false;
        }

        protected Optional<BlockPos> getTargetBlock() {
            Iterable<BlockPos> iterable = BlockPos.withinManhattan(AbstractCropCritterEntity.this.blockPosition(), 6, 3, 6);
            Long2LongOpenHashMap long2LongOpenHashMap = new Long2LongOpenHashMap();

            for(BlockPos blockPos : iterable) {
                long l = this.unreachableTargetsPosCache.getOrDefault(blockPos.asLong(), Long.MIN_VALUE);
                if (AbstractCropCritterEntity.this.level().getGameTime() < l) {
                    long2LongOpenHashMap.put(blockPos.asLong(), l);
                } else if (isAttractive(blockPos)) {
                    Path path = AbstractCropCritterEntity.this.navigation.createPath(blockPos, 0);
                    if (path != null && path.canReach()) {
                        return Optional.of(blockPos);
                    }

                    long2LongOpenHashMap.put(blockPos.asLong(), AbstractCropCritterEntity.this.level().getGameTime() + 600L);
                }
            }

            this.unreachableTargetsPosCache = long2LongOpenHashMap;
            return Optional.empty();
        }
    }



    static class TemptGoal extends net.minecraft.world.entity.ai.goal.TemptGoal {
        @Nullable
        private Player player;
        private final AbstractCropCritterEntity critter;

        public TemptGoal(AbstractCropCritterEntity critter, double speed, Predicate<ItemStack> foodPredicate, boolean canBeScared) {
            super(critter, speed, foodPredicate, canBeScared);
            this.critter = critter;
        }

        public void tick() {
            super.tick();
            if (this.player == null && this.mob.getRandom().nextInt(this.adjustedTickDelay(600)) == 0) {
                this.player = this.player;
            } else if (this.mob.getRandom().nextInt(this.adjustedTickDelay(500)) == 0) {
                this.player = null;
            }

        }

        protected boolean canScare() {
            return (this.player == null || !this.player.equals(this.player)) && super.canScare();
        }

        public boolean canUse() {
            return super.canUse() && !this.critter.isTrusting();
        }
    }
}
