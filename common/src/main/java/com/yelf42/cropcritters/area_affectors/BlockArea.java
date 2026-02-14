package com.yelf42.cropcritters.area_affectors;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public record BlockArea(BlockPos position, int horizontalRange, int verticalRange, ShapeType shape) {
    private static final Map<BlockArea, List<SectionPos>> BLOCK_AREA_SECTIONS = new ConcurrentHashMap<>();
    public static final MapCodec<BlockArea> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(BlockPos.CODEC.fieldOf(
                    "position").forGetter(BlockArea::position),
            ExtraCodecs.POSITIVE_INT.fieldOf("horizontal_range").forGetter(BlockArea::horizontalRange),
            ExtraCodecs.POSITIVE_INT.fieldOf("vertical_range").forGetter(BlockArea::verticalRange),
            ShapeType.CODEC.fieldOf("shape").forGetter(BlockArea::shape)).apply(instance, BlockArea::new));

    BlockArea(AffectorType type, BlockPos position) {
        this(position,
                type.width,
                type.height,
                type.shape);
    }

    public boolean isPositionInside(BlockPos blockPos) {
        return this.shape().isPositionInside(this.position(), blockPos, this.horizontalRange(), this.verticalRange());
    }

    public void getAllSections(Consumer<SectionPos> sectionConsumer) {
        BLOCK_AREA_SECTIONS.computeIfAbsent(this.asKey(), (BlockArea blockArea) -> {
            ImmutableList.Builder<@NotNull SectionPos> builder = ImmutableList.builder();
            getAllSections(this.position(), this.horizontalRange(), this.verticalRange(), this.shape(), builder::add);
            return builder.build();
        }).forEach(sectionConsumer);
    }

    private BlockArea asKey() {
        // we only allow for chunk intervals as range in the config, which allows us to simplify the position here to the section positions
        return new BlockArea(SectionPos.of(this.position()).origin(),
                this.horizontalRange(),
                this.verticalRange(),
                this.shape());
    }

    public static void getAllSections(BlockPos blockPos, int horizontalRange, int verticalRange, ShapeType shapeType, Consumer<SectionPos> ChunkSectionPosConsumer) {
        SectionPos minSection = SectionPos.of(blockPos.offset(-horizontalRange, -verticalRange, -horizontalRange));
        SectionPos maxSection = SectionPos.of(blockPos.offset(horizontalRange, verticalRange, horizontalRange));
        for (int sectionX = minSection.x(); sectionX <= maxSection.x(); sectionX++) {
            int posX = getClosestCoordinate(blockPos.getX(), sectionX);
            for (int sectionY = minSection.y(); sectionY <= maxSection.y(); sectionY++) {
                int posY = getClosestCoordinate(blockPos.getY(), sectionY);
                for (int sectionZ = minSection.z(); sectionZ <= maxSection.z(); sectionZ++) {
                    int posZ = getClosestCoordinate(blockPos.getZ(), sectionZ);
                    if (shapeType.isPositionInside(blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            posX,
                            posY,
                            posZ,
                            horizontalRange,
                            verticalRange)) {
                        ChunkSectionPosConsumer.accept(SectionPos.of(sectionX, sectionY, sectionZ));
                    }
                }
            }
        }
    }

    private static int getClosestCoordinate(int blockCoordinate, int sectionCoordinate) {
        int min = SectionPos.sectionToBlockCoord(sectionCoordinate);
        if (blockCoordinate < min) {
            return min;
        }

        int max = SectionPos.sectionToBlockCoord(sectionCoordinate, 15);
        if (blockCoordinate > max) {
            return max;
        }

        return blockCoordinate;
    }
}