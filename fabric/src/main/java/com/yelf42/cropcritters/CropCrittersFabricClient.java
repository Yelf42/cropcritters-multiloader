package com.yelf42.cropcritters;

import com.yelf42.cropcritters.client.particle.*;
import com.yelf42.cropcritters.client.renderer.blockentity.StrangleFernBlockEntityRenderer;
import com.yelf42.cropcritters.client.renderer.entity.AbstractCritterRenderer;
import com.yelf42.cropcritters.client.renderer.entity.PopperPodEntityRenderer;
import com.yelf42.cropcritters.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CropCrittersFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ModBlockEntities.STRANGLE_FERN, StrangleFernBlockEntityRenderer::new);

        BlockColorRegistry.register(List.of(new BlockTintSource() {
            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return BiomeColors.getAverageGrassColor(level, pos);
            }

            @Override
            public int color(BlockState state) {
                return 0x91BD59;
            }
        }), ModBlocks.STRANGLE_FERN, ModBlocks.MAZEWOOD, ModBlocks.MAZEWOOD_SAPLING, ModBlocks.ORNAMENTAL_BUSH, ModBlocks.TALL_BUSH);

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
        ParticleProviderRegistry.getInstance().register(ModParticles.WATER_SPRAY, WaterSprayParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.SPORES, SporeParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.SOUL_SIPHON, SoulSiphonParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.SOUL_HEART, HeartParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.SOUL_GLOW, SoulGlowParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.SOUL_GLINT, SuspendedTownParticle.HappyVillagerProvider::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.SOUL_GLINT_PLUME, SoulGlintPlumeParticle.Factory::new);

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
