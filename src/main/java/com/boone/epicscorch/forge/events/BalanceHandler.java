package com.boone.epicscorch.forge.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.client.network.ClientPlayHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageAim;
import top.ribs.scguns.network.message.C2SMessageReload;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.AnimationController;
import com.boone.epicscorch.config.EpicScorchConfig;
import com.boone.epicscorch.mixins.scg.AimingHandlerAccessor;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Client-side balance layer between Epic Fight and Scorched Guns.
 * Centralises movement-based restriction logic and ensures that aiming and
 * reloading states are cancelled when necessary, while protecting the reload
 * state during a jump.
 */
@Mod.EventBusSubscriber(modid = "epicscorch", value = Dist.CLIENT)
public class BalanceHandler {

    private static int inActionTicks = 0;
    private static int restrictionCooldown = 0;
    private static int reloadCancelCooldown = 0;
    private static boolean currentTickRestricted = false;
    private static int previousSlot = -1;
    private static Item previousItem = null;
    private static boolean wasReloadingLastTick = false;
    private static boolean wasManualReloadLastTick = false;
    private static int pendingResumeSlot = -1;
    private static Item pendingResumeItem = null;
    private static final Map<Long, Long> lastAnimReset = new HashMap<>();
    private static final Map<UUID, Integer> stopTimeout = new HashMap<>();

    public static boolean isCurrentlyRestricted() {
        return currentTickRestricted;
    }

    /**
     * Determines if aiming should be blocked.
     * Blocked by sprinting, active Epic Fight inaction (dodge/attack), or being
     * airborne while not reloading.
     * <p>
     * Only {@link yesman.epicfight.api.animation.types.EntityState#inaction()} is
     * used as the action gate; {@code canUseSkill()} is intentionally excluded
     * because it oscillates during skill cooldowns and transitions, which would
     * cause the ADS animation to stutter on every other tick.
     */
    public static boolean shouldBlockAiming(LocalPlayer player) {
        if (player == null)
            return false;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();

        boolean inAction = playerPatch != null
                && playerPatch.isEpicFightMode()
                && playerPatch.getEntityState().inaction()
                && EpicScorchConfig.CANCEL_AIM_ON_ACTION.get();

        boolean sprintBlocked = player.isSprinting() && EpicScorchConfig.FORCE_CANCEL_AIM_WHILE_SPRINTING.get();

        boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);
        boolean isAirborne = !isReloading
                && !player.isPassenger()
                && ((!player.onGround() && Math.abs(player.getDeltaMovement().y) > 0.01)
                        || mc.options.keyJump.isDown());

