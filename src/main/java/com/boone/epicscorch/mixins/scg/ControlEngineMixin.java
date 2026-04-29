package com.boone.epicscorch.mixins.scg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.client.events.engine.ControlEngine;

@Mixin(ControlEngine.class)
public class ControlEngineMixin {
    @Inject(method = "shouldDisableVanillaAttack", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onShouldDisableVanillaAttack(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack stack = mc.player.getMainHandItem();
            if (stack.getItem() instanceof GunItem) {
                // Allow vanilla attack (which SCGuns uses to fire) even in combat mode
                cir.setReturnValue(false);
            }
        }
    }
}
