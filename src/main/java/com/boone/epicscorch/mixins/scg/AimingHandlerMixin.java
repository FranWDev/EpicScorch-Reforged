package com.boone.epicscorch.mixins.scg;

import java.util.HashMap;
import java.util.Map;
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
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageReload;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.AnimationController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import com.boone.epicscorch.config.EpicScorchConfig;

/**
 * Prevents aiming while sprinting. Intercepts onClientTick after shouldBeAiming is calculated.
 */
@Mixin(value = AimingHandler.class, remap = false)
public abstract class AimingHandlerMixin {

    @Shadow
    private boolean aiming;

    @Unique
    private static boolean epicscorch$wasInaction = false;
    @Unique
    private static final Map<Long, Long> epicscorch$lastAnimReset = new HashMap<>();

    @Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true)
    private void epicscorch$cancelAimingOnDodgeStart(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        if (event.phase != TickEvent.Phase.START) return;
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        
        boolean reloading = ModSyncedDataKeys.RELOADING.getValue(player);
        boolean restricted = false;
        
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        
        if (EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get()) {
            restricted = player.isSprinting();
            
            if (playerPatch != null && playerPatch.isEpicFightMode()) {
                EntityState state = playerPatch.getEntityState();
                if (state.inaction() || !state.canUseSkill()) {
                    restricted = true;
                }
            }
        }
        
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);
            
            if (!gun.canAimDownSight()) {
                this.aiming = false;
            }

            CompoundTag tag = heldItem.getOrCreateTag();
            boolean hasReloadTags = tag.getBoolean("IsReloading") || tag.getBoolean("scguns:IsReloading") || tag.contains("scguns:ReloadState");

            if (restricted || (!reloading && hasReloadTags)) {
                if (reloading) {
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));

                    if (heldItem.getItem() instanceof GunItem) {
                        ReloadHandler.get().setReloading(false);
                    }
                }

                boolean isManual = gun.getReloads().getReloadType() == ReloadType.MANUAL;
                boolean isAimingNow = this.aiming;

                boolean isDrawing = tag.getBoolean("IsDrawing") && tag.getInt("DrawnTick") < 15;
                if (!isDrawing && heldItem.getItem() instanceof AnimatedGunItem animated) {
                    animated.cleanupReloadState(tag);
                    try {
                        long id = GeoItem.getId(heldItem);
                        long now = 0L;
                        try {
                            now = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : System.currentTimeMillis()/50L;
                        } catch (Exception e) { now = System.currentTimeMillis()/50L; }
                        Long last = epicscorch$lastAnimReset.getOrDefault(id, 0L);
                        if (now - last >= 6L) {
                            AnimationController<?> controller = animated.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");
                            if (controller != null) {
                                controller.forceAnimationReset();
                                controller.tryTriggerAnimation(animated.isInCarbineMode(heldItem) ? "carbine_idle" : "idle");
                                epicscorch$lastAnimReset.put(id, now);
                            }
                        }
                    } catch (Exception e) {}
                }

                if (!isAimingNow && isManual && restricted) {
                    try { ClientPlayHandler.handleStopReload(null); } catch (Exception e) {}
                }

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

        if (EpicScorchConfig.CANCEL_AIM_ON_ACTION.get()) {
            if (player.isSprinting()) {
                this.aiming = false;
                return;
            }
            
            if (playerPatch != null && playerPatch.isEpicFightMode()) {
                EntityState state = playerPatch.getEntityState();
                if (state.inaction()) {
                    this.aiming = false;
                    epicscorch$wasInaction = true;
                }
            }
        }
    }

    @Inject(
        method = "onClientTick",
        at = @At(value = "FIELD", target = "Ltop/ribs/scguns/client/handler/AimingHandler;aiming:Z", ordinal = 2, shift = At.Shift.AFTER),
        cancellable = false
    )
    private void epicscorch$preventAimingDuringSprint(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem gunItem) {
                Gun gun = gunItem.getModifiedGun(heldItem);
                if (!gun.canAimDownSight()) {
                    this.aiming = false;
                }
            }

            if (EpicScorchConfig.CANCEL_AIM_ON_ACTION.get()) {
                if (player.isSprinting()) {
                    this.aiming = false;
                    return;
                }

                LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
                if (playerPatch != null && playerPatch.isEpicFightMode()) {
                    EntityState state = playerPatch.getEntityState();
                    boolean isInaction = state.inaction();

                    if (isInaction) {
                        this.aiming = false;
                    }
                    
                    epicscorch$wasInaction = isInaction;
                }
            }
        }
    }
}
