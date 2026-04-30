package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Syncs reload state with Epic Fight's combat state.
 * Can't reload while in combat actions or cooldowns.
 */
@Mixin(value = ReloadHandler.class, remap = false)
public abstract class ReloadHandlerMixin {

    @Shadow
    private int reloadTimer;

    @Shadow
    public abstract void setReloading(boolean reloading);

    @Unique
    private static boolean epicscorch$wasInaction = false;

    /**
     * Pre-emptive check: block reload request before it happens.
     * Uses ReloadHandler's native methods to properly clean state.
     */
    @Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true)
    private void epicscorch$blockReloadDuringRestrictions(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        if (event.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        // If we're not allowed to reload, immediately cancel and exit
        if (!epicscorch$canReloadNow(player)) {
            // Use ReloadHandler's native methods to properly clean state
            ReloadHandler handler = ReloadHandler.get();
            if (handler != null) {
                // Force stop via setReloading - this triggers proper cleanup on server too
                handler.setReloading(false);
            }
            
            // Sync to false - server has authority and will confirm
            ModSyncedDataKeys.RELOADING.setValue(player, false);
            
            // Block method execution completely to prevent timer increment
            ci.cancel();
        }
    }

    /**
     * Centralized check: can reload happen right now?
     */
    @Unique
    private static boolean epicscorch$canReloadNow(LocalPlayer player) {
        // Check sprint
        Minecraft mc = Minecraft.getInstance();
        if (player.isSprinting() || mc.options.keySprint.isDown())
            return false;

        // Check Epic Fight state
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            EntityState state = playerPatch.getEntityState();
            
            // Block during dodge/action
            if (state.inaction())
                return false;
            
            // Block if on cooldown
            if (!state.canUseSkill())
                return false;
        }

        // Check jumping
        if (!player.onGround() && player.getDeltaMovement().y > 0.01)
            return false;

        return true;
    }
}
