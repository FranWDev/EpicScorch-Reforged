package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = LocalPlayer.class)
public abstract class LocalPlayerSwingMixin {

    /**
     * This blocks the arm swing animation when holding a gun in Battle Mode.
     * Fixed signature: Expected (InteractionHand, CallbackInfo)
     */
    @Inject(method = {"swing", "m_6674_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void epicscorch$cancelArmSwing(InteractionHand hand, CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        ItemStack stack = player.getItemInHand(hand);
        
        if (stack.getItem() instanceof GunItem) {
            PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
            if (patch != null && patch.isEpicFightMode()) {
                ci.cancel();
            }
        }
    }
}
