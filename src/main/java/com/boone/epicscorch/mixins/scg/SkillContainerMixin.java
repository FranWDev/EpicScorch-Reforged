package com.boone.epicscorch.mixins.scg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.client.events.engine.ControlEngine;
import top.ribs.scguns.item.GunItem;
import net.minecraft.world.item.ItemStack;

@Mixin(value = SkillContainer.class, remap = false)
public abstract class SkillContainerMixin {

    @Inject(method = "sendCastRequest", at = @At("HEAD"), cancellable = true)
    private void epicscorch$blockComboSkillRequest(LocalPlayerPatch executor, ControlEngine controlEngine, CallbackInfoReturnable<SkillCastEvent> cir) {
        SkillContainer container = (SkillContainer) (Object) this;
        
        if (container.getSlot() == SkillSlots.BASIC_ATTACK) {
            ItemStack stack = executor.getOriginal().getMainHandItem();
            if (stack.getItem() instanceof GunItem) {

                cir.setReturnValue(new SkillCastEvent(executor, container, null));
            }
        }
    }
}
