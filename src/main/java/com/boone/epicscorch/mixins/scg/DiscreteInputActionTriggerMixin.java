package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.client.input.DiscreteActionHandler;
import yesman.epicfight.client.input.DiscreteInputActionTrigger;

/**
 * Prevents Epic Fight from consuming mouse clicks when holding a gun.
 * This lets Scorched Guns properly detect and handle fire inputs.
 */
@Mixin(value = DiscreteInputActionTrigger.class, remap = false)
public abstract class DiscreteInputActionTriggerMixin {

    /**
     * Block Epic Fight's click consumption for both attack and use keys.
     * Epic Fight normally "drains" all clicks to run its combat system,
     * leaving nothing for Scorched Guns to detect.
     */
    @Inject(method = "handleKeyboardAndMouse", at = @At("HEAD"), cancellable = true)
    private static void handleKeyboardAndMouse(KeyMapping keyMapping, DiscreteActionHandler handler, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            Minecraft mc = Minecraft.getInstance();
            
            if (keyMapping == mc.options.keyAttack || keyMapping == mc.options.keyUse) {
                ci.cancel();
            }
        }
        
        if (AimingHandler.get().isAiming()) {
            if (keyMapping == Minecraft.getInstance().options.keyAttack) {
                ci.cancel();
            }
        }
    }
}
