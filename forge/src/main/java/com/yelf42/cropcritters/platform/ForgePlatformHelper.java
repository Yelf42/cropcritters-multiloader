package com.yelf42.cropcritters.platform;

import com.mojang.datafixers.DSL;
import com.mojang.serialization.DataResult;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.ForgeClientPacketHandlers;
import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.platform.services.IPlatformHelper;
import com.yelf42.cropcritters.registry.ModPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.nio.file.Path;
import java.util.function.BiFunction;

public class ForgePlatformHelper implements IPlatformHelper {

    // ---- CAPABILITY DEFINITION ----

    public static final Capability<AffectorPositionsHolder> AFFECTOR_POSITIONS =
            CapabilityManager.get(new CapabilityToken<>() {});

    private static final ResourceLocation AFFECTOR_POSITIONS_KEY =
            new ResourceLocation(CropCritters.MOD_ID, "affector_positions");

    // Mutable holder so we can swap the immutable AffectorPositions value
    public static class AffectorPositionsHolder implements INBTSerializable<Tag> {
        public AffectorPositions value = AffectorPositions.EMPTY;

        @Override
        public Tag serializeNBT() {
            return AffectorPositions.CODEC.encodeStart(NbtOps.INSTANCE, value)
                    .getOrThrow(false, e -> CropCritters.LOGGER.error("Failed to serialize AffectorPositions: {}", e));
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            DataResult<AffectorPositions> result = AffectorPositions.CODEC.parse(NbtOps.INSTANCE, nbt);
            result.resultOrPartial(e -> CropCritters.LOGGER.error("Failed to deserialize AffectorPositions: {}", e))
                    .ifPresent(v -> value = v);
        }
    }

    // ---- CAPABILITY PROVIDER ----

    private static class AffectorPositionsProvider implements ICapabilitySerializable<Tag> {
        private final AffectorPositionsHolder holder = new AffectorPositionsHolder();
        private final LazyOptional<AffectorPositionsHolder> lazy = LazyOptional.of(() -> holder);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return AFFECTOR_POSITIONS.orEmpty(cap, lazy);
        }

        @Override
        public Tag serializeNBT() {
            return holder.serializeNBT();
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            holder.deserializeNBT(nbt);
        }
    }

    // ---- REGISTRATION ----

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CropCritters.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register(IEventBus modBus, IEventBus forgeBus) {
        modBus.addListener(ForgePlatformHelper::registerCapabilities);
        forgeBus.addGenericListener(Level.class, ForgePlatformHelper::attachCapabilities);

        int id = 0;
        CHANNEL.messageBuilder(ModPackets.WaterSprayS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ModPackets.WaterSprayS2CPacket::encode)
                .decoder(ModPackets.WaterSprayS2CPacket::decode)
                .consumerMainThread(ForgeClientPacketHandlers::handleWaterSpray)
                .add();

        CHANNEL.messageBuilder(ModPackets.ParticleRingS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ModPackets.ParticleRingS2CPacket::encode)
                .decoder(ModPackets.ParticleRingS2CPacket::decode)
                .consumerMainThread(ForgeClientPacketHandlers::handleParticleRing)
                .add();
    }

    @Override
    public void sendParticleRingToNearbyPlayers(ServerLevel world, Vec3 center, float radius, int count) {
        ModPackets.ParticleRingS2CPacket packet = new ModPackets.ParticleRingS2CPacket(center, radius, count);
        for (ServerPlayer player : world.players()) {
            if (center.closerThan(player.position(), 64)) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    @Override
    public void sendWaterSpray(ServerLevel world, Vec3 pos, Vec3 dir) {
        ModPackets.WaterSprayS2CPacket packet = new ModPackets.WaterSprayS2CPacket(pos, dir);
        for (ServerPlayer player : world.players()) {
            if (pos.closerThan(player.position(), 64)) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(AffectorPositionsHolder.class);
    }

    private static void attachCapabilities(AttachCapabilitiesEvent<Level> event) {
        if (event.getObject() instanceof ServerLevel) {
            event.addCapability(AFFECTOR_POSITIONS_KEY, new AffectorPositionsProvider());
        }
    }

    // ---- INTERFACE IMPLEMENTATION ----

    @Override
    public AffectorPositions getAffectorPositions(ServerLevel world) {
        return world.getCapability(AFFECTOR_POSITIONS)
                .map(h -> h.value)
                .orElse(AffectorPositions.EMPTY);
    }

    @Override
    public void setAffectorPositions(ServerLevel world, AffectorPositions positions) {
        world.getCapability(AFFECTOR_POSITIONS)
                .ifPresent(h -> h.value = positions);
    }

    // ---- OTHER PLATFORM METHODS ----

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public CreativeModeTab.Builder tabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> function, Block... validBlocks) {
        return BlockEntityType.Builder.of(function::apply, validBlocks).build(DSL.remainderType());
    }

    @Override
    public SimpleParticleType simpleParticleType() {
        return new SimpleParticleType(false);
    }

    @Override
    public Path getConfigPath() {
        return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
    }
}