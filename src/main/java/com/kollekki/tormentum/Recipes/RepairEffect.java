package com.kollekki.tormentum.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record RepairEffect(int durabilityPerBlood) implements RitualEffect {

    public static final MapCodec<RepairEffect> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.optionalFieldOf("durability_per_blood", 1)
                    .forGetter(RepairEffect::durabilityPerBlood)
    ).apply(inst, RepairEffect::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RepairEffect> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, RepairEffect::durabilityPerBlood,
                    RepairEffect::new);

    @Override
    public ItemStack execute(RitualContext ctx) {
        ItemStack item = ctx.inputItem().copy();
        if (item.isEmpty() || !item.isDamaged()) return item;

        int repairAmount = ctx.bloodAvailable() * durabilityPerBlood;
        int newDamage = Math.max(0, item.getDamageValue() - repairAmount);
        item.setDamageValue(newDamage);
        return item;
    }

    @Override
    public RitualEffectType<?> effectType() {
        return RitualEffectType.REPAIR;
    }
}