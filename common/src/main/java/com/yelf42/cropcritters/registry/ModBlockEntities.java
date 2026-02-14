package com.yelf42.cropcritters.registry;

import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.blocks.MazewoodSaplingBlockEntity;
import com.yelf42.cropcritters.blocks.SoulPotBlockEntity;
import com.yelf42.cropcritters.blocks.SoulRoseBlockEntity;
import com.yelf42.cropcritters.blocks.StrangleFernBlockEntity;
import com.yelf42.cropcritters.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ModBlockEntities {

    public static final LinkedHashMap<String, BlockEntityType<?>> REGISTERED_BLOCK_ENTITIES = new LinkedHashMap<>();

    public static final BlockEntityType<MazewoodSaplingBlockEntity> MAZEWOOD_SAPLING = register("mazewood_sapling",MazewoodSaplingBlockEntity::new, ModBlocks.MAZEWOOD_SAPLING);
    public static final BlockEntityType<StrangleFernBlockEntity> STRANGLE_FERN = register("strangle_fern",StrangleFernBlockEntity::new, ModBlocks.STRANGLE_FERN);
    public static final BlockEntityType<SoulRoseBlockEntity> SOUL_ROSE = register("soul_rose", SoulRoseBlockEntity::new, ModBlocks.SOUL_ROSE);
    public static BlockEntityType<SoulPotBlockEntity> SOUL_POT = register("soul_pot", SoulPotBlockEntity::new, ModBlocks.SOUL_POT);

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BiFunction<BlockPos, BlockState, T> function, Block block) {
        var blockEntity = Services.PLATFORM.blockEntityType(function, block);
        REGISTERED_BLOCK_ENTITIES.put(name, blockEntity);
        return blockEntity;
    }

    public static void register(BiConsumer<BlockEntityType<?>, Identifier> consumer) {
        REGISTERED_BLOCK_ENTITIES.forEach((key, value) -> consumer.accept(value, CropCritters.identifier(key)));
    }
}
