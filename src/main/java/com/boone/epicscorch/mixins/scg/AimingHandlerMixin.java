package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.AimingHandler;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.client.network.ClientPlayHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.AnimationController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Prevents aiming while sprinting. Intercepts onClientTick after shouldBeAiming is calculated.
 */
@Mixin(value = AimingHandler.class, remap = false)
public abstract class AimingHandlerMixin {

    @Shadow
    private boolean aiming;

    @Unique
    private static boolean epicscorch$wasInaction = false;

    /**
     * Early check: prevent aiming if in dodge/roll at method start.
     */
    @Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true)
    private void epicscorch$cancelAimingOnDodgeStart(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        if (event.phase != TickEvent.Phase.START) return;
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        
        // --- RELOAD BLOCKING LOGIC ---
        boolean reloading = ModSyncedDataKeys.RELOADING.getValue(player);
        boolean restricted = player.isSprinting() || mc.options.keySprint.isDown();
        
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            EntityState state = playerPatch.getEntityState();
            if (state.inaction() || !state.canUseSkill()) {
                restricted = true;
            }
        }
        
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem gunItem) {
            CompoundTag tag = heldItem.getOrCreateTag();
            boolean hasReloadTags = tag.getBoolean("IsReloading") || tag.getBoolean("scguns:IsReloading") || tag.contains("scguns:ReloadState");

            if (restricted || (!reloading && hasReloadTags)) {
                if (reloading) {
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    ReloadHandler.get().setReloading(false);
                }

                Gun gun = gunItem.getModifiedGun(heldItem);
                boolean isManual = gun.getReloads().getReloadType() == ReloadType.MANUAL;
                boolean isAimingNow = this.aiming;

                // Force reset GeckoLib state to prevent animation blending/stuttering
                if (heldItem.getItem() instanceof AnimatedGunItem animated) {
                    animated.cleanupReloadState(tag);
                    try {
                        long id = GeoItem.getId(heldItem);
                        AnimationController<?> controller = animated.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");
                        if (controller != null) {
                            controller.forceAnimationReset();
                            controller.tryTriggerAnimation(animated.isInCarbineMode(heldItem) ? "carbine_idle" : "idle");
                        }
                    } catch (Exception e) {}
                }

                if (!isAimingNow && isManual && restricted) {
                    try { ClientPlayHandler.handleStopReload(null); } catch (Exception e) {}
                }

                // Exhaustive cleanup of NBT state tags
                tag.remove("InCriticalReloadPhase");
                tag.remove("IsReloading");
                tag.remove("scguns:IsReloading");
                tag.remove("InReloadLoop");
                tag.remove("IsManualReload");
                tag.remove("scguns:ReloadComplete");
                tag.remove("scguns:ReloadProgress");
                tag.remove("scguns:ReloadState");
                tag.remove("scguns:IsPlayingReloadStop");
            }
        }

        // Cancel if sprinting
        if (player.isSprinting() || mc.options.keySprint.isDown()) {
            this.aiming = false;
            return;
        }
        
        // Cancel if dodging/rolling
        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            EntityState state = playerPatch.getEntityState();
            if (state.inaction()) {
                this.aiming = false;
                epicscorch$wasInaction = true;
            }
        }
    }

    /**
     * Block aiming if player is sprinting, rolling, or holding sprint key.
     */
    @Inject(
        method = "onClientTick",
        at = @At(value = "FIELD", target = "Ltop/ribs/scguns/client/handler/AimingHandler;aiming:Z", ordinal = 2, shift = At.Shift.AFTER),
        cancellable = false
    )
    private void epicscorch$preventAimingDuringSprint(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            // Cancel aiming during sprint
            if (player.isSprinting() || mc.options.keySprint.isDown()) {
                this.aiming = false;
                return;
            }
            
            // Cancel aiming during dodge/roll
            LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
            if (playerPatch != null && playerPatch.isEpicFightMode()) {
                EntityState state = playerPatch.getEntityState();
                boolean isInaction = state.inaction();
                
                // Cancel aiming if dodge just started or currently dodging
                if (isInaction) {
                    this.aiming = false;
                }
                
                epicscorch$wasInaction = isInaction;
            }
        }
    }
}
