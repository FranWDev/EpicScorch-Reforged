package com.boone.epicscorch.forge.gameasset.animation.type;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class LockedAnimation extends StaticAnimation {
   public LockedAnimation(boolean repeatPlay, AnimationAccessor<? extends LockedAnimation> accessor, String path, AssetAccessor<? extends Armature> armature) {
      super(repeatPlay, accessor, armature);
      this.resourceLocation = accessor.registryName(); // In LockedAnimation it seems it was intended to use the path as registry name
   }

   @Override
   public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entityPatch, float time, float partialTicks) {
      super.modifyPose(animation, pose, entityPatch, time, partialTicks);
      if (!entityPatch.isFirstPerson()) {
         JointTransform headTransform = pose.orElseEmpty("Head");
         LivingEntity entity = entityPatch.getOriginal();
         float bodyYaw = entity.yBodyRot;
         float headYaw = entity.getYRot();
         float yawDifference = Mth.wrapDegrees(bodyYaw - headYaw);
         Quaternionf rotationQuaternion = new Quaternionf().rotationY((float)Math.toRadians(yawDifference));
         headTransform.frontResult(JointTransform.rotation(rotationQuaternion), OpenMatrix4f::mul);
      }
   }
}
