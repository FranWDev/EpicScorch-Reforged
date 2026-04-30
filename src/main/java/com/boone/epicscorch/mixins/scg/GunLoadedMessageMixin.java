package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageGunLoaded;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Blocks ammo syncing when in combat (dodge/roll).
 * Intercepts right before C2SMessageGunLoaded is sent to prevent increaseAmmo() on server.
 */
@Mixin(PacketHandler.class)
public class GunLoadedMessageMixin {

    // This is a placeholder. The actual implementation requires hooking at the right point.
    // Will be handled by preventing the packet send in ReloadHandler via a separate approach.
}


