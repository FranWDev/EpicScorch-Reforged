package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.client.events.engine.ControlEngine;

@Mixin(value = ControlEngine.class, remap = false)
public abstract class ControlEngineMixin {

    @Inject(method = "maybeAttack", at = @At("HEAD"), cancellable = true)
    private void maybeAttack(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof GunItem || AimingHandler.get().isAiming()) {
                System.out.println("[EpicScorch-Debug] maybeAttack BLOCKED for " + stack.getItem().toString());
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleSeparateWeaponInnateSkill", at = @At("HEAD"), cancellable = true)
    private void handleSeparateWeaponInnateSkill(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getMainHandItem().getItem() instanceof GunItem) {
            System.out.println("[EpicScorch-Debug] handleSeparateWeaponInnateSkill BLOCKED");
            ci.cancel();
        }
    }

    @Inject(method = "shouldDisableVanillaAttack", at = @At("HEAD"), cancellable = true)
    private static void shouldDisableVanillaAttack(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof GunItem || AimingHandler.get().isAiming()) {
                // We return FALSE to let vanilla clicks pass through to SCGuns
                cir.setReturnValue(false);
            }
        }
    }

    /* 
    // This is currently causing a crash because the method name isn't found in the runtime JAR.
    // We'll re-enable it once we identify the correct target name.
    @Inject(method = "onInteractionKeyMappingTriggered", at = @At("HEAD"), cancellable = true)
    private void onInteractionKeyMappingTriggered(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            System.out.println("[EpicScorch-Debug] onInteractionKeyMappingTriggered CANCELLED by EpicScorch");
            ci.cancel();
        }
    }
    */
}
