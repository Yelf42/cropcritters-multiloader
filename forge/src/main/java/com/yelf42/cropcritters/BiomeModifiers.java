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
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BiomeModifiers {

    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, CropCritters.MOD_ID);

    public static final RegistryObject<Codec<CropCrittersBiomeModifier>> CONFIGURABLE_MODIFIER =
            BIOME_MODIFIER_SERIALIZERS.register("configurable_feature",
                    CropCrittersBiomeModifier.CODEC::codec);

    public static void register(IEventBus bus) {
        BIOME_MODIFIER_SERIALIZERS.register(bus);
    }

    public record CropCrittersBiomeModifier(
            HolderSet<Biome> biomes,
            HolderSet<PlacedFeature> features,
            GenerationStep.Decoration step,
            String configKey
    ) implements BiomeModifier {

        public static final MapCodec<CropCrittersBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Biome.LIST_CODEC.fieldOf("biomes").forGetter(CropCrittersBiomeModifier::biomes),
                        PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(CropCrittersBiomeModifier::features),
                        GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(CropCrittersBiomeModifier::step),
                        Codec.STRING.fieldOf("config_key").forGetter(CropCrittersBiomeModifier::configKey)
                ).apply(instance, CropCrittersBiomeModifier::new)
        );

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.ADD || !this.biomes.contains(biome)) return;

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
                this.features.forEach(holder ->
                        builder.getGenerationSettings().addFeature(this.step, holder));
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return CONFIGURABLE_MODIFIER.get();
        }
    }
}