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
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;
import com.boone.epicscorch.config.EpicScorchConfig;
import com.boone.epicscorch.forge.client.ClientEventHandler;
import com.boone.epicscorch.forge.world.capabilities.items.GunCapabilityPresets;

@Mod("epicscorch")
public class EpicScorch {
   public static final String MOD_ID = "epicscorch";

   public EpicScorch() {
      ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EpicScorchConfig.SPEC);
      
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      modEventBus.addListener(this::onCommonSetup);
      
      // Epic Fight Presets need the MOD bus
      modEventBus.register(GunCapabilityPresets.class);

      this.registerSharedEventListeners();
      
      DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEventHandler::registerClient);
      DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> ServerEventHandler::registerServer);
   }

   private void onCommonSetup(FMLCommonSetupEvent event) {
   }

   private void registerSharedEventListeners() {
      MinecraftForge.EVENT_BUS.register(PlayerHandler.class);
   }
}
