package com.yelf42.cropcritters.registry;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.platform.Services;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import com.yelf42.cropcritters.CropCritters;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ModParticles {

    public static final LinkedHashMap<String, ParticleType<?>> REGISTERED_PARTICLES = new LinkedHashMap<>();


    public static final SimpleParticleType WATER_SPRAY = registerSimple("water_spray_particle");

    public static final SimpleParticleType SOUL_SIPHON = registerSimple("soul_siphon_particle");

    public static final ParticleType<ColorParticleOption> SPORES = registerTinted("spore_particle");

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

    private static ParticleType<ColorParticleOption> registerTinted(String name) {
        var colorParticleType = new ParticleType<ColorParticleOption>(false) {
            @Override
            public MapCodec<ColorParticleOption> codec() {
                return ColorParticleOption.codec(this);
            }
            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                return ColorParticleOption.streamCodec(this);
            }
        };

        REGISTERED_PARTICLES.put(name, colorParticleType);
        return colorParticleType;
    }

    /// BINDER
    public static void register(BiConsumer<ParticleType<?>, ResourceLocation> consumer) {
        REGISTERED_PARTICLES.forEach((key, value) -> consumer.accept(value, CropCritters.identifier(key)));
    }
}
