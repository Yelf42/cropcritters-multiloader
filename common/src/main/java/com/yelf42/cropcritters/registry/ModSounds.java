package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.CropCritters;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ModSounds {

    public static final LinkedHashMap<String, SoundEvent> REGISTERED_SOUNDS = new LinkedHashMap<>();

    public static final SoundEvent BONE_TRAP_CLOSE = register("bone_trap_close");
    public static final SoundEvent BONE_TRAP_OPEN = register("bone_trap_open");

    public static final SoundEvent LOST_SOUL_JAR_CHIME = register("lost_soul_jar_chime");

    public static final SoundEvent MAZEWOOD_MATURE = register("mazewood_mature");
    public static final SoundEvent MAZEWOOD_GROW = register("mazewood_grow");

    public static final SoundEvent TICKING = register("ticking");
    public static final Holder<SoundEvent> PUFFBOMB_EXPLODE = registerHolder("puffbomb_explode");

    public static final SoundEvent WITHER_ROSE_CONVERT = register("wither_rose_convert");
    public static final SoundEvent WITHER_ROSE_CONVERT_EXTRA = register("wither_rose_convert_extra");

    public static final SoundEvent SPORE_INFEST = register("spore_infest");

    public static final SoundEvent SPAWN_SLIME = register("spawn_slime");
    public static final SoundEvent SPAWN_CRITTER = register("spawn_critter");

    public static final SoundEvent THROW_SEED_BALL = register("throw_seed_ball");

    public static final SoundEvent REVIVE_CORAL = register("revive_coral");

    public static final SoundEvent ENTITY_CRITTER_AMBIENT = register("entity_critter_ambient");
    public static final SoundEvent ENTITY_CRITTER_EVIL_AMBIENT = register("entity_critter_evil_ambient");
    public static final SoundEvent ENTITY_CRITTER_EVIL_STING = register("entity_critter_evil_sting");
    public static final SoundEvent ENTITY_CRITTER_HURT = register("entity_critter_hurt");
    public static final SoundEvent ENTITY_CRITTER_DEATH = register("entity_critter_death");
    public static final SoundEvent ENTITY_CRITTER_LARGE = register("entity_critter_large");
    public static final SoundEvent ENTITY_CRITTER_TILL = register("entity_critter_till");
    public static final SoundEvent ENTITY_CRITTER_SHEAR = register("entity_critter_shear");
    public static final SoundEvent ENTITY_CRITTER_DROP = register("entity_critter_drop");
    public static final SoundEvent ENTITY_CRITTER_SPIT = register("entity_critter_spit");
    public static final SoundEvent ENTITY_CRITTER_WATER = register("entity_critter_water");
    public static final SoundEvent ENTITY_CRITTER_EAT = register("entity_critter_eat");

    public static final SoundEvent POPPER_POD_POP = register("popper_pod_pop");
    public static final SoundEvent POPPER_POD_LAUNCH = register("popper_pod_launch");


    private static SoundEvent register(String name) {
        var soundEvent = SoundEvent.createVariableRangeEvent(CropCritters.identifier(name));
        REGISTERED_SOUNDS.put(name, soundEvent);
        return soundEvent;
    }

    private static Holder<SoundEvent> registerHolder(String name) {
        var soundEvent = SoundEvent.createVariableRangeEvent(CropCritters.identifier(name));
        REGISTERED_SOUNDS.put(name, soundEvent);
        return Holder.direct(soundEvent);
    }

    /// BINDER
    public static void register(BiConsumer<SoundEvent, ResourceLocation> consumer) {
        REGISTERED_SOUNDS.forEach((key, value) -> consumer.accept(value, CropCritters.identifier(key)));
    }

}
