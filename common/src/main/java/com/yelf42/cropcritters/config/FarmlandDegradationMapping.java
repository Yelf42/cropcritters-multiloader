package com.yelf42.cropcritters.config;

import com.yelf42.cropcritters.CropCritters;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class FarmlandDegradationMapping extends SimpleJsonResourceReloadListener<ConfigManager.BlockToBlockListMappingFile> {


    public static final FarmlandDegradationMapping INSTANCE = new FarmlandDegradationMapping();

    private static final Random random = new Random();

    // Storage
    private final Map<Identifier, List<Identifier>> blockToBlockList = new HashMap<>();

    public FarmlandDegradationMapping() {
        super(ConfigManager.BlockToBlockListMappingFile.CODEC, FileToIdConverter.json("block_mappings/farmland_degradation"));
    }

    @Override
    protected void apply(Map<Identifier, ConfigManager.BlockToBlockListMappingFile> jsons, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        blockToBlockList.clear();

        for (ConfigManager.BlockToBlockListMappingFile file : jsons.values()) {
            for (ConfigManager.BlockToBlockListMapping mapping : file.mappings()) {
                if (!BuiltInRegistries.BLOCK.containsKey(mapping.from())) {
                    CropCritters.LOGGER.debug("Skipping mapping - block not found: {}", mapping.from());
                    continue;
                }

                List<Identifier> mappedTo = new ArrayList<>();
                for (Identifier id : mapping.to()) {
                    if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                        CropCritters.LOGGER.debug("Skipping mapping - block not found: {}", id);
                        continue;
                    }
                    mappedTo.add(id);
                }

                if (!mappedTo.isEmpty()) {
                    blockToBlockList.put(mapping.from(), mappedTo);
                }
            }
        }
    }


    public Optional<BlockState> getFarmlandDegradationMapping(BlockState blockState) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());

        List<Identifier> list = blockToBlockList.get(blockId);
        if (list == null || list.isEmpty()) return Optional.empty();

        Block mappedBlock = BuiltInRegistries.BLOCK.get(list.get(random.nextInt(list.size())))
                .map(Holder.Reference::value)
                .orElse(null);

        if (mappedBlock == null) return Optional.empty();

        BlockState mappedState = mappedBlock.defaultBlockState();
        return Optional.of(mappedState);
    }

    public boolean growingMedium(BlockState blockState) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());

        return blockToBlockList.containsKey(blockId) || blockState.getBlock() instanceof FarmlandBlock;
    }
}
