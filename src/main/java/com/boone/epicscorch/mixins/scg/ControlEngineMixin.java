package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.client.events.engine.ControlEngine;
import com.boone.epicscorch.config.EpicScorchConfig;

/**
 * Prevents Epic Fight's combat actions from interfering with gun firing.
 */
@Mixin(value = ControlEngine.class, remap = false)
public abstract class ControlEngineMixin {

    @Inject(method = "maybeAttack", at = @At("HEAD"), cancellable = true)
    private void maybeAttack(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            // Bloquea cualquier intento de ataque de Epic Fight si se sostiene un arma
            if (stack.getItem() instanceof GunItem || AimingHandler.get().isAiming()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleSeparateWeaponInnateSkill", at = @At("HEAD"), cancellable = true)
    private void handleSeparateWeaponInnateSkill(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getMainHandItem().getItem() instanceof GunItem) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldDisableVanillaAttack", at = @At("HEAD"), cancellable = true)
    private static void shouldDisableVanillaAttack(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof GunItem || AimingHandler.get().isAiming()) {
                cir.setReturnValue(false);
            }
        }
    }

}

