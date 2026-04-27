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
      this.registerSharedEventListeners();
      DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> com.boone.epicscorch.forge.client.ClientEventHandler::registerClient);
      DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> ServerEventHandler::registerServer);
      System.out.println("EpicScorch Mod Loaded: Bridging Epic Fight and Scorched Guns 2!");
   }

   private void onCommonSetup(FMLCommonSetupEvent event) {
      System.out.println("Common setup for EpicScorch.");
   }

   private void registerSharedEventListeners() {
      MinecraftForge.EVENT_BUS.register(PlayerHandler.class);
   }
}
