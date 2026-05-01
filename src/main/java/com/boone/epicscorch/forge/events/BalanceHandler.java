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

@Mod.EventBusSubscriber(modid = "epicscorch", value = Dist.CLIENT)
public class BalanceHandler {
    private static double lastPlayerY = 0;
    private static boolean lastTickSprintKeyDown = false;
    private static final Map<Long, Long> epicscorch$lastAnimReset = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.side.isServer()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player != event.player) return;

        boolean isAiming = AimingHandler.get().isAiming();
        boolean isReloading = ReloadHandler.get().getReloadTimer() > 0;

        boolean isAirborne = !player.onGround() && Math.abs(player.getDeltaMovement().y) > 0.01;

        if (isAiming && isAirborne) {
            cancelAiming(player);
        }

        if (isAiming || isReloading) {
            // Do not cancel reload just because sprint is toggled on; only real sprinting should matter.
            if (player.isSprinting()) {
                cancelAimAndReload(player);
            }
        }
        
        lastPlayerY = player.getY();
    }

    private static void cancelAiming(LocalPlayer player) {
        if (AimingHandler.get().isAiming()) {
            AimingHandler.get().aiming = false;
            ModSyncedDataKeys.AIMING.setValue(player, false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));
        }
    }

    private static void cancelAimAndReload(LocalPlayer player) {
        boolean wasReloading = ReloadHandler.get().getReloadTimer() > 0;

        ItemStack heldItem = player.getMainHandItem();
        boolean hasReloadTags = false;
        if (heldItem != null && heldItem.getItem() instanceof GunItem) {
            CompoundTag tag = heldItem.getOrCreateTag();
            hasReloadTags = tag.getBoolean("IsReloading") || tag.getBoolean("scguns:IsReloading") || tag.contains("scguns:ReloadState");
        }

        if (wasReloading || hasReloadTags || ModSyncedDataKeys.RELOADING.getValue(player)) {
            ModSyncedDataKeys.RELOADING.setValue(player, false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));

            if (ReloadHandler.get() != null) {
                ReloadHandler.get().setReloading(false);
            }

            if (heldItem != null && heldItem.getItem() instanceof GunItem gunItem) {
                CompoundTag tag = heldItem.getOrCreateTag();

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

                try {
                    Gun gun = gunItem.getModifiedGun(heldItem);
                    boolean isManual = gun.getReloads().getReloadType() == ReloadType.MANUAL;
                    if (isManual) {
                        try { ClientPlayHandler.handleStopReload(null); } catch (Exception e) {}
                    }
                } catch (Exception e) {}

                tag.putBoolean("scguns:IsReloading", false);
                tag.putString("scguns:ReloadState", "");
                tag.putBoolean("scguns:IsPlayingReloadStop", false);
                tag.putBoolean("IsReloading", false);
                tag.remove("InCriticalReloadPhase");
                tag.remove("InReloadLoop");
                tag.remove("IsManualReload");
                tag.remove("scguns:ReloadComplete");
                tag.remove("scguns:ReloadProgress");
                tag.remove("scguns:ReloadState");
            }
        }

        cancelAiming(player);
    }
}
