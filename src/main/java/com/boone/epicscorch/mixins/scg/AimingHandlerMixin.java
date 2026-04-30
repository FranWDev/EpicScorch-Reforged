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
import top.ribs.scguns.client.handler.AimingHandler;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Prevents aiming while sprinting. Intercepts onClientTick after shouldBeAiming is calculated.
 */
@Mixin(value = AimingHandler.class, remap = false)
public abstract class AimingHandlerMixin {

    @Shadow
    private boolean aiming;

    @Unique
    private static boolean epicscorch$wasInaction = false;

    /**
     * Early check: prevent aiming if in dodge/roll at method start.
     */
    @Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true)
    private void epicscorch$cancelAimingOnDodgeStart(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null)
            return;
        
        // Cancel if sprinting
        if (player.isSprinting() || mc.options.keySprint.isDown()) {
            this.aiming = false;
            return;
        }
        
        // Cancel if dodging/rolling
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            EntityState state = playerPatch.getEntityState();
            
            if (state.inaction()) {
                this.aiming = false;
                epicscorch$wasInaction = true;
            }
        }
    }

    /**
     * Block aiming if player is sprinting, rolling, or holding sprint key.
     */
    @Inject(
        method = "onClientTick",
        at = @At(value = "FIELD", target = "Ltop/ribs/scguns/client/handler/AimingHandler;aiming:Z", ordinal = 2, shift = At.Shift.AFTER),
        cancellable = false
    )
    private void epicscorch$preventAimingDuringSprint(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            // Cancel aiming during sprint
            if (player.isSprinting() || mc.options.keySprint.isDown()) {
                this.aiming = false;
                return;
            }
            
            // Cancel aiming during dodge/roll
            LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
            if (playerPatch != null && playerPatch.isEpicFightMode()) {
                EntityState state = playerPatch.getEntityState();
                boolean isInaction = state.inaction();
                
                // Cancel aiming if dodge just started or currently dodging
                if (isInaction) {
                    this.aiming = false;
                }
                
                epicscorch$wasInaction = isInaction;
            }
        }
    }
}
