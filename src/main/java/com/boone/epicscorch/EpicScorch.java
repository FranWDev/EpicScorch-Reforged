package com.boone.epicscorch;

import com.boone.epicscorch.forge.PlayerHandler;
import com.boone.epicscorch.forge.ServerEventHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("epicscorch")
public class EpicScorch {
   public static final String MOD_ID = "epicscorch";

   public EpicScorch() {
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      modEventBus.addListener(this::onCommonSetup);
      
      // Epic Fight Presets need the MOD bus
      modEventBus.register(com.boone.epicscorch.forge.world.capabilities.items.GunCapabilityPresets.class);

      this.registerSharedEventListeners();
      
      DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> com.boone.epicscorch.forge.client.ClientEventHandler::registerClient);
      DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> com.boone.epicscorch.forge.ServerEventHandler::registerServer);
   }

   private void onCommonSetup(FMLCommonSetupEvent event) {
   }

   private void registerSharedEventListeners() {
      MinecraftForge.EVENT_BUS.register(PlayerHandler.class);
   }
}
