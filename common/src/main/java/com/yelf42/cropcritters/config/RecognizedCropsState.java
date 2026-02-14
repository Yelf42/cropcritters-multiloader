package com.yelf42.cropcritters.config;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.CropCritters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RecognizedCropsState extends SavedData {
    private final Set<Item> knownCrops = new HashSet<>();

    private static final Codec<RecognizedCropsState> CODEC = Codec.list(BuiltInRegistries.ITEM.byNameCodec())
            .xmap(
                    list -> {
                        RecognizedCropsState state = new RecognizedCropsState();
                        state.knownCrops.addAll(list);
                        return state;
                    },
                    state -> new ArrayList<>(state.knownCrops)
            );

    private static final SavedDataType<RecognizedCropsState> type = new SavedDataType<>(
            CropCritters.MOD_ID,
            RecognizedCropsState::new,
            CODEC,
            null
    );

    private RecognizedCropsState() {
        // Default constructor for empty state
    }

    public void addCrop(Item item) {
        if (knownCrops.add(item)) {
            this.setDirty(); // mark for save
        }
    }

    public boolean hasCrop(Item item) {
        return knownCrops.contains(item);
    }

    public static RecognizedCropsState getServerState(MinecraftServer server) {
        ServerLevel serverWorld = server.getLevel(Level.OVERWORLD);
        assert serverWorld != null;
        RecognizedCropsState state = serverWorld.getDataStorage().computeIfAbsent(type);
        state.setDirty();
        return state;
    }

}
