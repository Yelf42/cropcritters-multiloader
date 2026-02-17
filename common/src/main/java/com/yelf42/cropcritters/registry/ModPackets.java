package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.CropCritters;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

// Needs payload handling in the loaders
public class ModPackets {

    private static final StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new
    );

    public record WaterSprayS2CPayload(Vec3 pos, Vec3 dir) implements CustomPacketPayload {
        public static final ResourceLocation WATER_SPRAY_PAYLOAD_ID = CropCritters.identifier("water_spray_packet");
        public static final CustomPacketPayload.Type<WaterSprayS2CPayload> ID = new CustomPacketPayload.Type<>(WATER_SPRAY_PAYLOAD_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, WaterSprayS2CPayload> CODEC = StreamCodec.composite(
                VEC3_STREAM_CODEC, WaterSprayS2CPayload::pos,
                VEC3_STREAM_CODEC, WaterSprayS2CPayload::dir,
                WaterSprayS2CPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record ParticleRingS2CPayload(Vec3 pos, float radius, int count, ParticleOptions effect) implements CustomPacketPayload {
        public static final ResourceLocation PARTICLE_RING_PAYLOAD_ID = CropCritters.identifier("particle_ring_packet");
        public static final CustomPacketPayload.Type<ParticleRingS2CPayload> ID = new CustomPacketPayload.Type<>(PARTICLE_RING_PAYLOAD_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ParticleRingS2CPayload> CODEC = StreamCodec.composite(
                VEC3_STREAM_CODEC, ParticleRingS2CPayload::pos,
                ByteBufCodecs.FLOAT, ParticleRingS2CPayload::radius,
                ByteBufCodecs.INT, ParticleRingS2CPayload::count,
                ParticleTypes.STREAM_CODEC, ParticleRingS2CPayload::effect,
                ParticleRingS2CPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

}
