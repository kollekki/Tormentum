package com.kollekki.tormentum.Recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, "tormentum");

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, "tormentum");

    public static final Supplier<RecipeType<RitualRecipe>> RITUAL_TYPE =
            TYPES.register("ritual", RecipeType::simple);

    public static final Supplier<RecipeSerializer<RitualRecipe>> RITUAL_SERIALIZER =
            SERIALIZERS.register("ritual",
                    () -> new RecipeSerializer<>(RitualRecipe.CODEC, RitualRecipe.STREAM_CODEC)
            );
}