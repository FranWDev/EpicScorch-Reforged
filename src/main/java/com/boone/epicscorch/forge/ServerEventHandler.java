package com.boone.epicscorch.forge;

import net.neoforged.neoforge.common.NeoForge;

public class ServerEventHandler {
   public static void registerServer() {
      // No server-only event handlers currently registered.
      // Avoid calling NeoForge.EVENT_BUS.register() without @SubscribeEvent methods to prevent IllegalArgumentException.
   }
}

