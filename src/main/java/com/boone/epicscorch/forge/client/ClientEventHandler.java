package com.boone.epicscorch.forge.client;

import com.boone.epicscorch.compat.shouldersurfing.ShoulderSurfingAimRotationHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {
   public static void registerClient() {
      MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == TickEvent.Phase.START) {
         ShoulderSurfingAimRotationHandler.onClientTick();
      }
   }

   @SubscribeEvent
   public static void onRenderTick(TickEvent.RenderTickEvent event) {
      if (event.phase == TickEvent.Phase.START) {
         ShoulderSurfingAimRotationHandler.onRenderTick();
      }
   }
}
