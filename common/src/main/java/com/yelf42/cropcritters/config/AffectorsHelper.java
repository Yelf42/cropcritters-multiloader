package com.yelf42.cropcritters.config;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.area_affectors.AffectorType;
import com.yelf42.cropcritters.area_affectors.TypedBlockArea;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.blocks.SoulRoseBlock;
import com.yelf42.cropcritters.blocks.SoulRoseType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class AffectorsHelper {

    // Converts block state to affector type
    public static @Nullable AffectorType getTypeFromBlockState(BlockState state) {
        if (state.is(ModBlocks.SOUL_ROSE) && state.getValueOrElse(SoulRoseBlock.HALF, DoubleBlockHalf.UPPER) == DoubleBlockHalf.LOWER) {
            int level = state.getValueOrElse(SoulRoseBlock.LEVEL, 0);
            SoulRoseType type = state.getValueOrElse(SoulRoseBlock.TYPE, SoulRoseType.NONE);
            return switch (level) {
                case 1 -> switch (type) {
                    case GOLD -> AffectorType.SOUL_ROSE_GOLD_1;
                    case COPPER -> AffectorType.SOUL_ROSE_COPPER_1;
                    case IRON -> AffectorType.SOUL_ROSE_IRON_1;
                    default -> null;
                };
                case 2 -> switch (type) {
                    case GOLD -> AffectorType.SOUL_ROSE_GOLD_2;
                    case COPPER -> AffectorType.SOUL_ROSE_COPPER_2;
                    case IRON -> AffectorType.SOUL_ROSE_IRON_2;
                    default -> null;
                };
                case 3 -> switch (type) {
                    case GOLD -> AffectorType.SOUL_ROSE_GOLD_3;
                    case COPPER -> AffectorType.SOUL_ROSE_COPPER_3;
                    case IRON -> AffectorType.SOUL_ROSE_IRON_3;
                    default -> null;
                };
                default -> null;
            };
        }
        return null;
    }

    // Checks if block affected by copper Soul Rose
    public static boolean copperSoulRoseCheck(ServerLevel serverWorld, BlockPos blockPos) {
        AffectorPositions affectorPositions = CropCritters.getAffectorPositions(serverWorld);
        Collection<? extends TypedBlockArea> affectorsInSection = affectorPositions.getAffectorsInSection(blockPos);
        if (!affectorsInSection.isEmpty()) {
            for (TypedBlockArea typedBlockArea : affectorsInSection) {
                AffectorType type = typedBlockArea.type();
                if (type == AffectorType.SOUL_ROSE_COPPER_3 || type == AffectorType.SOUL_ROSE_COPPER_2 || type == AffectorType.SOUL_ROSE_COPPER_1) {
                    if (typedBlockArea.blockArea().isPositionInside(blockPos)) return true;
                }
            }
        }
        return false;
    }

    // returns level if block affected by iron Soul Rose
    public static int ironSoulRoseCheck(ServerLevel serverWorld, BlockPos blockPos) {
        AffectorPositions affectorPositions = CropCritters.getAffectorPositions(serverWorld);
        Collection<? extends TypedBlockArea> affectorsInSection = affectorPositions.getAffectorsInSection(blockPos);
        int largest = 0;
        for (TypedBlockArea area : affectorsInSection) {
            AffectorType type = area.type();
            if ((type == AffectorType.SOUL_ROSE_IRON_3 ||
                    type == AffectorType.SOUL_ROSE_IRON_2 ||
                    type == AffectorType.SOUL_ROSE_IRON_1) &&
                    area.blockArea().isPositionInside(blockPos)) {

                if (type == AffectorType.SOUL_ROSE_IRON_3) return 3;
                largest = Math.max(largest, (type == AffectorType.SOUL_ROSE_IRON_2) ? 2 : 1);
            }
        }
        return largest;
    }

}
