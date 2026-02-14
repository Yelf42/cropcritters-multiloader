package com.yelf42.cropcritters.area_affectors;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum AffectorType implements StringRepresentable {
    DEFAULT(ShapeType.CUBOID, 0, 0),

    // Applies soul siphon affect, which kills undead and bone-meals crops / spawns critters
    SOUL_ROSE_GOLD_1(ShapeType.CYLINDER, 6, 3),
    SOUL_ROSE_GOLD_2(ShapeType.CYLINDER, 13, 3),
    SOUL_ROSE_GOLD_3(ShapeType.CYLINDER, 20, 3),

    // Slows crop growth down, but stops the growth of weeds
    SOUL_ROSE_COPPER_1(ShapeType.CYLINDER, 6, 3),
    SOUL_ROSE_COPPER_2(ShapeType.CYLINDER, 13, 3),
    SOUL_ROSE_COPPER_3(ShapeType.CYLINDER, 20, 3),

    SOUL_ROSE_IRON_1(ShapeType.CYLINDER, 6, 3),
    SOUL_ROSE_IRON_2(ShapeType.CYLINDER, 13, 3),
    SOUL_ROSE_IRON_3(ShapeType.CYLINDER, 20, 3);

    public final ShapeType shape;
    public final int width;
    public final int height;

    AffectorType(ShapeType shape, int width, int height) {
        this.shape = shape;
        this.width = width;
        this.height = height;
    }

    public static final Codec<AffectorType> CODEC = StringRepresentable.fromEnum(AffectorType::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT) + " | " + this.shape.toString() + " | (" + this.width + " x " + this.height + ")";
    }
}