package com.yelf42.cropcritters.entity;

import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.function.Predicate;

import static net.minecraft.world.level.block.Block.pushEntitiesUp;

public class PoisonousPotatoCritterEntity extends AbstractCropCritterEntity implements Enemy {

    private static final ColorParticleOption PARTICLE_EFFECT = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, ARGB.color(1F, 8889187));

    private static final Predicate<Entity> POISON_PREDICATE = (entity) -> {
        if (entity instanceof Player playerEntity) return !playerEntity.isCreative();
        return !entity.getType().is(CropCritters.CROP_CRITTERS);
    };

    private boolean lastTargetMature = false;
    private int destroyFarmland = 0;
    private BlockPos destroyFarmlandPos = null;

    public PoisonousPotatoCritterEntity(EntityType<? extends AbstractCropCritterEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new DestroyEggGoal(this, (double)1.0F, 3));
        this.targetWorkGoal = new TargetWorkGoal();
        this.goalSelector.addGoal(8, this.targetWorkGoal);
        this.goalSelector.addGoal(12, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(20, new RandomLookAroundGoal(this));
    }

    @Override
    public void playAmbientSound() {
        makeSound(ModSounds.ENTITY_CRITTER_EVIL_AMBIENT);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> (blockState.getBlock() instanceof CropBlock));
    }

    @Override
    protected int getTargetOffset() {
        return 0;
    }

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.POISONOUS_POTATO, 2);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return false;
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(this.lastTargetMature ? Mth.nextInt(this.random, 900, 1200) : Mth.nextInt(this.random, 400, 600));
    }

    @Override
    public void completeTargetGoal() {
        if (this.level().isClientSide() || this.targetPos == null) return;
        BlockState target = this.level().getBlockState(this.targetPos);
        this.jumpFromGround();
        this.destroyFarmland = 17;
        this.destroyFarmlandPos = this.targetPos.below();
        this.lastTargetMature = (target.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(target));
    }

    @Override
    public boolean canWork() {return true;}

    @Override
    public boolean isTrusting() {return false;}

    @Override
    protected void tryTame(Player player) {
        this.level().broadcastEntityEvent(this, (byte)6);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.destroyFarmlandPos == null) this.destroyFarmland = 0;
            if (this.destroyFarmland > 0) {
                if (this.destroyFarmland == 1 && this.blockPosition().closerThan(this.destroyFarmlandPos, 2)) {
                    BlockState soil = this.level().getBlockState(this.destroyFarmlandPos);
                    Block toDirt = soil.is(Blocks.FARMLAND) ? Blocks.DIRT : soil.is(ModBlocks.SOUL_FARMLAND) ? Blocks.SOUL_SAND : null;
                    if (toDirt != null) {
                        this.level().setBlock(this.destroyFarmlandPos, toDirt.defaultBlockState(), Block.UPDATE_CLIENTS);
                        pushEntitiesUp(Blocks.FARMLAND.defaultBlockState(), toDirt.defaultBlockState(), this.level(), this.destroyFarmlandPos);
                    }
                    BlockState crop = this.level().getBlockState(this.destroyFarmlandPos.above());
                    if (this.lastTargetMature) Block.dropResources(crop, this.level(), this.destroyFarmlandPos.above());
                    this.level().setBlock(this.destroyFarmlandPos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    this.destroyFarmlandPos = null;
                }
                this.destroyFarmland--;
            }
        } else {
            if (this.level().random.nextInt(10) != 0) return;
            double x = this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth();
            double y = this.getY() + this.getBbHeight() * 0.5;
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth();
            this.level().addParticle(PARTICLE_EFFECT, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel serverWorld) {
            if (this.isAlive()) {
                for(LivingEntity mobEntity : serverWorld.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.3), POISON_PREDICATE)) {
                    if (mobEntity.isAlive()) {
                        this.sting(serverWorld, mobEntity);
                    }
                }
            }
        }

    }

    private void sting(ServerLevel world, LivingEntity target) {
        if (target.hurtServer(world, this.damageSources().mobAttack(this), (float)(1))) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 3, 0), this);
            this.playSound(ModSounds.ENTITY_CRITTER_EVIL_STING, 1.0F, 1.0F);
        }
    }

    class DestroyEggGoal extends RemoveBlockGoal {
        DestroyEggGoal(final PathfinderMob mob, final double speed, final int maxYDifference) {
            super(Blocks.TURTLE_EGG, mob, speed, maxYDifference);
        }

        public void playDestroyProgressSound(LevelAccessor world, BlockPos pos) {
            world.playSound(null, pos, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 0.5F, 0.9F + PoisonousPotatoCritterEntity.this.random.nextFloat() * 0.2F);
        }

        public void playBreakSound(Level world, BlockPos pos) {
            world.playSound(null, pos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + world.random.nextFloat() * 0.2F);
        }

        public double acceptedDistance() {
            return 1.14;
        }
    }
}
