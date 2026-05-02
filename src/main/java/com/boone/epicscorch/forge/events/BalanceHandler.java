package com.boone.epicscorch.forge.events;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageAim;
import top.ribs.scguns.network.message.C2SMessageReload;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.client.network.ClientPlayHandler;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.AnimationController;
import com.boone.epicscorch.config.EpicScorchConfig;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import top.ribs.scguns.client.KeyBinds;
import com.boone.epicscorch.mixins.scg.AimingHandlerAccessor;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "epicscorch", value = Dist.CLIENT)
public class BalanceHandler {
    private static double lastPlayerY = 0;
    private static boolean lastTickSprintKeyDown = false;
    private static boolean wasRestricted = false;
    private static boolean wasAimingBeforeRestriction = false;
    private static final Map<Long, Long> epicscorch$lastAnimReset = new HashMap<>();
    private static final Map<UUID, Integer> epicscorch$stopTimeout = new HashMap<>();
    private static int restrictionCooldown = 0;
    private static boolean currentTickRestricted = false;

    public static boolean isCurrentlyRestricted() {
        return currentTickRestricted;
    }

    public static boolean shouldBlockAiming(LocalPlayer player) {
        if (player == null)
            return false;
        Minecraft mc = Minecraft.getInstance();

        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        boolean inAction = (playerPatch != null && playerPatch.isEpicFightMode() &&
                (playerPatch.getEntityState().inaction() || !playerPatch.getEntityState().canUseSkill()) &&
                EpicScorchConfig.CANCEL_AIM_ON_ACTION.get());

        boolean isSprinting = player.isSprinting();
        boolean isSprintKeyDown = mc.options.keySprint.isDown();

        // Immediate sprint detection
        boolean sprintBlocked = isSprintKeyDown || isSprinting;
        boolean isAirborne = (!player.onGround() && Math.abs(player.getDeltaMovement().y) > 0.01)
                || mc.options.keyJump.isDown();

        return sprintBlocked || inAction || isAirborne;
    }

    public static boolean shouldBlockReloading(LocalPlayer player) {
        if (player == null)
            return false;
        Minecraft mc = Minecraft.getInstance();

        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        boolean inAction = (playerPatch != null && playerPatch.isEpicFightMode() &&
                (playerPatch.getEntityState().inaction() || !playerPatch.getEntityState().canUseSkill()) &&
                EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get());

        boolean isSprinting = player.isSprinting();
        boolean isSprintKeyDown = mc.options.keySprint.isDown();

        // Reload is NOT blocked by jumping/airborne
        return isSprinting || isSprintKeyDown || inAction;
    }

    public static boolean shouldBeRestricted(LocalPlayer player) {
        return shouldBlockAiming(player) || shouldBlockReloading(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClientTickPre(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        boolean restricted = shouldBeRestricted(player);
        if (restricted) {
            restrictionCooldown = 2; // Stay restricted for 2 more ticks to prevent flickering
        } else if (restrictionCooldown > 0) {
            restrictionCooldown--;
            restricted = true;
        }
        currentTickRestricted = restricted;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientTickPost(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        boolean restricted = currentTickRestricted;
        AimingHandler aimingHandler = AimingHandler.get();
        ItemStack heldItem = player.getMainHandItem();
        boolean isStoppingReload = false;

        if (heldItem.getItem() instanceof GunItem) {
            isStoppingReload = heldItem.getOrCreateTag().getBoolean("scguns:IsPlayingReloadStop");
        }

        // We continue calling cancelAimAndReload if restricted OR if we are in the
        // middle of a forced reload stop
        boolean blockAim = shouldBlockAiming(player) || restrictionCooldown > 0;
        boolean blockReload = shouldBlockReloading(player);

        if (blockAim || blockReload || isStoppingReload) {
            boolean reloading = ModSyncedDataKeys.RELOADING.getValue(player);
            boolean aiming = aimingHandler.isAiming()
                    || ((AimingHandlerAccessor) aimingHandler).getNormalisedAdsProgress() > 0.01;

            if (aiming || reloading || isStoppingReload) {
                cancelAimAndReload(player, blockAim, blockReload);

                if (blockAim) {
                    ((AimingHandlerAccessor) aimingHandler).setNormalisedAdsProgress(0.0);
                }
            }
        }

        wasRestricted = restricted;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.side.isServer())
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player != event.player)
            return;

        lastPlayerY = player.getY();
    }

    private static void cancelAiming(LocalPlayer player) {
        if (AimingHandler.get().isAiming()) {
            AimingHandler.get().aiming = false;
            ModSyncedDataKeys.AIMING.setValue(player, false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));
        }
    }

