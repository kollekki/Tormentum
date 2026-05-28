package com.kollekki.tormentum.Recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record RitualInput(ItemStack inputItem) implements RecipeInput {

    @Override
    public ItemStack getItem(int slot) {
        if (slot == 0) return inputItem;
        throw new IllegalArgumentException("No slot " + slot + " in RitualInput");
    }

    @Override
    public int size() {
        return 1;
    }
}