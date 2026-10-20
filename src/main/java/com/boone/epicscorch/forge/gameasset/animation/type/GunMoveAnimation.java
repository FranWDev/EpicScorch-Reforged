package com.boone.epicscorch.forge.gameasset.animation.type;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AimAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GunMoveAnimation extends GunAimAnimation {
   public GunMoveAnimation(float transitionTime, boolean repeatPlay, AnimationAccessor<? extends AimAnimation> accessor, String path1, String path2, String path3, String path4, AssetAccessor<? extends Armature> armature) {
      super(transitionTime, repeatPlay, accessor, path1, path2, path3, path4, armature);
      this.removeProperty(yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER);
   }


   @Override
   public float getPlaySpeed(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
      if (animation.isLinkAnimation()) {
         return 1.0F;
      }

      float movementSpeed = 1.0F;
      LivingEntity entity = entitypatch.getOriginal();
      float walkSpeed = entity.walkAnimation.speed();
      float walkSpeed1 = entity.walkAnimation.speed(1);
      float diff = Math.abs(walkSpeed - walkSpeed1);

      if (diff < 0.007F) {
         movementSpeed *= (walkSpeed * 1.16F);
      }

      return movementSpeed;
   }

   @Override
   public boolean canBePlayedReverse() {
      return true;
   }
}
