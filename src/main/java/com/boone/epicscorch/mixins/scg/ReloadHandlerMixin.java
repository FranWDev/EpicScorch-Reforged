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
import com.boone.epicscorch.forge.events.BalanceHandler;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Shadow;
import top.ribs.scguns.init.ModSyncedDataKeys;
import net.minecraft.client.KeyMapping;
import top.ribs.scguns.client.KeyBinds;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ReloadHandler.class, remap = false)
public abstract class ReloadHandlerMixin {
    
    private static final KeyMapping DUMMY_MAPPING = new KeyMapping("epicscorch.dummy", -1, "key.categories.scguns") {
        @Override
        public boolean isDown() {
            return false;
        }
    };



    @Redirect(method = "onMouseInput(Lnet/minecraftforge/client/event/InputEvent$MouseButton;)V", 
              at = @At(value = "INVOKE", target = "Ltop/ribs/scguns/client/KeyBinds;getAimMapping()Lnet/minecraft/client/KeyMapping;", remap = false))
    private KeyMapping epicscorch$getAimMappingMouse() {
        if (BalanceHandler.shouldBlockAiming(Minecraft.getInstance().player)) {
            return DUMMY_MAPPING;
        }
        return KeyBinds.getAimMapping();
    }



    @Inject(method = "onKeyPressed", at = @At("HEAD"), cancellable = true)
    private void epicscorch$blockReloadStart(InputEvent.Key event, CallbackInfo ci) {
        if (!EpicScorchConfig.CANCEL_RELOAD_ON_ACTION.get()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // Use the centralized restriction check for starting new reloads
        if (BalanceHandler.shouldBlockReloading(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "onClientTick(Lnet/minecraftforge/event/TickEvent$ClientTickEvent;)V", at = @At("HEAD"))
    private void epicscorch$onClientTickHead(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        // Handled in BalanceHandler
    }
}
