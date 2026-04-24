package com.kollekki.tormentum;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.kollekki.tormentum.items.*;
import com.kollekki.tormentum.blocks.*;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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

    public static final DeferredBlock<Block> CHALK_BLOCK = BLOCKS.registerBlock("chalk_block",ChalkBlock::new, p -> p.sound(SoundType.SAND).mapColor(MapColor.QUARTZ).noCollision().instabreak());
    
    public static final DeferredItem<Item> CHALK = ITEMS.registerItem("chalk", ChalkItem::new);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tormentum", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tormentum")) 
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> CHALK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CHALK.get());
            }).build());

    public Tormentum(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        
        ITEMS.register(modEventBus);
        
        CREATIVE_MODE_TABS.register(modEventBus);
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
