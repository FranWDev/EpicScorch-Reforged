package com.boone.epicscorch.forge.gameasset.animation.type;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GunMoveAnimation extends GunAimAnimation {
   public GunMoveAnimation(boolean repeatPlay, String path1, String path2, String path3, String path4, Armature armature) {
      super(repeatPlay, path1, path2, path3, path4, armature);
   }

   public float getPlaySpeed(LivingEntityPatch<?> entitypatch) {
      float movementSpeed = 1.0F;
      LivingEntity entity = (LivingEntity)entitypatch.getOriginal();
      double prevX = entity.f_19790_;
      double currentX = entity.m_20185_();
      if (Math.abs(currentX - prevX) < 0.007F) {
         movementSpeed = (float)(movementSpeed * (currentX * 1.16F));
      }

      return movementSpeed;
   }

   public boolean canBePlayedReverse() {
      return true;
   }
}
