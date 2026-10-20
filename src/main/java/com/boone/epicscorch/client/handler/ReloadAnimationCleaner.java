package com.boone.epicscorch.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.item.GunItem;
import com.boone.epicscorch.forge.gameasset.animation.type.LockedAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Detects when dodge ends (inaction() true → false) and forces reload layer to
 * stay off.
 * Epic Fight re-activates RELOAD layer after dodge because isUsingItem()
 * remains true.
 * This catches the transition and prevents re-activation.
 */
@Mod.EventBusSubscriber(modid = "epicscorch", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ReloadAnimationCleaner {

    private static boolean wasInaction = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            wasInaction = false;
            return;
        }

        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch == null || !playerPatch.isEpicFightMode()) {
            wasInaction = false;
            return;
        }

        EntityState state = playerPatch.getEntityState();
        boolean isInaction = state.inaction();

        // Detect transition: inaction() true → false (just exited dodge/roll)
        if (wasInaction && !isInaction) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof GunItem) {
                CompoundTag tag = stack.getOrCreateTag();
                boolean wasReloading = tag.getBoolean("scguns:IsReloading") || !tag.getString("scguns:ReloadState").isEmpty();

                var animator = playerPatch.getClientAnimator();
                if (animator != null) {
                    try {
                        Layer reloadLayer = animator.getCompositeLayer(Layer.Priority.MIDDLE);
                        if (reloadLayer != null && !reloadLayer.isOff()) {
                            DynamicAnimation currentAnim =
                                reloadLayer.animationPlayer.getRealAnimation() != null
                                    ? reloadLayer.animationPlayer.getRealAnimation().get()
                                    : null;
                            boolean isPlayingReload = currentAnim instanceof LockedAnimation;

                            // Only force reload layer off if the player was actively reloading
                            if (wasReloading || isPlayingReload) {
                                reloadLayer.off(playerPatch);
                            }
                        }
                    } catch (Exception e) {
                        // Ignore errors in cleanup
                    }
                }

                if (wasReloading) {
                    // Clean NBT flags so Epic Fight doesn't re-detect reload
                    tag.putBoolean("scguns:IsReloading", false);
                    tag.putString("scguns:ReloadState", "");
                    tag.putBoolean("scguns:IsPlayingReloadStop", false);
                }
            }
        }

        wasInaction = isInaction;
    }
}
