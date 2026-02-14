package com.yelf42.cropcritters.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.entity.SeedBallProjectileEntity;
import com.yelf42.cropcritters.registry.ModSounds;

public class SeedBallItem extends Item implements ProjectileItem {
    public SeedBallItem(Properties settings) {
        super(settings);
    }

    @Override
    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        return new SeedBallProjectileEntity(pos.x(), pos.y(), pos.z(), world, stack);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.THROW_SEED_BALL, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (world instanceof ServerLevel serverWorld) {
            Projectile.spawnProjectileFromRotation(SeedBallProjectileEntity::new, serverWorld, itemStack, user, 0.0F, 1.5F, 1.0F);
        }

        itemStack.consume(1, user);
        return InteractionResult.SUCCESS;
    }
}
