package com.boone.epicscorch.forge.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

    private static final int AIM_LEAVE_HYSTERESIS = 4;
    private static final Map<UUID, Integer> aimHoldCounters = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCompositeMotion(UpdatePlayerMotionEvent.CompositeLayer event) {
        if (!(event.getPlayerPatch().getOriginal() instanceof AbstractClientPlayer)) return;
        AbstractClientPlayer player = (AbstractClientPlayer) event.getPlayerPatch().getOriginal();
        if (!event.getPlayerPatch().isEpicFightMode()) return;

        ItemStack stack = player.getMainHandItem();
        UUID id = player.getUUID();

        if (!(stack.getItem() instanceof GunItem)) {
            aimHoldCounters.remove(id);
            return;
        }
        
        boolean aiming = isActuallyAiming(player);
        boolean reloading = isActuallyReloading(player);
        boolean stoppingReload = isStoppingReload(stack);

        if (stoppingReload) {
            event.setMotion(LivingMotions.IDLE);
            return;
        }

        if (aiming && !isDrawingWeapon(player)) {
            aimHoldCounters.put(id, AIM_LEAVE_HYSTERESIS);
            event.setMotion(LivingMotions.AIM);
            return;
        }

        int aimHold = aimHoldCounters.getOrDefault(id, 0);
        if (aimHold > 0) {
            aimHoldCounters.put(id, aimHold - 1);
            event.setMotion(LivingMotions.AIM);
            return;
        }

        if (reloading) {
            event.setMotion(LivingMotions.RELOAD);
            handleReloadLooping(player, event);
            return;
        }
    }

    private static void handleReloadLooping(AbstractClientPlayer player, UpdatePlayerMotionEvent.CompositeLayer event) {
        if (!event.getPlayerPatch().isLogicalClient()) return;
        
        ClientAnimator animator = (ClientAnimator) event.getPlayerPatch().getAnimator();
        AssetAccessor<? extends StaticAnimation> reloadAnimAsset = animator.getCompositeLivingMotion(LivingMotions.RELOAD);
        
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
            if (KeyBinds.getAimMapping().isDown()) return true;
        }

        if (AimingHandler.get().isAiming()) return true;
        
        return false;
    }

    private static boolean isActuallyReloading(AbstractClientPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem)) return false;

        CompoundTag tag = stack.getOrCreateTag();
        if (isStoppingReload(stack)) return false;

        if (ModSyncedDataKeys.RELOADING.getValue(player)) return true;

        String reloadState = tag.getString("scguns:ReloadState");
        
        return (reloadState.equals("RELOAD") || reloadState.equals("START") || 
                reloadState.equals("STARTING") || reloadState.equals("LOADING"));
    }

    private static boolean isStoppingReload(ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem)) return false;

        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean("scguns:IsPlayingReloadStop")
                || "STOP".equals(tag.getString("scguns:ReloadState"))
                || "STOPPING".equals(tag.getString("scguns:ReloadState"))
                || "STOP".equals(tag.getString("scguns:AnimationReloadState"))
                || "STOPPING".equals(tag.getString("scguns:AnimationReloadState"));
    }

    private static boolean isDrawingWeapon(AbstractClientPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem)) return false;
        
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean("IsDrawing") && tag.getInt("DrawnTick") < 15;
    }
}
