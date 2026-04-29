package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.client.events.engine.RenderEngine;

/**
 * Prevents Epic Fight from canceling the RenderHandEvent when holding a SCGuns firearm.
 *
 * Epic Fight intercepts RenderHandEvent at HIGHEST priority and calls event.setCanceled(true),
 * which completely prevents SCGuns from rendering its own first-person arm animations
 * (aiming, reloading, iron sights positioning).
 *
 * By injecting at HEAD and canceling our injection (not the event), we skip Epic Fight's
 * entire renderHand method, leaving the RenderHandEvent uncanceled and free for SCGuns.
 */
@Mixin(value = RenderEngine.Events.class, remap = false)
public abstract class RenderEngineMixin {

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private static void epicscorch$skipFirstPersonOverride(RenderHandEvent event, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();

        // If holding a gun in either hand, prevent EF from canceling the RenderHandEvent.
        // SCGuns will then get the event and render its own first-person animations.
        if (mainHand.getItem() instanceof GunItem || offHand.getItem() instanceof GunItem) {
            ci.cancel(); // Cancels our injection → skips EF's renderHand entirely
        }
    }
}
