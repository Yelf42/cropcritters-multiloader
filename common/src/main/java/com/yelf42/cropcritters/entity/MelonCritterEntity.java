package com.yelf42.cropcritters.entity;

import com.yelf42.cropcritters.registry.ModPackets;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.registry.ModItems;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.*;
import java.util.function.Predicate;

public class MelonCritterEntity extends AbstractCropCritterEntity implements RangedAttackMob {

    int wateringDuration = -1;

    public MelonCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        net.minecraft.world.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.is(ModItems.LOST_SOUL), false);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, temptGoal);
        this.targetWorkGoal = new TargetWorkGoal();
        this.goalSelector.addGoal(3, this.targetWorkGoal);
        this.goalSelector.addGoal(4, new WateringGoal());
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
                .add(Attributes.FOLLOW_RANGE, 10);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.getBlock() instanceof CropBlock);
    }

    @Override
    protected  int getTargetOffset() {return 0;}

    @Override
    public void completeTargetGoal() {
        wateringDuration = 80;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            this.targetWorkGoal.cancel();
            this.wateringDuration = -1;
            return super.hurt(source, amount);
        }
    }

    @Override
    protected void handleTicksUntilCanWork() {
        if (this.ticksUntilCanWork > 0 && this.wateringDuration <= 0) {
            --this.ticksUntilCanWork;
        }
    }

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.MELON_SLICE, 8);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.is(Items.MELON_SLICE) || itemStack.is(Items.MELON) || itemStack.is(Items.MELON_SEEDS);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(Mth.nextInt(this.random, 300, 500));
    }

    @Override
    protected boolean canWork() {
        return super.canWork() && this.wateringDuration <= 0;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        double d = target.getX() - this.getX();
        double e = target.getEyeY() - 0.4F;
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f) * (double)0.2F;
        Level var12 = this.level();
        if (var12 instanceof ServerLevel serverWorld) {
            ItemStack itemStack = new ItemStack(Items.MELON_SEEDS);
            SpitSeedProjectileEntity spitSeed = new SpitSeedProjectileEntity(serverWorld, this, itemStack);
            spitSeed.shoot(d, e + g - this.getY(), f, 1.2F, 3.0F);
            this.level().addFreshEntity(spitSeed);
        }
        this.playSound(ModSounds.ENTITY_CRITTER_SPIT, 2.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    class WateringGoal extends Goal {
        List<BlockPos> wateringTargets;

        WateringGoal() {
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (MelonCritterEntity.this.wateringDuration <= 0) return false;
            wateringTargets = new ArrayList<>(3);
            findWateringTargets();
            if (wateringTargets.isEmpty()) {
                MelonCritterEntity.this.wateringDuration = 0;
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            MelonCritterEntity.this.navigation.stop();
        }

        @Override
        public boolean canContinueToUse() {
            return MelonCritterEntity.this.wateringDuration > 0;
        }

        @Override
        public void tick() {
            MelonCritterEntity.this.wateringDuration--;
            MelonCritterEntity.this.navigation.stop();
            Level world = MelonCritterEntity.this.level();
            if (world instanceof ServerLevel serverWorld) {
                if (MelonCritterEntity.this.random.nextInt(10) == 0) {
                    BlockPos toWater = wateringTargets.get(MelonCritterEntity.this.random.nextInt(wateringTargets.size()));
                    BlockState toWaterState = serverWorld.getBlockState(toWater);
                    if (toWaterState.getBlock() instanceof BonemealableBlock fertilizable) {
                        if (fertilizable.isValidBonemealTarget(serverWorld, toWater, toWaterState)) {
                            if (fertilizable.isBonemealSuccess(serverWorld, serverWorld.random, toWater, toWaterState)) {
                                fertilizable.performBonemeal(serverWorld, serverWorld.random, toWater, toWaterState);
                            }
                        }
                    }
                }
                Vec3 facing = MelonCritterEntity.this.getLookAngle().normalize().scale(3);
                Vec3 start = MelonCritterEntity.this.position().add(0F, 0.1F, 0F);

                ModPackets.WaterSprayS2CPayload payload = new ModPackets.WaterSprayS2CPayload(start, facing);
                ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);

                for (ServerPlayer player : serverWorld.players()) {
                    if (start.closerThan(player.position(), 64)) {
                        player.connection.send(packet);
                    }
                }

                MelonCritterEntity.this.playSound(ModSounds.ENTITY_CRITTER_WATER, 0.01F, 0.8F / (MelonCritterEntity.this.getRandom().nextFloat() * 0.4F + 0.8F));
            }


        }

        private void findWateringTargets() {
            Direction dir = MelonCritterEntity.this.getNearestViewDirection();
            Vec3 facing = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            BlockPos start = MelonCritterEntity.this.blockPosition();
            float stepSize = 0.3F;
            for (int i = 0; i < 10; i++) {
                Vec3 offset = facing.scale(i * stepSize);
                BlockPos check = start.offset(Math.round((float)offset.x), 0 , Math.round((float)offset.z));
                if (!MelonCritterEntity.this.level().getBlockState(check).getCollisionShape(MelonCritterEntity.this.level(), check).isEmpty()) return;
                if (!wateringTargets.contains(check)) wateringTargets.add(check);
            }
        }
    }

    class MelonActiveTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {

        public MelonActiveTargetGoal() {
            super(MelonCritterEntity.this, LivingEntity.class, 10, true, false, (entity) -> (!(entity.getType().is(CropCritters.CROP_CRITTERS))));
        }

        @Override
        protected void findTarget() {
            this.target = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), (livingEntity) -> true), this.getAndUpdateTargetPredicate(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }

        private TargetingConditions getAndUpdateTargetPredicate() {
            return this.targetConditions.range(this.getFollowDistance());
        }

        @Override
        public boolean canUse() {
            return super.canUse() && (!MelonCritterEntity.this.isTrusting() || this.target instanceof Monster);
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && (!MelonCritterEntity.this.isTrusting() || this.target instanceof Monster);
        }
    }
}
