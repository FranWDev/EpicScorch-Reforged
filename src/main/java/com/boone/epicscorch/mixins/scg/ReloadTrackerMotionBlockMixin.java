package com.boone.epicscorch.mixins.scg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraftforge.event.TickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.ribs.scguns.common.ReloadTracker;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageStopReload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import java.util.Map;
import org.spongepowered.asm.mixin.Shadow;
import com.boone.epicscorch.config.EpicScorchConfig;

@Mixin(ReloadTracker.class)
public abstract class ReloadTrackerMotionBlockMixin {

    @Shadow
    private static Map<Player, ReloadTracker> RELOAD_TRACKER_MAP;

    @Inject(
        method = "onPlayerTick",
        at = @At("HEAD"),
        remap = false,
        cancellable = true
    )
    private static void blockReloadDuringSprint(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player instanceof ServerPlayer)) return;

        ServerPlayer player = (ServerPlayer) event.player;
        boolean shouldBlock = false;

        if (EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get()) {
            boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);
            // Block reload progression instead of disabling sprint to maintain movement fluidness
            
            shouldBlock = player.isSprinting();
            
            // Do not block reload if jumping/airborne (fulfills user request)
            if (!player.onGround()) {
                shouldBlock = false;
            }
        }

        ItemStack heldItem = player.getMainHandItem();
        boolean hasReloadTag = false;
        if (heldItem.getItem() instanceof GunItem) {
            CompoundTag tag = heldItem.getOrCreateTag();
            hasReloadTag = tag.getBoolean("IsReloading") || tag.getBoolean("scguns:IsReloading");
        }

        if (shouldBlock && (ModSyncedDataKeys.RELOADING.getValue(player) || hasReloadTag)) {
            ModSyncedDataKeys.RELOADING.setValue(player, false);
            ModSyncedDataKeys.AIMING.setValue(player, false);
            
            if (heldItem.getItem() instanceof GunItem gunItem) {
                CompoundTag tag = heldItem.getOrCreateTag();
                
                if (gunItem instanceof AnimatedGunItem animated) {
                    animated.cleanupReloadState(tag);
                }
                
                tag.remove("ReloadComplete");
                tag.remove("scguns:ReloadComplete");
                // Use the flag that ReloadTracker.onPlayerTick() checks on the server
                // (line 375 of ReloadTracker.java) to break the reload loop cleanly.
                // Previously we only set ReloadState="IDLE" which the server ignores.
                tag.putBoolean("scguns:PausedDuringReload", true);
                tag.putString("scguns:ReloadState", "IDLE");
                tag.remove("scguns:IsPlayingReloadStop");
                tag.remove("Reloading");
                tag.remove("scguns:Reloading");
                tag.remove("scguns:ShouldStopAfterLoop"); // The special loop tag
                
                // Notify client to stop reload visually and logically
                PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageStopReload());
            }
            
            if (RELOAD_TRACKER_MAP != null) {
                RELOAD_TRACKER_MAP.remove(player);
            }
            
            ci.cancel();
        }
    }
}
