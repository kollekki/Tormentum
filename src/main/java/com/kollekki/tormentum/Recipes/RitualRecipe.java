package com.kollekki.tormentum.Recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class RitualRecipe implements Recipe<RitualInput> {

    private final Recipe.CommonInfo commonInfo;

    public final int ritualTime;
    public final Identifier ritualSkull;
    public final Identifier ritualCatalyst;
    public final int chalkSquareSize;
    public final int bloodConsumption;
    public final int herbsConsumption;
    public final List<Identifier> customBlocksNeeded;
    public final Identifier sacrifice;
    public final Identifier resultItem;
    public final Identifier resultEntity;

    public RitualRecipe(
            Recipe.CommonInfo commonInfo,
            int ritualTime,
            Identifier ritualSkull,
            Identifier ritualCatalyst,
            int chalkSquareSize,
            int bloodConsumption,
            int herbsConsumption,
            List<Identifier> customBlocksNeeded,
            Identifier sacrifice,
            Identifier resultItem,
            Identifier resultEntity
    ) {
        this.commonInfo = commonInfo;
        this.ritualTime = ritualTime;
        this.ritualSkull = ritualSkull;
        this.ritualCatalyst = ritualCatalyst;
        this.chalkSquareSize = chalkSquareSize;
        this.bloodConsumption = bloodConsumption;
        this.herbsConsumption = herbsConsumption;
        this.customBlocksNeeded = customBlocksNeeded;
        this.sacrifice = sacrifice;
        this.resultItem = resultItem;
        this.resultEntity = resultEntity;
    }

    @Override
    public boolean matches(RitualInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RitualInput input) {
        if (resultItem != null) {
            return BuiltInRegistries.ITEM
                    .getOptional(resultItem)
                    .map(Item::getDefaultInstance)
                    .orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RitualInput>> getSerializer() {
        return ModRecipes.RITUAL_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<RitualInput>> getType() {
        return ModRecipes.RITUAL_TYPE.get();
    }

    @Override
    public boolean showNotification() {
        return this.commonInfo.showNotification();
    }

    public static final MapCodec<RitualRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),

            Codec.INT.fieldOf("ritualTime").forGetter(r -> r.ritualTime),
            Identifier.CODEC.fieldOf("ritualSkull").forGetter(r -> r.ritualSkull),
            Identifier.CODEC.fieldOf("ritualCatalyst").forGetter(r -> r.ritualCatalyst),

            Codec.INT.fieldOf("chalkSquareSize").forGetter(r -> r.chalkSquareSize),
            Codec.INT.fieldOf("bloodConsumption").forGetter(r -> r.bloodConsumption),
            Codec.INT.fieldOf("herbsConsumption").forGetter(r -> r.herbsConsumption),

            Identifier.CODEC.listOf().optionalFieldOf("customBlocksNeeded", List.of())
                    .forGetter(r -> r.customBlocksNeeded),

            Identifier.CODEC.optionalFieldOf("sacrifice")
                    .forGetter(r -> Optional.ofNullable(r.sacrifice)),

            Identifier.CODEC.optionalFieldOf("resultItem")
                    .forGetter(r -> Optional.ofNullable(r.resultItem)),

            Identifier.CODEC.optionalFieldOf("resultEntity")
                    .forGetter(r -> Optional.ofNullable(r.resultEntity))

    ).apply(inst, (common, time, skull, catalyst, chalk, blood, herbs, blocks, sacrifice, resultItem, resultEntity) ->
            new RitualRecipe(
                    common,
                    time,
                    skull,
                    catalyst,
                    chalk,
                    blood,
                    herbs,
                    blocks,
                    sacrifice.orElse(null),
                    resultItem.orElse(null),
                    resultEntity.orElse(null)
            )
    ));

    public static final StreamCodec<RegistryFriendlyByteBuf, RitualRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Recipe.CommonInfo.STREAM_CODEC, (RitualRecipe r) -> r.commonInfo,
                    ByteBufCodecs.INT, (RitualRecipe r) -> r.ritualTime,
                    Identifier.STREAM_CODEC, (RitualRecipe r) -> r.ritualSkull,
                    Identifier.STREAM_CODEC, (RitualRecipe r) -> r.ritualCatalyst,
                    ByteBufCodecs.INT, (RitualRecipe r) -> r.chalkSquareSize,
                    ByteBufCodecs.INT, (RitualRecipe r) -> r.bloodConsumption,
                    ByteBufCodecs.INT, (RitualRecipe r) -> r.herbsConsumption,
                    Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), (RitualRecipe r) -> r.customBlocksNeeded,

                    ByteBufCodecs.optional(Identifier.STREAM_CODEC), (RitualRecipe r) -> Optional.ofNullable(r.sacrifice),
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC), (RitualRecipe r) -> Optional.ofNullable(r.resultItem),
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC), (RitualRecipe r) -> Optional.ofNullable(r.resultEntity),

                    (common, time, skull, catalyst, chalk, blood, herbs, blocks, sacrifice, resultItem, resultEntity) ->
                            new RitualRecipe(
                                    common,
                                    time,
                                    skull,
                                    catalyst,
                                    chalk,
                                    blood,
                                    herbs,
                                    blocks,
                                    sacrifice.orElse(null),
                                    resultItem.orElse(null),
                                    resultEntity.orElse(null)
                            )
            );
}