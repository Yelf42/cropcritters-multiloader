package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.effects.PuffbombPoisoningEffect;
import com.yelf42.cropcritters.effects.SoulSiphonEffect;
import com.yelf42.cropcritters.effects.SporesEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import com.yelf42.cropcritters.CropCritters;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ModEffects {

    public static final LinkedHashMap<String, MobEffect> REGISTERED_EFFECTS = new LinkedHashMap<>();


    public static final Holder<MobEffect> SPORES = register("spores_effect", new SporesEffect(MobEffectCategory.NEUTRAL, 5882118));
    public static final MobEffectInstance NATURAL_SPORES = new MobEffectInstance(SPORES, 6000, 0, true, true, false);

    public static final Holder<MobEffect> PUFFBOMB_POISONING = register("puffbomb_poisoning", new PuffbombPoisoningEffect(MobEffectCategory.HARMFUL, 16770790));
    public static final MobEffectInstance EATEN_PUFFBOMB_POISONING = new MobEffectInstance(PUFFBOMB_POISONING, 2400, 0, false, false, true);

    public static final Holder<MobEffect> SOUL_SIPHON = register("soul_siphon", new SoulSiphonEffect(MobEffectCategory.HARMFUL, 7561558));


    private static Holder<MobEffect> register(String id, MobEffect statusEffect) {
        REGISTERED_EFFECTS.put(id, statusEffect);
        return Holder.direct(statusEffect);
    }

    /// BINDER
    public static void register(BiConsumer<MobEffect, ResourceLocation> consumer) {
        REGISTERED_EFFECTS.forEach((key, value) -> consumer.accept(value, CropCritters.identifier(key)));
    }
}
