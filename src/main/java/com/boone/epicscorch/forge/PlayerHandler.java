package com.boone.epicscorch.forge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import yesman.epicfight.api.animation.LivingMotion;

@EventBusSubscriber(modid = "epicscorch", bus = Bus.FORGE)
public class PlayerHandler {
   private static final Map<UUID, LivingMotion> preLivingMotions = new HashMap<>();

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      preLivingMotions.remove(event.getEntity().getUUID());
   }

   public static Map<UUID, LivingMotion> getPreLivingMotions() {
      return preLivingMotions;
   }
}
