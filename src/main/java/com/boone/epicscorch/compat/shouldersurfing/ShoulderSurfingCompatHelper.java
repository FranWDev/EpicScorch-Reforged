package com.boone.epicscorch.compat.shouldersurfing;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.ShootingHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;

public class ShoulderSurfingCompatHelper {
    public static final String SHOULDER_SURFING_MODID = "shouldersurfing";

    public static boolean isShoulderSurfingLoaded() {
        return ModList.get().isLoaded(SHOULDER_SURFING_MODID);
    }

    public static boolean isHoldingGun(LivingEntity entity) {
        if (entity == null) return false;
        ItemStack mainHand = entity.getMainHandItem();
        return mainHand.getItem() instanceof GunItem;
    }

    public static boolean isAimingGun(LivingEntity entity) {
        if (!isHoldingGun(entity)) return false;

        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player) {
            return AimingHandler.get().isAiming();
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