        return sprintBlocked || inAction || isAirborne;
    }

    /**
     * Determines if reloading should be blocked.
     * Blocked by sprinting or grounded actions. Jumping does not cancel reloading.
     */
    public static boolean shouldBlockReloading(LocalPlayer player) {
        if (player == null)
            return false;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();

        if (player.isSprinting() && EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get()) {
            return true;
        }

        boolean inAction = playerPatch != null
                && playerPatch.isEpicFightMode()
                && playerPatch.getEntityState().inaction()
                && EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get();

        if (inAction && (player.onGround() || player.isPassenger())) {
            inActionTicks++;
        } else {
            inActionTicks = 0;
        }

        if ((!player.onGround() && !player.isPassenger()) || (mc.options.keyJump.isDown() && !player.isPassenger())) {
            return false;
        }

        boolean confirmedInAction = inActionTicks >= 2;
        return confirmedInAction;
    }

    /** Composite restriction check. */
    public static boolean shouldBeRestricted(LocalPlayer player) {
        if (player == null)
            return false;

        boolean isStopping = player.getMainHandItem().getOrCreateTag()
                .getBoolean("scguns:IsPlayingReloadStop");

        return shouldBlockAiming(player)
                || shouldBlockReloading(player)
                || isStopping
                || reloadCancelCooldown > 0;
    }

    /**
     * Pre-tick: runs at HIGHEST priority, before SCGuns' AimingHandler.
     * Stamps the NBT invariants that SCGuns reads every tick, computes the
     * restriction state, and resolves any STOPPING softlock so the AimingHandler
     * always sees a clean, non-blocking reload state.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClientTickPre(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        int currentSlot = player.getInventory().selected;
        ItemStack heldItem = player.getMainHandItem();
        Item currentItem = heldItem.getItem();

        if (pendingResumeSlot == currentSlot) {
            if (pendingResumeItem == currentItem && currentItem instanceof GunItem
                    && !ModSyncedDataKeys.RELOADING.getValue(player)) {
                ReloadHandler.get().setReloading(true);
            }
            pendingResumeSlot = -1;
            pendingResumeItem = null;
        }

        // --- Detect Item Swap / Drop without memory allocations ---
        boolean slotChanged = previousSlot != currentSlot;
        boolean itemChanged = previousItem != currentItem;

        if (slotChanged || itemChanged) {
            if (previousItem instanceof GunItem && wasReloadingLastTick) {
                if (wasManualReloadLastTick) {
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    ModSyncedDataKeys.AIMING.setValue(player, false);
                    top.ribs.scguns.network.PacketHandler.getPlayChannel().sendToServer(new top.ribs.scguns.network.message.C2SMessageReload(false));
                    top.ribs.scguns.network.PacketHandler.getPlayChannel().sendToServer(new top.ribs.scguns.network.message.C2SMessageAim(false));
                    reloadCancelCooldown = 10;
                
                    // Clear any guns in inventory to remove lingering states before setting up new one
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack invStack = player.getInventory().getItem(i);
                        if (invStack.getItem() instanceof GunItem && invStack.hasTag()) {
                            // Skip clearing the new held item if we swapped to another gun!
                            if (currentSlot == i) {
                                continue;
                            }
                            clearReloadNbt(invStack.getOrCreateTag());
                        }
                    }
                } else {
                    if (previousSlot >= 0 && previousSlot < player.getInventory().getContainerSize()) {
                        ItemStack previousStack = player.getInventory().getItem(previousSlot);
                        if (previousStack.getItem() instanceof GunItem) {
                            CompoundTag previousTag = previousStack.getOrCreateTag();
                            if (previousStack.getItem() instanceof AnimatedGunItem animated) {
                                animated.cleanupReloadState(previousTag);
                            }
                            clearReloadNbt(previousTag);
                        }
                    }

                    pendingResumeSlot = previousSlot;
                    pendingResumeItem = previousItem;
                }
            }
        }
        
        previousSlot = currentSlot;
        previousItem = currentItem;
        wasReloadingLastTick = false;
        wasManualReloadLastTick = false;

        if (currentItem instanceof GunItem gunItem) {
            CompoundTag tag = heldItem.getTag();
            if (tag != null) {
                wasReloadingLastTick = tag.getBoolean("scguns:IsReloading") ||
                    tag.getString("scguns:ReloadState").contains("RELOAD") ||
                    tag.getString("scguns:ReloadState").contains("START") ||
                    ModSyncedDataKeys.RELOADING.getValue(player);
            } else {
                wasReloadingLastTick = ModSyncedDataKeys.RELOADING.getValue(player);
            }

            Gun gun = gunItem.getModifiedGun(heldItem);
            wasManualReloadLastTick = gun.getReloads().getReloadType() == ReloadType.MANUAL;
        }

        // --- NBT guarantees (must run before AimingHandler reads the item tag) ---
        if (heldItem.getItem() instanceof GunItem) {
            CompoundTag tag = heldItem.getOrCreateTag();

            // Re-stamp every tick: server syncs wipe client-only tags.
            tag.putBoolean("epicscorch:Initialized", true);

            boolean serverReloading = ModSyncedDataKeys.RELOADING.getValue(player);

            // Resolve STOPPING softlock before AimingHandler runs.
            // Epic Fight owns the skeleton during its animations, preventing
            // GeckoLib from firing the "animation finished" callback that would
            // normally transition the weapon out of STOPPING.
            if ("STOPPING".equals(tag.getString("scguns:ReloadState"))) {
                int stopTicks = tag.getInt("epicscorch:StopTicks") + 1;
                tag.putInt("epicscorch:StopTicks", stopTicks);

                if (!serverReloading || stopTicks > 15) {
                    tag.remove("scguns:ReloadState");
                    tag.remove("scguns:IsPlayingReloadStop");
                    tag.remove("epicscorch:StopTicks");

                    if (serverReloading) {
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        top.ribs.scguns.network.PacketHandler.getPlayChannel().sendToServer(new top.ribs.scguns.network.message.C2SMessageReload(false));
                    }

                    if (heldItem.getItem() instanceof AnimatedGunItem animated) {
                        animated.cleanupReloadState(tag);
                    }
                }
            } else {
                tag.remove("epicscorch:StopTicks");
            }


        }

        // --- Restriction state ---
        boolean restricted = shouldBeRestricted(player);

        if (restricted) {
            restrictionCooldown = 1;
        } else if (restrictionCooldown > 0) {
            restrictionCooldown--;
            restricted = true;
        }

        if (reloadCancelCooldown > 0 && !ModSyncedDataKeys.RELOADING.getValue(player)) {
            reloadCancelCooldown--;
        }

        currentTickRestricted = restricted;
    }

    /** Post-tick enforcement of restrictions and animation desync correction. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientTickPost(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack heldItem = player.getMainHandItem();
        boolean isStoppingReload = false;

        if (heldItem.getItem() instanceof GunItem) {
            CompoundTag tag = heldItem.getOrCreateTag();
            boolean serverReloading = ModSyncedDataKeys.RELOADING.getValue(player);

            if (!serverReloading && heldItem.getItem() instanceof AnimatedGunItem animated) {
                repairGeckolibDesync(heldItem, tag, animated);
            }

            isStoppingReload = tag.getBoolean("scguns:IsPlayingReloadStop");
        }

        boolean blockAim = shouldBlockAiming(player) || restrictionCooldown > 0
                || isStoppingReload || reloadCancelCooldown > 0;
        boolean blockReload = shouldBlockReloading(player);

        if (!blockAim && !blockReload && !isStoppingReload)
            return;

        AimingHandler aimingHandler = AimingHandler.get();
        boolean reloading = ModSyncedDataKeys.RELOADING.getValue(player);
        boolean aiming = aimingHandler.isAiming()
                || ((AimingHandlerAccessor) aimingHandler).getNormalisedAdsProgress() > 0.01;

        boolean forceCancelAim = aiming && blockAim;
        boolean forceCancelReload = (reloading || isStoppingReload) && blockReload;

        if (forceCancelAim || forceCancelReload) {
            cancelAimAndReload(player, heldItem, forceCancelAim, forceCancelReload);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof PauseScreen) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem() instanceof GunItem) {
                    cancelAimAndReload(player, heldItem, true, true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.side.isServer())
            return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player != event.player)
            return;
    }

    /**
     * Detects and repairs GeckoLib animation desyncs.
     * If the server reports not reloading but the animation persists, it forces a
     * reset.
     */
    private static void repairGeckolibDesync(ItemStack stack, CompoundTag tag, AnimatedGunItem animated) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !mc.player.onGround() && !mc.player.isPassenger())
            return;
        if (tag.getBoolean("ReloadComplete") || tag.getBoolean("scguns:ReloadComplete"))
            return;

        boolean isStuck = isStuckInReloadAnimation(tag);

        if (!isStuck) {
            try {
                long id = GeoItem.getId(stack);
                AnimationController<?> controller = getAnimationController(animated, id);
                if (controller != null && controller.getCurrentAnimation() != null) {
                    isStuck = isReloadAnimationName(controller.getCurrentAnimation().animation().name());
                }
            } catch (Exception ignored) {
            }
        }

        if (!isStuck)
            return;

        clearReloadNbt(tag);
        tag.remove("scguns:ReloadState");
        animated.cleanupReloadState(tag);

        try {
            long id = GeoItem.getId(stack);
            AnimationController<?> controller = getAnimationController(animated, id);
            if (controller != null) {
                controller.stop();
                controller.forceAnimationReset();
                controller.tryTriggerAnimation(animated.isInCarbineMode(stack) ? "carbine_idle" : "idle");
            }
        } catch (Exception ignored) {
        }
    }

    /** Checks if the weapon is stuck in a reload animation via NBT. */
    private static boolean isStuckInReloadAnimation(CompoundTag tag) {
        String reloadState = tag.getString("scguns:ReloadState");
        return reloadState.equals("RELOAD")
                || reloadState.equals("START")
                || reloadState.equals("RELOAD_LOOP")
                || tag.getBoolean("scguns:IsPlayingReloadLoop")
                || tag.getBoolean("scguns:IsReloading")
                || tag.getBoolean("InCriticalReloadPhase");
    }

    /** Checks if the animation name belongs to a reload cycle. */
    private static boolean isReloadAnimationName(String name) {
        return name.equals("reload_loop")
                || name.equals("carbine_reload_loop")
                || name.equals("reload")
                || name.equals("carbine_reload")
                || name.equals("reload_start")
                || name.equals("carbine_reload_start");
    }

    /** Cancels the aim and/or reload state and notifies the server. */
    private static void cancelAimAndReload(LocalPlayer player, ItemStack heldItem,
            boolean forceCancelAim, boolean forceCancelReload) {

        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            if (forceCancelAim)
                cancelAiming(player);
            return;
        }

        CompoundTag tag = heldItem.getOrCreateTag();
        String reloadState = tag.getString("scguns:ReloadState");

        boolean isActuallyReloading = reloadState.equals("RELOAD")
                || reloadState.equals("START")
                || reloadState.equals("STARTING")
                || reloadState.equals("LOADING");
        boolean isStopping = tag.getBoolean("scguns:IsPlayingReloadStop")
                || reloadState.contains("STOP");

        if ((isActuallyReloading || isStopping) && forceCancelReload) {
            boolean reloading = ModSyncedDataKeys.RELOADING.getValue(player);

            if (reloading) {
                ModSyncedDataKeys.RELOADING.setValue(player, false);
                ModSyncedDataKeys.AIMING.setValue(player, false);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));

                if (heldItem.getItem() instanceof AnimatedGunItem animated) {
                    animated.cleanupReloadState(tag);
                    clearReloadNbt(tag);
                    tag.remove("scguns:ReloadState");
                    resetGeckolibToIdle(heldItem, animated);
                }

                reloadCancelCooldown = 10;
            }

            if (isStopping) {
                int ticks = stopTimeout.getOrDefault(player.getUUID(), 0) + 1;
                stopTimeout.put(player.getUUID(), ticks);
                boolean isPlayingStopAnim = tag.getBoolean("scguns:IsPlayingReloadStop");
                if (ticks > 25 || (!isPlayingStopAnim && ticks > 5)) {
                    isStopping = false;
                }
            }

            if (!isStopping) {
                stopTimeout.remove(player.getUUID());

                if (heldItem.getItem() instanceof AnimatedGunItem animated) {
                    animated.cleanupReloadState(tag);
                    throttledGeckolibReset(heldItem, animated);
                }

                Gun gun = gunItem.getModifiedGun(heldItem);
                boolean isManual = gun.getReloads().getReloadType() == ReloadType.MANUAL;
                if (isManual && isActuallyReloading) {
                    try {
                        ClientPlayHandler.handleStopReload(null);
                    } catch (Exception ignored) {
                    }
                }

                clearReloadNbt(tag);
                reloadCancelCooldown = 10;
            }
        }

        if (forceCancelAim) {
            cancelAiming(player);
        }
    }

    /** Cancels aiming and synchronises with the server. */
    private static void cancelAiming(LocalPlayer player) {
        if (AimingHandler.get().isAiming()) {
            AimingHandler.get().aiming = false;
            ModSyncedDataKeys.AIMING.setValue(player, false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));
        }
    }

    /** Forces a GeckoLib animation reset to idle. */
    private static void resetGeckolibToIdle(ItemStack stack, AnimatedGunItem animated) {
        try {
            long id = GeoItem.getId(stack);
            AnimationController<?> controller = getAnimationController(animated, id);
            if (controller != null) {
                controller.stop();
                controller.forceAnimationReset();
                controller.tryTriggerAnimation(animated.isInCarbineMode(stack) ? "carbine_idle" : "idle");
            }
        } catch (Exception ignored) {
        }
    }

    /** Rate-limited GeckoLib reset to avoid flickering. */
    private static void throttledGeckolibReset(ItemStack stack, AnimatedGunItem animated) {
        try {
            long id = GeoItem.getId(stack);
            Minecraft mc = Minecraft.getInstance();
            long now = mc.level != null ? mc.level.getGameTime() : System.currentTimeMillis() / 50L;

            if (now - lastAnimReset.getOrDefault(id, 0L) >= 6L) {
                AnimationController<?> controller = getAnimationController(animated, id);
                if (controller != null) {
                    controller.forceAnimationReset();
                    controller.tryTriggerAnimation(animated.isInCarbineMode(stack) ? "carbine_idle" : "idle");
                    lastAnimReset.put(id, now);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /** Retrieves the GeckoLib animation controller or null if not ready. */
    private static AnimationController<?> getAnimationController(AnimatedGunItem animated, long id) {
        var manager = animated.getAnimatableInstanceCache().getManagerForId(id);
        if (manager == null)
            return null;
        return manager.getAnimationControllers().get("controller");
    }

    /** Clears all reload-related NBT tags. */
    public static void clearReloadNbt(CompoundTag tag) {
        tag.remove("ReloadTick");
        tag.remove("ReloadLoopTick");
        tag.remove("ReloadComplete");
        tag.remove("IsReloading");
        tag.remove("IsManualReload");
        tag.remove("InCriticalReloadPhase");
        tag.remove("InReloadLoop");
        tag.remove("Reloading");
        tag.remove("scguns:ReloadComplete");
        tag.remove("scguns:ReloadProgress");
        tag.remove("scguns:ReloadTick");
        tag.remove("scguns:ReloadLoopTick");
        tag.remove("scguns:AnimationReloadState");
        tag.remove("scguns:IsPlayingReloadStop");
        tag.remove("scguns:IsPlayingReloadLoop");
        tag.remove("scguns:IsReloading");
        tag.remove("scguns:Reloading");
        tag.remove("scguns:ShouldStopAfterLoop");
        tag.remove("scguns:ReloadState");
    }
}
