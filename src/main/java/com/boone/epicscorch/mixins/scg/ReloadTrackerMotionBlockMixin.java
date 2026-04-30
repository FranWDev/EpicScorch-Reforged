package com.boone.epicscorch.mixins.scg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraftforge.event.TickEvent;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.common.ReloadTracker;
import top.ribs.scguns.init.ModSyncedDataKeys;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/**
 * Blocks reload on server while player is sprinting or in Epic Fight combat action.
 * Forces RELOADING to false every tick to prevent reload during combat/sprint.
 */
@Mixin(ReloadTracker.class)
public class ReloadTrackerMotionBlockMixin {

    @Inject(
        method = "onPlayerTick",
        at = @At("HEAD"),
        remap = false
    )
    private static void blockReloadDuringSprint(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!(event.player instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.player;

        // Block reload if sprinting
        if (player.isSprinting()) {
            ModSyncedDataKeys.RELOADING.setValue(player, false);
            return;
        }

        // Block reload if in Epic Fight combat action
        try {
            PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
            if (patch != null && patch.isEpicFightMode()) {
                EntityState state = patch.getEntityState();
                if (state.inaction()) {
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    return;
                }
            }
        } catch (Exception e) {
            // Ignore Epic Fight errors
        }
    }
}
