package com.boone.epicscorch.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Cancels reload completely when sprint or dodge starts.
 * Prevents resuming reload when sprint/dodge ends.
 */
@Mod.EventBusSubscriber(modid = "epicscorch", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ReloadMotionBlocker {

    private static boolean wasSprinting = false;
    private static boolean wasInaction = false;
    private static boolean isCurrentlyBlocked = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            wasSprinting = false;
            wasInaction = false;
            isCurrentlyBlocked = false;
            return;
        }

        // Check current state
        boolean isSprinting = player.isSprinting() || mc.options.keySprint.isDown();
        
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        boolean isInaction = false;
        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            EntityState state = playerPatch.getEntityState();
            isInaction = state.inaction();
        }

        // Detect transition TO sprint: cancel reload completely
        if (isSprinting && !wasSprinting) {
            cancelReloadCompletely(player, playerPatch);
            isCurrentlyBlocked = true;
        }

        // Detect transition TO action: cancel reload completely
        if (isInaction && !wasInaction) {
            cancelReloadCompletely(player, playerPatch);
            isCurrentlyBlocked = true;
        }

        // While sprint/dodge is active, continuously clean reload state
        if ((isSprinting || isInaction) && isCurrentlyBlocked) {
            keepReloadCleaned(player, playerPatch);
        }

        // Detect exit from sprint/dodge
        if (!isSprinting && !isInaction && isCurrentlyBlocked) {
            isCurrentlyBlocked = false;
        }

        wasSprinting = isSprinting;
        wasInaction = isInaction;
    }

    /**
     * Completely cancels reload - Scorched Guns action AND Epic Fight animation.
     * Ensures reload won't resume when sprint/dodge ends.
     */
    private static void cancelReloadCompletely(LocalPlayer player, LocalPlayerPatch playerPatch) {
        // Cancel Scorched Guns reload state
        ReloadHandler handler = ReloadHandler.get();
        if (handler != null) {
            handler.setReloading(false);
        }
        ModSyncedDataKeys.RELOADING.setValue(player, false);

        // Release item to ensure isUsingItem() becomes false
        player.releaseUsingItem();

        // Cancel Epic Fight RELOAD animation layer
        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            try {
                var animator = playerPatch.getClientAnimator();
                if (animator != null) {
                    Layer reloadLayer = animator.getCompositeLayer(Layer.Priority.MIDDLE);
                    if (reloadLayer != null && !reloadLayer.isOff()) {
                        reloadLayer.off(playerPatch);
                    }
                }
            } catch (Exception e) {
                // Ignore errors in animation cancellation
            }
        }

        // Clean NBT flags to prevent re-activation
        cleanReloadNBT(player);
    }

    /**
     * Keeps reload cleaned while sprint/dodge is active.
     * Runs every frame to fight against server re-synchronization.
     */
    private static void keepReloadCleaned(LocalPlayer player, LocalPlayerPatch playerPatch) {
        // Ensure ModSyncedDataKeys stays false
        ModSyncedDataKeys.RELOADING.setValue(player, false);

        // Release item continuously
        if (player.isUsingItem()) {
            player.releaseUsingItem();
        }

        // Clean NBT aggressively each frame
        cleanReloadNBT(player);
    }

    /**
     * Aggressively cleans all reload-related NBT tags from the item in hand.
     */
    private static void cleanReloadNBT(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean("scguns:IsReloading", false);
            tag.putBoolean("IsReloading", false);
            tag.putString("scguns:ReloadState", "");
            tag.putBoolean("scguns:IsPlayingReloadStop", false);
            tag.putBoolean("InCriticalReloadPhase", false);
            tag.putBoolean("scguns:PausedDuringReload", false);
            // Clear any reload-related tags that might persist
            tag.remove("scguns:ReloadTicks");
            tag.remove("scguns:ReloadProgress");
        }
    }
}
