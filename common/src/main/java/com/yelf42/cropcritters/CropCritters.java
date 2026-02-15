package com.yelf42.cropcritters;

import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.config.ConfigManager;
import com.yelf42.cropcritters.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CropCritters {

    public static final String MOD_ID = "cropcritters";
    public static final String MOD_NAME = "Crop Critters";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String[] INT_TO_ROMAN = {" ", " I", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};


    // TAGS
    public static final TagKey<EntityType<?>> WEED_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, identifier( "weed_immune"));
    public static final TagKey<EntityType<?>> CROP_CRITTERS = TagKey.create(Registries.ENTITY_TYPE, identifier( "crop_critters"));
    public static final TagKey<EntityType<?>> SCARE_CRITTERS = TagKey.create(Registries.ENTITY_TYPE, identifier( "scare_critters"));
    public static final TagKey<EntityType<?>> HAS_LOST_SOUL = TagKey.create(Registries.ENTITY_TYPE, identifier( "has_lost_soul"));

    public static final TagKey<Block> UNDERWATER_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, identifier( "underwater_strange_fertilizers"));
    public static final TagKey<Block> ON_LAND_COMMON_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, identifier( "on_land_common_strange_fertilizers"));
    public static final TagKey<Block> ON_LAND_RARE_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, identifier( "on_land_rare_strange_fertilizers"));
    public static final TagKey<Block> ON_NYLIUM_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, identifier( "on_nylium_strange_fertilizers"));
    public static final TagKey<Block> ON_MYCELIUM_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, identifier( "on_mycelium_strange_fertilizers"));
    public static final TagKey<Block> IGNORE_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, identifier( "ignore_strange_fertilizers"));
    public static final TagKey<Block> WEEDS = TagKey.create(Registries.BLOCK, identifier( "weeds"));
    public static final TagKey<Block> PATH_PENALTY_WEEDS = TagKey.create(Registries.BLOCK, identifier( "path_penalty_weeds"));
    public static final TagKey<Block> SPORES_INFECT = TagKey.create(Registries.BLOCK, identifier( "spores_infectable"));
    public static final TagKey<Block> IMMUNE_PLANTS = TagKey.create(Registries.BLOCK, identifier( "immune_plants"));
    public static final TagKey<Block> SNOW_FALL_KILLS = TagKey.create(Registries.BLOCK, identifier( "snow_fall_kills"));

    public static final TagKey<Item> SEED_BALL_CROPS = TagKey.create(Registries.ITEM, identifier( "seed_ball_crops"));

    public static void init() {

        LOGGER.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());

        if (Services.PLATFORM.isModLoaded("cropcritters")) {
            ConfigManager.setConfigPath(Services.PLATFORM.getConfigPath());
            ConfigManager.load();
        }
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, path);
    }

    public static AffectorPositions getAffectorPositions(ServerLevel world) {
        return Services.PLATFORM.getAffectorPositions(world);
    }

    public static void setAffectorPositions(ServerLevel world, AffectorPositions positions) {
        Services.PLATFORM.setAffectorPositions(world, positions);
    }

}