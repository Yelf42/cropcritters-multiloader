package com.yelf42.cropcritters.items;

import com.yelf42.cropcritters.registry.ModComponents;
import com.yelf42.cropcritters.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import com.yelf42.cropcritters.CropCritters;

import java.util.ArrayList;
import java.util.List;

public class SeedBallRecipe extends CustomRecipe {

    public SeedBallRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        if (input.getItems().size() != 9 || input.getHeight() != 3 || input.getWidth() != 3) return false;
        if (!input.getItem(4).is(Items.MUD)) return false;

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) continue;
                ItemStack is = input.getItem(j + i * 3);

                if (!is.is(Items.POISONOUS_POTATO) && validItem(is) == null) return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registryAccess) {
        ItemStack result = new ItemStack(ModItems.SEED_BALL, 1);

        List<ResourceLocation> usedSeeds = new ArrayList<>();
        int poisonousPotatoes = 0;

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++) {
                ItemStack is = input.getItem(j + i * 3);
                if (is.is(Items.POISONOUS_POTATO)) {
                    poisonousPotatoes += 1;
                } else {
                    ResourceLocation id = validItem(is);
                    if (id != null) {
                        if (!usedSeeds.contains(id)) usedSeeds.add(id);
                    }
                }
            }
        }

        ModComponents.setPoisonous(result, new ModComponents.PoisonousComponent(poisonousPotatoes));
        ModComponents.setSeedTypes(result, new ModComponents.SeedTypesComponent(usedSeeds));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return i * i1 >= 9;
    }

    private static ResourceLocation validItem(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem && (stack.is(CropCritters.SEED_BALL_CROPS) || blockItem.getBlock() instanceof CropBlock)) {
            return BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        }
        return null;
    }

    @Override
    public RecipeSerializer<SeedBallRecipe> getSerializer() {
        return ModItems.SEED_BALL_RECIPE;
    }
}
