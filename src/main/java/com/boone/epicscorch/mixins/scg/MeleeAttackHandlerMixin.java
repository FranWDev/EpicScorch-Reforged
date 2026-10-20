package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.forge.gameasset.animation.Animations;
import com.boone.epicscorch.forge.gameasset.animation.type.GunMeleeAnimation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.MeleeAttackHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageMeleeAttack;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mixin(value = MeleeAttackHandler.class, remap = false)
public abstract class MeleeAttackHandlerMixin {

    @Inject(method = "startBanzai(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"), cancellable = true)
    private static void epicscorch$redirectBanzaiInEpicFight(ServerPlayer player, CallbackInfo ci) {
        if (player == null) {
            return;
        }

        ServerPlayerPatch patch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (patch != null && patch.isEpicFightMode()) {
            MeleeAttackHandler.performMeleeAttack(player);
            ci.cancel();
        }
    }

    @Inject(method = "performMeleeAttack(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"), cancellable = true)
    private static void epicscorch$interceptMeleeAttack(ServerPlayer player, CallbackInfo ci) {
        if (player == null) {
            return;
        }

        ServerPlayerPatch patch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (patch == null || !patch.isEpicFightMode()) {
            return;
        }

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }

        if (MeleeAttackHandler.isMeleeOnCooldown(player, heldItem)) {
            ci.cancel();
            return;
        }

        if (patch.getEntityState().inaction()) {
            ci.cancel();
            return;
        }

        // Cancel reloading state if active
        if (ModSyncedDataKeys.RELOADING.getValue(player)) {
            ModSyncedDataKeys.RELOADING.setValue(player, false);
        }

        // Apply weapon cooldown according to Scorched Guns configuration
        MeleeAttackHandler.setMeleeCooldown(player, heldItem, gunItem);

        // Notify client to update cooldown HUD
        PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageMeleeAttack(heldItem));

        // Play sweep sound
        player.level().playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP,
            SoundSource.PLAYERS,
            1.0F,
            1.0F
        );

        // 50/50 Epic Fight animation
        AnimationAccessor<GunMeleeAnimation> anim = player.getRandom().nextBoolean()
            ? Animations.BIPED_GUN_MELEE1
            : Animations.BIPED_GUN_MELEE2;

        patch.playAnimationSynchronized(anim, 0.05F);

        ci.cancel();
    }
}
