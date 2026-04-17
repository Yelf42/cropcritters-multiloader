package com.yelf42.cropcritters.items;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModComponents;
import com.yelf42.cropcritters.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.CropCritters;

import java.util.ArrayList;
import java.util.List;

public class SeedBallRecipe extends CustomRecipe {
    public static final SeedBallRecipe INSTANCE = new SeedBallRecipe();
    public static final MapCodec<SeedBallRecipe> MAP_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, SeedBallRecipe> STREAM_CODEC;
    public static final RecipeSerializer<SeedBallRecipe> SERIALIZER;


    public SeedBallRecipe() {
        super();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        if (input.ingredientCount() != 9 || input.height() != 3 || input.width() != 3) return false;
        if (!input.getItem(1,1).is(Items.MUD)) return false;

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) continue;
                ItemStack is = input.getItem(i,j);

                if (!is.is(Items.POISONOUS_POTATO) && validItem(is) == null) return false;
            }
        }

//        // 4 direction version
//        if (!input.getStackInSlot(0,1).isIn(CropCritters.SEED_BALL_CROPS) && !input.getStackInSlot(0,1).isOf(Items.POISONOUS_POTATO)) return false;
//        if (!input.getStackInSlot(1,0).isIn(CropCritters.SEED_BALL_CROPS) && !input.getStackInSlot(1,0).isOf(Items.POISONOUS_POTATO)) return false;
//        if (!input.getStackInSlot(1,2).isIn(CropCritters.SEED_BALL_CROPS) && !input.getStackInSlot(1,2).isOf(Items.POISONOUS_POTATO)) return false;
//        if (!input.getStackInSlot(2,1).isIn(CropCritters.SEED_BALL_CROPS) && !input.getStackInSlot(2,1).isOf(Items.POISONOUS_POTATO)) return false;
//

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = new ItemStack(ModItems.SEED_BALL, 1);

        List<Identifier> usedSeeds = new ArrayList<>();
        int poisonousPotatoes = 0;

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++) {
                ItemStack is = input.getItem(i,j);
                if (is.is(Items.POISONOUS_POTATO)) {
                    poisonousPotatoes += 1;
                } else {
                    Identifier id = validItem(is);
                    if (id != null) {
                        if (!usedSeeds.contains(id)) usedSeeds.add(id);
                    }
                }
            }
        }

        result.set(ModComponents.POISONOUS_SEED_BALL, new ModComponents.PoisonousComponent(poisonousPotatoes));

        result.set(ModComponents.SEED_TYPES, new ModComponents.SeedTypesComponent(usedSeeds));
        return result;
    }

    private static Identifier validItem(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem && (stack.is(CropCritters.SEED_BALL_CROPS) || blockItem.getBlock() instanceof CropBlock)) {
            return BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        }
        return null;
    }

    public RecipeSerializer<SeedBallRecipe> getSerializer() {
        return SERIALIZER;
    }

    static {
        MAP_CODEC = MapCodec.unit(INSTANCE);
        STREAM_CODEC = StreamCodec.unit(INSTANCE);
        SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
    }
}
