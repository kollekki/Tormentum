package com.kollekki.tormentum;

import com.kollekki.tormentum.Particles.WaterPuddleParticle;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Tormentum.MODID, dist = Dist.CLIENT)
public class TormentumClient {

    public TormentumClient(IEventBus modEventBus, ModContainer container) {

        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                ConfigurationScreen::new
        );

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerParticles);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
    }

    private void registerParticles(RegisterParticleProvidersEvent event) {

        event.registerSpriteSet(
                Tormentum.WATER_PUDDLE_PARTICLE.get(),
                WaterPuddleParticle.Provider::new
        );
    }
}