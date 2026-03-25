package com.yelf42.cropcritters.platform;

import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.platform.services.IPlatformHelper;
import com.yelf42.cropcritters.registry.ModPackets;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab.Builder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.function.BiFunction;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Builder tabBuilder() {
        return FabricItemGroup.builder();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> function, Block... validBlocks) {
        return FabricBlockEntityTypeBuilder.create(function::apply, validBlocks).build();
    }

    @Override
    public SimpleParticleType simpleParticleType() {
        return FabricParticleTypes.simple();
    }

    @Override
    public java.nio.file.Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    // AFFECTORS
    public static final AttachmentType<AffectorPositions> AFFECTOR_POSITIONS_ATTACHMENT_TYPE =
            AttachmentRegistry.<AffectorPositions>builder()
                    .persistent(AffectorPositions.CODEC)
                    .buildAndRegister(CropCritters.identifier("affector_positions"));

    // TODO test this
    @Override
    public AffectorPositions getAffectorPositions(ServerLevel world) {
        return ((AttachmentTarget) world).getAttachedOrElse(
                AFFECTOR_POSITIONS_ATTACHMENT_TYPE,
                AffectorPositions.EMPTY
        );
    }

    @Override
    public void setAffectorPositions(ServerLevel world, AffectorPositions positions) {
        ((AttachmentTarget) world).setAttached(AFFECTOR_POSITIONS_ATTACHMENT_TYPE, positions);
    }

    public static final ResourceLocation WATER_SPRAY_ID = CropCritters.identifier("water_spray_packet");
    public static final ResourceLocation PARTICLE_RING_ID = CropCritters.identifier("particle_ring_packet");

    // Call this during mod init
    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(WATER_SPRAY_ID, (server, player, handler, buf, responseSender) -> {});
        ServerPlayNetworking.registerGlobalReceiver(PARTICLE_RING_ID, (server, player, handler, buf, responseSender) -> {});
        // S2C packets don't need server-side receivers, but client needs to register handlers
        // That goes in your client init class
    }

    @Override
    public void sendWaterSpray(ServerLevel world, Vec3 pos, Vec3 dir) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        ModPackets.WaterSprayS2CPacket packet = new ModPackets.WaterSprayS2CPacket(pos, dir);
        ModPackets.WaterSprayS2CPacket.encode(packet, buf);
        for (ServerPlayer player : world.players()) {
            if (pos.closerThan(player.position(), 64)) {
                ServerPlayNetworking.send(player, WATER_SPRAY_ID, buf);
            }
        }
    }

    @Override
    public void sendParticleRingToNearbyPlayers(ServerLevel world, Vec3 center, float radius, int count) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        ModPackets.ParticleRingS2CPacket packet = new ModPackets.ParticleRingS2CPacket(center, radius, count);
        ModPackets.ParticleRingS2CPacket.encode(packet, buf);
        for (ServerPlayer player : world.players()) {
            if (center.closerThan(player.position(), 64)) {
                ServerPlayNetworking.send(player, PARTICLE_RING_ID, buf);
            }
        }
    }
}
