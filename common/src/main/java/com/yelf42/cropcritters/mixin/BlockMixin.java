package com.yelf42.cropcritters.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.yelf42.cropcritters.config.AffectorsHelper;
import com.yelf42.cropcritters.registry.ModParticles;

import java.util.List;

@Mixin(Block.class)
public class BlockMixin {

    // Iron soul rose chance to gain more crop yield
    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void onGetDroppedStacks4Param(BlockState state, ServerLevel world, BlockPos pos, @Nullable BlockEntity blockEntity, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.getBlock() instanceof CropBlock cropBlock && !cropBlock.isValidBonemealTarget(world, pos, state)) {
            if (world.random.nextDouble() < 0.15F * AffectorsHelper.ironSoulRoseCheck(world, pos)) {
                List<ItemStack> list = cir.getReturnValue();
                for (int i = list.size() - 1; i >= 0; i--) {
                    list.add(i + 1, list.get(i));
                }
                world.sendParticles(ModParticles.SOUL_GLINT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 7, 0.5, 0.5, 0.5, 0.0);
                cir.setReturnValue(list);
            }
        }
    }
    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void onGetDroppedStacks6Param(BlockState state, ServerLevel world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.getBlock() instanceof CropBlock cropBlock && !cropBlock.isValidBonemealTarget(world, pos, state)) {
            if (world.random.nextDouble() < 0.15F * AffectorsHelper.ironSoulRoseCheck(world, pos)) {
                List<ItemStack> list = cir.getReturnValue();
                for (int i = list.size() - 1; i >= 0; i--) {
                    list.add(i + 1, list.get(i));
                }
                world.sendParticles(ModParticles.SOUL_GLINT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 7, 0.5, 0.5, 0.5, 0.0);
                cir.setReturnValue(list);
            }
        }
    }
}
