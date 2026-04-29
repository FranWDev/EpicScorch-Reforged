package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.client.input.DiscreteActionHandler;
import yesman.epicfight.client.input.DiscreteInputActionTrigger;

@Mixin(value = DiscreteInputActionTrigger.class, remap = false)
public abstract class DiscreteInputActionTriggerMixin {

    /**
     * Prevents Epic Fight from consuming mouse clicks when the player is holding a gun.
     * This is critical because Epic Fight normally "drains" all clicks from the attack key
     * to trigger its combat system, leaving nothing for vanilla Minecraft (and other mods).
     * By preventing this consumption, Scorched Guns can correctly detect the clicks
     * and fire the weapon, even when looking into the air.
     */
    @Inject(method = "handleKeyboardAndMouse", at = @At("HEAD"), cancellable = true)
    private static void handleKeyboardAndMouse(KeyMapping keyMapping, DiscreteActionHandler handler, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            // Check if this key mapping is the vanilla attack key
            if (keyMapping == Minecraft.getInstance().options.keyAttack) {
                // Return early without consuming any clicks.
                // This allows Minecraft (and SCGuns) to see the clicks in their own tick handlers.
                ci.cancel();
            }
        }
    }
}
