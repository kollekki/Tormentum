package com.kollekki.tormentum;

import com.kollekki.tormentum.BlockEntities.BloodCollectorBlockEntity;
import com.kollekki.tormentum.BlockEntities.CrackedSkullBlockEntity;
import com.kollekki.tormentum.BlockEntities.FluidStorageTankBlockEntity;
import com.kollekki.tormentum.Blocks.*;
import com.kollekki.tormentum.Effects.BleedingEffect;
import com.kollekki.tormentum.Fluids.BloodFluid;
import com.kollekki.tormentum.Fluids.BloodFluidType;
import com.kollekki.tormentum.Items.*;
import com.kollekki.tormentum.Recipes.ModRecipes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.util.function.Supplier;

@Mod(Tormentum.MODID)
public class Tormentum {

    public static final String MODID = "tormentum";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> KOLLEKKI_SOUND =
            SOUNDS.register(
                    "kollekki",
                    registryName -> SoundEvent.createFixedRangeEvent(registryName, 8f)
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> MUS_MUSIC_BOX =
            SOUNDS.register(
                    "mus_music_box",
                    registryName -> SoundEvent.createFixedRangeEvent(registryName, 16f)
            );

    public static final DeferredHolder<FluidType, BloodFluidType> BLOOD_FLUID_TYPE =
            FLUID_TYPES.register("blood", () -> new BloodFluidType(
                    FluidType.Properties.create()
                            .density(1200)
                            .viscosity(1600)
                            .temperature(310)
            ));

    public static final DeferredHolder<Fluid, BloodFluid.Source> BLOOD_SOURCE =
            FLUIDS.register("blood_source", () -> new BloodFluid.Source(bloodProperties()));

    public static final DeferredHolder<Fluid, BloodFluid.Flowing> BLOOD_FLOWING =
            FLUIDS.register("blood_flowing", () -> new BloodFluid.Flowing(bloodProperties()));

    private static BaseFlowingFluid.Properties bloodProperties() {
        return new BaseFlowingFluid.Properties(
                BLOOD_FLUID_TYPE,
                () -> BLOOD_SOURCE.get(),
                () -> BLOOD_FLOWING.get()
        );
    }

    public static final DeferredBlock<Block> MUSIC_BOX = BLOCKS.registerSimpleBlock("music_box",
            p -> p.sound(SoundType.WOOD).mapColor(MapColor.WOOD).noOcclusion().strength(1).ignitedByLava());

    public static final DeferredItem<BlockItem> MUSIC_BOX_ITEM = ITEMS.registerSimpleBlockItem("music_box", MUSIC_BOX);

    public static final DeferredBlock<Block> CRACKED_SKULL_BLOCK = BLOCKS.registerBlock("cracked_skull",
            CrackedSkullBlock::new,
            p -> p.sound(SoundType.BONE_BLOCK).mapColor(MapColor.COLOR_BLACK).noOcclusion().strength(1));

    public static final DeferredItem<BlockItem> CRACKED_SKULL_ITEM = ITEMS.registerSimpleBlockItem("cracked_skull", CRACKED_SKULL_BLOCK);

    public static final DeferredBlock<Block> CHALK_BLOCK = BLOCKS.registerBlock("chalk_block",
            ChalkBlock::new,
            p -> p.sound(SoundType.SAND).mapColor(MapColor.QUARTZ).noCollision().instabreak().replaceable().forceSolidOn());

    public static final DeferredBlock<Block> KOLLEKKI = BLOCKS.registerBlock("kollekki",
            Kollekki::new,
            p -> p.sound(SoundType.WOOL).mapColor(MapColor.GOLD).strength(1).noOcclusion());

    public static final DeferredItem<BlockItem> KOLLEKKI_ITEM = ITEMS.registerSimpleBlockItem("kollekki", KOLLEKKI, p -> p.equippable(EquipmentSlot.HEAD).stacksTo(666));

    public static final DeferredBlock<Block> BLOOD_STAIN = BLOCKS.registerBlock("blood_stain",
            BloodStain::new,
            p -> p.sound(SoundType.MUD).noCollision().replaceable().strength(3, 5).forceSolidOn().mapColor(MapColor.COLOR_RED));

    public static final DeferredBlock<Block> BLOOD_PUDDLE = BLOCKS.registerBlock("blood_puddle",
            BloodPuddle::new,
            p -> p.sound(SoundType.MUD).noCollision().replaceable().strength(3, 5).forceSolidOn().mapColor(MapColor.COLOR_RED));

    public static final DeferredBlock<Block> FLUID_STORAGE_TANK =
            BLOCKS.registerBlock("fluid_storage_tank",
                    FluidStorageTankBlock::new,
                    p -> p.mapColor(MapColor.METAL)
                            .strength(3.5F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());

    public static final DeferredItem<BlockItem> FLUID_STORAGE_TANK_ITEM = ITEMS.registerSimpleBlockItem("fluid_storage_tank", FLUID_STORAGE_TANK);

    public static final DeferredBlock<Block> BLOOD_COLLECTOR =
            BLOCKS.registerBlock("blood_collector",
                    BloodCollectorBlock::new,
                    p -> p.mapColor(MapColor.METAL)
                            .strength(3.5F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());


    public static final DeferredItem<BlockItem> BLOOD_COLLECTOR_ITEM = ITEMS.registerSimpleBlockItem("blood_collector", BLOOD_COLLECTOR);

    public static final DeferredItem<Item> MORTAR_AND_PESTLE = ITEMS.registerSimpleItem("mortar_and_pestle", p -> p.stacksTo(1));
    public static final DeferredItem<Item> MOP = ITEMS.registerItem("mop", Mop::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> FICOVAT_HEART = ITEMS.registerSimpleItem("ficovat_heart");
    public static final DeferredItem<Item> CHALK = ITEMS.registerItem("chalk", ChalkItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> ATHAME = ITEMS.registerItem("athame", Athame::new, p -> p.stacksTo(1).useCooldown(6));

    public static final DeferredHolder<MobEffect, MobEffect> BLEEDING = MOB_EFFECTS.register("bleeding",
            () -> new BleedingEffect(
                    MobEffectCategory.HARMFUL,
                    0xff0000
            ).addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.fromNamespaceAndPath("tormentum", "effect.bleeding_slow"), -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    public static final Supplier<BlockEntityType<CrackedSkullBlockEntity>> CRACKED_SKULL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("cracked_skull",
                    () -> new BlockEntityType<>(
                            CrackedSkullBlockEntity::new,
                            false,
                            CRACKED_SKULL_BLOCK.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidStorageTankBlockEntity>>
            FLUID_STORAGE_TANK_BE =
            BLOCK_ENTITIES.register("fluid_storage_tank",
                    () -> new BlockEntityType<>(
                            FluidStorageTankBlockEntity::new,
                            false,
                            FLUID_STORAGE_TANK.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloodCollectorBlockEntity>>
            BLOOD_COLLECTOR_BE =
            BLOCK_ENTITIES.register("blood_collector",
                    () -> new BlockEntityType<>(
                            BloodCollectorBlockEntity::new,
                            false,
                            BLOOD_COLLECTOR.get()
                    ));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WATER_PUDDLE_PARTICLE =
            PARTICLES.register("water_puddle", () -> new SimpleParticleType(false));

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
            .icon(() -> FICOVAT_HEART.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CHALK.get());
                output.accept(ATHAME.get());
                output.accept(MOP.get());
                output.accept(MORTAR_AND_PESTLE.get());
                output.accept(CRACKED_SKULL_ITEM.get());
                output.accept(FICOVAT_HEART.get());
                output.accept(MUSIC_BOX_ITEM.get());
                output.accept(FLUID_STORAGE_TANK_ITEM.get());
                output.accept(BLOOD_COLLECTOR_ITEM.get());
            }).build());

    public Tormentum(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        PARTICLES.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        SOUNDS.register(modEventBus);
        ModRecipes.TYPES.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);

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
        LOGGER.info("hi from kollekki");
    }
}