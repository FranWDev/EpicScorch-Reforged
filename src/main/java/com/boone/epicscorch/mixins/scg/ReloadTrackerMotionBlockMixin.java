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
        boolean shouldBlock = player.isSprinting();

        if (!shouldBlock) {
            try {
                PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
                if (patch != null && patch.isEpicFightMode()) {
                    EntityState state = patch.getEntityState();
                    if (state.inaction()) shouldBlock = true;
                }
            } catch (Exception e) {}
        }

        ItemStack heldItem = player.getMainHandItem();
        boolean hasReloadTag = false;
        if (heldItem.getItem() instanceof GunItem) {
            CompoundTag tag = heldItem.getOrCreateTag();
            hasReloadTag = tag.getBoolean("IsReloading") || tag.getBoolean("scguns:IsReloading");
        }

        if (shouldBlock && (ModSyncedDataKeys.RELOADING.getValue(player) || hasReloadTag)) {
            ModSyncedDataKeys.RELOADING.setValue(player, false);
            
            if (heldItem.getItem() instanceof GunItem gunItem) {
                CompoundTag tag = heldItem.getOrCreateTag();
                // Atomic cleanup of reload NBT state to ensure immediate transition to idle/aim
                tag.remove("IsReloading");
                tag.remove("scguns:IsReloading");
                tag.remove("InReloadLoop");
                tag.remove("InCriticalReloadPhase");
                tag.remove("IsManualReload");
                tag.remove("scguns:ReloadComplete");
                tag.remove("scguns:ReloadState");
                tag.remove("scguns:IsPlayingReloadStop");
            }
            
            ci.cancel();
        }
    }
}
