package com.yelf42.cropcritters.items;

import com.yelf42.cropcritters.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.Level;

public class SeedBarRecipe extends CustomRecipe {

    public SeedBarRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    private static boolean validItem(ItemStack stack) {
        Item item = stack.getItem();
        String itemName = stack.getHoverName().getString();
        return itemName.contains("Seeds") && item instanceof BlockItem blockItem && blockItem.getBlock() instanceof BushBlock;
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        if (input.getItems().size() != 9 || input.getHeight() != 3 || input.getWidth() != 3) return false;
        if (!input.getItem(4).is(Items.SWEET_BERRIES) && !input.getItem(4).is(Items.GLOW_BERRIES)) return false;

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) continue;
                ItemStack is = input.getItem(j + i * 3);

                if (!validItem(is)) return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        return new ItemStack(ModItems.SEED_BAR, 2);
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return i * i1 >= 9;
    }

    @Override
    public RecipeSerializer<SeedBarRecipe> getSerializer() {
        return ModItems.SEED_BAR_RECIPE;
    }
}