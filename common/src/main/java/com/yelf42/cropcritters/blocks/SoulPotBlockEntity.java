package com.yelf42.cropcritters.blocks;

import com.yelf42.cropcritters.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentGetter;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SoulPotBlockEntity extends BlockEntity implements RandomizableContainer, ContainerSingleItem.BlockContainerSingleItem {
    public long lastWobbleTime;
    public @Nullable WobbleType lastWobbleType;
    private ItemStack stack;
    protected @Nullable ResourceKey<LootTable> lootTableId;
    protected long lootTableSeed;

    public SoulPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUL_POT, pos, state);
        this.stack = ItemStack.EMPTY;
    }

    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        if (!this.trySaveLootTable(view) && !this.stack.isEmpty()) {
            view.store("item", ItemStack.CODEC, this.stack);
        }

    }

    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        if (!this.tryLoadLootTable(view)) {
            this.stack = view.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
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

    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.stack = (components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).copyOne();
    }

    public void removeComponentsFromTag(ValueOutput view) {
        super.removeComponentsFromTag(view);
        view.discard("item");
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
        }

        return itemStack;
    }

    public void increaseStack() {
        this.unpackLootTable(null);
        this.stack.grow(1);

        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void setTheItem(ItemStack stack) {
        this.unpackLootTable(null);
        this.stack = stack;

        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    public void wobble(WobbleType wobbleType) {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, wobbleType.ordinal());
        }
    }

    public boolean triggerEvent(int type, int data) {
        if (this.level != null && type == 1 && data >= 0 && data < WobbleType.values().length) {
            this.lastWobbleTime = this.level.getGameTime();
            this.lastWobbleType = WobbleType.values()[data];
            return true;
        } else {
            return super.triggerEvent(type, data);
        }
    }

    public static enum WobbleType {
        POSITIVE(7),
        NEGATIVE(10);

        public final int lengthInTicks;

        private WobbleType(final int lengthInTicks) {
            this.lengthInTicks = lengthInTicks;
        }
    }
}