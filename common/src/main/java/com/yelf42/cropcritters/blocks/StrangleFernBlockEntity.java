package com.yelf42.cropcritters.blocks;

import com.yelf42.cropcritters.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;

public class StrangleFernBlockEntity extends BlockEntity {
    private BlockState infestedState = Blocks.DEAD_BUSH.defaultBlockState();

    public StrangleFernBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STRANGLE_FERN, pos, state);
        if (Math.random() > 0.5) infestedState = Blocks.SHORT_GRASS.defaultBlockState();
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        infestedState = view.read("InfestedState", BlockState.CODEC).orElse(Blocks.DEAD_BUSH.defaultBlockState());
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.store("InfestedState", BlockState.CODEC, infestedState);
    }

    public BlockState getInfestedState() {
        return infestedState;
    }

    public void setInfestedState(BlockState state) {
        infestedState = state;
        updateListeners();
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    private void updateListeners() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
}
