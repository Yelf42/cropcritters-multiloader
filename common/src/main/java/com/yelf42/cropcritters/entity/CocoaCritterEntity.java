package com.yelf42.cropcritters.entity;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import com.yelf42.cropcritters.config.RecognizedCropsState;
import com.yelf42.cropcritters.registry.ModItems;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.*;
import java.util.function.Predicate;

public class CocoaCritterEntity extends AbstractCropCritterEntity {

    private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER = (entity) -> !entity.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity);
    private static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = (item) -> !item.hasPickUpDelay() && item.isAlive();
    public static final RawAnimation HOLD = RawAnimation.begin().thenPlayAndHold("holding");
    private static final Set<Item> DEFAULT_KNOWN_ITEMS = new HashSet<>();

    private static final int MAX_HOPPER_DISTANCE = 32;

    @Nullable
    BlockPos hopperPos;

    static {
        DEFAULT_KNOWN_ITEMS.add(ModItems.STRANGE_FERTILIZER);
        DEFAULT_KNOWN_ITEMS.add(ModItems.LOST_SOUL);
        DEFAULT_KNOWN_ITEMS.add(Items.COCOA_BEANS);
        DEFAULT_KNOWN_ITEMS.add(Items.WHEAT_SEEDS);
        DEFAULT_KNOWN_ITEMS.add(Items.WHEAT);
        DEFAULT_KNOWN_ITEMS.add(Items.CARROT);
        DEFAULT_KNOWN_ITEMS.add(Items.POTATO);
        DEFAULT_KNOWN_ITEMS.add(Items.POISONOUS_POTATO);
        DEFAULT_KNOWN_ITEMS.add(Items.MELON_SLICE);
        DEFAULT_KNOWN_ITEMS.add(Items.MELON_SEEDS);
        DEFAULT_KNOWN_ITEMS.add(Items.PUMPKIN_SEEDS);
        DEFAULT_KNOWN_ITEMS.add(Items.TORCHFLOWER);
        DEFAULT_KNOWN_ITEMS.add(Items.TORCHFLOWER_SEEDS);
        DEFAULT_KNOWN_ITEMS.add(Items.PITCHER_PLANT);
        DEFAULT_KNOWN_ITEMS.add(Items.PITCHER_POD);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.storeNullable("hopper_pos", BlockPos.CODEC, this.hopperPos);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.hopperPos = (BlockPos)view.read("hopper_pos", BlockPos.CODEC).orElse(null);
    }


    public CocoaCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
        this.setCanPickUpLoot(true);
    }

    protected void registerGoals() {
        net.minecraft.world.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.is(ModItems.LOST_SOUL), true);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, temptGoal);
        this.goalSelector.addGoal(6, new AvoidEntityGoal<>(this, Player.class, 10.0F, 1.6, 1.4, (entity) -> NOTICEABLE_PLAYER_FILTER.test(entity) && !this.isTrusting()));
        this.goalSelector.addGoal(7, new DepositInHopperGoal());
        this.targetWorkGoal = new TargetWorkGoal();
        this.goalSelector.addGoal(8, this.targetWorkGoal);
        this.goalSelector.addGoal(9, new PickupItemGoal());
        this.goalSelector.addGoal(12, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(20, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(20, new RandomLookAroundGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                DefaultAnimations.genericWalkIdleController(),
                new AnimationController<>("Hold", test -> {
                    if ((this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty())) {
                        //test.controller().reset();
                        return PlayState.STOP;
                    } else {
                        return (test.setAndContinue(HOLD));
                    }
                    //return this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty() ? PlayState.STOP : (test.setAndContinue(HOLD));
                })
        );
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> ((blockState.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(blockState)))
                || (blockState.getBlock() instanceof NetherWartBlock && blockState.getValueOrElse(NetherWartBlock.AGE, 0) >= NetherWartBlock.MAX_AGE));
    }

    @Override
    protected int getTargetOffset() {
        return 0;
    }

    @Override
    protected boolean canWork() {
        return this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
    }

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.COCOA_BEANS, 3);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.is(Items.COCOA_BEANS);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(Mth.nextInt(this.random, 500, 600));
    }


    @Override
    public void completeTargetGoal() {
        if (!this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() || this.targetPos == null || this.level().isClientSide()) return;
        ServerLevel world = (ServerLevel) this.level();
        BlockState state = world.getBlockState(this.targetPos);
        if (!getTargetBlockFilter().test(state)) return;

        LootParams.Builder builder = (new LootParams.Builder(world)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.targetPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        List<ItemStack> items = state.getDrops(builder);
        if (items.isEmpty()) return;

        int index = world.random.nextInt(items.size());
        ItemStack toDrop = items.remove(index);
        toDrop.setCount(Math.clamp(toDrop.getCount(), 1, 5));
        this.setItemSlot(EquipmentSlot.MAINHAND, toDrop);
        this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        recordCrop(toDrop.getItem());
        items.forEach((stack) -> {
            recordCrop(stack.getItem());
            Block.popResource(world, this.targetPos, stack);
        });

        world.levelEvent(this, 2001, this.targetPos, Block.getId(state));
        world.setBlock(this.targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private void recordCrop(Item item) {
        if (this.level().isClientSide()) return;
        ServerLevel world = (ServerLevel)this.level();
        RecognizedCropsState state = RecognizedCropsState.getServerState(world.getServer());
        state.addCrop(item);
    }

    private boolean checkCrop(Item item) {
        if (this.level().isClientSide()) return false;
        if (DEFAULT_KNOWN_ITEMS.contains(item)) return true;
        ServerLevel world = (ServerLevel)this.level();
        RecognizedCropsState state = RecognizedCropsState.getServerState(world.getServer());
        return state.hasCrop(item);
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel world, DamageSource damageSource) {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!itemStack.isEmpty()) {
            this.spawnAtLocation(world, itemStack);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        super.dropAllDeathLoot(world, damageSource);
    }

    // Right click with empty hand makes critter try drop held item
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult actionResult = super.mobInteract(player, hand);
        if (!this.level().isClientSide() && !actionResult.consumesAction() && this.isTrusting() && player.getItemInHand(hand).isEmpty()) {
            if (tryPutDown(this.getItemBySlot(EquipmentSlot.MAINHAND), true)) return InteractionResult.SUCCESS;
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
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return itemStack.isEmpty() && checkCrop(stack.getItem());
    }

    private void dropItem(ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack);
        this.level().addFreshEntity(itemEntity);
    }

    @Override
    protected void pickUpItem(ServerLevel world, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (this.canHoldItem(itemStack)) {
            int i = itemStack.getCount();
            if (i > 5) {
                this.dropItem(itemStack.split(i - 5));
            }
            this.onItemPickup(itemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack.split(5));
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take(itemEntity, itemStack.getCount());
            itemEntity.discard();
        }

    }

    protected boolean validHopperPos() {
        return this.hopperPos != null
                && this.level().getBlockState(this.hopperPos).is(Blocks.HOPPER)
                && this.hopperPos.closerToCenterThan(this.position(), MAX_HOPPER_DISTANCE);
    }

    class DepositInHopperGoal extends Goal {
        protected Long2LongOpenHashMap unreachableTargetsPosCache = new Long2LongOpenHashMap();
        protected int ticks;
        protected Vec3 nextTarget;

        @Override
        public boolean canUse() {
            if (!CocoaCritterEntity.this.isTrusting() || CocoaCritterEntity.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) return false;
            if (validHopperPos()) return true;
            Optional<BlockPos> optional = this.getTargetBlock();
            if (optional.isPresent()) {
                CocoaCritterEntity.this.hopperPos = optional.get();
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            this.ticks = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return validHopperPos() && !CocoaCritterEntity.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
        }

        @Override
        public void tick() {
            ++this.ticks;
            if (this.ticks > 300 || !validHopperPos()) {
                CocoaCritterEntity.this.hopperPos = null;
            } else {
                // validHopperPos() checks for null, ignore warning
                Vec3 vec3d = Vec3.atBottomCenterOf(CocoaCritterEntity.this.hopperPos).add(0.0F, 1, 0.0F);
                if (vec3d.distanceToSqr(CocoaCritterEntity.this.position()) > (double)1.0F) {
                    this.nextTarget = vec3d;
                    this.moveToNextTarget();
                } else {
                    if (this.nextTarget == null) {
                        this.nextTarget = vec3d;
                    }

                    boolean bl = CocoaCritterEntity.this.position().distanceTo(this.nextTarget) <= 0.5;
                    if (!bl && this.ticks > 300) {
                        CocoaCritterEntity.this.hopperPos = null;
                    } else if (bl) {
                        ItemStack stack = CocoaCritterEntity.this.getItemBySlot(EquipmentSlot.MAINHAND);
                        CocoaCritterEntity.this.tryPutDown(stack, false);
                    } else {
                        CocoaCritterEntity.this.getMoveControl().setWantedPosition(this.nextTarget.x(), this.nextTarget.y(), this.nextTarget.z(), 0.8F);
                    }
                }
            }
        }

        protected void moveToNextTarget() {
            CocoaCritterEntity.this.navigation.moveTo(CocoaCritterEntity.this.navigation.createPath(this.nextTarget.x(), this.nextTarget.y(), this.nextTarget.z(), 0), 1.2F);
        }

        protected boolean checkHopper(BlockPos blockPos) {
            return (CocoaCritterEntity.this.level().getBlockState(blockPos).is(Blocks.HOPPER))
                    && (CocoaCritterEntity.this.level().getBlockState(blockPos.above()).isPathfindable(PathComputationType.LAND));
        }

        protected Optional<BlockPos> getTargetBlock() {
            Iterable<BlockPos> iterable = BlockPos.withinManhattan(CocoaCritterEntity.this.blockPosition(), 12, 2, 12);
            Long2LongOpenHashMap long2LongOpenHashMap = new Long2LongOpenHashMap();

            for(BlockPos blockPos : iterable) {
                long l = this.unreachableTargetsPosCache.getOrDefault(blockPos.asLong(), Long.MIN_VALUE);
                if (CocoaCritterEntity.this.level().getGameTime() < l) {
                    long2LongOpenHashMap.put(blockPos.asLong(), l);
                } else if (checkHopper(blockPos)) {
                    Path path = CocoaCritterEntity.this.navigation.createPath(blockPos, 0);
                    if (path != null && path.canReach()) {
                        return Optional.of(blockPos);
                    }
                    long2LongOpenHashMap.put(blockPos.asLong(), CocoaCritterEntity.this.level().getGameTime() + 600L);
                }
            }
            this.unreachableTargetsPosCache = long2LongOpenHashMap;
            return Optional.empty();
        }

    }

    class PickupItemGoal extends Goal {
        public PickupItemGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            if (!CocoaCritterEntity.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) return false;
            if (CocoaCritterEntity.this.getRandom().nextInt(reducedTickDelay(10)) != 0) {
                return false;
            } else {
                List<ItemEntity> list = CocoaCritterEntity.this.level().getEntitiesOfClass(ItemEntity.class, CocoaCritterEntity.this.getBoundingBox().inflate(8.0F, 8.0F, 8.0F), CocoaCritterEntity.PICKABLE_DROP_FILTER);
                return !list.isEmpty();
            }
        }

        public void tick() {
            List<ItemEntity> list = CocoaCritterEntity.this.level().getEntitiesOfClass(ItemEntity.class, CocoaCritterEntity.this.getBoundingBox().inflate(8.0F, 8.0F, 8.0F), CocoaCritterEntity.PICKABLE_DROP_FILTER);
            ItemStack itemStack = CocoaCritterEntity.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (itemStack.isEmpty() && !list.isEmpty()) {
                moveTowardsCropItem(list);
            }
        }

        public void start() {
            List<ItemEntity> list = CocoaCritterEntity.this.level().getEntitiesOfClass(ItemEntity.class, CocoaCritterEntity.this.getBoundingBox().inflate(8.0F, 8.0F, 8.0F), CocoaCritterEntity.PICKABLE_DROP_FILTER);
            if (!list.isEmpty()) {
                moveTowardsCropItem(list);
            }
        }

        private void moveTowardsCropItem(List<ItemEntity> list) {
            for (ItemEntity itemEntity : list) {
                if (CocoaCritterEntity.this.checkCrop(itemEntity.getItem().getItem())) {
                    CocoaCritterEntity.this.getNavigation().moveTo(itemEntity, 1.2F);
                    return;
                }
            }
        }
    }

}
