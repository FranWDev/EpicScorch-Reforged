package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = RenderEngine.Events.class, remap = false)
public abstract class RenderEngineMixin {

    @Unique
    private static boolean epicscorch$wasInaction = false;

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private static void epicscorch$skipFirstPersonOverride(RenderHandEvent event, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();

        if (mainHand.getItem() instanceof GunItem || offHand.getItem() instanceof GunItem) {
            LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
            
            if (playerPatch != null && playerPatch.isEpicFightMode()) {
                EntityState state = playerPatch.getEntityState();
                boolean isInaction = state.inaction();
                
                // Transition detection: Just finished rolling/dashing
                if (epicscorch$wasInaction && !isInaction) {
                    // Reset the equip progress to 0 to trigger the "draw weapon" animation
                    ItemInHandRenderer itemRenderer = mc.getEntityRenderDispatcher().getItemInHandRenderer();
                    if (itemRenderer != null) {
                        ((ItemInHandRendererAccessor) itemRenderer).setMainHandHeight(0.0f);
                    }
                }
                
                epicscorch$wasInaction = isInaction;

                if (isInaction) {
                    return; // Let Epic Fight take over
                }
            }
            
            ci.cancel(); 
        }
    }
}
