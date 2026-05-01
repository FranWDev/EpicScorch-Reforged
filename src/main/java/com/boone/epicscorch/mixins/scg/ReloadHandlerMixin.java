package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.ReloadHandler;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import com.boone.epicscorch.config.EpicScorchConfig;

@Mixin(value = ReloadHandler.class, remap = false)
public abstract class ReloadHandlerMixin {

    @Inject(method = "onKeyPressed", at = @At("HEAD"), cancellable = true)
    private void epicscorch$blockReloadStart(InputEvent.Key event, CallbackInfo ci) {
        if (!EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (player.isSprinting()) {
            ci.cancel();
            return;
        }

        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && playerPatch.isEpicFightMode()) {
            EntityState state = playerPatch.getEntityState();
            if (state.inaction() || !state.canUseSkill()) {
                ci.cancel();
            }
        }
    }
}
