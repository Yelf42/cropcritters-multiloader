package com.yelf42.cropcritters.blocks;

import net.minecraft.util.StringRepresentable;

public enum MazewoodShape implements StringRepresentable {
    NONE("none"),
    TALL("tall");

    private final String name;

    private MazewoodShape(final String name) {
        this.name = name;
    }

    public String toString() {
        return this.getSerializedName();
    }

    public String getSerializedName() {
        return this.name;
    }
}
