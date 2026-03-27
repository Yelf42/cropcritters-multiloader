package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.effects.PuffbombPoisoningEffect;
import com.yelf42.cropcritters.effects.SoulSiphonEffect;
import com.yelf42.cropcritters.effects.SporesEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceLocation;
import com.yelf42.cropcritters.CropCritters;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ModEffects {

    public static final LinkedHashMap<String, MobEffect> REGISTERED_EFFECTS = new LinkedHashMap<>();

    public static final MobEffect SPORES = register("spores_effect", new SporesEffect(MobEffectCategory.NEUTRAL, 0));
    public static final Supplier<MobEffectInstance> NATURAL_SPORES = () -> new MobEffectInstance(SPORES, 6000, 0, true, true, false);

    public static final MobEffect PUFFBOMB_POISONING = register("puffbomb_poisoning", new PuffbombPoisoningEffect(MobEffectCategory.HARMFUL, 0));
    public static final Supplier<MobEffectInstance> EATEN_PUFFBOMB_POISONING = () -> new MobEffectInstance(PUFFBOMB_POISONING, 2400, 0, false, false, true);

    public static final MobEffect SOUL_SIPHON = register("soul_siphon", new SoulSiphonEffect(MobEffectCategory.HARMFUL, 0));

    private static MobEffect register(String id, MobEffect statusEffect) {
        REGISTERED_EFFECTS.put(id, statusEffect);
        return statusEffect;
    }

    /// BINDER
    public static void register(BiConsumer<MobEffect, ResourceLocation> consumer) {
        REGISTERED_EFFECTS.forEach((key, value) -> consumer.accept(value, CropCritters.identifier(key)));
    }


}
