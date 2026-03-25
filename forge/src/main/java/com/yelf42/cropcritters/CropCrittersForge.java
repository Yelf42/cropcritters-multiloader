package com.yelf42.cropcritters;

import com.yelf42.cropcritters.entity.*;
import com.yelf42.cropcritters.platform.ForgePlatformHelper;
import com.yelf42.cropcritters.registry.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(CropCritters.MOD_ID)
public class CropCrittersForge {
    public static IEventBus eventBus;

    public CropCrittersForge() {

        CropCrittersForge.eventBus = FMLJavaModLoadingContext.get().getModEventBus();;

        ForgePlatformHelper.register(eventBus, MinecraftForge.EVENT_BUS);

        bind(Registries.MOB_EFFECT, ModEffects::register);

        bind(Registries.PARTICLE_TYPE, ModParticles::register);

        bind(Registries.BLOCK, ModBlocks::registerBlocks);
        bind(Registries.ITEM, ModBlocks::registerItems);

        bind(Registries.BLOCK_ENTITY_TYPE, ModBlockEntities::register);

        bind(Registries.ITEM, ModItems::register);
        bind(Registries.CREATIVE_MODE_TAB, ModItems::registerTabs);
        bind(Registries.RECIPE_SERIALIZER, ModItems::registerRecipes);

        bind(Registries.FEATURE, ModFeatures::registerFeatures);

        bind(Registries.ENTITY_TYPE, ModEntities::register);
        eventBus.addListener(this::registerEntityAttributes);

        // Fuel & Compost & Dispenser
        eventBus.addListener(this::setupCommon);
        MinecraftForge.EVENT_BUS.addListener((FurnaceFuelBurnTimeEvent event) -> {
            Item item = event.getItemStack().getItem();
            if (item == ModItems.LOST_SOUL) {
                event.setBurnTime(80 * 20);
            }
        });


        // Client
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
            eventBus.addListener(CropCrittersForgeClient::registerParticleFactories);
            eventBus.addListener(CropCrittersForgeClient::registerEntityRenderers);
            eventBus.addListener(CropCrittersForgeClient::registerBlocks);
            eventBus.addListener(CropCrittersForgeClient::registerBlockColors);
            eventBus.addListener(CropCrittersForgeClient::registerItemColors);
        }

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

    public void setupCommon(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerCompostable();
            ModDispenserBehaviours.runDispenserRegistration();
        });
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

    private void registerCompostable() {
        ComposterBlock.COMPOSTABLES.put(ModItems.STRANGE_FERTILIZER, 1.0f);
        ComposterBlock.COMPOSTABLES.put(ModItems.SEED_BALL, 0.8f);
        ComposterBlock.COMPOSTABLES.put(ModItems.SEED_BAR, 0.8f);
        ComposterBlock.COMPOSTABLES.put(ModItems.PUFFBOMB_SLICE, 0.4f);

        ComposterBlock.COMPOSTABLES.put(ModBlocks.MAZEWOOD.asItem(), 0.8f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.PUFFBOMB_MUSHROOM.asItem(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK.asItem(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.BONE_TRAP.asItem(), 0.6f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.MAZEWOOD_SAPLING.asItem(), 0.4f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.CRAWL_THISTLE.asItem(), 0.3f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.CRIMSON_THORNWEED.asItem(), 0.2f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.STRANGLE_FERN.asItem(), 0.2f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.POPPER_PLANT.asItem(), 0.2f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.LIVERWORT.asItem(), 0.2f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.WAFTGRASS.asItem(), 0.2f);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.WITHERING_SPITEWEED.asItem(), 0f);
    }
}