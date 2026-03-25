package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.platform.Services;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import com.yelf42.cropcritters.CropCritters;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ModParticles {

    public static final LinkedHashMap<String, ParticleType<?>> REGISTERED_PARTICLES = new LinkedHashMap<>();


    public static final SimpleParticleType WATER_SPRAY = registerSimple("water_spray_particle");

    public static final SimpleParticleType SOUL_SIPHON = registerSimple("soul_siphon_particle");

    public static final SimpleParticleType SPORES = registerSimple("spore_particle");

    public static final SimpleParticleType SOUL_HEART = registerSimple("soul_heart_particle");

    public static final SimpleParticleType SOUL_GLOW = registerSimple("soul_glow_particle");

    public static final SimpleParticleType LOST_SOUL_GLOW = registerSimple("lost_soul_glow_particle");

    public static final SimpleParticleType SOUL_GLINT = registerSimple("soul_glint_particle");

    public static final SimpleParticleType SOUL_GLINT_PLUME = registerSimple("soul_glint_plume_particle");

    private static SimpleParticleType registerSimple(String name) {
        var simpleParticleType = Services.PLATFORM.simpleParticleType();
        REGISTERED_PARTICLES.put(name, simpleParticleType);
        return simpleParticleType;
    }

    /// BINDER
    public static void register(BiConsumer<ParticleType<?>, ResourceLocation> consumer) {
        REGISTERED_PARTICLES.forEach((key, value) -> consumer.accept(value, CropCritters.identifier(key)));
    }
}
