package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.items.*;
import com.yelf42.cropcritters.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Rarity;
import com.yelf42.cropcritters.CropCritters;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ModItems {

    // ITEMS
    public static final LinkedHashMap<String, Item> REGISTERED_ITEMS = new LinkedHashMap<>();

    public static final Item STRANGE_FERTILIZER = registerItem("strange_fertilizer", StrangeFertilizerItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final Item LOST_SOUL = registerItem("lost_soul", LostSoulItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final Item SEED_BALL = registerItem("seed_ball", SeedBallItem::new, new Item.Properties().stacksTo(16).component(ModComponents.POISONOUS_SEED_BALL, new ModComponents.PoisonousComponent(0)));
    public static final Item POPPER_POD = registerItem("popper_pod", PopperPodItem::new, new Item.Properties());
    public static final Item HERBICIDE = registerItem("herbicide", HerbicideItem::new, new Item.Properties().stacksTo(16));

    public static final Item PUFFBOMB_SLICE = registerItem("puffbomb_slice", Item::new, new Item.Properties().food((new FoodProperties.Builder()).nutrition(2).saturationModifier(0.4F).build()));
    public static final Item COOKED_PUFFBOMB_STEAK = registerItem("cooked_puffbomb_steak", Item::new, new Item.Properties().food((new FoodProperties.Builder()).nutrition(7).saturationModifier(0.9F).build()));
    public static final Item SEED_BAR = registerItem("seed_bar", Item::new, new Item.Properties().food((new FoodProperties.Builder()).nutrition(5).saturationModifier(0.7F).build()));

    public static final Item WHEAT_CRITTER_SPAWN_EGG = registerSpawnEgg("wheat_critter_spawn_egg", ModEntities.WHEAT_CRITTER);
    public static final Item MELON_CRITTER_SPAWN_EGG = registerSpawnEgg("melon_critter_spawn_egg", ModEntities.MELON_CRITTER);
    public static final Item CARROT_CRITTER_SPAWN_EGG = registerSpawnEgg("carrot_critter_spawn_egg", ModEntities.CARROT_CRITTER);
    public static final Item PUMPKIN_CRITTER_SPAWN_EGG = registerSpawnEgg("pumpkin_critter_spawn_egg", ModEntities.PUMPKIN_CRITTER);
    public static final Item POTATO_CRITTER_SPAWN_EGG = registerSpawnEgg("potato_critter_spawn_egg", ModEntities.POTATO_CRITTER);
    public static final Item BEETROOT_CRITTER_SPAWN_EGG = registerSpawnEgg("beetroot_critter_spawn_egg", ModEntities.BEETROOT_CRITTER);
    public static final Item NETHER_WART_CRITTER_SPAWN_EGG = registerSpawnEgg("nether_wart_critter_spawn_egg", ModEntities.NETHER_WART_CRITTER);
    public static final Item POISONOUS_POTATO_CRITTER_SPAWN_EGG = registerSpawnEgg("poisonous_potato_critter_spawn_egg", ModEntities.POISONOUS_POTATO_CRITTER);
    public static final Item TORCHFLOWER_CRITTER_SPAWN_EGG = registerSpawnEgg("torchflower_critter_spawn_egg", ModEntities.TORCHFLOWER_CRITTER);
    public static final Item PITCHER_CRITTER_SPAWN_EGG = registerSpawnEgg("pitcher_critter_spawn_egg", ModEntities.PITCHER_CRITTER);
    public static final Item COCOA_CRITTER_SPAWN_EGG = registerSpawnEgg("cocoa_critter_spawn_egg", ModEntities.COCOA_CRITTER);

    private static ResourceKey<Item> vanillaItemId(String name) {
        return ResourceKey.create(Registries.ITEM, CropCritters.identifier(name));
    }

    public static Item registerItem(String name, Function<Item.Properties, Item> factory) {
        return registerItem(vanillaItemId(name), factory, new Item.Properties());
    }

    public static Item registerItem(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        return registerItem(vanillaItemId(name), factory, properties);
    }

    public static Item registerItem(String name, Item.Properties properties) {
        return registerItem(vanillaItemId(name), Item::new, properties);
    }

    public static Item registerItem(String name) {
        return registerItem(vanillaItemId(name), Item::new, new Item.Properties());
    }

    public static Item registerSpawnEgg(String name, EntityType<? extends Mob> entityType) {
        return registerItem(vanillaItemId(name), SpawnEggItem::new, new Item.Properties().spawnEgg(entityType));
    }

    public static Item registerItem(ResourceKey<Item> key, Function<Item.Properties, Item> factory) {
        return registerItem(key, factory, new Item.Properties());
    }

    public static Item registerItem(ResourceKey<Item> key, Function<Item.Properties, Item> factory, Item.Properties properties) {
        var item = factory.apply(properties.setId(key));
        REGISTERED_ITEMS.put(key.identifier().getPath(), item);

        return item;
    }

    /// BINDER
    public static void register(BiConsumer<Item, Identifier> consumer) {
        REGISTERED_ITEMS.forEach((key, value) -> consumer.accept(value, CropCritters.identifier(key)));
    }

    // TAB
    public static final CreativeModeTab CROPCRITTERS_TAB = Services.PLATFORM.tabBuilder()
            .icon(() -> new ItemStack(ModItems.LOST_SOUL))
            .title(Component.translatable("itemGroup.cropcritters"))
            .displayItems((itemDisplayParameters, output) -> {
                ModItems.REGISTERED_ITEMS.forEach((s, item) -> output.accept(item));
                ModBlocks.REGISTERED_BLOCK_ITEMS.forEach((s, item) -> output.accept(item));
            }).build();

    /// BINDER
    public static void registerTabs(BiConsumer<CreativeModeTab, Identifier> consumer) {
        consumer.accept(CROPCRITTERS_TAB, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "cropcritters_tab"));
    }


    // RECIPES
    public static final RecipeSerializer<SeedBallRecipe> SEED_BALL_RECIPE = new CustomRecipe.Serializer<>(SeedBallRecipe::new);
    public static final RecipeSerializer<SeedBarRecipe> SEED_BAR_RECIPE = new CustomRecipe.Serializer<>(SeedBarRecipe::new);

    /// BINDER
    public static void registerRecipes(BiConsumer<RecipeSerializer<?>, Identifier> consumer) {
        consumer.accept(SEED_BALL_RECIPE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "crafting_special_seed_ball"));
        consumer.accept(SEED_BAR_RECIPE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "crafting_special_seed_bar"));
    }
//    public static void initialize() {
//
//
//        // Compostable
//        CompostingChanceRegistry.INSTANCE.add(ModItems.STRANGE_FERTILIZER, 1.0f);
//        CompostingChanceRegistry.INSTANCE.add(ModItems.SEED_BALL, 0.8f);
//        CompostingChanceRegistry.INSTANCE.add(ModItems.SEED_BAR, 0.8f);
//        CompostingChanceRegistry.INSTANCE.add(ModItems.PUFFBOMB_SLICE, 0.4f);
//
//        // Fuel
//        FuelRegistryEvents.BUILD.register((builder, context) -> {
//            builder.add(ModItems.LOST_SOUL, 80 * 20);
//        });
//
//    }
}
