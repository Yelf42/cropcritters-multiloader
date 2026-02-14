package com.yelf42.cropcritters;

import com.yelf42.cropcritters.client.particle.*;
import com.yelf42.cropcritters.client.renderer.blockentity.SoulPotBlockEntityRenderer;
import com.yelf42.cropcritters.client.renderer.blockentity.SoulPotModelRenderer;
import com.yelf42.cropcritters.client.renderer.blockentity.StrangleFernBlockEntityRenderer;
import com.yelf42.cropcritters.client.renderer.entity.AbstractCritterRenderer;
import com.yelf42.cropcritters.client.renderer.entity.PopperPodEntityRenderer;
import com.yelf42.cropcritters.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class CropCrittersFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlock(ModBlocks.LOST_SOUL_IN_A_JAR, ChunkSectionLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(ModBlocks.CRAWL_THISTLE, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.CRIMSON_THORNWEED, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.WAFTGRASS, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.PUFFBOMB_MUSHROOM, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.LIVERWORT, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.WITHERING_SPITEWEED, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.POPPER_PLANT, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.BONE_TRAP, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.SOUL_ROSE, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TRIMMED_SOUL_ROSE, ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.POTTED_SOUL_ROSE, ChunkSectionLayer.CUTOUT);

        BlockRenderLayerMap.putBlock(ModBlocks.TALL_BUSH, ChunkSectionLayer.CUTOUT);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0x91BD59, ModBlocks.TALL_BUSH
        );
        BlockRenderLayerMap.putBlock(ModBlocks.ORNAMENTAL_BUSH, ChunkSectionLayer.CUTOUT);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0x91BD59, ModBlocks.ORNAMENTAL_BUSH
        );
        BlockRenderLayerMap.putBlock(ModBlocks.MAZEWOOD, ChunkSectionLayer.CUTOUT);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0x91BD59, ModBlocks.MAZEWOOD
        );
        BlockRenderLayerMap.putBlock(ModBlocks.MAZEWOOD_SAPLING, ChunkSectionLayer.CUTOUT);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0x91BD59, ModBlocks.MAZEWOOD_SAPLING
        );

        BlockRenderLayerMap.putBlock(ModBlocks.STRANGLE_FERN, ChunkSectionLayer.CUTOUT);
        BlockEntityRenderers.register(ModBlockEntities.STRANGLE_FERN, StrangleFernBlockEntityRenderer::new);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0x91BD59, ModBlocks.STRANGLE_FERN
        );

        SpecialModelRenderers.ID_MAPPER.put(
                CropCritters.identifier("soul_pot"),
                SoulPotModelRenderer.Unbaked.CODEC
        );
        BlockEntityRenderers.register(ModBlockEntities.SOUL_POT, SoulPotBlockEntityRenderer::new);

        // Entities
        EntityRenderers.register(ModEntities.WHEAT_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "wheat_critter"), true));
        EntityRenderers.register(ModEntities.MELON_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "melon_critter"), true));
        EntityRenderers.register(ModEntities.PUMPKIN_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "pumpkin_critter"), false));
        EntityRenderers.register(ModEntities.POTATO_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "potato_critter"), true));
        EntityRenderers.register(ModEntities.CARROT_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "carrot_critter"), true));
        EntityRenderers.register(ModEntities.BEETROOT_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "beetroot_critter"), true));
        EntityRenderers.register(ModEntities.NETHER_WART_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "nether_wart_critter"), true));
        EntityRenderers.register(ModEntities.POISONOUS_POTATO_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "poisonous_potato_critter"), true));
        EntityRenderers.register(ModEntities.TORCHFLOWER_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "torchflower_critter"), true));
        EntityRenderers.register(ModEntities.PITCHER_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "pitcher_critter"), false));
        EntityRenderers.register(ModEntities.COCOA_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "cocoa_critter"), false));

        EntityRenderers.register(ModEntities.SEED_BALL_PROJECTILE, ThrownItemRenderer::new);
        EntityRenderers.register(ModEntities.SPIT_SEED_PROJECTILE, ThrownItemRenderer::new);
        EntityRenderers.register(ModEntities.POPPER_POD_PROJECTILE, PopperPodEntityRenderer::new);
        EntityRenderers.register(ModEntities.POPPER_SEED_PROJECTILE, ThrownItemRenderer::new);
        EntityRenderers.register(ModEntities.HERBICIDE_PROJECTILE, ThrownItemRenderer::new);

        // Particles
        ParticleFactoryRegistry.getInstance().register(ModParticles.WATER_SPRAY, WaterSprayParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SPORES, SporeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_SIPHON, SoulSiphonParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_HEART, HeartParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_GLOW, SoulGlowParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_GLINT, SuspendedTownParticle.HappyVillagerProvider::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_GLINT_PLUME, SoulGlintPlumeParticle.Factory::new);

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
