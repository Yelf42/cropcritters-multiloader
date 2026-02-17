package com.yelf42.cropcritters.events;

import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.blocks.*;
import com.yelf42.cropcritters.registry.ModItems;
import com.yelf42.cropcritters.registry.ModSounds;

// Converted from ModEvents, which was all FabricAPI callbacks
// Now functionality is found in Mixins
public class ModEventHandlers {

    public static boolean handleHoeUse(Level world, BlockPos pos, BlockState state) {
        if (world.isClientSide()) {
            return (state.is(Blocks.SOUL_SOIL) || state.is(Blocks.SOUL_SAND));
        }

        // Soul Soil Tilling
        if (state.is(Blocks.SOUL_SOIL)) {
            world.setBlock(pos, ModBlocks.SOUL_FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
            world.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        // Soul Sand to Soil
        if (state.is(Blocks.SOUL_SAND)) {
            world.setBlock(pos, Blocks.SOUL_SOIL.defaultBlockState(), Block.UPDATE_ALL);
            world.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }


    public static boolean handleShearsUse(Player player, Level world, ItemStack stack, BlockPos pos, BlockState state) {
        if (world.isClientSide()) {
            return (state.is(ModBlocks.STRANGLE_FERN)
                    || (state.is(ModBlocks.POPPER_PLANT) && state.getOptionalValue(PopperPlantBlock.AGE).orElse(0) == PopperPlantBlock.MAX_AGE));
        }

        // Snip Strangle Fern
        if (state.is(ModBlocks.STRANGLE_FERN)) {
            StrangleFernBlockEntity sfbe = (StrangleFernBlockEntity) world.getBlockEntity(pos);
            BlockState infested = Blocks.DEAD_BUSH.defaultBlockState();
            if (sfbe != null) {
                infested = sfbe.getInfestedState();
            }
            world.setBlock(pos, infested, Block.UPDATE_ALL);
            if (!player.isCreative()) stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
            world.playSound(null, pos, SoundEvents.BEEHIVE_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            return true;
        }

        // Harvest Popper Pod
        if (state.is(ModBlocks.POPPER_PLANT) && state.getOptionalValue(PopperPlantBlock.AGE).orElse(0) == PopperPlantBlock.MAX_AGE) {
            Vec3 center = pos.getCenter();
            ItemStack itemStack = new ItemStack(ModItems.POPPER_POD);
            ItemEntity itemEntity = new ItemEntity(world, center.x, center.y, center.z, itemStack);
            itemEntity.setDefaultPickUpDelay();
            ((ServerLevel) world).addFreshEntity(itemEntity);
            world.setBlockAndUpdate(pos, state.setValue(PopperPlantBlock.AGE, 0));
            if (!player.isCreative()) stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
            world.playSound(null, pos, SoundEvents.BEEHIVE_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }

    public static boolean handleStrangleFernPlanting(Player player, Level world, ItemStack stack, BlockPos pos, BlockState state) {
        if (world.isClientSide()) return false;

        BlockState toPlant = ModBlocks.STRANGLE_FERN.defaultBlockState();
        if (toPlant.canSurvive(world, pos) && StrangleFern.canInfest(state)) {
            world.setBlockAndUpdate(pos, toPlant);
            stack.consume(1, player);
            world.playSound(null, pos, ModSounds.SPORE_INFEST, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }

}
