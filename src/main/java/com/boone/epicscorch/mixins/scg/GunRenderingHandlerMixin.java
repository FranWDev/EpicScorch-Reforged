package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderHandEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = GunRenderingHandler.class, remap = false)
public abstract class GunRenderingHandlerMixin {

    @Inject(method = "onRenderOverlay", at = @At("HEAD"), cancellable = true)
    private void epicscorch$suppressGunOverlayDuringInaction(RenderHandEvent event, CallbackInfo ci) {
        LocalPlayerPatch patch = EpicFightCapabilities.getEntityPatch(Minecraft.getInstance().player, LocalPlayerPatch.class);
        if (patch != null && patch.isEpicFightMode() && patch.getEntityState().inaction()) {
            ci.cancel();
        }
    }
}
