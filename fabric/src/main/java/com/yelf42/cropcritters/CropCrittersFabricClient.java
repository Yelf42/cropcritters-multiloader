package com.yelf42.cropcritters;

import com.yelf42.cropcritters.client.particle.*;
import com.yelf42.cropcritters.client.renderer.blockentity.StrangleFernBlockEntityRenderer;
import com.yelf42.cropcritters.client.renderer.entity.AbstractCritterRenderer;
import com.yelf42.cropcritters.client.renderer.entity.PopperPodEntityRenderer;
import com.yelf42.cropcritters.platform.FabricPlatformHelper;
import com.yelf42.cropcritters.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

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
        ParticleFactoryRegistry.getInstance().register(ModParticles.LOST_SOUL_GLOW, SoulGlowParticle.LostSoulFactory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_GLINT, SuspendedTownParticle.HappyVillagerProvider::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_GLINT_PLUME, SoulGlintPlumeParticle.Factory::new);

        ClientPlayNetworking.registerGlobalReceiver(FabricPlatformHelper.WATER_SPRAY_ID, (client, handler, buf, responseSender) -> {
            ModPackets.WaterSprayS2CPacket packet = ModPackets.WaterSprayS2CPacket.decode(buf);
            client.execute(() -> {
                ClientLevel world = client.level;
                if (world == null) return;
                world.addParticle(ModParticles.WATER_SPRAY,
                        packet.pos().x, packet.pos().y + 0.2, packet.pos().z,
                        packet.dir().x, 0, packet.dir().z);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricPlatformHelper.PARTICLE_RING_ID, (client, handler, buf, responseSender) -> {
            ModPackets.ParticleRingS2CPacket packet = ModPackets.ParticleRingS2CPacket.decode(buf);
            client.execute(() -> {
                ClientLevel world = client.level;
                if (world == null) return;
                float angle = (float) ((Math.PI * 2.0) / ((float) packet.count()));
                for (int i = 0; i < packet.count(); i++) {
                    world.addParticle(ModParticles.SOUL_GLOW,
                            packet.pos().x + Math.sin(angle * i) * packet.radius(),
                            packet.pos().y,
                            packet.pos().z + Math.cos(angle * i) * packet.radius(),
                            0, 0, 0);
                }
            });
        });
    }


}
