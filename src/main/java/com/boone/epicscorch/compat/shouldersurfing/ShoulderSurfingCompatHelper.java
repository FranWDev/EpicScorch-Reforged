package com.boone.epicscorch.compat.shouldersurfing;

import com.boone.epicscorch.forge.events.BalanceHandler;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.ControllerHandler;
import top.ribs.scguns.client.handler.ShootingHandler;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;

public class ShoulderSurfingCompatHelper {
    public static final String SHOULDER_SURFING_MODID = "shouldersurfing";

    public static boolean isShoulderSurfingLoaded() {
        return ModList.get().isLoaded(SHOULDER_SURFING_MODID);
    }

    public static boolean isActiveShoulderSurfing() {
        if (!isShoulderSurfingLoaded()) return false;
        try {
            return IShoulderSurfing.getInstance().isShoulderSurfing();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isHoldingGun(LivingEntity entity) {
        if (entity == null) return false;
        ItemStack mainHand = entity.getMainHandItem();
        return mainHand.getItem() instanceof GunItem;
    }

    public static boolean isAimKeyPressed() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null || player.isSpectator()) {
            return false;
        }

        if (BalanceHandler.isCurrentlyRestricted()) {
            return false;
        }

        if (ModSyncedDataKeys.RELOADING.getValue(player)) {
            return false;
        }

        try {
            if (AimingHandler.get().isLookingAtInteractableBlock()) {
                return false;
            }
        } catch (Throwable ignored) {
        }

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof GunItem gunItem) {
            try {
                Gun modifiedGun = gunItem.getModifiedGun(mainHand);
                if (modifiedGun != null && modifiedGun.getGeneral() != null) {
                    if (modifiedGun.getGeneral().getGripType(mainHand) == GripType.ONE_HANDED && !player.getOffhandItem().isEmpty()) {
                        return false;
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        try {
            KeyMapping aimKey = KeyBinds.getAimMapping();
            if (aimKey != null && aimKey.isDown()) {
                return true;
            }
        } catch (Throwable ignored) {
        }

        try {
            if (ScorchedGuns.controllableLoaded && ControllerHandler.isAiming()) {
                return true;
            }
        } catch (Throwable ignored) {
        }

        return false;
    }

    public static boolean isAimingGun(LivingEntity entity) {
        if (!isHoldingGun(entity)) return false;

        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player) {
            return AimingHandler.get().isAiming() || isAimKeyPressed();
        }

        if (entity instanceof Player player) {
            return ModSyncedDataKeys.AIMING.getValue(player);
        }

        return false;
    }

    public static boolean isShootingGun(LivingEntity entity) {
        if (!isHoldingGun(entity)) return false;

        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player) {
            return ShootingHandler.get().isShooting();
        }

        if (entity instanceof Player player) {
            return ModSyncedDataKeys.SHOOTING.getValue(player);
        }

        return false;
    }
}
