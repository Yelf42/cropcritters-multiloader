package com.yelf42.cropcritters.config;

import com.yelf42.cropcritters.CropCritters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static Path CONFIG_PATH;
    public static CropCrittersConfig CONFIG = CropCrittersConfig.getDefaults();

    public static void setConfigPath(Path configPath) {
        CONFIG_PATH = configPath.resolve("crop-critters-config.toml");
    }

    public static void load() {
        if (CONFIG_PATH == null) {
            throw new IllegalStateException("Config path not initialized! Call setConfigPath() first.");
        }

        if (!Files.exists(CONFIG_PATH)) {
            CropCritters.LOGGER.info("Generating first time CropCritters config");
            save(); // Save defaults
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || !line.contains("=")) continue;

                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "regularWeedsGrowChance" -> CONFIG.regularWeedChance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "netherWeedsGrowChance" -> CONFIG.netherWeedChance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "mazewoodSpread" -> CONFIG.mazewoodSpread = Math.clamp(Integer.parseInt(value), 1, 16);
                    case "lostSoulDropChance" -> CONFIG.lostSoulDropChance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "monoculturePenalize" -> CONFIG.monoculturePenalize = Boolean.parseBoolean(value);
                    case "critterSpawnChance" -> CONFIG.critterSpawnChance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "critterWorkSpeedMultiplier" -> CONFIG.critterWorkSpeedMultiplier = Math.clamp(Float.parseFloat(value), 0, 10);
                    case "deadCoralGeneration" -> CONFIG.deadCoralGeneration = Boolean.parseBoolean(value);
                    case "soulRoseHintGeneration" -> CONFIG.soulRoseHintGeneration = Boolean.parseBoolean(value);
                    case "thornweedGeneration" -> CONFIG.thornweedGeneration = Boolean.parseBoolean(value);
                    case "waftgrassGeneration" -> CONFIG.waftgrassGeneration = Boolean.parseBoolean(value);
                    case "spiteweedGeneration" -> CONFIG.spiteweedGeneration = Boolean.parseBoolean(value);
                    case "strangleFernGeneration" -> CONFIG.strangleFernGeneration = Boolean.parseBoolean(value);
                    case "puffbombGeneration" -> CONFIG.puffbombGeneration = Boolean.parseBoolean(value);
                    case "liverwortGeneration" -> CONFIG.liverwortGeneration = Boolean.parseBoolean(value);
                    case "goldSoulRoseSlowdown" -> CONFIG.goldSoulRoseSlowdown = Math.clamp(Integer.parseInt(value), 0, 100);

                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            writer.write("# CropCritters Configuration\n");
            writer.write("# Config changes only apply on restart\n");
            writer.write("# \n");
            writer.write("# Weed percentage grow chances:\n");
            writer.write("regularWeedsGrowChance = " + CONFIG.regularWeedChance + "\n");
            writer.write("netherWeedsGrowChance = " + CONFIG.netherWeedChance + "\n");
            writer.write("# Should monocultures have increased weed chances:\n");
            writer.write("monoculturePenalize = " + CONFIG.monoculturePenalize + "\n");
            writer.write("# \n");
            writer.write("# How far should Mazewood spread (1-128):\n");
            writer.write("# (Be careful, spreads fast) \n");
            writer.write("mazewoodSpread = " + CONFIG.mazewoodSpread + "\n");
            writer.write("# \n");
            writer.write("# Lost soul mob drop chance:\n");
            writer.write("lostSoulDropChance = " + CONFIG.lostSoulDropChance + "\n");
            writer.write("# \n");
            writer.write("# Crop critter spawn chance on crop just matured\n");
            writer.write("# or on randomTick in SoulSandValley.\n");
            writer.write("# Chance doubled if on a 'Soul' block:\n");
            writer.write("critterSpawnChance = " + CONFIG.critterSpawnChance + "\n");
            writer.write("# \n");
            writer.write("# Multiplier on critter work speed:\n");
            writer.write("# (Between 0.01 and 10.0)\n");
            writer.write("critterWorkSpeedMultiplier = " + CONFIG.critterWorkSpeedMultiplier + "\n");
            writer.write("# \n");
            writer.write("# Biome generation toggles: \n");
            writer.write("deadCoralGeneration = " + CONFIG.deadCoralGeneration + "\n");
            writer.write("soulRoseHintGeneration = " + CONFIG.soulRoseHintGeneration + "\n");
            writer.write("thornweedGeneration = " + CONFIG.thornweedGeneration + "\n");
            writer.write("waftgrassGeneration = " + CONFIG.waftgrassGeneration + "\n");
            writer.write("spiteweedGeneration = " + CONFIG.spiteweedGeneration + "\n");
            writer.write("strangleFernGeneration = " + CONFIG.strangleFernGeneration + "\n");
            writer.write("puffbombGeneration = " + CONFIG.puffbombGeneration + "\n");
            writer.write("liverwortGeneration = " + CONFIG.liverwortGeneration + "\n");
            writer.write("# \n");
            writer.write("# Gold Soul Rose crop growth rate slowdown (percentage): \n");
            writer.write("goldSoulRoseSlowdown" + CONFIG.goldSoulRoseSlowdown + "\n");
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }


}
