package com.kollekki.tormentum.Recipes;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public interface RitualEffect {
    ItemStack execute(RitualContext ctx);

    RitualEffectType<?> effectType();

    Codec<RitualEffect> CODEC = RitualEffectType.DISPATCH_CODEC;
    StreamCodec<RegistryFriendlyByteBuf, RitualEffect> STREAM_CODEC = RitualEffectType.DISPATCH_STREAM_CODEC;
}