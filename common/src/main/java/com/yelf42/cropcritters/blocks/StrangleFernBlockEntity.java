package com.yelf42.cropcritters.blocks;

import com.yelf42.cropcritters.registry.ModBlockEntities;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.BlockPos;

public class StrangleFernBlockEntity extends BlockEntity {
    private BlockState infestedState = Blocks.DEAD_BUSH.defaultBlockState();

    public StrangleFernBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STRANGLE_FERN, pos, state);
        if (Math.random() > 0.5) infestedState = Blocks.GRASS.defaultBlockState();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("InfestedState")) {
            infestedState = BlockState.CODEC.parse(NbtOps.INSTANCE, tag.get("InfestedState"))
                    .resultOrPartial(e -> {})
                    .orElse(Blocks.DEAD_BUSH.defaultBlockState());
        } else {
            infestedState = Blocks.DEAD_BUSH.defaultBlockState();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        BlockState.CODEC.encodeStart(NbtOps.INSTANCE, infestedState)
                .resultOrPartial(e -> {})
                .ifPresent(nbt -> tag.put("InfestedState", nbt));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
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

    private void updateListeners() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
}
