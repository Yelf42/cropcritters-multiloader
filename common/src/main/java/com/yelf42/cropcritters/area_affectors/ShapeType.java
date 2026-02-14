package com.yelf42.cropcritters.area_affectors;

import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;

import java.util.Locale;

public enum ShapeType implements StringRepresentable {
    ELLIPSOID {
        @Override
        public boolean isPositionInside(int posX, int posY, int posZ, int width, int height) {
            return (posX * posX + posZ * posZ) / (float) (width * width) + (posY * posY) / (float) (height * height)
                    <= 1.0;
        }
    },
    CYLINDER {
        @Override
        public boolean isPositionInside(int posX, int posY, int posZ, int width, int height) {
            return posX * posX + posZ * posZ <= width * width && posY <= height;
        }
    },
    CUBOID {
        @Override
        public boolean isPositionInside(int posX, int posY, int posZ, int width, int height) {
            return posX <= width && posZ <= width && posY <= height;
        }
    };

    public static final EnumCodec<ShapeType> CODEC = StringRepresentable.fromEnum(ShapeType::values);

    public boolean isPositionInside(BlockPos centerPosition, BlockPos sectionPosition, int width, int height) {
        return this.isPositionInside(centerPosition.getX(),
                centerPosition.getY(),
                centerPosition.getZ(),
                sectionPosition.getX(),
                sectionPosition.getY(),
                sectionPosition.getZ(),
                width,
                height);
    }

    public boolean isPositionInside(int centerX, int centerY, int centerZ, int sectionX, int sectionY, int sectionZ, int width, int height) {
        return this.isPositionInside(Math.abs(centerX - sectionX),
                Math.abs(centerY - sectionY),
                Math.abs(centerZ - sectionZ),
                width,
                height);
    }

    public abstract boolean isPositionInside(int posX, int posY, int posZ, int width, int height);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}