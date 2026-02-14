package com.yelf42.cropcritters.mixin;

import com.yelf42.cropcritters.events.ModEventHandlers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public abstract class HoeItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onHoeUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return;

        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);

        if (ModEventHandlers.handleHoeUse(world, pos, state)) {
            context.getItemInHand().hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
