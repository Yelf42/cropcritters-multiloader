package com.yelf42.cropcritters.config;

import com.google.gson.*;
import com.yelf42.cropcritters.CropCritters;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TillingBlockMapping extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();

    public static final TillingBlockMapping CARROT_INSTANCE = new TillingBlockMapping("block_mappings/carrot_tilling");

    public static final TillingBlockMapping POTATO_INSTANCE = new TillingBlockMapping("block_mappings/potato_tilling");

    public static final TillingBlockMapping BEETROOT_INSTANCE = new TillingBlockMapping("block_mappings/beetroot_tilling");


    // Storage
    private final Map<ResourceLocation, ResourceLocation> blockToBlock = new HashMap<>();

    public TillingBlockMapping(String location) {
        super(GSON, location);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        blockToBlock.clear();

        for (JsonElement element : jsons.values()) {
            JsonArray mappings = element.getAsJsonObject().getAsJsonArray("mappings");
            for (JsonElement el : mappings) {
                JsonObject obj = el.getAsJsonObject();
                ResourceLocation from = ResourceLocation.tryParse(obj.get("from").getAsString());
                ResourceLocation to = ResourceLocation.tryParse(obj.get("to").getAsString());

                if (from == null || !BuiltInRegistries.BLOCK.containsKey(from)) {
                    CropCritters.LOGGER.debug("Skipping mapping - block not found: {}", from);
                    continue;
                }
                if (to == null || !BuiltInRegistries.BLOCK.containsKey(to)) {
                    CropCritters.LOGGER.debug("Skipping mapping - block not found: {}", to);
                    continue;
                }

                blockToBlock.put(from, to);
            }
        }
    }

    public Optional<BlockState> getTillingMapping(BlockState blockState) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());

        ResourceLocation mappedId = blockToBlock.get(blockId);
        if (mappedId == null || !BuiltInRegistries.BLOCK.containsKey(mappedId)) return Optional.empty();

        Block mappedBlock = BuiltInRegistries.BLOCK.get(mappedId);

        return Optional.of(mappedBlock.defaultBlockState());
    }

    public boolean canTill(BlockState blockState) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        return blockToBlock.containsKey(blockId);
    }
}
