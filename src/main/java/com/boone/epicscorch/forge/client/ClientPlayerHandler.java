package com.boone.epicscorch.forge.client;

import com.boone.epicscorch.forge.PlayerHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent.BaseLayer;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent.CompositeLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;

@EventBusSubscriber(modid = "epicscorch", bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientPlayerHandler {
   @SubscribeEvent
   public static void onPlayerMotionBase(BaseLayer event) {
      AbstractClientPlayerPatch<?> playerPatch = event.getPlayerPatch();
      if (playerPatch.getOriginal() instanceof AbstractClientPlayer) {
         AbstractClientPlayer player = (AbstractClientPlayer)playerPatch.getOriginal();
         if (player.getMainHandItem().getItem() instanceof GunItem && Boolean.TRUE.equals(ModSyncedDataKeys.AIMING.getValue(player))) {
            PlayerHandler.getPreLivingMotions().put(player.getUUID(), playerPatch.currentLivingMotion);
            playerPatch.currentLivingMotion = LivingMotions.AIM;
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerMotionComposite(CompositeLayer event) {
      AbstractClientPlayerPatch<?> playerPatch = event.getPlayerPatch();
      if (playerPatch.getOriginal() instanceof AbstractClientPlayer) {
         AbstractClientPlayer player = (AbstractClientPlayer)playerPatch.getOriginal();
         LivingMotion preLivingMotion = PlayerHandler.getPreLivingMotions().get(player.getUUID());
         if (preLivingMotion != null) {
            playerPatch.currentLivingMotion = preLivingMotion;
            PlayerHandler.getPreLivingMotions().remove(player.getUUID());
         }

         if (player.getMainHandItem().getItem() instanceof GunItem) {
            if (Boolean.TRUE.equals(ModSyncedDataKeys.RELOADING.getValue(player))) {
               playerPatch.currentCompositeMotion = LivingMotions.RELOAD;
            } else if (Boolean.TRUE.equals(ModSyncedDataKeys.AIMING.getValue(player))) {
               playerPatch.currentCompositeMotion = LivingMotions.AIM;
            } else if (Boolean.TRUE.equals(ModSyncedDataKeys.SHOOTING.getValue(player))) {
               playerPatch.getClientAnimator().playShootingAnimation();
            } else {
               playerPatch.currentCompositeMotion = playerPatch.currentLivingMotion;
            }
         }
      }
   }
}
