package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ModEntities {

    public static final LinkedHashMap<String, EntityType<?>> REGISTERED_ENTITIES = new LinkedHashMap<>();


    public static final EntityType<SeedBallProjectileEntity> SEED_BALL_PROJECTILE = registerProjectile("seed_ball_projectile", SeedBallProjectileEntity::new);
    public static final EntityType<SpitSeedProjectileEntity> SPIT_SEED_PROJECTILE = registerProjectile("spit_ball_projectile", SpitSeedProjectileEntity::new);
    public static final EntityType<PopperPodEntity> POPPER_POD_PROJECTILE = registerProjectile("popper_pod_projectile", PopperPodEntity::new);
    public static final EntityType<PopperSeedEntity> POPPER_SEED_PROJECTILE = registerProjectile("popper_seed_projectile", PopperSeedEntity::new);
    public static final EntityType<HerbicideEntity> HERBICIDE_PROJECTILE = registerProjectile("herbicide_projectile", HerbicideEntity::new);

    public static final EntityType<MelonCritterEntity> MELON_CRITTER = registerCritter("melon_critter", MelonCritterEntity::new, 0.65f, 0.7f, 0.25f);
    public static final EntityType<PumpkinCritterEntity> PUMPKIN_CRITTER = registerCritter("pumpkin_critter", PumpkinCritterEntity::new, 0.65f, 0.65f, 0.25f);
    public static final EntityType<WheatCritterEntity> WHEAT_CRITTER = registerCritter("wheat_critter", WheatCritterEntity::new, 0.5f, 0.9f, 0.25f);
    public static final EntityType<CarrotCritterEntity> CARROT_CRITTER = registerCritter("carrot_critter", CarrotCritterEntity::new, 0.5f, 0.8f, 0.25f);
    public static final EntityType<PotatoCritterEntity> POTATO_CRITTER = registerCritter("potato_critter", PotatoCritterEntity::new, 0.5f, 0.6f, 0.25f);
    public static final EntityType<BeetrootCritterEntity> BEETROOT_CRITTER = registerCritter("beetroot_critter", BeetrootCritterEntity::new, 0.5f, 0.6f, 0.25f);
    public static final EntityType<NetherWartCritterEntity> NETHER_WART_CRITTER = registerCritter("nether_wart_critter", NetherWartCritterEntity::new, 0.3f, 0.5f, 0.15f);
    public static final EntityType<PoisonousPotatoCritterEntity> POISONOUS_POTATO_CRITTER = registerCritter("poisonous_potato_critter", PoisonousPotatoCritterEntity::new, 0.5f, 0.6f, 0.25f);
    public static final EntityType<PitcherCritterEntity> PITCHER_CRITTER = registerCritter("pitcher_critter", PitcherCritterEntity::new, 0.85f, 1.1f, 0.7f);
    public static final EntityType<CocoaCritterEntity> COCOA_CRITTER = registerCritter("cocoa_critter", CocoaCritterEntity::new, 0.5f, 0.75f, 0.25f);
    public static final EntityType<TorchflowerCritterEntity> TORCHFLOWER_CRITTER = registerCritter("torchflower_critter", TorchflowerCritterEntity::new,0.5f, 0.6f, 0.25f);

    private static ResourceKey<EntityType<?>> vanillaEntityId(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, CropCritters.identifier(name));
    }

    public static <T extends Entity> EntityType<T> registerProjectile(String name, EntityType.EntityFactory<T> factory) {
        var entity = EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(10)
                .build(name);
        REGISTERED_ENTITIES.put(name, entity);
        return entity;
    }

    public static <T extends Entity> EntityType<T> registerCritter(String name, EntityType.EntityFactory<T> factory, float width, float height, float eyeHeight) {
        var entity = EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(width, height)
                .eyeHeight(eyeHeight)
                .clientTrackingRange(10)
                .build(name);
        REGISTERED_ENTITIES.put(name, entity);
        return entity;
    }

    /// BINDER
    public static void register(BiConsumer<EntityType<?>, ResourceLocation> consumer) {
        REGISTERED_ENTITIES.forEach((key, value) -> consumer.accept(value, CropCritters.identifier(key)));
    }


}
