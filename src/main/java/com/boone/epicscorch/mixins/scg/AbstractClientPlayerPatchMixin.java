package com.boone.epicscorch.mixins.scg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(AbstractClientPlayerPatch.class)
public abstract class AbstractClientPlayerPatchMixin extends PlayerPatch<AbstractClientPlayer> {
    @Inject(method = "updateMotion", at = @At("TAIL"), remap = false)
    private void scguns$updateMotion(CallbackInfo ci) {
        AbstractClientPlayer player = this.getOriginal();
        if (player == null || !this.isEpicFightMode()) return;

        if (AimingHandler.get().isAiming()) {
            this.currentCompositeMotion = LivingMotions.AIM;
        }

        boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);
        if (!isReloading) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof GunItem) {
                CompoundTag tag = stack.getOrCreateTag();
                String reloadState = tag.getString("scguns:ReloadState");
                boolean isPlayingReloadStop = tag.getBoolean("scguns:IsPlayingReloadStop");
                if ((!reloadState.equals("NONE") && !reloadState.isEmpty()) || isPlayingReloadStop) {
                    isReloading = true;
                }
            }
        }

        if (isReloading) {
            this.currentCompositeMotion = LivingMotions.RELOAD;
        }
    }
}
