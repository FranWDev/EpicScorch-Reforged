package com.boone.epicscorch.compat.shouldersurfing;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.world.phys.PickContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShoulderSurfingAimRotationHandler {

    /**
     * Calculates the exact 3D target location where the Shoulder Surfing crosshair is pointing.
     * Uses Shoulder Surfing's object picker raycast, falling back to camera forward vector.
     */
    public static Vec3 getCrosshairTarget(Minecraft mc, LocalPlayer player) {
        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) {
            return player.getEyePosition().add(player.getViewVector(1.0F).scale(100.0));
        }

        try {
            IShoulderSurfing ss = IShoulderSurfing.getInstance();
            if (ss != null && ss.getObjectPicker() != null) {
                PickContext pickContext = new PickContext.Builder(camera).build();
                HitResult hitResult = ss.getObjectPicker().pick(pickContext, 400.0, 1.0F, (Player) player);
                if (hitResult != null && hitResult.getLocation() != null) {
                    return hitResult.getLocation();
                }
            }
        } catch (Throwable ignored) {
        }

        Vec3 cameraPos = camera.getPosition();
        Vec3 lookVector = new Vec3(camera.getLookVector());
        return cameraPos.add(lookVector.scale(100.0));
    }

    /**
     * Converts a 3D target point into exact (yaw, pitch) angles relative to the player's eye position.
     * Returned float array: [yaw, pitch]
     */
    public static float[] getAnglesToTarget(LocalPlayer player, Vec3 target) {
        Vec3 eyes = player.getEyePosition();
        double dx = target.x - eyes.x;
        double dy = target.y - eyes.y;
        double dz = target.z - eyes.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float) (-(Mth.atan2(dy, horizontalDist) * (180.0 / Math.PI)));

        return new float[]{Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90.0F, 90.0F)};
    }

    /**
     * Updates player entity rotation, head rotation, and body rotation to face the crosshair target.
     * This ensures the player model, skeleton, animations, and camera orientation are aligned in 3D.
     */
    public static void alignPlayerWithCrosshair(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 target = getCrosshairTarget(mc, player);
        float[] angles = getAnglesToTarget(player, target);
        float yaw = angles[0];
        float pitch = angles[1];

        player.setYRot(yaw);
        player.yRotO = yaw;
        player.setXRot(pitch);
        player.xRotO = pitch;
        player.yHeadRot = yaw;
        player.yHeadRotO = yaw;
        player.yBodyRot = yaw;
        player.yBodyRotO = yaw;
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.isPaused()) return;

        if (ShoulderSurfingCompatHelper.isActiveShoulderSurfing() &&
            (ShoulderSurfingCompatHelper.isAimingGun(player) || ShoulderSurfingCompatHelper.isShootingGun(player))) {
            alignPlayerWithCrosshair(player);
        }
    }

    public static void onRenderTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.isPaused()) return;

        if (ShoulderSurfingCompatHelper.isActiveShoulderSurfing() &&
            (ShoulderSurfingCompatHelper.isAimingGun(player) || ShoulderSurfingCompatHelper.isShootingGun(player))) {
            alignPlayerWithCrosshair(player);
        }
    }
}
