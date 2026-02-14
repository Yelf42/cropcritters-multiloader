package com.yelf42.cropcritters.area_affectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record TypedBlockArea(AffectorType type, BlockArea blockArea) {
    public static final Codec<TypedBlockArea> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AffectorType.CODEC.fieldOf("type").forGetter(TypedBlockArea::type),
            BlockArea.CODEC.forGetter(TypedBlockArea::blockArea)).apply(instance, TypedBlockArea::new));

    TypedBlockArea(AffectorType type, BlockPos position) {
        this(type, new BlockArea(type, position));
    }

    public BlockPos position() {
        return this.blockArea().position();
    }
}