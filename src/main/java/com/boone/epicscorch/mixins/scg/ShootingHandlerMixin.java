package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.client.handler.ShootingHandler;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Syncs gun firing with Epic Fight's combat state.
 * Can't fire while in combat actions, cooldowns, or airborne.
 */
@Mixin(value = ShootingHandler.class, remap = false)
public abstract class ShootingHandlerMixin {

    @Unique
    private static boolean epicscorch$wasInaction = false;

    /**
     * Prevent firing while: jumping, in combat action, or on cooldown.
     * Detects dodge state transitions to prevent firing during dodge initiation.
     */
    @Inject(method = "canFire", at = @At("HEAD"), cancellable = true)
    private void epicscorch$preventFireBasedOnState(Player player, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir) {

        boolean inAir = !player.onGround() && !player.isInWater() && Math.abs(player.getDeltaMovement().y) > 0.01;
        if (inAir && !player.getAbilities().flying) {
            cir.setReturnValue(false);
            return;
        }

        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            EntityState state = playerPatch.getEntityState();
            boolean isInaction = state.inaction();
            
            // Detect dodge initiation (transition from false to true)
            if (!epicscorch$wasInaction && isInaction) {
                epicscorch$wasInaction = isInaction;
                cir.setReturnValue(false);
                return;
            }
            
            if (isInaction) {
                cir.setReturnValue(false);
                epicscorch$wasInaction = isInaction;
                return;
            }
            
            if (!state.canUseSkill()) {
                cir.setReturnValue(false);
                epicscorch$wasInaction = isInaction;
                return;
            }
            
            epicscorch$wasInaction = isInaction;
        }
    }
}
