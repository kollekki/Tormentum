package com.kollekki.tormentum.Recipes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record RitualContext(
        ItemStack inputItem,
        int bloodAvailable,
        Level level,
        BlockPos pos
) {}