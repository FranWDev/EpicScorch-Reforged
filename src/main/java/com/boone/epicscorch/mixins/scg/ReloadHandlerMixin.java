package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = ReloadHandler.class, remap = false)
public abstract class ReloadHandlerMixin {

    @Shadow
    private int reloadTimer;

    @Shadow
    public abstract void setReloading(boolean reloading);

    @Inject(method = "onClientTick", at = @At("HEAD"))
    private void epicscorch$cancelReloadOnMovement(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        if (event.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        boolean isSprinting = player.isSprinting() || mc.options.keySprint.isDown();

        boolean isDodging = false;
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            EntityState state = playerPatch.getEntityState();
            isDodging = state.inaction();
        }

        if (isSprinting || isDodging) {
            // If the timer is active, force cancel everything
            if (this.reloadTimer > 0) {
                // System.out.println("[EpicScorch-Debug] Forcing Reload Cancel: Sprinting=" +
                // isSprinting + " Dodging=" + isDodging);
                this.reloadTimer = 0;
                this.setReloading(false);
                ModSyncedDataKeys.RELOADING.setValue(player, false);
            }
        }
    }
}
