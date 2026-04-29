package com.boone.epicscorch.mixins.scg;

import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;

@Mixin(value = CapabilityItem.class, remap = false)
public abstract class CapabilityItemMixin {

    @Inject(method = "getAutoAttackMotion", at = @At("HEAD"), cancellable = true)
    private void epicscorch$noPunchWithGuns(PlayerPatch<?> playerpatch, CallbackInfoReturnable<List<AnimationAccessor<? extends AttackAnimation>>> cir) {
        ItemStack stack = playerpatch.getOriginal().getMainHandItem();
        
        if (stack.getItem() instanceof GunItem) {
            System.out.println("[EpicScorch-Debug] getAutoAttackMotion returning empty list for " + stack.getItem().toString());
            cir.setReturnValue(Lists.newArrayList());
        }
    }
}
