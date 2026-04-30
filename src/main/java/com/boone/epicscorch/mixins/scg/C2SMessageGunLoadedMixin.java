package com.boone.epicscorch.mixins.scg;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
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
    private static void preventLoadedDuringAction(Supplier<NetworkEvent.Context> supplier, CallbackInfo ci) {
        NetworkEvent.Context ctx = supplier.get();
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        // First: must be in RELOADING state (server authority)
        if (!ModSyncedDataKeys.RELOADING.getValue(player)) {
            ci.cancel();
            return;
        }

        // Second: cannot be sprinting
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
