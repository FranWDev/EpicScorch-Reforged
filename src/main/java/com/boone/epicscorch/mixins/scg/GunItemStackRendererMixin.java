package com.boone.epicscorch.mixins.scg;

import com.boone.epicscorch.forge.ModCapabilities;
import com.boone.epicscorch.forge.world.capabilities.items.GunOwnerCapabilityProvider;
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

@Mixin(GunItemStackRenderer.class)
public class GunItemStackRendererMixin extends BlockEntityWithoutLevelRenderer {
   public GunItemStackRendererMixin(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
      super(blockEntityRenderDispatcher, entityModelSet);
   }

   @Overwrite(remap = false)
   public void m_108829_(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource source, int light, int overlay) {
      Minecraft mc = Minecraft.m_91087_();
      GunOwnerCapabilityProvider.OwnerId ownerId = (GunOwnerCapabilityProvider.OwnerId)stack.getCapability(ModCapabilities.OWNER_ID).orElse(null);
      LivingEntity livingEntity = null;
      if (ownerId != null && mc.f_91073_.m_6815_(ownerId.value) instanceof LivingEntity living) {
         livingEntity = living;
      }

      if (livingEntity == null) {
         livingEntity = mc.f_91074_;
      }

      poseStack.m_85836_();
      if (context == ItemDisplayContext.GROUND) {
         GunRenderingHandler.get().applyWeaponScale(stack, poseStack);
      }

      GunRenderingHandler.get().renderWeapon(livingEntity, stack, context, poseStack, source, light, Minecraft.m_91087_().m_91296_());
      poseStack.m_85849_();
   }
}
