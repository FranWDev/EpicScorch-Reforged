package com.boone.epicscorch.forge.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent;

@EventBusSubscriber(modid = "epicscorch", bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AbstractClientPlayerPatchMixin {

    private static final int AIM_LEAVE_HYSTERESIS = 10; // Increased to bridge roll gaps
    private static final Map<UUID, Integer> aimHoldCounters = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCompositeMotion(UpdatePlayerMotionEvent.CompositeLayer event) {
        if (!(event.getPlayerPatch().getOriginal() instanceof AbstractClientPlayer))
            return;
        AbstractClientPlayer player = (AbstractClientPlayer) event.getPlayerPatch().getOriginal();
        if (!event.getPlayerPatch().isEpicFightMode())
            return;

        ItemStack stack = player.getMainHandItem();
        UUID id = player.getUUID();

        if (!(stack.getItem() instanceof GunItem)) {
            aimHoldCounters.remove(id);
            return;
        }

        boolean aiming = isActuallyAiming(player);
        boolean restricted = player.isLocalPlayer() && BalanceHandler.shouldBeRestricted((LocalPlayer) player);
        boolean inAction = event.getPlayerPatch().getEntityState().inaction();
        boolean reloading = isActuallyReloading(player);
        boolean stoppingReload = isStoppingReload(stack);

        // Fix Bug 1: When restricted (sprinting, dodging, or in cancel-cooldown), zero
        // the hysteresis counter immediately so it doesn't keep LivingMotions.AIM active
        // for 10 extra ticks, which was creating "animation fighting" post-cancellation.
        if (restricted) {
            aimHoldCounters.put(id, 0);
        }

        if (aiming && !restricted && !inAction && !isDrawingWeapon(player)) {
            aimHoldCounters.put(id, AIM_LEAVE_HYSTERESIS);
            event.setMotion(LivingMotions.AIM);
            return;
        }

        if (inAction) {
            aimHoldCounters.put(id, 0);
            return;
        }

        int aimHold = aimHoldCounters.getOrDefault(id, 0);
        if (aimHold > 0 && !restricted) {
            aimHoldCounters.put(id, aimHold - 1);
            event.setMotion(LivingMotions.AIM);
            return;
        }

        if (stoppingReload) {
            return;
        }

        if (reloading && !restricted) {
            event.setMotion(LivingMotions.RELOAD);
            handleReloadLooping(player, event);
            return;
        }
    }

    private static void handleReloadLooping(AbstractClientPlayer player, UpdatePlayerMotionEvent.CompositeLayer event) {
        if (!event.getPlayerPatch().isLogicalClient())
            return;

        ClientAnimator animator = (ClientAnimator) event.getPlayerPatch().getAnimator();
        AssetAccessor<? extends StaticAnimation> reloadAnimAsset = animator
                .getCompositeLivingMotion(LivingMotions.RELOAD);

        if (reloadAnimAsset != null) {
            Layer reloadLayer = animator.getCompositeLayer(reloadAnimAsset.get().getPriority());

            if (reloadLayer != null && reloadLayer.animationPlayer.isEnd()) {
                ItemStack stack = player.getMainHandItem();
                CompoundTag tag = stack.getOrCreateTag();
                String reloadState = tag.getString("scguns:ReloadState");

                if (reloadState.equals("RELOAD") || reloadState.equals("LOADING")) {
                    animator.playAnimation(reloadAnimAsset, 0.0F);
                } else if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                    ReloadHandler.get().setReloading(false);
                }
            }
        }
    }

    private static boolean isActuallyAiming(AbstractClientPlayer player) {
        if (player.isLocalPlayer()) {
            // Use the local AimingHandler state, which correctly respects blocks during
            // actions/inaction
            return AimingHandler.get().isAiming();
        }

        // For remote players, use the synced data key to avoid mirroring the local
        // player's state
        return ModSyncedDataKeys.AIMING.getValue(player);
    }

    private static boolean isActuallyReloading(AbstractClientPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem))
            return false;

        CompoundTag tag = stack.getOrCreateTag();
        if (isStoppingReload(stack))
            return false;

        if (player.isLocalPlayer() && BalanceHandler.shouldBeRestricted((LocalPlayer) player))
            return false;

        if (ModSyncedDataKeys.RELOADING.getValue(player))
            return true;

        String reloadState = tag.getString("scguns:ReloadState");

        return (reloadState.equals("RELOAD") || reloadState.equals("START") ||
                reloadState.equals("STARTING") || reloadState.equals("LOADING"));
    }

    private static boolean isStoppingReload(ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem))
            return false;

        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean("scguns:IsPlayingReloadStop")
                || "STOP".equals(tag.getString("scguns:ReloadState"))
                || "STOPPING".equals(tag.getString("scguns:ReloadState"))
                || "STOP".equals(tag.getString("scguns:AnimationReloadState"))
                || "STOPPING".equals(tag.getString("scguns:AnimationReloadState"));
    }

    private static boolean isDrawingWeapon(AbstractClientPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem))
            return false;

        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean("IsDrawing") && tag.getInt("DrawnTick") < 15;
    }
}
