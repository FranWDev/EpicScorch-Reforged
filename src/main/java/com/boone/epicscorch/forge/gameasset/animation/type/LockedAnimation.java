package com.boone.epicscorch.forge.gameasset.animation.type;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class LockedAnimation extends StaticAnimation {
   public LockedAnimation(float transitionTime, boolean repeatPlay, AnimationAccessor<? extends LockedAnimation> accessor, String path, AssetAccessor<? extends Armature> armature) {
      super(transitionTime, repeatPlay, accessor, armature);

      this.addProperty(
         StaticAnimationProperty.PLAY_SPEED_MODIFIER,
         (DynamicAnimation animation, LivingEntityPatch<?> entitypatch, float speed, float prevElapsedTime, float elapsedTime) -> {
            if (animation.isLinkAnimation()) return 1.0F;

            float totalTime = animation.getTotalTime();
            if (elapsedTime >= totalTime - 0.1F) {
               // Only allow looping if Scorched Guns explicitly says we are in the middle of a reload cycle.
               // Otherwise, freeze at the end to prevent "infinite" loops after the reload is done.
               if (isLoopingReload(entitypatch)) {
                  return 1.0F;
               } else {
                  return 0.0F; // Freeze at the end
               }
            }
            return 1.0F;
         }
      );
   }

   private boolean isLoopingReload(LivingEntityPatch<?> entitypatch) {
      LivingEntity entity = entitypatch.getOriginal();
      ItemStack stack = entity.getMainHandItem();
      if (!(stack.getItem() instanceof GunItem)) return false;

      CompoundTag tag = stack.getOrCreateTag();
      String reloadState = tag.getString("scguns:ReloadState");
      
      // "RELOAD" is the state used for multi-bullet looping reloads in Scorched Guns.
      // Other states like "START", "STOP" or "NONE" should not loop the animation.
      return reloadState.equals("RELOAD");
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
