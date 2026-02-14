package com.yelf42.cropcritters.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum SoulRoseType implements StringRepresentable {
    NONE("none"),
    GOLD("gold"),
    COPPER("copper"),
    IRON("iron");

    private final String name;

    private SoulRoseType(final String name) {
        this.name = name;
    }

    public static SoulRoseType getType(BlockState state, int level) {
        if (level == 0) return NONE;
        if (state.is(Blocks.RAW_GOLD_BLOCK)) return GOLD;
        if (state.is(Blocks.RAW_COPPER_BLOCK)) return COPPER;
        if (state.is(Blocks.RAW_IRON_BLOCK)) return IRON;

        return NONE;
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}