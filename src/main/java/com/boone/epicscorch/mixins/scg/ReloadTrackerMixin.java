package com.boone.epicscorch.mixins.scg;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.common.ReloadTracker;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/**
 * Prevents ammo from being added while the player is performing combat actions or sprinting.
 * Jumping does not block ammo increment.
 */
@Mixin(ReloadTracker.class)
public class ReloadTrackerMixin {

    @Inject(method = "increaseAmmo", at = @At("HEAD"), cancellable = true)
    private void blockAmmoIncreaseDuringCombatAction(Player player, CallbackInfo ci) {
        if (player == null || player.level().isClientSide) return;

        if (player.isSprinting()) {
            ci.cancel();
            return;
        }

        // Allow ammo increase while airborne (jumping)
        if (!player.onGround() && !player.isPassenger()) {
            return;
        }

        PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (patch != null && patch.isEpicFightMode() && patch.getEntityState().inaction()) {
            ci.cancel();
        }
    }
}
