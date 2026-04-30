package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import com.boone.epicscorch.config.EpicScorchConfig;

@Mixin(value = EpicFightCameraAPI.class, remap = false)
public abstract class EpicFightCameraAPIMixin {

    @Shadow
    public abstract void setLockOn(boolean flag);

    @Shadow
    private boolean lockingOnTarget;

    /**
     * Prevents lock-on from being activated if holding a gun.
     */
    @Inject(method = "setLockOn", at = @At("HEAD"), cancellable = true)
    private void epicscorch$preventLockOnWithGuns(boolean flag, CallbackInfo ci) {
        if (flag && EpicScorchConfig.DISABLE_LOCK_ON.get()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && player.getMainHandItem().getItem() instanceof GunItem) {
                ci.cancel();
            }
        }
    }

    /**
     * Automatically releases lock-on if the player switches to a gun.
     * Checked every tick to close the "switch exploit".
     */
    @Inject(method = "preClientTick", at = @At("HEAD"))
    private void epicscorch$releaseLockOnOnUpdate(CallbackInfo ci) {
        if (this.lockingOnTarget && EpicScorchConfig.DISABLE_LOCK_ON.get()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && player.getMainHandItem().getItem() instanceof GunItem) {
                this.setLockOn(false);
            }
        }
    }
}
