package com.boone.epicscorch.forge.gameasset.animation.type;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.AimAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GunAimAnimation extends AimAnimation {
   public GunAimAnimation(boolean repeatPlay, AnimationAccessor<? extends GunAimAnimation> accessor, String path1, String path2, String path3, String path4, AssetAccessor<? extends Armature> armature) {
      super(repeatPlay, accessor, path1, path2, path3, path4, armature);
   }

   @Override
   public void tick(LivingEntityPatch<?> entitypatch) {
      super.tick(entitypatch);
      
      if (entitypatch.isLogicalClient()) {
         ClientAnimator animator = entitypatch.getClientAnimator();
         Layer layer = animator.getCompositeLayer(this.getPriority());
         AnimationPlayer player = layer.animationPlayer;
         
         if (this.isRepeat() && player.getElapsedTime() >= this.getTotalTime() - 0.06F) {
            layer.resume();
         }
      }
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
            qHead.rotateX((float)Math.toRadians(-80.0));
         }

         head.frontResult(JointTransform.rotation(qHead), OpenMatrix4f::mul);
         chest.frontResult(JointTransform.rotation(qBody), OpenMatrix4f::mul);
      }
   }
}
