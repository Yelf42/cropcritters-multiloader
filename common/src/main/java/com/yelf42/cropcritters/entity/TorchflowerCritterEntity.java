package com.yelf42.cropcritters.entity;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.ticks.TickPriority;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.registry.ModItems;

import java.util.function.Predicate;

public class TorchflowerCritterEntity extends AbstractCropCritterEntity {
    private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER = (entity) -> !entity.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity);
    private static final Predicate<Entity> FARM_ANIMALS_FILTER = (entity -> entity.getType().is(CropCritters.SCARE_CRITTERS));


    public TorchflowerCritterEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {return null;}
    @Override
    protected int getTargetOffset() {return 0;}

    @Override
    protected Tuple<Item, Integer> getLoot() {
        return new Tuple<>(Items.TORCHFLOWER, 1);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {return itemStack.is(Items.TORCHFLOWER);}

    @Override
    protected int resetTicksUntilCanWork() {return 0;}
    @Override
    public void completeTargetGoal() {}

    @Override
    protected void registerGoals() {
        net.minecraft.world.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.is(ModItems.LOST_SOUL), true);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, temptGoal);
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, (double)2.0F, 6.0F, 2.0F));
        this.goalSelector.addGoal(8, new AvoidEntityGoal<>(this, Animal.class, 10.0F, 1.6, 1.4, (entity) -> FARM_ANIMALS_FILTER.test(entity) && !this.isTrusting()));
        this.goalSelector.addGoal(8, new AvoidEntityGoal<>(this, Player.class, 10.0F, 1.6, 1.4, (entity) -> NOTICEABLE_PLAYER_FILTER.test(entity) && !this.isTrusting()));
        this.goalSelector.addGoal(12, new WaterAvoidingRandomStrollGoal(this, (double)1.0F));
        this.goalSelector.addGoal(20, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(20, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        Level world = this.level();
        BlockPos pos = this.blockPosition().above();
        if (world.getBlockState(pos).isAir()) {
            world.setBlockAndUpdate(pos, ModBlocks.TORCHFLOWER_SPARK.defaultBlockState());
            world.scheduleTick(pos, ModBlocks.TORCHFLOWER_SPARK, 200, TickPriority.EXTREMELY_LOW);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult actionResult = super.mobInteract(player, hand);
        //CropCritters.LOGGER.info(this.getOwner() == null ? "null" : "present");
        if (!actionResult.consumesAction() && this.isTrusting() && this.isOwnedBy(player)) {
            this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.SUCCESS;
        }
        return actionResult;
    }

    @Override
    protected void tryTame(Player player) {
        super.tryTame(player);
        if (this.isTame()) this.setOrderedToSit(true);
    }


}
