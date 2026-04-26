package com.kollekki.tormentum;

import com.kollekki.tormentum.Render.CrackedSkullBlockEntityRender;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Tormentum.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Tormentum.CRACKED_SKULL_BLOCK_ENTITY.get(),
                CrackedSkullBlockEntityRender::new
        );
    }
}