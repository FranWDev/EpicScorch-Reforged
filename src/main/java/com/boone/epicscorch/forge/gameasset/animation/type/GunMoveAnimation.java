package com.boone.epicscorch.forge.gameasset.animation.type;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GunMoveAnimation extends GunAimAnimation {
   public GunMoveAnimation(boolean repeatPlay, AnimationAccessor<? extends GunMoveAnimation> accessor, String path1, String path2, String path3, String path4, AssetAccessor<? extends Armature> armature) {
      super(repeatPlay, accessor, path1, path2, path3, path4, armature);
   }

   @Override
   public float getPlaySpeed(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
      float movementSpeed = 1.0F;
      LivingEntity entity = entitypatch.getOriginal();
      double prevX = entity.xo;
      double currentX = entity.getX();
      if (Math.abs(currentX - prevX) < 0.007F) {
         movementSpeed = (float)(movementSpeed * (currentX * 1.16F));
      }

      return movementSpeed;
   }

   @Override
   public boolean canBePlayedReverse() {
      return true;
   }
}
