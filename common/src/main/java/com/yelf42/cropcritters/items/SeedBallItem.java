package com.yelf42.cropcritters.items;

import com.yelf42.cropcritters.registry.ModComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.entity.SeedBallProjectileEntity;
import com.yelf42.cropcritters.registry.ModSounds;

import java.util.List;

public class SeedBallItem extends Item implements ProjectileItem {
    public SeedBallItem(Properties settings) {
        super(settings);
    }

    @Override
    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        return new SeedBallProjectileEntity(pos.x(), pos.y(), pos.z(), world, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.THROW_SEED_BALL, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (world instanceof ServerLevel serverWorld) {
            SeedBallProjectileEntity seedBall = new SeedBallProjectileEntity(serverWorld, user, itemStack);
            seedBall.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F, 1.0F);
            serverWorld.addFreshEntity(seedBall);
        }

        itemStack.consume(1, user);
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        ModComponents.PoisonousComponent poisonous = stack.get(ModComponents.POISONOUS_SEED_BALL);
        if (poisonous != null) {
            poisonous.addToTooltip(context, tooltipComponents::add, tooltipFlag);
        }

        // Get and render seed types component
        ModComponents.SeedTypesComponent seedTypes = stack.get(ModComponents.SEED_TYPES);
        if (seedTypes != null) {
            seedTypes.addToTooltip(context, tooltipComponents::add, tooltipFlag);
        }
    }
}
