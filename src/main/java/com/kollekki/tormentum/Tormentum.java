package com.kollekki.tormentum;

import com.kollekki.tormentum.Effects.BleedingEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.kollekki.tormentum.Items.*;
import com.kollekki.tormentum.Blocks.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;



@Mod(Tormentum.MODID)
public class Tormentum {

    public static final String MODID = "tormentum";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);

    public static final DeferredBlock<Block> CHALK_BLOCK = BLOCKS.registerBlock("chalk_block",ChalkBlock::new, p -> p.sound(SoundType.SAND).mapColor(MapColor.QUARTZ).noCollision().instabreak());

    public static final DeferredBlock<Block> BLOOD_STAIN = BLOCKS.registerBlock("blood_stain",BloodStain::new, p -> p.sound(SoundType.MUD).noCollision().instabreak().noLootTable().strength(0, 0).forceSolidOn().mapColor(MapColor.COLOR_RED));
    public static final DeferredBlock<Block> BLOOD_PUDDLE = BLOCKS.registerBlock("blood_puddle",BloodPuddle::new, p -> p.sound(SoundType.MUD).noCollision().instabreak().noLootTable().strength(0, 0).forceSolidOn().mapColor(MapColor.COLOR_RED));

    public static final DeferredItem<Item> MORTAR_AND_PESTLE = ITEMS.registerSimpleItem("mortar_and_pestle", p -> p.stacksTo(1));

    public static final DeferredItem<Item> CHALK = ITEMS.registerItem("chalk", ChalkItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<Item> BUTTER_KNIFE = ITEMS.registerItem("butter_knife", ButterKnife::new, p -> p.stacksTo(1).useCooldown(5));

    public static final DeferredHolder<MobEffect, MobEffect> BLEEDING = MOB_EFFECTS.register("bleeding", () -> new BleedingEffect(MobEffectCategory.HARMFUL, 0xff0000)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.fromNamespaceAndPath("tormentum", "effect.bleeding_slow"), -0.7, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    public static final ResourceKey<DamageType> BLEEDING_DAMAGE =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    Identifier.fromNamespaceAndPath(Tormentum.MODID, "bleeding")
            );

    public static DamageSource bleedingDamage(LivingEntity entity) {
        return new DamageSource(
                entity.level().registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(Tormentum.BLEEDING_DAMAGE),
                entity
        );
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tormentum", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tormentum"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> CHALK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CHALK.get());
                output.accept(BUTTER_KNIFE.get());
                output.accept(MORTAR_AND_PESTLE.get());
            }).build());

    public Tormentum(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);

        ITEMS.register(modEventBus);

        CREATIVE_MODE_TABS.register(modEventBus);

        MOB_EFFECTS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("hi from kollekki");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("hi from kollekki - Tormentum");
    }
}
