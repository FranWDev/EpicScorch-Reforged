package com.boone.epicscorch.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.client.network.ClientPlayHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import com.boone.epicscorch.config.EpicScorchConfig;

@Mod.EventBusSubscriber(modid = "epicscorch", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ReloadMotionBlocker {

    private static boolean wasSprinting = false;
    private static boolean wasInaction = false;
    private static boolean isCurrentlyBlocked = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            wasSprinting = wasInaction = isCurrentlyBlocked = false;
            return;
        }

        boolean isSprinting = false;
        boolean isInaction = false;
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();

        if (EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get()) {
            boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);
            if (isReloading && player.isSprinting()) {
                player.setSprinting(false);
            }

            isSprinting = player.isSprinting() || mc.options.keySprint.isDown();
            if (playerPatch != null && playerPatch.isEpicFightMode()) {
                EntityState state = playerPatch.getEntityState();
                isInaction = state.inaction();
            }
        }

        if ((isSprinting && !wasSprinting) || (isInaction && !wasInaction)) {
            cancelReloadCompletely(player, playerPatch);
            isCurrentlyBlocked = true;
        }

        if ((isSprinting || isInaction) && isCurrentlyBlocked) {
            keepReloadCleaned(player, playerPatch);
        }

        if (!isSprinting && !isInaction && isCurrentlyBlocked) {
            isCurrentlyBlocked = false;
        }

        wasSprinting = isSprinting;
        wasInaction = isInaction;
    }

    private static void cancelReloadCompletely(LocalPlayer player, LocalPlayerPatch playerPatch) {
        ReloadHandler handler = ReloadHandler.get();
        if (handler != null) {
            handler.setReloading(false);
        }
        ModSyncedDataKeys.RELOADING.setValue(player, false);
        player.releaseUsingItem();
        
        // Stop GeckoLib animations to prevent state blocking or visual artifacts
        try {
            ClientPlayHandler.handleStopReload(null);
        } catch (Exception e) {}

        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            try {
                var animator = playerPatch.getClientAnimator();
                if (animator != null) {
                    Layer reloadLayer = animator.getCompositeLayer(Layer.Priority.MIDDLE);
                    if (reloadLayer != null && !reloadLayer.isOff()) reloadLayer.off(playerPatch);
                }
            } catch (Exception e) {}
        }
    }

    private static void keepReloadCleaned(LocalPlayer player, LocalPlayerPatch playerPatch) {
        ModSyncedDataKeys.RELOADING.setValue(player, false);
        if (player.isUsingItem()) player.releaseUsingItem();
    }
}
