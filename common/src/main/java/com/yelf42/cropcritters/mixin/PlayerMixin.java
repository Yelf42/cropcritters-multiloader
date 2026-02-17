package com.yelf42.cropcritters.mixin;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.tags.ItemTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.config.ConfigManager;
import com.yelf42.cropcritters.registry.ModItems;

import java.util.Optional;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "killedEntity", at = @At("HEAD"))
    public void dropLostSouls(ServerLevel world, LivingEntity killedEntity, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        ItemStack stack = player.getMainHandItem();

        if (killedEntity.getType().is(CropCritters.HAS_LOST_SOUL)) {
            int dropChance = ConfigManager.CONFIG.lostSoulDropChance;

            // Hoe +| Critter
            if (stack.is(ItemTags.HOES)) {
                dropChance += ConfigManager.CONFIG.lostSoulDropChance;
                dropChance += (killedEntity.getType().is(CropCritters.CROP_CRITTERS)) ? ConfigManager.CONFIG.lostSoulDropChance : 0;
            }

            // Silk Touch
            Optional<Holder.Reference<Enchantment>> e = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.SILK_TOUCH);
            if (e.isPresent()) {
                dropChance += (EnchantmentHelper.getItemEnchantmentLevel(e.get(), stack) > 0) ? 2 * ConfigManager.CONFIG.lostSoulDropChance : 0;
            }

            // Looting
            Optional<Holder.Reference<Enchantment>> e2 = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.LOOTING);
            if (e2.isPresent()) {
                dropChance += (ConfigManager.CONFIG.lostSoulDropChance / 2) * EnchantmentHelper.getItemEnchantmentLevel(e2.get(), stack);
            }

            if (world.random.nextInt(100) + 1 < dropChance) {
                Vec3 pos = killedEntity.position();
                ItemEntity ls = new ItemEntity(world, pos.x, pos.y, pos.z, new ItemStack(ModItems.LOST_SOUL));
                ls.setDefaultPickUpDelay();
                world.addFreshEntity(ls);
            }
        }
    }

}
