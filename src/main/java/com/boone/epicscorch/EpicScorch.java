package com.boone.epicscorch;

import com.boone.epicscorch.config.EpicScorchConfig;
import com.boone.epicscorch.forge.PlayerHandler;
import com.boone.epicscorch.forge.ServerEventHandler;
import com.boone.epicscorch.forge.client.ClientEventHandler;
import com.boone.epicscorch.forge.world.capabilities.items.GunCapabilityPresets;
import com.boone.epicscorch.forge.world.capabilities.items.GunCapabilityProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod("epicscorch")
public class EpicScorch {
   public static final String MOD_ID = "epicscorch";

   public EpicScorch(IEventBus modEventBus, ModContainer modContainer) {
      modContainer.registerConfig(ModConfig.Type.COMMON, EpicScorchConfig.SPEC);
      
      modEventBus.addListener(this::onCommonSetup);
      modEventBus.addListener(GunCapabilityProvider::registerCapabilities);
      
      this.registerSharedEventListeners();
      GunCapabilityPresets.register();
      
      if (FMLEnvironment.dist.isClient()) {
         ClientEventHandler.registerClient();
      } else {
         ServerEventHandler.registerServer();
      }
   }

   private void onCommonSetup(FMLCommonSetupEvent event) {
   }

   private void registerSharedEventListeners() {
      NeoForge.EVENT_BUS.register(PlayerHandler.class);
   }
}
