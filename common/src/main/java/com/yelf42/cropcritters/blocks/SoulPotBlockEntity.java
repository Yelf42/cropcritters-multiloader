package com.yelf42.cropcritters.blocks;

import com.yelf42.cropcritters.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.ticks.ContainerSingleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoulPotBlockEntity extends BlockEntity implements RandomizableContainer, ContainerSingleItem.BlockContainerSingleItem {
    private ItemStack stack;
    protected @Nullable ResourceKey<LootTable> lootTableId;
    protected long lootTableSeed;

    public SoulPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUL_POT, pos, state);
        this.stack = ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag) && !this.stack.isEmpty()) {
            tag.put("item", this.stack.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!this.tryLoadLootTable(tag)) {
            if (tag.contains("item")) {
                this.stack = ItemStack.parse(registries, tag.getCompound("item")).orElse(ItemStack.EMPTY);
            } else {
                this.stack = ItemStack.EMPTY;
            }
        } else {
            this.stack = ItemStack.EMPTY;
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public Direction getHorizontalFacing() {
        return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public @Nullable ResourceKey<LootTable> getLootTable() {
        return this.lootTableId;
    }

    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.lootTableId = lootTable;
    }

    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setLootTableSeed(long lootTableSeed) {
        this.lootTableSeed = lootTableSeed;
    }

    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(this.stack)));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        this.stack = components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne();
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("item");
    }

    public ItemStack getTheItem() {
        this.unpackLootTable(null);
        return this.stack;
    }

    public ItemStack splitTheItem(int count) {
        this.unpackLootTable(null);
        ItemStack itemStack = this.stack.split(count);
        if (this.stack.isEmpty()) {
            this.stack = ItemStack.EMPTY;
        }

        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(SoulPotBlock.LEVEL, Math.clamp(this.count() / 2, 0, 12)), 3);
        }

        return itemStack;
    }

    public void increaseStack() {
        this.unpackLootTable(null);
        this.stack.grow(1);

        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(SoulPotBlock.LEVEL, Math.clamp(this.count() / 2, 0, 12)), 3);
        }
    }

    public void setTheItem(ItemStack stack) {
        this.unpackLootTable(null);
        this.stack = stack;

        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(SoulPotBlock.LEVEL, Math.clamp(this.count() / 2, 0, 12)), 3);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    public long count() {
        return stack.getCount();
    }
}