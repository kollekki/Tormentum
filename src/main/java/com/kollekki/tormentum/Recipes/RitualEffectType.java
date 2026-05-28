package com.kollekki.tormentum.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RitualEffectType<T extends RitualEffect> {

    private static final Map<String, RitualEffectType<?>> REGISTRY = new LinkedHashMap<>();

    public static final RitualEffectType<TransformEffect> TRANSFORM =
            register("tormentum:transform", TransformEffect.CODEC, TransformEffect.STREAM_CODEC);

    public static final RitualEffectType<RepairEffect> REPAIR =
            register("tormentum:repair", RepairEffect.CODEC, RepairEffect.STREAM_CODEC);

    public static final Codec<RitualEffect> DISPATCH_CODEC =
            Codec.STRING.dispatch(
                    "type",
                    effect -> effect.effectType().id,
                    id -> {
                        var t = REGISTRY.get(id);
                        if (t == null) throw new IllegalStateException("Unknown ritual effect type: " + id);
                        return t.mapCodec;
                    });

    public static final StreamCodec<RegistryFriendlyByteBuf, RitualEffect> DISPATCH_STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RitualEffect decode(RegistryFriendlyByteBuf buf) {
                    String id = buf.readUtf();
                    RitualEffectType<?> type = REGISTRY.get(id);
                    if (type == null) throw new IllegalStateException("Unknown ritual effect type: " + id);
                    return type.streamCodec.decode(buf);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, RitualEffect effect) {
                    buf.writeUtf(effect.effectType().id);
                    encodeUnchecked(buf, effect);
                }

                @SuppressWarnings("unchecked")
                private <T extends RitualEffect> void encodeUnchecked(
                        RegistryFriendlyByteBuf buf, RitualEffect effect) {
                    ((StreamCodec<RegistryFriendlyByteBuf, T>) effect.effectType().streamCodec)
                            .encode(buf, (T) effect);
                }
            };

    public final String id;
    private final MapCodec<T> mapCodec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    private RitualEffectType(
            String id,
            MapCodec<T> mapCodec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        this.id = id;
        this.mapCodec = mapCodec;
        this.streamCodec = streamCodec;
    }

    private static <T extends RitualEffect> RitualEffectType<T> register(
            String id,
            MapCodec<T> mapCodec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        var type = new RitualEffectType<>(id, mapCodec, streamCodec);
        REGISTRY.put(id, type);
        return type;
    }

}