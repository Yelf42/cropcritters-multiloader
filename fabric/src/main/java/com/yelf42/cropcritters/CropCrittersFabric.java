package com.yelf42.cropcritters;

import com.yelf42.cropcritters.entity.*;
import com.yelf42.cropcritters.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.CompostableRegistry;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CropCrittersFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        bind(BuiltInRegistries.PARTICLE_TYPE, ModParticles::register);

        bind(BuiltInRegistries.DATA_COMPONENT_TYPE, ModComponents::register);

        bind(BuiltInRegistries.BLOCK, ModBlocks::registerBlocks);
        bind(BuiltInRegistries.ITEM, ModBlocks::registerItems);

        bind(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModBlockEntities::register);

        bind(BuiltInRegistries.ITEM, ModItems::register);
        bind(BuiltInRegistries.CREATIVE_MODE_TAB, ModItems::registerTabs);
        bind(BuiltInRegistries.RECIPE_SERIALIZER, ModItems::registerRecipes);



        bind(BuiltInRegistries.MOB_EFFECT, ModEffects::register);

        bind(BuiltInRegistries.FEATURE, ModFeatures::registerFeatures);

        bind(BuiltInRegistries.ENTITY_TYPE, ModEntities::register);
        registerEntityAttributes();

        ModDispenserBehaviours.registerDispenserBehavior();
        ModDispenserBehaviours.runDispenserRegistration();

        BiomeModifiers.register();

        registerCompostable();
        registerFuel();

        PayloadTypeRegistry.clientboundPlay().register(ModPackets.WaterSprayS2CPayload.ID, ModPackets.WaterSprayS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ModPackets.ParticleRingS2CPayload.ID, ModPackets.ParticleRingS2CPayload.CODEC);

        CropCritters.init();
    }

    public static <T> void bind(Registry<T> registry, Consumer<BiConsumer<T, Identifier>> source) {
        source.accept((t, rl) -> Registry.register(registry, rl, t));
    }

    private void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(ModEntities.WHEAT_CRITTER, WheatCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.MELON_CRITTER, MelonCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.PUMPKIN_CRITTER, PumpkinCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.POTATO_CRITTER, PotatoCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CARROT_CRITTER, CarrotCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.BEETROOT_CRITTER, BeetrootCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.NETHER_WART_CRITTER, NetherWartCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.POISONOUS_POTATO_CRITTER, PoisonousPotatoCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.TORCHFLOWER_CRITTER, TorchflowerCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.PITCHER_CRITTER, PitcherCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.COCOA_CRITTER, CocoaCritterEntity.createAttributes());
    }

    private void registerCompostable() {
        CompostableRegistry.INSTANCE.add(ModItems.STRANGE_FERTILIZER, 1.0f);
        CompostableRegistry.INSTANCE.add(ModItems.SEED_BALL, 0.8f);
        CompostableRegistry.INSTANCE.add(ModItems.SEED_BAR, 0.8f);
        CompostableRegistry.INSTANCE.add(ModItems.PUFFBOMB_SLICE, 0.4f);

        CompostableRegistry.INSTANCE.add(ModBlocks.TALL_BUSH.asItem(), 0.8f);
        CompostableRegistry.INSTANCE.add(ModBlocks.ORNAMENTAL_BUSH.asItem(), 0.8f);
        CompostableRegistry.INSTANCE.add(ModBlocks.MAZEWOOD.asItem(), 0.8f);
        CompostableRegistry.INSTANCE.add(ModBlocks.PUFFBOMB_MUSHROOM.asItem(), 0.65f);
        CompostableRegistry.INSTANCE.add(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK.asItem(), 0.65f);
        CompostableRegistry.INSTANCE.add(ModBlocks.BONE_TRAP.asItem(), 0.6f);
        CompostableRegistry.INSTANCE.add(ModBlocks.MAZEWOOD_SAPLING.asItem(), 0.4f);
        CompostableRegistry.INSTANCE.add(ModBlocks.CRAWL_THISTLE.asItem(), 0.3f);
        CompostableRegistry.INSTANCE.add(ModBlocks.CRIMSON_THORNWEED.asItem(), 0.2f);
        CompostableRegistry.INSTANCE.add(ModBlocks.STRANGLE_FERN.asItem(), 0.2f);
        CompostableRegistry.INSTANCE.add(ModBlocks.POPPER_PLANT.asItem(), 0.2f);
        CompostableRegistry.INSTANCE.add(ModBlocks.LIVERWORT.asItem(), 0.2f);
        CompostableRegistry.INSTANCE.add(ModBlocks.WAFTGRASS.asItem(), 0.2f);
        CompostableRegistry.INSTANCE.add(ModBlocks.WITHERING_SPITEWEED.asItem(), 0f);
    }

    private void registerFuel() {
        FuelValueEvents.BUILD.register((builder, context) -> {
            builder.add(ModItems.LOST_SOUL, 80 * 20);
        });
    }
}
