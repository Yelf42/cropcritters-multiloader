package com.yelf42.cropcritters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yelf42.cropcritters.config.ConfigManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class BiomeModifiers {

    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, CropCritters.MOD_ID);

    public static void register(IEventBus bus) {
        BIOME_MODIFIER_SERIALIZERS.register(bus);
    }

    static {
        BIOME_MODIFIER_SERIALIZERS.register("configurable_feature", () -> ConfigurableFeatureBiomeModifier.CODEC);
    }

    public record ConfigurableFeatureBiomeModifier(
            HolderSet<Biome> biomes,
            HolderSet<PlacedFeature> features,
            GenerationStep.Decoration step,
            String configKey
    ) implements BiomeModifier {

        private static final MapCodec<ConfigurableFeatureBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Biome.LIST_CODEC.fieldOf("biomes").forGetter(ConfigurableFeatureBiomeModifier::biomes),
                        PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(ConfigurableFeatureBiomeModifier::features),
                        GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ConfigurableFeatureBiomeModifier::step),
                        Codec.STRING.fieldOf("config_key").forGetter(ConfigurableFeatureBiomeModifier::configKey)
                ).apply(instance, ConfigurableFeatureBiomeModifier::new)
        );

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && this.biomes.contains(biome)) {
                // Check config based on key
                boolean enabled = switch (configKey) {
                    case "dead_coral" -> ConfigManager.CONFIG.deadCoralGeneration;
                    case "thornweed" -> ConfigManager.CONFIG.thornweedGeneration;
                    case "waftgrass" -> ConfigManager.CONFIG.waftgrassGeneration;
                    case "spiteweed" -> ConfigManager.CONFIG.spiteweedGeneration;
                    case "strangle_fern" -> ConfigManager.CONFIG.strangleFernGeneration;
                    case "liverwort" -> ConfigManager.CONFIG.liverwortGeneration;
                    case "puffbomb" -> ConfigManager.CONFIG.puffbombGeneration;
                    case "soul_rose_hint" -> ConfigManager.CONFIG.soulRoseHintGeneration;
                    default -> false;
                };

                if (enabled) {
                    BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
                    this.features.forEach(holder -> generationSettings.addFeature(this.step, holder));
                }
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CODEC;
        }
    }
}
