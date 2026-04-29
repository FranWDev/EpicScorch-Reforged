package com.boone.epicscorch.forge;

import net.minecraftforge.common.MinecraftForge;

public class ServerEventHandler {
   public static void registerServer() {
      MinecraftForge.EVENT_BUS.register(ServerEventHandler.class);

   }
}
