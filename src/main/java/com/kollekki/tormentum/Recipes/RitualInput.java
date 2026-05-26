package com.kollekki.tormentum.Recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record RitualInput() implements RecipeInput {

    @Override
    public ItemStack getItem(int slot) {
        throw new IllegalArgumentException("aaaaa");
    }

    @Override
    public int size() {
        return 0;
    }
}