package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.client.RenderContextHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = RenderItemBase.class, remap = false)
public class RenderItemBaseEntityTrackerMixin {

    @Inject(
        method = "renderItemInHand(Lnet/minecraft/world/item/ItemStack;Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lnet/minecraft/world/InteractionHand;[Lyesman/epicfight/api/utils/math/OpenMatrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IF)V",
        at = @At("HEAD")
    )
    private void epicscorch$captureEpicFightEntity(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks, CallbackInfo ci) {
        if (entitypatch != null && entitypatch.getOriginal() instanceof net.minecraft.world.entity.LivingEntity living) {
            RenderContextHolder.setRenderingEntity(living);
        }
    }

    @Inject(
        method = "renderItemInHand(Lnet/minecraft/world/item/ItemStack;Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lnet/minecraft/world/InteractionHand;[Lyesman/epicfight/api/utils/math/OpenMatrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IF)V",
        at = @At("RETURN")
    )
    private void epicscorch$clearEpicFightEntity(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks, CallbackInfo ci) {
        RenderContextHolder.clear();
    }
}
