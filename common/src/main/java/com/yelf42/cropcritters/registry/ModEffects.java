package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.effects.PuffbombPoisoningEffect;
import com.yelf42.cropcritters.effects.SoulSiphonEffect;
import com.yelf42.cropcritters.effects.SporesEffect;
import com.yelf42.cropcritters.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import com.yelf42.cropcritters.CropCritters;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ModEffects {

    public static void init() {
        // just referencing the class forces static initialization
    }

    public static final Holder<MobEffect> SPORES = register("spores_effect", new SporesEffect(MobEffectCategory.NEUTRAL, 5882118));
    public static final Supplier<MobEffectInstance> NATURAL_SPORES = () -> new MobEffectInstance(SPORES, 6000, 0, true, true, false);

    public static final Holder<MobEffect> PUFFBOMB_POISONING = register("puffbomb_poisoning", new PuffbombPoisoningEffect(MobEffectCategory.HARMFUL, 16770790));
    public static final Supplier<MobEffectInstance> EATEN_PUFFBOMB_POISONING = () -> new MobEffectInstance(PUFFBOMB_POISONING, 2400, 0, false, false, true);

    public static final Holder<MobEffect> SOUL_SIPHON = register("soul_siphon", new SoulSiphonEffect(MobEffectCategory.HARMFUL, 7561558));


    private static Holder<MobEffect> register(String id, MobEffect statusEffect) {
        return Services.PLATFORM.registerEffectForHolder(CropCritters.identifier(id), statusEffect);
    }
}
