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

        // Safety check: Only apply these overrides if holding a Scorched Gun
        if (!(stack.getItem() instanceof GunItem)) {
            aimHoldCounters.remove(id);
            return;
        }
        
        boolean aiming = isActuallyAiming(player);
        boolean reloading = isActuallyReloading(player);

        // Priority 1: AIM
        // We now check if the gun is currently being "drawn" (equipped). 
        // SCGuns prevents aiming for the first 15 ticks after switching to a gun.
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

        // Priority 2: RELOAD
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
                
                // Only loop if it's a multi-bullet manual reload (Manual guns use LOADING state for the loop)
                if (reloadState.equals("RELOAD") || reloadState.equals("LOADING")) {
                    animator.playAnimation(reloadAnimAsset, 0.0F);
                } else if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                    ReloadHandler.get().setReloading(false);
                }
            }
        }
    }

    private static boolean isActuallyAiming(AbstractClientPlayer player) {
        // Direct key check for local player to ensure we catch the intent
        if (player.isLocalPlayer()) {
            if (KeyBinds.getAimMapping().isDown()) return true;
        }

        if (AimingHandler.get().isAiming()) return true;
        
        return false;
    }

    private static boolean isActuallyReloading(AbstractClientPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem)) return false;

        if (ModSyncedDataKeys.RELOADING.getValue(player)) return true;

        CompoundTag tag = stack.getOrCreateTag();
        String reloadState = tag.getString("scguns:ReloadState");
        
        // Expanded to cover all Scorched Guns 2 manual reload states
        return (reloadState.equals("RELOAD") || reloadState.equals("START") || 
                reloadState.equals("STARTING") || reloadState.equals("LOADING"));
    }

    private static boolean isDrawingWeapon(AbstractClientPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem)) return false;
        
        CompoundTag tag = stack.getOrCreateTag();
        // SCGuns uses DrawnTick (up to 15) to prevent aiming while the "draw" animation would be playing.
        return tag.getBoolean("IsDrawing") && tag.getInt("DrawnTick") < 15;
    }
}
