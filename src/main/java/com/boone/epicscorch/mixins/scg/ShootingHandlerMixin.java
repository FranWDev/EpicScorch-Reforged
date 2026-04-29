package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.client.handler.ShootingHandler;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = ShootingHandler.class, remap = false)
public abstract class ShootingHandlerMixin {

    @Inject(method = "canFire", at = @At("HEAD"), cancellable = true)
    private void epicscorch$preventFireBasedOnState(Player player, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir) {

        boolean inAir = !player.onGround() && Math.abs(player.getDeltaMovement().y) > 0.01;

        if (inAir && !player.getAbilities().flying) {
            // System.out.println("[EpicScorch-Debug] Fire BLOCKED: In Air");
            cir.setReturnValue(false);
            return;
        }

        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            EntityState state = playerPatch.getEntityState();
            if (state.inaction() || !state.canUseSkill()) {
                // System.out.println("[EpicScorch-Debug] Fire BLOCKED: EF Inaction State");
                cir.setReturnValue(false);
            }
        }
    }
}
