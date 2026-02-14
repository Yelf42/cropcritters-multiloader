package com.yelf42.cropcritters.entity;

import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import com.yelf42.cropcritters.registry.ModItems;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.function.Predicate;

public class PitcherCritterEntity extends AbstractCropCritterEntity {

    public static final RawAnimation EAT = RawAnimation.begin().thenPlay("attack.eat");

    private final TargetingConditions.Selector CAN_EAT = (entity, world) -> {
        if (this.consume > 0
                || (entity.getBoundingBox().getXsize() >= this.getBoundingBox().getXsize())
                || (entity.getBoundingBox().getYsize() >= this.getBoundingBox().getYsize())
                || entity.isInvulnerable()
                || entity.hasCustomName()
                || entity instanceof Pufferfish)
            return false;
        if (this.isTrusting()) {
            boolean extraChecks = (entity instanceof TamableAnimal tameableEntity && !tameableEntity.isTame());
            extraChecks |= entity instanceof Bee;
            extraChecks |= entity instanceof Allay;
            return extraChecks;
        }
        return true;
    };

    private int consume = 0;
    private boolean timeToConsume = false;
    private Entity consumptionTarget;
    private float lookAtPreyAngle = 0;

    public PitcherCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(DefaultAnimations.genericWalkIdleController(),
                new AnimationController<>("eat_controller", animTest -> PlayState.STOP)
                .triggerableAnim("eat", EAT));
    }

    @Override
    protected void registerGoals() {
        net.minecraft.world.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.is(ModItems.LOST_SOUL), false);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, temptGoal);
        this.goalSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, true, true, (entity, world) -> CAN_EAT.test(entity, world)));
        this.goalSelector.addGoal(4, new OcelotAttackGoal(this));
        this.goalSelector.addGoal(12, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(20, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(20, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.FOLLOW_RANGE, 10)
                .add(Attributes.TEMPT_RANGE, 10);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {return null;}
    @Override
    protected int getTargetOffset() {return 0;}
    @Override
    public void completeTargetGoal() {}

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.PITCHER_PLANT, 1);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.is(Items.PITCHER_PLANT) || itemStack.is(Items.PITCHER_POD);
    }
    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(Mth.nextInt(this.random, 100, 200));
    }
    @Override
    protected boolean canWork() {return true;}

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        if (this.consumptionTarget == null) {
            this.consume = 0;
            this.timeToConsume = false;
        } else {
            if (this.consume > 0) {
                Vec3 mouth = this.position().add(0, this.getEyeHeight() * 0.5, 0);
                Vec3 dir = mouth.subtract(this.consumptionTarget.position()).normalize().scale(0.2);
                this.setYRot(lookAtPreyAngle);
                this.consumptionTarget.setDeltaMovement(dir);
                this.consume--;
            }
            if (this.consume <= 1 && this.timeToConsume) {
                consume(this.level(), this.consumptionTarget);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel world, Entity target) {
        if (this.consume > 0) return false;
        if (!CAN_EAT.test((LivingEntity) target, world)) {
            this.setTarget(null);
            return false;
        }
        target.noPhysics = true;
        target.setSilent(true);
        target.setInvulnerable(true);
        target.setNoGravity(true);
        this.consume = 10;
        this.timeToConsume = true;
        this.consumptionTarget = target;
        Vec3 mouth = this.position().add(0, this.getEyeHeight() * 0.5, 0);
        Vec3 dir = mouth.subtract(this.consumptionTarget.position()).normalize().scale(0.2);
        this.lookAtPreyAngle = (float)(Mth.atan2(-dir.z, -dir.x) * (180F / Math.PI)) - 90F;
        triggerAnim("eat_controller", "eat");
        this.playSound(ModSounds.ENTITY_CRITTER_EAT, 2F, 1F);
        return true;
    }

    private void consume(Level world, Entity target) {
        if (world.isClientSide()) return;
        if (target instanceof AbstractCropCritterEntity critter) {
            critter.dropAllDeathLoot((ServerLevel) world, target.damageSources().genericKill());
        } else {
            Vec3 pos = target.position();
            ItemEntity item = new ItemEntity(world, pos.x, pos.y, pos.z, new ItemStack(ModItems.STRANGE_FERTILIZER));
            world.addFreshEntity(item);
        }
        target.discard();
        this.heal(1.f);
        this.timeToConsume = false;
        this.consumptionTarget = null;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (this.consumptionTarget != null) this.consumptionTarget.discard();
        super.die(damageSource);
    }
}
