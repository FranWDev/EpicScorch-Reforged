package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.forge.events.BalanceHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.AimingHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.ribs.scguns.client.KeyBinds;
import com.boone.epicscorch.forge.events.BalanceHandler;

@Mixin(value = AimingHandler.class, remap = false)
public abstract class AimingHandlerMixin {
    @Shadow
    private boolean aiming;

    private static final KeyMapping DUMMY_MAPPING = new KeyMapping("epicscorch.dummy", -1, "key.categories.scguns") {
        @Override
        public boolean isDown() {
            return false;
        }
    };

    @Redirect(method = "onClientTick(Lnet/minecraftforge/event/TickEvent$ClientTickEvent;)V", 
              at = @At(value = "INVOKE", target = "Ltop/ribs/scguns/client/KeyBinds;getAimMapping()Lnet/minecraft/client/KeyMapping;", remap = false))
    private KeyMapping epicscorch$getAimMapping() {
        if (BalanceHandler.shouldBeRestricted(Minecraft.getInstance().player)) {
            return DUMMY_MAPPING;
        }
        return KeyBinds.getAimMapping();
    }

    @Inject(method = "onClientTick(Lnet/minecraftforge/event/TickEvent$ClientTickEvent;)V", at = @At("HEAD"))
    private void epicscorch$onClientTickHead(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        if (BalanceHandler.shouldBlockAiming(Minecraft.getInstance().player)) {
            this.aiming = false;
        }
    }

    @Inject(method = "isAiming()Z", at = @At("HEAD"), cancellable = true)
    private void epicscorch$forceNotAiming(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (BalanceHandler.shouldBlockAiming(Minecraft.getInstance().player)) {
            cir.setReturnValue(false);
        }
    }
}
