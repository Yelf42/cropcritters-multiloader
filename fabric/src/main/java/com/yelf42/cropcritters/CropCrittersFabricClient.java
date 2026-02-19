package com.yelf42.cropcritters;

import com.yelf42.cropcritters.client.particle.*;
import com.yelf42.cropcritters.client.renderer.blockentity.SoulPotBlockEntityRenderer;
import com.yelf42.cropcritters.client.renderer.blockentity.StrangleFernBlockEntityRenderer;
import com.yelf42.cropcritters.client.renderer.entity.AbstractCritterRenderer;
import com.yelf42.cropcritters.client.renderer.entity.PopperPodEntityRenderer;
import com.yelf42.cropcritters.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;

public class CropCrittersFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LOST_SOUL_IN_A_JAR, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CRAWL_THISTLE, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CRIMSON_THORNWEED, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WAFTGRASS, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PUFFBOMB_MUSHROOM, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LIVERWORT, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WITHERING_SPITEWEED, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.POPPER_PLANT, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BONE_TRAP, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SOUL_ROSE, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TRIMMED_SOUL_ROSE, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.POTTED_SOUL_ROSE, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SOUL_POT, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MAZEWOOD, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MAZEWOOD_SAPLING, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.STRANGLE_FERN, RenderType.cutout());

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0x91BD59, ModBlocks.MAZEWOOD_SAPLING, ModBlocks.MAZEWOOD, ModBlocks.STRANGLE_FERN
        );

        ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
                        0x91BD59,
                ModBlocks.MAZEWOOD_SAPLING.asItem(), ModBlocks.MAZEWOOD.asItem()
        );

        BlockEntityRenderers.register(ModBlockEntities.SOUL_POT, SoulPotBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.STRANGLE_FERN, StrangleFernBlockEntityRenderer::new);

        // Entities
        EntityRendererRegistry.register(ModEntities.WHEAT_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "wheat_critter"), true));
        EntityRendererRegistry.register(ModEntities.MELON_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "melon_critter"), true));
        EntityRendererRegistry.register(ModEntities.PUMPKIN_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "pumpkin_critter"), false));
        EntityRendererRegistry.register(ModEntities.POTATO_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "potato_critter"), true));
        EntityRendererRegistry.register(ModEntities.CARROT_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "carrot_critter"), true));
        EntityRendererRegistry.register(ModEntities.BEETROOT_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "beetroot_critter"), true));
        EntityRendererRegistry.register(ModEntities.NETHER_WART_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "nether_wart_critter"), true));
        EntityRendererRegistry.register(ModEntities.POISONOUS_POTATO_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "poisonous_potato_critter"), true));
        EntityRendererRegistry.register(ModEntities.TORCHFLOWER_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "torchflower_critter"), true));
        EntityRendererRegistry.register(ModEntities.PITCHER_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "pitcher_critter"), false));
        EntityRendererRegistry.register(ModEntities.COCOA_CRITTER, context -> new AbstractCritterRenderer(context, CropCritters.identifier( "cocoa_critter"), false));

        EntityRendererRegistry.register(ModEntities.SEED_BALL_PROJECTILE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.SPIT_SEED_PROJECTILE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.POPPER_POD_PROJECTILE, PopperPodEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.POPPER_SEED_PROJECTILE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.HERBICIDE_PROJECTILE, ThrownItemRenderer::new);

        // Particles
        ParticleFactoryRegistry.getInstance().register(ModParticles.WATER_SPRAY, WaterSprayParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SPORES, SporeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_SIPHON, SoulSiphonParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_HEART, HeartParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_GLOW, SoulGlowParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_GLINT, SuspendedTownParticle.HappyVillagerProvider::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_GLINT_PLUME, SoulGlintPlumeParticle.Factory::new);

        // Packets
        PayloadTypeRegistry.playS2C().register(ModPackets.WaterSprayS2CPayload.ID, ModPackets.WaterSprayS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPackets.ParticleRingS2CPayload.ID, ModPackets.ParticleRingS2CPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.WaterSprayS2CPayload.ID, (payload, context) -> {
            ClientLevel world = context.client().level;
            if (world == null) return;

            Vec3 pos = payload.pos();
            Vec3 dir = payload.dir();
            world.addParticle(ModParticles.WATER_SPRAY, pos.x, pos.y + 0.2, pos.z, dir.x, 0, dir.z);
        });
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.ParticleRingS2CPayload.ID, (payload, context) -> {
            ClientLevel world = context.client().level;
            if (world == null) return;

            Vec3 pos = payload.pos();
            float radius = payload.radius();
            int count = payload.count();
            ParticleOptions effect = payload.effect();
            float angle = (float) ((Math.PI * 2.0) / ((float)count));
            for (int i = 0; i < count; i++) {
                world.addParticle(effect, pos.x + Math.sin(angle * i) * radius, pos.y, pos.z + Math.cos(angle * i) * radius, 0, 0, 0);
            }
        });
    }


}
