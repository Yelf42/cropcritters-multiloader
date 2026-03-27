package com.yelf42.cropcritters.blocks;

import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SoulPotBlockEntity extends BlockEntity {
    private ItemStack stack = ItemStack.EMPTY;

    public SoulPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUL_POT, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.stack.isEmpty()) {
            tag.put("item", this.stack.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("item")) {
            this.stack = ItemStack.of(tag.getCompound("item"));
        } else {
            this.stack = ItemStack.EMPTY;
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    public Direction getHorizontalFacing() {
        return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public ItemStack getTheItem() {
        return this.stack;
    }

    public ItemStack splitTheItem(int count) {
        ItemStack split = this.stack.split(count);
        if (this.stack.isEmpty()) {
            this.stack = ItemStack.EMPTY;
        }
        syncToClient();
        return split;
    }

    public void increaseStack() {
        this.stack.grow(1);
        syncToClient();
    }

    public void setTheItem(ItemStack stack) {
        this.stack = stack;
        syncToClient();
    }

    public long count() {
        return stack.getCount();
    }

    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    private void syncToClient() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(SoulPotBlock.LEVEL,
                    (int) CropCritters.clamp(this.count() / 2, 0, 12)), 3);
        }
    }
}