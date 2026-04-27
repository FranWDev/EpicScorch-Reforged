package com.boone.epicscorch.forge.client;

import net.minecraftforge.common.MinecraftForge;

public class ClientEventHandler {
   public static void registerClient() {
      MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
      System.out.println("Client-specific logic registered for EpicScorch.");
   }
}
