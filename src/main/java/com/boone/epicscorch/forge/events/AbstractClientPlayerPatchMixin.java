package com.boone.epicscorch.forge.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.entity.ModifyPlayerLivingMotionEvent;

@OnlyIn(Dist.CLIENT)
public class AbstractClientPlayerPatchMixin {

    public static void register() {
        EpicFightClientEventHooks.Entity.MODIFY_PLAYER_LIVING_MOTION_COMPOSITE.registerEvent(
            AbstractClientPlayerPatchMixin::onCompositeMotion,
            "epicscorch_motion_composite",
            -1000
        );
    }

    public static void onCompositeMotion(ModifyPlayerLivingMotionEvent.CompositeLayer event) {
        if (!(event.getPlayerPatch().getOriginal() instanceof AbstractClientPlayer))
            return;
        AbstractClientPlayer player = (AbstractClientPlayer) event.getPlayerPatch().getOriginal();
        if (!event.getPlayerPatch().isEpicFightMode())
            return;

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof GunItem)) {
            return;
        }

        boolean aiming = isActuallyAiming(player);
        boolean restricted = player.isLocalPlayer() && BalanceHandler.shouldBeRestricted((LocalPlayer) player);
        boolean inAction = event.getPlayerPatch().getEntityState().inaction();
        boolean reloading = isActuallyReloading(player);
        boolean stoppingReload = isStoppingReload(stack);

        if (aiming && !restricted && !inAction && !isDrawingWeapon(player)) {
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

    private static void handleReloadLooping(AbstractClientPlayer player, ModifyPlayerLivingMotionEvent.CompositeLayer event) {
        if (!event.getPlayerPatch().isLogicalClient())
            return;

        ClientAnimator animator = (ClientAnimator) event.getPlayerPatch().getAnimator();
        AssetAccessor<? extends StaticAnimation> reloadAnimAsset = animator
                .getCompositeLivingMotion(LivingMotions.RELOAD);

        if (reloadAnimAsset != null) {
            Layer reloadLayer = animator.getCompositeLayer(reloadAnimAsset.get().getPriority());

            if (reloadLayer != null && reloadLayer.animationPlayer.isEnd()) {
                ItemStack stack = player.getMainHandItem();
                CompoundTag tag = BalanceHandler.getStackTag(stack);
                String reloadState = tag != null ? tag.getString("scguns:ReloadState") : "";

                if (reloadState.equals("RELOAD") || reloadState.equals("LOADING") || 
                    reloadState.equals("RELOAD_LOOP") || reloadState.equals("START") || 
                    reloadState.equals("STARTING")) {
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

        CompoundTag tag = BalanceHandler.getStackTag(stack);
        if (isStoppingReload(stack))
            return false;

        if (player.isLocalPlayer() && BalanceHandler.shouldBeRestricted((LocalPlayer) player))
            return false;

        if (ModSyncedDataKeys.RELOADING.getValue(player))
            return true;

        if (tag == null)
            return false;

        String reloadState = tag.getString("scguns:ReloadState");

        return (reloadState.equals("RELOAD") || reloadState.equals("START") ||
                reloadState.equals("STARTING") || reloadState.equals("LOADING"));
    }

    private static boolean isStoppingReload(ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem))
            return false;

        CompoundTag tag = BalanceHandler.getStackTag(stack);
        if (tag == null)
            return false;

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

        CompoundTag tag = BalanceHandler.getStackTag(stack);
        if (tag == null)
            return false;

        return tag.getBoolean("IsDrawing") && tag.getInt("DrawnTick") < 15;
    }
}
