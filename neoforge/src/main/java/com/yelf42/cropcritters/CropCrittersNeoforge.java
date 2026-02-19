package com.yelf42.cropcritters;

import com.yelf42.cropcritters.entity.*;
import com.yelf42.cropcritters.platform.NeoForgePlatformHelper;
import com.yelf42.cropcritters.registry.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(CropCritters.MOD_ID)
public class CropCrittersNeoforge {

    public static IEventBus eventBus;

    public CropCrittersNeoforge(IEventBus eventBus, Dist dist) {

        CropCrittersNeoforge.eventBus = eventBus;

        NeoForgePlatformHelper.register(eventBus);
        ModEffects.init();

        bind(Registries.PARTICLE_TYPE, ModParticles::register);

        bind(Registries.BLOCK, ModBlocks::registerBlocks);
        bind(Registries.ITEM, ModBlocks::registerItems);

        bind(Registries.BLOCK_ENTITY_TYPE, ModBlockEntities::register);

        bind(Registries.ITEM, ModItems::register);
        bind(Registries.CREATIVE_MODE_TAB, ModItems::registerTabs);
        bind(Registries.RECIPE_SERIALIZER, ModItems::registerRecipes);

        bind(Registries.DATA_COMPONENT_TYPE, ModComponents::register);

        bind(Registries.FEATURE, ModFeatures::registerFeatures);

        bind(Registries.ENTITY_TYPE, ModEntities::register);
        eventBus.addListener(this::registerEntityAttributes);

        // Client
        if (dist.isClient()) {
            eventBus.addListener(CropCrittersNeoforgeClient::register);
            eventBus.addListener(CropCrittersNeoforgeClient::registerParticleFactories);
            eventBus.addListener(CropCrittersNeoforgeClient::registerEntityRenderers);
            eventBus.addListener(CropCrittersNeoforgeClient::registerBlocks);
            eventBus.addListener(CropCrittersNeoforgeClient::registerBlockColors);
            eventBus.addListener(CropCrittersNeoforgeClient::registerItemColors);
        }

        ModDispenserBehaviours.registerDispenserBehavior();
        eventBus.addListener(this::setupDispenserBehaviors);

        BiomeModifiers.register(eventBus);

        CropCritters.init();

    }

    public static <T> void bind(ResourceKey<Registry<T>> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
        eventBus.addListener((Consumer<RegisterEvent>) event -> {
            if (registry.equals(event.getRegistryKey())) {
                source.accept((t, rl) -> event.register(registry, rl, () -> t));
            }
        });
    }

    public void setupDispenserBehaviors(FMLCommonSetupEvent event) {
        event.enqueueWork(ModDispenserBehaviours::runDispenserRegistration);
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.WHEAT_CRITTER, WheatCritterEntity.createAttributes().build());
        event.put(ModEntities.MELON_CRITTER, MelonCritterEntity.createAttributes().build());
        event.put(ModEntities.PUMPKIN_CRITTER, PumpkinCritterEntity.createAttributes().build());
        event.put(ModEntities.POTATO_CRITTER, PotatoCritterEntity.createAttributes().build());
        event.put(ModEntities.CARROT_CRITTER, CarrotCritterEntity.createAttributes().build());
        event.put(ModEntities.BEETROOT_CRITTER, BeetrootCritterEntity.createAttributes().build());
        event.put(ModEntities.NETHER_WART_CRITTER, NetherWartCritterEntity.createAttributes().build());
        event.put(ModEntities.POISONOUS_POTATO_CRITTER, PoisonousPotatoCritterEntity.createAttributes().build());
        event.put(ModEntities.TORCHFLOWER_CRITTER, TorchflowerCritterEntity.createAttributes().build());
        event.put(ModEntities.PITCHER_CRITTER, PitcherCritterEntity.createAttributes().build());
        event.put(ModEntities.COCOA_CRITTER, CocoaCritterEntity.createAttributes().build());
    }

}