    private static void cancelAimAndReload(LocalPlayer player, boolean forceCancelAim, boolean forceCancelReload) {
        if (player == null)
            return;

        AimingHandler aimingHandler = AimingHandler.get();
        ReloadHandler reloadHandler = ReloadHandler.get();
        boolean reloading = ModSyncedDataKeys.RELOADING.getValue(player);
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof GunItem gunItem) {
            CompoundTag tag = heldItem.getOrCreateTag();
            String reloadState = tag.getString("scguns:ReloadState");
            boolean isActuallyReloading = reloading || reloadState.equals("RELOAD") || reloadState.equals("START") ||
                    reloadState.equals("STARTING") || reloadState.equals("LOADING");
            boolean isStopping = tag.getBoolean("scguns:IsPlayingReloadStop") || reloadState.contains("STOP");

            if ((isActuallyReloading || isStopping) && forceCancelReload) {
                Gun gun = gunItem.getModifiedGun(heldItem);
                boolean isManual = gun.getReloads().getReloadType() == ReloadType.MANUAL;

                if (reloading) {
                    if (isManual) {
                        if (!isStopping) {
                            tag.putString("scguns:ReloadState", "STOPPING");
                            tag.putBoolean("scguns:IsPlayingReloadStop", true);

                            // Clear intent tags immediately to prevent Scorched Guns (and our own mixins)
                            // from continuing the reload
                            tag.remove("InCriticalReloadPhase");
                            tag.remove("InReloadLoop");
                            tag.remove("IsReloading");
                            tag.remove("scguns:IsReloading");
                            tag.remove("scguns:ReloadComplete");
                            tag.remove("scguns:ReloadProgress");
                            tag.remove("scguns:AnimationReloadState");

                            // Call SCG's stop logic only ONCE at the beginning of the stop
                            if (reloadHandler != null)
                                reloadHandler.setReloading(false);
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                        }
                    } else {
                        // Non-manual reloads: stop immediately and clear data key
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        if (reloadHandler != null)
                            reloadHandler.setReloading(false);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));

                        // AGGRESSIVE CLEANUP for mag-fed guns to prevent "flickering" later
                        tag.remove("InCriticalReloadPhase");
                        tag.remove("IsReloading");
                        tag.remove("scguns:IsReloading");
                        tag.remove("InReloadLoop");
                        tag.remove("scguns:ReloadComplete");
                        tag.remove("scguns:ReloadProgress");
                        tag.remove("scguns:ReloadState");
                        tag.remove("scguns:AnimationReloadState");
                        tag.remove("scguns:IsPlayingReloadStop");
                        tag.remove("scguns:IsPlayingReloadLoop");
                        tag.remove("ReloadTick");
                        tag.remove("ReloadLoopTick");

                        // Force GeckoLib to reset to idle
                        if (heldItem.getItem() instanceof AnimatedGunItem animated) {
                            try {
                                long id = GeoItem.getId(heldItem);
                                AnimationController<?> controller = animated.getAnimatableInstanceCache()
                                        .getManagerForId(id).getAnimationControllers().get("controller");
                                if (controller != null) {
                                    controller.forceAnimationReset();
                                    controller.tryTriggerAnimation(
                                            animated.isInCarbineMode(heldItem) ? "carbine_idle" : "idle");
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                }

                boolean isPlayingStopAnim = tag.getBoolean("scguns:IsPlayingReloadStop");
                if (isStopping) {
                    int ticks = epicscorch$stopTimeout.getOrDefault(player.getUUID(), 0) + 1;
                    epicscorch$stopTimeout.put(player.getUUID(), ticks);
                    if (ticks > 25 || (!isPlayingStopAnim && ticks > 5))
                        isStopping = false;
                }

                if (!isStopping) {
                    epicscorch$stopTimeout.remove(player.getUUID());
                    if (heldItem.getItem() instanceof AnimatedGunItem animated) {
                        animated.cleanupReloadState(tag);
                        try {
                            long id = GeoItem.getId(heldItem);
                            long now = Minecraft.getInstance().level != null
                                    ? Minecraft.getInstance().level.getGameTime()
                                    : System.currentTimeMillis() / 50L;
                            Long last = epicscorch$lastAnimReset.getOrDefault(id, 0L);
                            if (now - last >= 6L) {
                                AnimationController<?> controller = animated.getAnimatableInstanceCache()
                                        .getManagerForId(id).getAnimationControllers().get("controller");
                                if (controller != null) {
                                    controller.forceAnimationReset();
                                    controller.tryTriggerAnimation(
                                            animated.isInCarbineMode(heldItem) ? "carbine_idle" : "idle");
                                    epicscorch$lastAnimReset.put(id, now);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }

                    if (isManual && isActuallyReloading) {
                        try {
                            ClientPlayHandler.handleStopReload(null);
                        } catch (Exception e) {
                        }
                    }

                    tag.remove("InCriticalReloadPhase");
                    tag.remove("IsReloading");
                    tag.remove("scguns:IsReloading");
                    tag.remove("InReloadLoop");
                    tag.remove("IsManualReload");
                    tag.remove("scguns:ReloadComplete");
                    tag.remove("scguns:ReloadProgress");
                    tag.remove("scguns:ReloadState");
                    tag.remove("scguns:AnimationReloadState");
                    tag.remove("scguns:IsPlayingReloadStop");
                    tag.remove("scguns:IsPlayingReloadLoop");
                    tag.remove("ReloadTick");
                    tag.remove("ReloadLoopTick");
                }
            }
        }

        if (forceCancelAim) {
            cancelAiming(player);
        }
    }
}
