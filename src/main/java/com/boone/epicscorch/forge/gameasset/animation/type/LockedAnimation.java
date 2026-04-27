package com.boone.epicscorch.forge.gameasset.animation.type;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class LockedAnimation extends StaticAnimation {
   public LockedAnimation(boolean repeatPlay, String path, Armature armature) {
      super(repeatPlay, path, armature);
   }

   public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entityPatch, float time, float partialTicks) {
      super.modifyPose(animation, pose, entityPatch, time, partialTicks);
      if (!entityPatch.isFirstPerson()) {
         JointTransform headTransform = pose.getOrDefaultTransform("Head");
         LivingEntity entity = (LivingEntity)entityPatch.getOriginal();
         float bodyYaw = entity.f_20883_;
         float headYaw = entity.m_6080_();
         float yawDifference = Mth.m_14177_(bodyYaw - headYaw);
         Quaternionf rotationQuaternion = new Quaternionf().rotationY((float)Math.toRadians(yawDifference));
         headTransform.frontResult(JointTransform.getRotation(rotationQuaternion), OpenMatrix4f::mul);
      }
   }
}
