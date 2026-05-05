package com.yelf42.cropcritters.config;

import com.google.gson.*;
import com.yelf42.cropcritters.CropCritters;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class FarmlandDegradationMapping extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();

    public static final FarmlandDegradationMapping INSTANCE = new FarmlandDegradationMapping();

    private static final Random random = new Random();

    // Storage
    private final Map<ResourceLocation, List<ResourceLocation>> blockToBlockList = new HashMap<>();

    public FarmlandDegradationMapping() {
        super(GSON, "block_mappings/farmland_degradation");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        blockToBlockList.clear();
        for (JsonElement element : jsons.values()) {
            JsonArray mappings = element.getAsJsonObject().getAsJsonArray("mappings");
            for (JsonElement el : mappings) {
                JsonObject obj = el.getAsJsonObject();
                ResourceLocation from = ResourceLocation.tryParse(obj.get("from").getAsString());

                if (from == null || !BuiltInRegistries.BLOCK.containsKey(from)) {
                    CropCritters.LOGGER.debug("Skipping mapping - block not found: {}", from);
                    continue;
                }

                List<ResourceLocation> mappedTo = new ArrayList<>();
                for (JsonElement toEl : obj.get("to").getAsJsonArray()) {
                    ResourceLocation id = ResourceLocation.tryParse(toEl.getAsString());
                    if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
                        CropCritters.LOGGER.debug("Skipping mapping - block not found: {}", id);
                        continue;
                    }
                    mappedTo.add(id);
                }

                if (!mappedTo.isEmpty()) {
                    blockToBlockList.put(from, mappedTo);
                }
            }
        }
    }


    public Optional<BlockState> getFarmlandDegradationMapping(BlockState blockState) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());

        List<ResourceLocation> list = blockToBlockList.get(blockId);
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }

        ResourceLocation id = list.get(random.nextInt(list.size()));
        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
            return Optional.empty();
        }

        Block mappedBlock = BuiltInRegistries.BLOCK.get(id);
        BlockState mappedState = mappedBlock.defaultBlockState();

        return Optional.of(mappedState);
    }

    public boolean growingMedium(BlockState blockState) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());

        return blockToBlockList.containsKey(blockId) || blockState.getBlock() instanceof FarmBlock;
    }
}
