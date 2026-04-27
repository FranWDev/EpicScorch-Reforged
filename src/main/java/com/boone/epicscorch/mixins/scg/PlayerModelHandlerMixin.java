package com.boone.epicscorch.mixins.scg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.PlayerModelHandler;

@Mixin(PlayerModelHandler.class)
public class PlayerModelHandlerMixin {
   @Inject(method = "onRenderPlayer*", at = @At("HEAD"), cancellable = true, remap = false)
   public void onRenderPlayerInject(CallbackInfo ci) {
      ci.cancel();
   }
}
