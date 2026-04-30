package com.boone.epicscorch.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Forces reload off every frame when player is dodging/rolling.
 * Clears NBT reload flags to stop animations that persist after dodge starts.
 */
@Mod.EventBusSubscriber(modid = "epicscorch", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DodgeBlocker {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null)
            return;

        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch == null || !playerPatch.isEpicFightMode())
            return;

        // If not in combat action, nothing to block
        EntityState state = playerPatch.getEntityState();
        if (!state.inaction())
            return;

        // Stop reload via ReloadHandler
        ReloadHandler reloadHandler = ReloadHandler.get();
        if (reloadHandler != null) {
            reloadHandler.setReloading(false);
        }

        // Sync to false
        ModSyncedDataKeys.RELOADING.setValue(player, false);

        // Clean up NBT flags that keep reload animation alive
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean("scguns:IsReloading", false);
            tag.putString("scguns:ReloadState", "");
            tag.putBoolean("scguns:IsPlayingReloadStop", false);
            tag.putBoolean("IsReloading", false);
            tag.putBoolean("InCriticalReloadPhase", false);
            tag.putString("scguns:AnimationReloadState", "");
        }
    }
}
