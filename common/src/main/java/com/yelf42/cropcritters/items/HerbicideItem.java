package com.yelf42.cropcritters.items;

import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.entity.HerbicideEntity;

public class HerbicideItem extends Item {
    public HerbicideItem(Properties settings) {
        super(settings);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (world instanceof ServerLevel serverWorld) {
            HerbicideEntity herbicide = new HerbicideEntity(serverWorld, user);
            herbicide.shootFromRotation(user, user.getXRot(), user.getYRot(), -20.0F, 0.5F, 1.0F);
            serverWorld.addFreshEntity(herbicide);
        }

        user.awardStat(Stats.ITEM_USED.get(this));
        itemStack.shrink(1);
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }
}
