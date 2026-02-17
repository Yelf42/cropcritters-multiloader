package com.yelf42.cropcritters.area_affectors;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.config.AffectorsHelper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AffectorPositions {
    public static final AffectorPositions EMPTY = new AffectorPositions();
    public static final Codec<AffectorPositions> CODEC = TypedBlockArea.CODEC.listOf()
            .xmap(AffectorPositions::new, (AffectorPositions AffectorPositions) -> {
                return new ArrayList<>(AffectorPositions.affectorPositions.values());
            });

    private final Map<BlockPos, TypedBlockArea> affectorPositions;
    private final Long2ObjectMap<Map<BlockPos, TypedBlockArea>> sectionPositions;

    private AffectorPositions() {
        this.affectorPositions = Collections.emptyMap();
        this.sectionPositions = Long2ObjectMaps.emptyMap();
    }

    private AffectorPositions(List<TypedBlockArea> AffectorPositions) {
        this(AffectorPositions.stream().collect(Collectors.toMap(TypedBlockArea::position, Function.identity())),
                createChunkSectionPositions(AffectorPositions));
    }

    private AffectorPositions(Map<BlockPos, TypedBlockArea> AffectorPositions, Long2ObjectMap<Map<BlockPos, TypedBlockArea>> sectionPositions) {
        this.affectorPositions = AffectorPositions;
        this.sectionPositions = sectionPositions;
    }

    private static Long2ObjectMap<Map<BlockPos, TypedBlockArea>> createChunkSectionPositions(List<TypedBlockArea> AffectorPositions) {
        Long2ObjectMap<Map<BlockPos, TypedBlockArea>> sectionPositions = new Long2ObjectOpenHashMap<>();
        for (TypedBlockArea torchPosition : AffectorPositions) {
            addAllSections(torchPosition, sectionPositions);
        }

        return sectionPositions;
    }

    private static void addAllSections(TypedBlockArea torchPosition, Long2ObjectMap<Map<BlockPos, TypedBlockArea>> sectionPositions) {
        torchPosition.blockArea().getAllSections((SectionPos sectionPos) -> {
            Map<BlockPos, TypedBlockArea> positions = sectionPositions.computeIfAbsent(sectionPos.asLong(),
                    (long sectionPosX) -> new LinkedHashMap<>());
            positions.put(torchPosition.position(), torchPosition);
        });
    }

    private static void removeAllSections(TypedBlockArea torchPosition, Long2ObjectMap<Map<BlockPos, TypedBlockArea>> sectionPositions) {
        torchPosition.blockArea().getAllSections((SectionPos sectionPos) -> {
            Map<BlockPos, TypedBlockArea> positions = sectionPositions.get(sectionPos.asLong());
            if (positions != null) {
                positions.remove(torchPosition.position(), torchPosition);
                if (positions.isEmpty()) {
                    sectionPositions.remove(sectionPos.asLong(), positions);
                }
            }
        });
    }

    public Collection<? extends TypedBlockArea> getAffectorsInSection(BlockPos blockPos) {
        return this.sectionPositions.getOrDefault(SectionPos.asLong(blockPos), Collections.emptyMap()).values();
    }

    public AffectorPositions add(BlockPos blockPos, AffectorType type) {
        TypedBlockArea oldType = this.affectorPositions.get(blockPos);
        if (oldType == null || oldType.type() != type) {
            TypedBlockArea torchPosition = new TypedBlockArea(type, blockPos);
            Map<BlockPos, TypedBlockArea> AffectorPositions = ImmutableMap.<BlockPos, TypedBlockArea>builder()
                    .putAll(this.affectorPositions)
                    .put(blockPos, torchPosition)
                    .buildKeepingLast();
            Long2ObjectMap<Map<BlockPos, TypedBlockArea>> sectionPositions = new Long2ObjectOpenHashMap<>(this.sectionPositions);
            addAllSections(torchPosition, sectionPositions);
            return new AffectorPositions(AffectorPositions, sectionPositions);
        } else {
            return this;
        }
    }

    public AffectorPositions remove(BlockPos blockPos, AffectorType type) {
        TypedBlockArea oldType = this.affectorPositions.get(blockPos);
        if (oldType != null && oldType.type() == type) {
            Map<BlockPos, TypedBlockArea> AffectorPositions = this.affectorPositions.entrySet()
                    .stream()
                    .filter((Map.Entry<BlockPos, TypedBlockArea> entry) -> {
                        return !entry.getKey().equals(blockPos);
                    })
                    .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
            Long2ObjectMap<Map<BlockPos, TypedBlockArea>> sectionPositions = new Long2ObjectOpenHashMap<>(this.sectionPositions);
            removeAllSections(oldType, sectionPositions);
            return new AffectorPositions(AffectorPositions, sectionPositions);
        } else {
            return this;
        }
    }


    public static void onBlockStateChange(ServerLevel serverWorld, BlockPos pos, BlockState oldState, BlockState newState) {
        Optional<AffectorType> oldType = getAffectorType(oldState);
        Optional<AffectorType> newType = getAffectorType(newState);
        if (!Objects.equals(oldType, newType)) {
            BlockPos blockPos = pos.immutable();
            oldType.ifPresent((AffectorType type) -> {
                serverWorld.getServer().execute(() -> {
                    AffectorPositions affectorPositions = CropCritters.getAffectorPositions(serverWorld);
                    CropCritters.setAffectorPositions(serverWorld, affectorPositions.remove(blockPos, type));
                });
            });
            newType.ifPresent((AffectorType type) -> {
                serverWorld.getServer().execute(() -> {
                    AffectorPositions affectorPositions = CropCritters.getAffectorPositions(serverWorld);
                    CropCritters.setAffectorPositions(serverWorld, affectorPositions.add(blockPos, type));
                });
            });
            //print(serverWorld, false);
        }
    }

    private static Optional<AffectorType> getAffectorType(BlockState blockState) {
        return Optional.ofNullable(AffectorsHelper.getTypeFromBlockState(blockState));
    }


    private static void print(ServerLevel serverWorld, boolean sections) {
        AffectorPositions affectorPositions = CropCritters.getAffectorPositions(serverWorld);

        if (sections) {
            CropCritters.LOGGER.info("=== Section Positions ===");
            affectorPositions.sectionPositions.forEach((sectionLong, posMap) -> {
                SectionPos sectionPos = SectionPos.of(sectionLong);
                CropCritters.LOGGER.info("Section {}: {} affectors", sectionPos, posMap.size());
                posMap.forEach((pos, typedArea) -> {
                    CropCritters.LOGGER.info("  - {} ({})", pos, typedArea.type());
                });
            });
        } else {
            CropCritters.LOGGER.info("=== Affector Positions ===");
            affectorPositions.affectorPositions.forEach((pos, typedArea) -> {
                CropCritters.LOGGER.info("Position: {} | Type: {}",
                        pos,
                        typedArea.type()
                );
            });
        }
    }
}