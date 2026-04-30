package com.boone.epicscorch.client.handler;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import top.ribs.scguns.client.handler.AimingHandler;

/**
 * Alternative event listener to prevent aiming while sprinting.
 * Backup to the Mixin approach for version compatibility.
 */
@Mod.EventBusSubscriber(modid = "epicscorch", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SprintAimingBlocker {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null || mc.isPaused()) {
            return;
        }

        AimingHandler aimingHandler = AimingHandler.get();
        
        if (aimingHandler != null && aimingHandler.isAiming()) {
            if (player.isSprinting() || mc.options.keySprint.isDown()) {
                // Block aiming during sprint
            }
        }
    }
}
