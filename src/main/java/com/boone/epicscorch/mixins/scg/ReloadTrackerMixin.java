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
 * Prevents ammo increment when player is in dodge/roll on server side.
 * Blocks increaseAmmo() from executing during combat actions.
 */
@Mixin(ReloadTracker.class)
public class ReloadTrackerMixin {

    @Inject(method = "increaseAmmo", at = @At("HEAD"), cancellable = true)
    private void blockAmmoIncreaseDuringDodge(Player player, CallbackInfo ci) {
        if (player == null) return;
        if (player.level().isClientSide) return;

        // Block if sprinting
        if (player.isSprinting()) {
            ci.cancel();
            return;
        }

        // Block if in combat action (dodge/roll)
        PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (patch != null && patch.isEpicFightMode()) {
            EntityState state = patch.getEntityState();
            if (state.inaction()) {
                ci.cancel();
            }
        }
    }
}
