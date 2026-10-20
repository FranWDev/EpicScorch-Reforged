package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.client.RenderContextHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import top.ribs.scguns.client.GunItemStackRenderer;
import top.ribs.scguns.client.handler.GunRenderingHandler;

@Mixin(value = GunItemStackRenderer.class, remap = false)
public class GunItemStackRendererMixin extends BlockEntityWithoutLevelRenderer {

    public GunItemStackRendererMixin(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }

    @Overwrite
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource source, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        LivingEntity livingEntity = RenderContextHolder.getRenderingEntity();
        if (livingEntity == null) {
            livingEntity = mc.player;
        }

        poseStack.popPose();
        poseStack.pushPose();

        if (context == ItemDisplayContext.GROUND) {
            GunRenderingHandler.get().applyWeaponScale(stack, poseStack);
        }

        float deltaTicks = mc.getTimer().getGameTimeDeltaTicks();
        GunRenderingHandler.get().renderWeapon(livingEntity, stack, context, poseStack, source, light, deltaTicks);

        poseStack.popPose();
        poseStack.pushPose();
    }
}
