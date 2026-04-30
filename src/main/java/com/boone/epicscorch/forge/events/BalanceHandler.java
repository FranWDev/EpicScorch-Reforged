package com.boone.epicscorch.forge.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageAim;

@Mod.EventBusSubscriber(modid = "epicscorch", value = Dist.CLIENT)
public class BalanceHandler {
    private static double lastPlayerY = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.side.isServer()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player != event.player) return;

        boolean isAiming = AimingHandler.get().isAiming();
        boolean isReloading = ReloadHandler.get().getReloadTimer() > 0;

        // Check if player is airborne (jumping/falling)
        boolean isAirborne = !player.onGround() && Math.abs(player.getDeltaMovement().y) > 0.01;
        
        // Cancel aiming if player jumps/becomes airborne
        if (isAiming && isAirborne) {
            // System.out.println("[EpicScorch-Debug] Cancelled AIM: Player airborne");
            cancelAiming(player);
        }

        if (isAiming || isReloading) {
            // Block sprinting if aiming or reloading
            if (player.isSprinting()) {
                // System.out.println("[EpicScorch-Debug] Forcing SPRINT OFF (Aiming/Reloading)");
                player.setSprinting(false);
                mc.options.keySprint.setDown(false);
            }

            // Cancel actions if the player actively tries to sprint
            if (mc.options.keySprint.isDown()) {
                // System.out.println("[EpicScorch-Debug] Intent to SPRINT detected, cancelling actions");
                cancelAimAndReload(player);
            }
        }

        lastPlayerY = player.getY();
    }

    private static void cancelAiming(LocalPlayer player) {
        if (AimingHandler.get().isAiming()) {
            AimingHandler.get().aiming = false;
            ModSyncedDataKeys.AIMING.setValue(player, false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));
        }
    }

    private static void cancelAimAndReload(LocalPlayer player) {
        if (ReloadHandler.get().getReloadTimer() > 0) {
            ReloadHandler.get().setReloading(false);
        }

        cancelAiming(player);
    }
}
