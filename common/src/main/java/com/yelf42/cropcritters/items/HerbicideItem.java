package com.yelf42.cropcritters.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.entity.HerbicideEntity;

public class HerbicideItem extends Item implements ProjectileItem {
    public HerbicideItem(Properties settings) {
        super(settings);
    }

    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (world instanceof ServerLevel serverWorld) {
            Projectile.spawnProjectileFromRotation(HerbicideEntity::new, serverWorld, itemStack, user, -20.0F, 0.5F, 1.0F);
        }

        user.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, user);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        return new HerbicideEntity(pos.x(), pos.y(), pos.z(), world, stack);
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return DispenseConfig.builder().uncertainty(DispenseConfig.DEFAULT.uncertainty() * 0.5F).power(DispenseConfig.DEFAULT.power() * 1.25F).build();
    }
}
