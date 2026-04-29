package com.boone.epicscorch.forge.gameasset.animation.type;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.types.AimAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import top.ribs.scguns.client.handler.AimingHandler;

public class GunAimAnimation extends AimAnimation {
   public GunAimAnimation(float transitionTime, boolean repeatPlay, AnimationAccessor<? extends AimAnimation> accessor, String path1, String path2, String path3, String path4, AssetAccessor<? extends Armature> armature) {
      super(transitionTime, repeatPlay, accessor, path1, path2, path3, path4, armature);

      // Override the built-in PLAY_SPEED_MODIFIER to also handle SCGuns ADS
      // (the parent version only checks isUsingItem(), which SCGuns doesn't use for ADS).
      this.addProperty(
         StaticAnimationProperty.PLAY_SPEED_MODIFIER,
         (DynamicAnimation animation, LivingEntityPatch<?> entitypatch, float speed, float prevElapsedTime, float elapsedTime) -> {
            if (animation.isLinkAnimation()) {
               return 1.0F;
            }

            // Freeze at final frame for SCGuns ADS or vanilla bow/crossbow usage
            boolean isAiming = AimingHandler.get().isAiming() || entitypatch.getOriginal().isUsingItem();
            if (isAiming) {
               return (this.getTotalTime() - elapsedTime) / this.getTotalTime();
            }

            return 1.0F;
         }
      );
   }

   @Override
   public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
      if (!entitypatch.isFirstPerson()) {
         JointTransform chest = pose.orElseEmpty("Chest");
         JointTransform head = pose.orElseEmpty("Head");
         float maxRotation = 90.0F;
         LivingEntity entity = entitypatch.getOriginal();
         float headPitch = Math.abs(entity.getXRot());
         float ratio = (maxRotation - headPitch) / maxRotation;
         float bodyYaw = entity.yBodyRot;
         float headYaw = entity.yHeadRot;
         Quaternionf qHead = new Quaternionf().rotationY(Mth.wrapDegrees(bodyYaw - headYaw) * ratio * (float) (Math.PI / 180.0));
         Quaternionf qBody = new Quaternionf().rotationY(Mth.wrapDegrees(headYaw - bodyYaw) * ratio * (float) (Math.PI / 180.0));

         if (entitypatch.getCurrentLivingMotion() == LivingMotions.SWIM) {
            qHead.rotateX((float) Math.toRadians(-80.0));
         }

         head.frontResult(JointTransform.rotation(qHead), OpenMatrix4f::mul);
         chest.frontResult(JointTransform.rotation(qBody), OpenMatrix4f::mul);
      }
   }
}
