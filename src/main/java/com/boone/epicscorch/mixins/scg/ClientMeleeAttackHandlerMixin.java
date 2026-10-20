package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.ClientMeleeAttackHandler;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = ClientMeleeAttackHandler.class, remap = false)
public abstract class ClientMeleeAttackHandlerMixin {

    @Inject(method = "startMeleeAnimation", at = @At("HEAD"), cancellable = true)
    private static void epicscorch$overrideClientMeleeAnimation(GunItem gunItem, ItemStack heldItem, CallbackInfo ci) {
        LocalPlayerPatch patch = EpicFightCapabilities.getEntityPatch(Minecraft.getInstance().player, LocalPlayerPatch.class);
        if (patch != null && patch.isEpicFightMode()) {
            ClientMeleeAttackHandler.updateMeleeCooldownHUD(gunItem, heldItem);
            ci.cancel();
        }
    }
}
