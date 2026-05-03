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
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TillingBlockMapping extends SimpleJsonResourceReloadListener<ConfigManager.BlockToBlockMappingFile> {

    public static final TillingBlockMapping CARROT_INSTANCE = new TillingBlockMapping("block_mappings/carrot_tilling");

    public static final TillingBlockMapping POTATO_INSTANCE = new TillingBlockMapping("block_mappings/potato_tilling");

    public static final TillingBlockMapping BEETROOT_INSTANCE = new TillingBlockMapping("block_mappings/beetroot_tilling");


    // Storage
    private final Map<Identifier, Identifier> blockToBlock = new HashMap<>();

    public TillingBlockMapping(String location) {
        super(ConfigManager.BlockToBlockMappingFile.CODEC, FileToIdConverter.json(location));
    }

    @Override
    protected void apply(Map<Identifier, ConfigManager.BlockToBlockMappingFile> jsons, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        blockToBlock.clear();

        for (ConfigManager.BlockToBlockMappingFile file : jsons.values()) {
            for (ConfigManager.BlockToBlockMapping mapping : file.mappings()) {
                if (!BuiltInRegistries.BLOCK.containsKey(mapping.from())) {
                    CropCritters.LOGGER.debug("Skipping mapping - block not found: {}", mapping.from());
                    continue;
                }
                if (!BuiltInRegistries.BLOCK.containsKey(mapping.to())) {
                    CropCritters.LOGGER.debug("Skipping mapping - block not found: {}", mapping.to());
                    continue;
                }

                blockToBlock.put(mapping.from(), mapping.to());
            }
        }
    }

    public Optional<BlockState> getTillingMapping(BlockState blockState) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());

        Identifier mappedId = blockToBlock.get(blockId);
        if (mappedId == null) return Optional.empty();

        Block mappedBlock = BuiltInRegistries.BLOCK.get(mappedId)
                .map(Holder.Reference::value)
                .orElse(null);

        if (mappedBlock == null) return Optional.empty();

        return Optional.of(mappedBlock.defaultBlockState());
    }

    public boolean canTill(BlockState blockState) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        return blockToBlock.containsKey(blockId);
    }
}
