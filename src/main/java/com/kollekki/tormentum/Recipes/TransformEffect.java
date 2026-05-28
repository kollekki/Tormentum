package com.kollekki.tormentum.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record TransformEffect(Identifier resultItem) implements RitualEffect {

    public static final MapCodec<TransformEffect> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Identifier.CODEC.fieldOf("result_item").forGetter(TransformEffect::resultItem)
    ).apply(inst, TransformEffect::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransformEffect> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC, TransformEffect::resultItem,
                    TransformEffect::new);

    @Override
    public ItemStack execute(RitualContext ctx) {
        return BuiltInRegistries.ITEM
                .getOptional(resultItem)
                .map(Item::getDefaultInstance)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public RitualEffectType<?> effectType() {
        return RitualEffectType.TRANSFORM;
    }
}