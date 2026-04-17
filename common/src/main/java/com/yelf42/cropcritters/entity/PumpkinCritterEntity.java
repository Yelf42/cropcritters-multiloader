package com.yelf42.cropcritters.entity;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.constant.DefaultAnimations;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.blocks.SoulFarmland;
import com.yelf42.cropcritters.registry.ModItems;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.Optional;
import java.util.function.Predicate;

public class PumpkinCritterEntity extends AbstractCropCritterEntity implements RangedAttackMob {

    public static final RawAnimation LOB_SEEDS = RawAnimation.begin().thenPlay("plant");


    public PumpkinCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        net.minecraft.world.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.is(ModItems.LOST_SOUL), false);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, temptGoal);
        this.targetWorkGoal = new PumpkinTargetWorkGoal();
        this.goalSelector.addGoal(3, this.targetWorkGoal);
        this.targetSelector.addGoal(7, new MelonActiveTargetGoal());
        this.goalSelector.addGoal(7, new RangedAttackGoal(this, 1.25F, 20, 10.0F));
        this.goalSelector.addGoal(12, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(20, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(20, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.FOLLOW_RANGE, 10)
                .add(Attributes.TEMPT_RANGE, 10);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(DefaultAnimations.genericWalkIdleController(),
                new AnimationController<>("plant_controller", animTest -> PlayState.STOP)
                        .triggerableAnim("plant", LOB_SEEDS));
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.getBlock() instanceof FarmlandBlock || blockState.getBlock() instanceof SoulFarmland);
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        triggerAnim("plant_controller", "plant");
        Vec3 dir = this.getLookAngle();
        Level world = this.level();
        if (world instanceof ServerLevel serverWorld) {
            ItemStack held = this.getItemBySlot(EquipmentSlot.MAINHAND);
            ItemStack itemStack = held.isEmpty() ? new ItemStack(ModItems.SEED_BALL) : held;
            float dist = (float) this.position().distanceTo(this.targetPos.getCenter());
            Projectile.spawnProjectile(new SeedBallProjectileEntity(serverWorld, this, itemStack, 1), serverWorld, itemStack, (entity) -> entity.shoot(dir.x, 1.8F, dir.z, 0.4F * (dist / 5.0F), 0.0F));
        }
        this.playSound(ModSounds.ENTITY_CRITTER_SPIT, 2.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    // Right click with empty hand makes critter try drop held item
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult actionResult = super.mobInteract(player, hand);
        ItemStack inHand = player.getItemInHand(hand);
        if (!this.level().isClientSide() && !actionResult.consumesAction() && this.isTrusting()) {
            if (inHand.isEmpty()) {
                if (tryPutDown(this.getItemBySlot(EquipmentSlot.MAINHAND), true)) return InteractionResult.SUCCESS;
                return InteractionResult.PASS;
            } else if (canHoldItem(inHand)) {
                if (!this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) tryPutDown(this.getItemBySlot(EquipmentSlot.MAINHAND), true);
                this.setItemSlot(EquipmentSlot.MAINHAND, inHand.split(1));
                this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            }
        }
        return actionResult;
    }

    // For dropping held item
    private boolean tryPutDown(ItemStack stack, boolean withVelocity) {
        if (!stack.isEmpty()) {
            ItemEntity itemEntity;
            if (withVelocity) {
                itemEntity = new ItemEntity(this.level(), this.getX() + this.getLookAngle().x, this.getY() + (double)0.6F, this.getZ() + this.getLookAngle().z, stack);
            } else {
                itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + (double)0.6F, this.getZ(), stack, 0F, 0F, 0F);
            }
            itemEntity.setPickUpDelay(40);
            itemEntity.setThrower(this);
            this.playSound(ModSounds.ENTITY_CRITTER_DROP, 1.0F, 1.0F);
            this.level().addFreshEntity(itemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return stack.is(ModItems.SEED_BALL);
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return false;
        } else {
            this.targetWorkGoal.cancel();
            return super.hurtServer(world, source, amount);
        }
    }

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.PUMPKIN, 1);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.is(Items.PUMPKIN) || itemStack.is(Items.PUMPKIN_SEEDS) || itemStack.is(Items.CARVED_PUMPKIN);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(Mth.nextInt(this.random, 500, 1000));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        double d = target.getX() - this.getX();
        double e = target.getEyeY() - 0.4F;
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f) * (double)0.2F;
        Level var12 = this.level();
        if (var12 instanceof ServerLevel serverWorld) {
            ItemStack itemStack = new ItemStack(Items.PUMPKIN_SEEDS);
            Projectile.spawnProjectile(new SpitSeedProjectileEntity(serverWorld, this, itemStack), serverWorld, itemStack, (entity) -> entity.shoot(d, e + g - entity.getY(), f, 1.2F, 3.0F));
        }
        this.playSound(ModSounds.ENTITY_CRITTER_SPIT, 2.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }


    class PumpkinTargetWorkGoal extends AbstractCropCritterEntity.TargetWorkGoal {
        @Override
        public void start() {
            super.start();
            this.nextTarget = Vec3.atBottomCenterOf(PumpkinCritterEntity.this.targetPos).add(0.0F, getTargetOffset(), 0.0F);
        }

        @Override
        public void tick() {
            if (PumpkinCritterEntity.this.targetPos != null) {
                ++this.ticks;
                if (this.ticks > 600 || !(isAttractive(PumpkinCritterEntity.this.targetPos))) {
                    PumpkinCritterEntity.this.clearTargetPos();
                } else {
                    if (this.nextTarget.distanceToSqr(PumpkinCritterEntity.this.position()) > (double)16.0F) {
                        this.moveToNextTarget();
                    } else {
                        PumpkinCritterEntity.this.navigation.stop();
                        PumpkinCritterEntity.this.setYBodyRot((float) Mth.rotLerp(0.3, PumpkinCritterEntity.this.getVisualRotationYInDegrees(), targetYaw(this.nextTarget)));
                        boolean bl2 = Mth.abs(Mth.wrapDegrees(PumpkinCritterEntity.this.getVisualRotationYInDegrees() - targetYaw(this.nextTarget))) < 5F;
                        if (bl2) {
                            PumpkinCritterEntity.this.getLookControl().setLookAt(this.nextTarget);
                            PumpkinCritterEntity.this.completeTargetGoal();
                            PumpkinCritterEntity.this.clearTargetPos();
                        }
                    }
                }
            }
        }

        protected float targetYaw(Vec3 target) {
            Vec3 d = target.subtract(PumpkinCritterEntity.this.position());
            return (float)(Mth.atan2(d.z, d.x) * (180.0 / Math.PI)) - 90.0F;
        }

        @Override
        protected Optional<BlockPos> getTargetBlock() {
            Iterable<BlockPos> iterable = BlockPos.withinManhattan(PumpkinCritterEntity.this.blockPosition(), 12, 2, 12);
            Long2LongOpenHashMap long2LongOpenHashMap = new Long2LongOpenHashMap();

            for(BlockPos blockPos : iterable) {
                long l = this.unreachableTargetsPosCache.getOrDefault(blockPos.asLong(), Long.MIN_VALUE);
                if (PumpkinCritterEntity.this.level().getGameTime() < l) {
                    long2LongOpenHashMap.put(blockPos.asLong(), l);
                } else if (isAttractive(blockPos)) {
                    Path path = PumpkinCritterEntity.this.navigation.createPath(blockPos, 0);
                    if (path != null && path.canReach() && !blockPos.closerToCenterThan(PumpkinCritterEntity.this.position(), 3)) {
                        return Optional.of(blockPos);
                    }

                    long2LongOpenHashMap.put(blockPos.asLong(), PumpkinCritterEntity.this.level().getGameTime() + 600L);
                }
            }

            this.unreachableTargetsPosCache = long2LongOpenHashMap;
            return Optional.empty();
        }
    }

    class MelonActiveTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {

        public MelonActiveTargetGoal() {
            super(PumpkinCritterEntity.this, LivingEntity.class, 10, true, false, (entity, serverWorld) -> (!(entity.is(CropCritters.CROP_CRITTERS))));
        }

        @Override
        protected void findTarget() {
            ServerLevel serverWorld = getServerLevel(this.mob);
            this.target = serverWorld.getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), (livingEntity) -> true), this.getAndUpdateTargetPredicate(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }

        private TargetingConditions getAndUpdateTargetPredicate() {
            return this.targetConditions.range(this.getFollowDistance());
        }

        @Override
        public boolean canUse() {
            return super.canUse() && (!PumpkinCritterEntity.this.isTrusting() || this.target instanceof Monster);
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && (!PumpkinCritterEntity.this.isTrusting() || this.target instanceof Monster);
        }
    }
}
