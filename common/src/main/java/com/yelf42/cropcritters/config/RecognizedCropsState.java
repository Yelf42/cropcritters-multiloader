package com.yelf42.cropcritters.config;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.CropCritters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class RecognizedCropsState extends SavedData {
    private static final String DATA_NAME = CropCritters.MOD_ID;
    private final Set<Item> knownCrops = new HashSet<>();

    public static final SavedData.Factory<RecognizedCropsState> FACTORY = new SavedData.Factory<>(
            RecognizedCropsState::new,
            RecognizedCropsState::load,
            DataFixTypes.SAVED_DATA_MAP_DATA
    );

    public RecognizedCropsState() {}

    public static RecognizedCropsState load(CompoundTag tag, HolderLookup.Provider registries) {
        RecognizedCropsState state = new RecognizedCropsState();
        ListTag list = tag.getList("KnownCrops", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                BuiltInRegistries.ITEM.getOptional(id).ifPresent(state.knownCrops::add);
            }
        }
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Item item : knownCrops) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null) {
                list.add(StringTag.valueOf(id.toString()));
            }
        }
        tag.put("KnownCrops", list);
        return tag;
    }

    public void addCrop(Item item) {
        if (knownCrops.add(item)) {
            this.setDirty();
        }
    }

    public boolean hasCrop(Item item) {
        return knownCrops.contains(item);
    }

    public static RecognizedCropsState getServerState(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        assert overworld != null;
        RecognizedCropsState state = overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
        state.setDirty();
        return state;
    }
}
