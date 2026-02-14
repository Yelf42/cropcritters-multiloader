package com.yelf42.cropcritters.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.events.ModEventHandlers;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onStrangleFernSporesUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level world = context.getLevel();
        if (world.isClientSide()) return;

        Player player = context.getPlayer();
        if (player == null) return;

        ItemStack stack = player.getItemInHand(context.getHand());
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);

        if (stack.is(ModBlocks.STRANGLE_FERN.asItem()) && ModEventHandlers.handleStrangleFernPlanting(player, world, stack, pos, state)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
