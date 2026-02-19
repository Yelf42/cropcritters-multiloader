package com.yelf42.cropcritters;

import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.area_affectors.TypedBlockArea;
import com.yelf42.cropcritters.entity.*;
import com.yelf42.cropcritters.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.security.Permissions;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CropCrittersFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        ModEffects.init();

        bind(BuiltInRegistries.PARTICLE_TYPE, ModParticles::register);

        bind(BuiltInRegistries.BLOCK, ModBlocks::registerBlocks);
        bind(BuiltInRegistries.ITEM, ModBlocks::registerItems);

        bind(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModBlockEntities::register);

        bind(BuiltInRegistries.ITEM, ModItems::register);
        bind(BuiltInRegistries.CREATIVE_MODE_TAB, ModItems::registerTabs);
        bind(BuiltInRegistries.RECIPE_SERIALIZER, ModItems::registerRecipes);

        bind(BuiltInRegistries.DATA_COMPONENT_TYPE, ModComponents::register);

        bind(BuiltInRegistries.FEATURE, ModFeatures::registerFeatures);

        bind(BuiltInRegistries.ENTITY_TYPE, ModEntities::register);
        registerEntityAttributes();

        ModDispenserBehaviours.registerDispenserBehavior();
        ModDispenserBehaviours.runDispenserRegistration();

        BiomeModifiers.register();

        CropCritters.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("test_soul_rose")
                    .requires(source -> source.hasPermission(4))
                    .executes(context -> {
                        context.getSource().sendSuccess(() -> {
                            if (context.getSource().getPlayer() == null) return Component.literal("TestSoulRose: Player calls only");
                            AffectorPositions affectorPositions = CropCritters.getAffectorPositions(context.getSource().getLevel());
                            Collection<? extends TypedBlockArea> affectorsInSection = affectorPositions.getAffectorsInSection(context.getSource().getPlayer().getOnPos());
                            return Component.literal(affectorsInSection.stream().map(typedArea -> "Type: " + typedArea.type()).collect(Collectors.joining("\n")));
                        }, false);
                        return 1;
                    })
            );
        });
    }

    public static <T> void bind(Registry<T> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
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
}
