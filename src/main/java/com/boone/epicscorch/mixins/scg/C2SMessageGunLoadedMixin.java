package com.boone.epicscorch.mixins.scg;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.network.message.C2SMessageGunLoaded;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.api.animation.types.EntityState;

import top.ribs.scguns.init.ModSyncedDataKeys;

import java.util.function.Supplier;

@Mixin(C2SMessageGunLoaded.class)
public class C2SMessageGunLoadedMixin {

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void preventLoadedDuringAction(C2SMessageGunLoaded message, MessageContext context, CallbackInfo ci) {
        ServerPlayer player = context.getPlayer();
        if (player == null) return;

        // If the player is airborne, skip the RELOADING check entirely.
        // During a jump the client-side RELOADING SyncedDataKey can be transiently desynced
        // (the jump exemption path in BalanceHandler does not write it, but edge-case tick
        // ordering may leave it stale on the server). The sprint and action guards below still
        // apply, so there is no exploit surface from bypassing only this check mid-air.
        if (!player.onGround() && !player.isPassenger()) return;

        // Must be in RELOADING state (server authority) — guards against infinite reload loops
        // and magazine weapons receiving extra ammo outside a valid reload cycle.
        if (!ModSyncedDataKeys.RELOADING.getValue(player)) {
            ci.cancel();
            return;
        }

        // Cannot be sprinting.
        if (player.isSprinting()) {
            ci.cancel();
            return;
        }

        // Third: not in combat action (dodge/roll)
        PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (patch != null && patch.isEpicFightMode()) {
            EntityState state = patch.getEntityState();
            if (state.inaction()) {
                ci.cancel();
            }
        }
    }
}
