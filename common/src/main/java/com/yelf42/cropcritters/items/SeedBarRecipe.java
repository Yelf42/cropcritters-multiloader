package com.yelf42.cropcritters.items;

import com.yelf42.cropcritters.registry.ModItems;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;

public class SeedBarRecipe extends CustomRecipe {

    public SeedBarRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        if (input.ingredientCount() != 9 || input.height() != 3 || input.width() != 3) return false;
        if (!input.getItem(1,1).is(Items.SWEET_BERRIES) && !input.getItem(1,1).is(Items.GLOW_BERRIES)) return false;

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) continue;
                ItemStack is = input.getItem(i,j);

                if (!validItem(is)) return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(ModItems.SEED_BAR, 2);
    }

    private static boolean validItem(ItemStack stack) {
        Item item = stack.getItem();
        String itemName = stack.getHoverName().getString();
        return itemName.contains("Seeds") && item instanceof BlockItem blockItem && blockItem.getBlock() instanceof VegetationBlock;
    }

    @Override
    public RecipeSerializer<SeedBarRecipe> getSerializer() {
        return ModItems.SEED_BAR_RECIPE;
    }
}