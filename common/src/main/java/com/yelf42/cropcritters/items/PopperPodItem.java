package com.yelf42.cropcritters.items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.entity.PopperPodEntity;

public class PopperPodItem extends Item implements ProjectileItem {

    public PopperPodItem(Properties settings) {
        super(settings);
    }

    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Player playerEntity = context.getPlayer();
        if (playerEntity != null && playerEntity.isFallFlying()) {
            return InteractionResult.PASS;
        } else {
            if (world instanceof ServerLevel serverWorld) {
                ItemStack itemStack = context.getItemInHand();
                Vec3 vec3d = context.getClickLocation();
                Direction direction = context.getClickedFace();
                Projectile.spawnProjectile(new PopperPodEntity(world, context.getPlayer(), vec3d.x + (double)direction.getStepX() * 0.15, vec3d.y + (double)direction.getStepY() * 0.15, vec3d.z + (double)direction.getStepZ() * 0.15, itemStack), serverWorld, itemStack);
                itemStack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }
    }

    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (user.isFallFlying()) {
            ItemStack itemStack = user.getItemInHand(hand);
            if (world instanceof ServerLevel) {
                ServerLevel serverWorld = (ServerLevel)world;
                if (user.dropAllLeashConnections((Player)null)) {
                    world.playSound((Entity)null, user, SoundEvents.LEAD_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
                }

                Projectile.spawnProjectile(new PopperPodEntity(world, itemStack, user), serverWorld, itemStack);
                itemStack.consume(1, user);
                user.awardStat(Stats.ITEM_USED.get(this));
            }

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        return new PopperPodEntity(world, stack.copyWithCount(1), pos.x(), pos.y(), pos.z(), true);
    }

    public DispenseConfig createDispenseConfig() {
        return DispenseConfig.builder().positionFunction(PopperPodItem::position).uncertainty(1.0F).power(0.5F).overrideDispenseEvent(1004).build();
    }

    private static Vec3 position(BlockSource pointer, Direction facing) {
        return pointer.center().add((double)facing.getStepX() * 0.5f, (double)facing.getStepY() * 0.5f, (double)facing.getStepZ() * 0.5f);
    }
}