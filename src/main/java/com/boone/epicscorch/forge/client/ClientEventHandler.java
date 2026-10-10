package com.boone.epicscorch.forge.client;

import com.boone.epicscorch.compat.shouldersurfing.ShoulderSurfingAimRotationHandler;
import com.boone.epicscorch.forge.events.AbstractClientPlayerPatchMixin;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ClientEventHandler {
   public static void registerClient() {
      AbstractClientPlayerPatchMixin.register();
      NeoForge.EVENT_BUS.register(ClientEventHandler.class);
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent.Pre event) {
      ShoulderSurfingAimRotationHandler.onClientTick();
   }

   @SubscribeEvent
   public static void onRenderFrame(RenderFrameEvent.Pre event) {
      ShoulderSurfingAimRotationHandler.onRenderTick();
   }
}

