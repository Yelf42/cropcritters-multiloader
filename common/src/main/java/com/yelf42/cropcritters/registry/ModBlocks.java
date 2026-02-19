package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.blocks.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.food.Foods;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class ModBlocks {

    public static final LinkedHashMap<String, Item> REGISTERED_BLOCK_ITEMS = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Block> REGISTERED_BLOCKS = new LinkedHashMap<>();

    private static ResourceKey<Item> vanillaItemId(String name) {
        return ResourceKey.create(Registries.ITEM, CropCritters.identifier(name));
    }
    private static ResourceKey<Block> vanillaBlockId(String name) {
        return ResourceKey.create(Registries.BLOCK, CropCritters.identifier(name));
    }

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        Block block = factory.apply(settings);
        REGISTERED_BLOCKS.put(name, block);
        REGISTERED_BLOCK_ITEMS.put(name, new BlockItem(block, itemSettings));
        return block;
    }

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, boolean shouldRegisterItem) {
        Block block = factory.apply(settings);
        REGISTERED_BLOCKS.put(name, block);
        if (shouldRegisterItem) {
            REGISTERED_BLOCK_ITEMS.put(name, new BlockItem(block, new Item.Properties()));
        }
        return block;
    }

    private static Block registerPotted(String name, BlockBehaviour.Properties settings, Block flower) {
        Block block = new FlowerPotBlock(flower, settings);
        REGISTERED_BLOCKS.put(name, block);
        return block;
    }

    /// BINDERS
    public static void registerItems(BiConsumer<Item, ResourceLocation> consumer) {
        REGISTERED_BLOCK_ITEMS.forEach((key, value) -> consumer.accept(value, CropCritters.identifier(key)));
    }
    public static void registerBlocks(BiConsumer<Block, ResourceLocation> consumer) {
        REGISTERED_BLOCKS.forEach((key, value) -> consumer.accept(value, CropCritters.identifier(key)));
    }

    public static final Block SOUL_FARMLAND = register(
            "soul_farmland",
            SoulFarmland::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.5F)
                    .sound(SoundType.SOUL_SOIL),
            true
    );

    public static final Block CRAWL_THISTLE = register(
            "crawl_thistle",
            CrawlThistle::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .strength(0.4f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block MAZEWOOD_SAPLING = register(
            "mazewood_sapling",
            MazewoodSaplingBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );
    public static final Block MAZEWOOD = register(
            "mazewood",
            MazewoodBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .forceSolidOn()
                    .strength(0.7f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .pushReaction(PushReaction.DESTROY),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block CRIMSON_THORNWEED = register(
            "crimson_thornweed",
            CrimsonThornweed::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NETHER)
                    .noCollission()
                    .randomTicks()
                    .strength(0.6f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block WITHERING_SPITEWEED = register(
            "withering_spiteweed",
            WitheringSpiteweed::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollission()
                    .randomTicks()
                    .strength(0.6f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block WAFTGRASS = register(
            "waftgrass",
            Waftgrass::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .noCollission()
                    .randomTicks()
                    .strength(0.6f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block STRANGLE_FERN = register(
            "strangle_fern",
            StrangleFern::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .strength(0.4f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block POPPER_PLANT = register(
            "popper_plant",
            PopperPlantBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .strength(0.4f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block BONE_TRAP = register(
            "bone_trap",
            BoneTrapBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .noCollission()
                    .strength(0.4f)
                    .sound(SoundType.BONE_BLOCK)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block PUFFBOMB_MUSHROOM = register(
            "puffbomb_mushroom",
            PuffbombPlantBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.FUNGUS)
                    .pushReaction(PushReaction.DESTROY),
            new Item.Properties().food((new FoodProperties.Builder())
                            .effect(ModEffects.EATEN_PUFFBOMB_POISONING.get(), 1.0F)
                            .build())
    );

    public static final Block PUFFBOMB_MUSHROOM_BLOCK = register(
            "puffbomb_mushroom_block",
            HugeMushroomBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(0.2F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava(),
            true
    );

    public static final Block LIVERWORT = register(
            "liverwort",
            LiverwortBlock::new,
            BlockBehaviour.Properties.of()
                    .replaceable()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GLOW_LICHEN)
                    .ignitedByLava().pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block SOUL_ROSE = register(
            "soul_rose",
            SoulRoseBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIAMOND)
                    .noCollission()
                    .strength(0.9f)
                    .sound(SoundType.TWISTING_VINES)
                    .emissiveRendering(((blockState, blockGetter, blockPos) -> true))
                    .pushReaction(PushReaction.DESTROY),
            new Item.Properties().rarity(Rarity.RARE)
    );

    public static final Block TRIMMED_SOUL_ROSE = register(
            "trimmed_soul_rose",
            TrimmedSoulRoseBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIAMOND)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.TWISTING_VINES)
                    .emissiveRendering(((blockState, blockGetter, blockPos) -> true))
                    .pushReaction(PushReaction.DESTROY),
            new Item.Properties().rarity(Rarity.RARE)
    );

    public static final Block POTTED_SOUL_ROSE = registerPotted(
            "potted_soul_rose",
            BlockBehaviour.Properties.of()
                    .instabreak()
                    .emissiveRendering(((blockState, blockGetter, blockPos) -> true))
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY),
            SOUL_ROSE
    );

    public static final Block LOST_SOUL_IN_A_JAR = register(
            "lost_soul_in_a_jar",
            LostSoulInAJarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .forceSolidOn()
                    .strength(0.3F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 12)
                    .noOcclusion()
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY),
            new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block SOUL_POT = register(
            "soul_pot",
            SoulPotBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .strength(0.0F, 0.0F)
                    .lightLevel(state -> state.getValue(SoulPotBlock.LEVEL))
                    .pushReaction(PushReaction.DESTROY)
                    .noOcclusion(),
            true
    );

    public static final Block TORCHFLOWER_SPARK = register(
            "torchflower_spark",
            TorchflowerSparkBlock::new,
            BlockBehaviour.Properties.of()
                    .lightLevel((state) -> 12)
                    .replaceable()
                    .noCollission()
                    .noLootTable()
                    .randomTicks()
                    .air(),
            false
    );

    // TODO add
//    public static void initialize() {
//        CropCritters.LOGGER.info("Initializing blocks for " + CropCritters.MOD_ID);
//
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.TALL_BUSH.asItem(), 0.8f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.ORNAMENTAL_BUSH.asItem(), 0.8f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.MAZEWOOD.asItem(), 0.8f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.PUFFBOMB_MUSHROOM.asItem(), 0.65f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK.asItem(), 0.65f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.BONE_TRAP.asItem(), 0.6f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.MAZEWOOD_SAPLING.asItem(), 0.4f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.CRAWL_THISTLE.asItem(), 0.3f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.CRIMSON_THORNWEED.asItem(), 0.2f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.STRANGLE_FERN.asItem(), 0.2f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.POPPER_PLANT.asItem(), 0.2f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.LIVERWORT.asItem(), 0.2f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.WAFTGRASS.asItem(), 0.2f);
//        CompostingChanceRegistry.INSTANCE.add(ModBlocks.WITHERING_SPITEWEED.asItem(), 0f);
//
//
//    }

    public static VoxelShape[] boxes(int i, IntFunction<VoxelShape> intFunction) {
        return IntStream.rangeClosed(0, i).mapToObj(intFunction).toArray(VoxelShape[]::new);
    }

    public static VoxelShape boxZ(double d, double e, double f, double g, double h) {
        double i = d / (double)2.0F;
        return Block.box((double)8.0F - i, e, g, (double)8.0F + i, f, h);
    }

    public static VoxelShape column(double sizeXz, double minY, double maxY) {
        double h = sizeXz / (double)2.0F;
        double i = sizeXz / (double)2.0F;
        return Block.box((double)8.0F - h, minY, (double)8.0F - i, (double)8.0F + h, maxY, (double)8.0F + i);
    }
}
