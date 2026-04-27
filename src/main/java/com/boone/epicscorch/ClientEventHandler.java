package com.boone.epicscorch;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "epicscorch", bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {
   public static void register() {
   }
}
