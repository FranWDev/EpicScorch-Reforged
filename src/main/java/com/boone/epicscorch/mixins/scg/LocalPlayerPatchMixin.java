package com.boone.epicscorch.mixins.scg;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Prevents Epic Fight from playing attack animations when holding guns.
 */
@Mixin(value = LocalPlayerPatch.class, remap = false)
public abstract class LocalPlayerPatchMixin {

    @Inject(method = "canPlayAttackAnimation", at = @At("HEAD"), cancellable = true)
    private void canPlayAttackAnimation(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayerPatch patch = (LocalPlayerPatch) (Object) this;
        ItemStack stack = patch.getOriginal().getMainHandItem();
        
        if (stack.getItem() instanceof GunItem || AimingHandler.get().isAiming()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "playAnimationSynchronized", at = @At("HEAD"), cancellable = true)
    private void epicscorch$blockPunchAnimations(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, CallbackInfo ci) {
        LocalPlayerPatch patch = (LocalPlayerPatch) (Object) this;
        ItemStack stack = patch.getOriginal().getMainHandItem();

        if (stack.getItem() instanceof GunItem) {
            StaticAnimation anim = animation.get();
            if (anim instanceof AttackAnimation) {
                ci.cancel();
            }
        }
    }
}
