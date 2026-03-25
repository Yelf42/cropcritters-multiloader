package com.yelf42.cropcritters.registry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class ModPackets {

    public record WaterSprayS2CPacket(Vec3 pos, Vec3 dir) {

        public static void encode(WaterSprayS2CPacket packet, FriendlyByteBuf buf) {
            buf.writeDouble(packet.pos().x); buf.writeDouble(packet.pos().y); buf.writeDouble(packet.pos().z);
            buf.writeDouble(packet.dir().x); buf.writeDouble(packet.dir().y); buf.writeDouble(packet.dir().z);
        }

        public static WaterSprayS2CPacket decode(FriendlyByteBuf buf) {
            Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            Vec3 dir = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            return new WaterSprayS2CPacket(pos, dir);
        }
    }

    public record ParticleRingS2CPacket(Vec3 pos, float radius, int count) {

        public static void encode(ParticleRingS2CPacket packet, FriendlyByteBuf buf) {
            buf.writeDouble(packet.pos().x); buf.writeDouble(packet.pos().y); buf.writeDouble(packet.pos().z);
            buf.writeFloat(packet.radius());
            buf.writeInt(packet.count());
        }

        public static ParticleRingS2CPacket decode(FriendlyByteBuf buf) {
            Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            float radius = buf.readFloat();
            int count = buf.readInt();
            return new ParticleRingS2CPacket(pos, radius, count);
        }
    }
}