package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.features.LiverwortFeature;
import com.yelf42.cropcritters.features.PuffbombBlobFeature;
import com.yelf42.cropcritters.features.SoulRoseHintFeature;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.function.BiConsumer;

// TODO mixin to FeatureUtils#bootstrap to add the configured features
public class ModFeatures {

    public static final Feature<NoneFeatureConfiguration> PUFFBOMB_BLOB_FEATURE = new PuffbombBlobFeature(NoneFeatureConfiguration.CODEC);
    public static final ResourceKey<ConfiguredFeature<?, ?>> PUFFBOMB_BLOB_CONFIGURED_FEATURE =
            ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "puffbomb_blob")
            );

    public static final Feature<CountConfiguration> LIVERWORT_FEATURE = new LiverwortFeature(CountConfiguration.CODEC);
    public static final ResourceKey<ConfiguredFeature<?, ?>> LIVERWORT_CONFIGURED_FEATURE =
            ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "liverwort_patch")
            );

    public static final Feature<NoneFeatureConfiguration> SOUL_ROSE_HINT_FEATURE = new SoulRoseHintFeature(NoneFeatureConfiguration.CODEC);
    public static final ResourceKey<ConfiguredFeature<?, ?>> SOUL_ROSE_HINT_CONFIGURED_FEATURE =
            ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "soul_rose_hint")
            );

    /// BINDER
    public static void registerFeatures(BiConsumer<Feature<?>, Identifier> consumer) {
        consumer.accept(PUFFBOMB_BLOB_FEATURE, CropCritters.identifier("puffbomb_blob"));
        consumer.accept(LIVERWORT_FEATURE, CropCritters.identifier("liverwort_patch"));
        consumer.accept(SOUL_ROSE_HINT_FEATURE, CropCritters.identifier("soul_rose_hint"));
    }
}
