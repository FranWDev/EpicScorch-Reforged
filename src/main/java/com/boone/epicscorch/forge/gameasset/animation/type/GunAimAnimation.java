package com.boone.epicscorch.forge.gameasset.animation.type;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.AimAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GunAimAnimation extends AimAnimation {
   private float totalTime;

   public GunAimAnimation(boolean repeatPlay, String path1, String path2, String path3, String path4, Armature armature) {
      super(repeatPlay, path1, path2, path3, path4, armature);
   }

   public void tick(LivingEntityPatch<?> entitypatch) {
      super.tick(entitypatch);
      ClientAnimator animator = entitypatch.getClientAnimator();
      Layer layer = animator.getCompositeLayer(this.getPriority());
      AnimationPlayer player = layer.animationPlayer;
      if (this.isRepeat() && player.getElapsedTime() >= this.totalTime - 0.06F) {
         layer.resume();
      }
   }

   public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
      if (!entitypatch.isFirstPerson()) {
         JointTransform chest = pose.getOrDefaultTransform("Chest");
         JointTransform head = pose.getOrDefaultTransform("Head");
         float maxRotation = 90.0F;
         LivingEntity entity = (LivingEntity)entitypatch.getOriginal();
         float headPitch = Math.abs(entity.m_146909_());
         float ratio = (maxRotation - headPitch) / maxRotation;
         float bodyYaw = entity.f_20883_;
         float headYaw = entity.f_20885_;
         Quaternionf qHead = new Quaternionf().rotationY(Mth.m_14177_(bodyYaw - headYaw) * ratio * (float) (Math.PI / 180.0));
         Quaternionf qBody = new Quaternionf().rotationY(Mth.m_14177_(headYaw - bodyYaw) * ratio * (float) (Math.PI / 180.0));
         if (entitypatch.currentLivingMotion == LivingMotions.SWIM) {
            qHead.rotateX((float)Math.toRadians(-80.0));
         }

         new OpenMatrix4f();
         head.frontResult(JointTransform.getRotation(qHead), OpenMatrix4f::mul);
         chest.frontResult(JointTransform.getRotation(qBody), OpenMatrix4f::mul);
      }
   }
}
