package com.yelf42.cropcritters;

import com.yelf42.cropcritters.client.particle.*;
import com.yelf42.cropcritters.client.renderer.blockentity.StrangleFernBlockEntityRenderer;
import com.yelf42.cropcritters.client.renderer.entity.AbstractCritterRenderer;
import com.yelf42.cropcritters.client.renderer.entity.PopperPodEntityRenderer;
import com.yelf42.cropcritters.registry.*;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CropCrittersForgeClient {
    @SubscribeEvent
    public static void registerBlocks(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LOST_SOUL_IN_A_JAR, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRAWL_THISTLE, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRIMSON_THORNWEED, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WAFTGRASS, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PUFFBOMB_MUSHROOM, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIVERWORT, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WITHERING_SPITEWEED, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.POPPER_PLANT, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BONE_TRAP, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SOUL_ROSE, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRIMMED_SOUL_ROSE, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_SOUL_ROSE, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MAZEWOOD, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MAZEWOOD_SAPLING, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.STRANGLE_FERN, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SOUL_POT, RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) ->
                        world != null && pos != null
                                ? BiomeColors.getAverageGrassColor(world, pos)
                                : 0x91BD59,
                ModBlocks.MAZEWOOD, ModBlocks.MAZEWOOD_SAPLING, ModBlocks.STRANGLE_FERN
        );
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((state, i) -> 0x91BD59,
                ModBlocks.MAZEWOOD, ModBlocks.MAZEWOOD_SAPLING
        );
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Block entity renderers
        event.registerBlockEntityRenderer(ModBlockEntities.STRANGLE_FERN, StrangleFernBlockEntityRenderer::new);

        // Entity renderers
        event.registerEntityRenderer(ModEntities.WHEAT_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("wheat_critter"), true));
        event.registerEntityRenderer(ModEntities.MELON_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("melon_critter"), true));
        event.registerEntityRenderer(ModEntities.PUMPKIN_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("pumpkin_critter"), false));
        event.registerEntityRenderer(ModEntities.POTATO_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("potato_critter"), true));
        event.registerEntityRenderer(ModEntities.CARROT_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("carrot_critter"), true));
        event.registerEntityRenderer(ModEntities.BEETROOT_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("beetroot_critter"), true));
        event.registerEntityRenderer(ModEntities.NETHER_WART_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("nether_wart_critter"), true));
        event.registerEntityRenderer(ModEntities.POISONOUS_POTATO_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("poisonous_potato_critter"), true));
        event.registerEntityRenderer(ModEntities.TORCHFLOWER_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("torchflower_critter"), true));
        event.registerEntityRenderer(ModEntities.PITCHER_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("pitcher_critter"), false));
        event.registerEntityRenderer(ModEntities.COCOA_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier("cocoa_critter"), false));

        // Projectile renderers
        event.registerEntityRenderer(ModEntities.SEED_BALL_PROJECTILE, ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.SPIT_SEED_PROJECTILE, ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.POPPER_POD_PROJECTILE, PopperPodEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.POPPER_SEED_PROJECTILE, ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.HERBICIDE_PROJECTILE, ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.WATER_SPRAY, WaterSprayParticle.Factory::new);
        event.registerSpriteSet(ModParticles.SPORES, SporeParticle.Factory::new);
        event.registerSpriteSet(ModParticles.SOUL_SIPHON, SoulSiphonParticle.Factory::new);
        event.registerSpriteSet(ModParticles.SOUL_HEART, HeartParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SOUL_GLOW, SoulGlowParticle.Factory::new);
        event.registerSpriteSet(ModParticles.LOST_SOUL_GLOW, SoulGlowParticle.LostSoulFactory::new);
        event.registerSpriteSet(ModParticles.SOUL_GLINT, SuspendedTownParticle.HappyVillagerProvider::new);
        event.registerSpriteSet(ModParticles.SOUL_GLINT_PLUME, SoulGlintPlumeParticle.Factory::new);
    }

}
