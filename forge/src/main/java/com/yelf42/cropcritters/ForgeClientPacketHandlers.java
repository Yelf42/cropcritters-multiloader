package com.yelf42.cropcritters;

import com.yelf42.cropcritters.registry.ModPackets;
import com.yelf42.cropcritters.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class ForgeClientPacketHandlers {
    public static void handleWaterSpray(ModPackets.WaterSprayS2CPacket packet, Supplier<Context> ctx) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        world.addParticle(ModParticles.WATER_SPRAY,
                packet.pos().x, packet.pos().y + 0.2, packet.pos().z,
                packet.dir().x, 0, packet.dir().z);
    }

    public static void handleParticleRing(ModPackets.ParticleRingS2CPacket packet, Supplier<Context> ctx) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        float angle = (float) ((Math.PI * 2.0) / ((float) packet.count()));
        for (int i = 0; i < packet.count(); i++) {
            world.addParticle(ModParticles.SOUL_GLOW,
                    packet.pos().x + Math.sin(angle * i) * packet.radius(),
                    packet.pos().y,
                    packet.pos().z + Math.cos(angle * i) * packet.radius(),
                    0, 0, 0);
        }
    }
}
