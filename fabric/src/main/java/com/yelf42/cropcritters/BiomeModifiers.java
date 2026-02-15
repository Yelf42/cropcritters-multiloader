package com.yelf42.cropcritters;

import com.yelf42.cropcritters.config.ConfigManager;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeModifiers {

    public static void register() {
        CropCritters.LOGGER.info("Starting biome changes for " + CropCritters.MOD_ID);
        if (ConfigManager.CONFIG.deadCoralGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.tag(BiomeTags.IS_BEACH),
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "dead_coral_shelf"))
            );
        }
        if (ConfigManager.CONFIG.thornweedGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.CRIMSON_FOREST),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "crimson_thornweed"))
            );
        }
        if (ConfigManager.CONFIG.waftgrassGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.WARPED_FOREST),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "waftgrass"))
            );
        }
        if (ConfigManager.CONFIG.spiteweedGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
                    GenerationStep.Decoration.SURFACE_STRUCTURES,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "withering_spiteweed"))
            );
        }
        if (ConfigManager.CONFIG.strangleFernGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.SWAMP),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "strangle_fern"))
            );
        }
        if (ConfigManager.CONFIG.liverwortGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.SWAMP),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "liverwort"))
            );
        }
        if (ConfigManager.CONFIG.puffbombGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.PLAINS),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "plains_puffbomb_blob"))
            );
        }
        if (ConfigManager.CONFIG.soulRoseHintGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "soul_rose_hint"))
            );
        }
    }
}
