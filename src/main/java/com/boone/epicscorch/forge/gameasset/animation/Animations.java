package com.boone.epicscorch.forge.gameasset.animation;

import com.boone.epicscorch.forge.gameasset.animation.type.GunAimAnimation;
import com.boone.epicscorch.forge.gameasset.animation.type.GunMoveAnimation;
import com.boone.epicscorch.forge.gameasset.animation.type.LockedAnimation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.animation.types.ReboundAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;

@EventBusSubscriber(modid = "epicscorch", bus = Bus.MOD)
public class Animations {
   public static AnimationAccessor<GunAimAnimation> BIPED_HOLD_PISTOL;
   public static AnimationAccessor<GunMoveAnimation> BIPED_WALK_PISTOL;
   public static AnimationAccessor<GunMoveAnimation> BIPED_RUN_PISTOL;
   public static AnimationAccessor<GunMoveAnimation> BIPED_SNEAK_PISTOL;
   public static AnimationAccessor<GunAimAnimation> BIPED_PISTOL_AIM;
   public static AnimationAccessor<LockedAnimation> BIPED_PISTOL_RELOAD;
   public static AnimationAccessor<GunAimAnimation> BIPED_HOLD_RIFLE;
   public static AnimationAccessor<GunMoveAnimation> BIPED_WALK_RIFLE;
   public static AnimationAccessor<GunMoveAnimation> BIPED_RUN_RIFLE;
   public static AnimationAccessor<GunMoveAnimation> BIPED_SNEAK_RIFLE;
   public static AnimationAccessor<GunAimAnimation> BIPED_RIFLE_AIM;
   public static AnimationAccessor<LockedAnimation> BIPED_RIFLE_RELOAD;
   public static AnimationAccessor<GunAimAnimation> BIPED_HOLD_BAZOOKA;
   public static AnimationAccessor<GunMoveAnimation> BIPED_WALK_BAZOOKA;
   public static AnimationAccessor<GunMoveAnimation> BIPED_RUN_BAZOOKA;
   public static AnimationAccessor<GunMoveAnimation> BIPED_SNEAK_BAZOOKA;
   public static AnimationAccessor<GunAimAnimation> BIPED_BAZOOKA_AIM;
   public static AnimationAccessor<LockedAnimation> BIPED_BAZOOKA_RELOAD;
   public static AnimationAccessor<GunAimAnimation> BIPED_HOLD_MINI_GUN;
   public static AnimationAccessor<GunMoveAnimation> BIPED_WALK_MINI_GUN;
   public static AnimationAccessor<GunMoveAnimation> BIPED_RUN_MINI_GUN;
   public static AnimationAccessor<GunMoveAnimation> BIPED_SNEAK_MINI_GUN;
   public static AnimationAccessor<LockedAnimation> BIPED_MINI_GUN_RELOAD;
   public static AnimationAccessor<GunAimAnimation> BIPED_GRENADE_ARM;
   public static AnimationAccessor<ReboundAnimation> BIPED_GRENADE_THROW;

   @SubscribeEvent
   public static void registerAnimations(AnimationRegistryEvent event) {
      event.newBuilder("epicscorch", Animations::buildAnimations);
   }

   private static void buildAnimations(AnimationBuilder builder) {
      AssetAccessor<HumanoidArmature> bipedArmature = Armatures.BIPED;
      
      BIPED_HOLD_PISTOL = builder.nextAccessor("biped/living/hold_pistol", (accessor) -> new GunAimAnimation(
         0.05f, true, accessor, "biped/living/hold_pistol_mid", "biped/living/hold_pistol_up", "biped/living/hold_pistol_down", "biped/living/hold_pistol_up", bipedArmature
      ));
      BIPED_WALK_PISTOL = builder.nextAccessor("biped/living/walk_pistol", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/walk_pistol_mid", "biped/living/hold_pistol_up", "biped/living/hold_pistol_down", "biped/living/hold_pistol_up", bipedArmature
      ));
      BIPED_RUN_PISTOL = builder.nextAccessor("biped/living/run_pistol", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/run_pistol_mid", "biped/living/hold_pistol_up", "biped/living/hold_pistol_down", "biped/living/hold_pistol_up", bipedArmature
      ));
      BIPED_SNEAK_PISTOL = builder.nextAccessor("biped/living/sneak_pistol", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/sneak_pistol_mid", "biped/living/hold_pistol_up", "biped/living/hold_pistol_down", "biped/living/hold_pistol_up", bipedArmature
      ));
      BIPED_PISTOL_AIM = builder.nextAccessor("biped/combat/pistol_aim", (accessor) -> new GunAimAnimation(
         0.05f, true, accessor, "biped/combat/pistol_aim_mid", "biped/combat/pistol_aim_up", "biped/combat/pistol_aim_down", "biped/combat/pistol_aim_up", bipedArmature
      ));
      BIPED_PISTOL_RELOAD = builder.nextAccessor("biped/combat/pistol_reload", (accessor) -> new LockedAnimation(0.05f, true, accessor, "biped/combat/pistol_reload", bipedArmature));
      
      BIPED_HOLD_RIFLE = builder.nextAccessor("biped/living/hold_rifle", (accessor) -> new GunAimAnimation(
         0.05f, true, accessor, "biped/living/hold_rifle_mid", "biped/living/hold_rifle_up", "biped/living/hold_rifle_down", "biped/living/hold_rifle_up", bipedArmature
      ));
      BIPED_WALK_RIFLE = builder.nextAccessor("biped/living/walk_rifle", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/walk_rifle_mid", "biped/living/hold_rifle_up", "biped/living/hold_rifle_down", "biped/living/hold_rifle_up", bipedArmature
      ));
      BIPED_RUN_RIFLE = builder.nextAccessor("biped/living/run_rifle", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/run_rifle_mid", "biped/living/hold_rifle_up", "biped/living/hold_rifle_down", "biped/living/hold_rifle_up", bipedArmature
      ));
      BIPED_SNEAK_RIFLE = builder.nextAccessor("biped/living/sneak_rifle", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/sneak_rifle_mid", "biped/living/hold_rifle_up", "biped/living/hold_rifle_down", "biped/living/hold_rifle_up", bipedArmature
      ));
      BIPED_RIFLE_AIM = builder.nextAccessor("biped/combat/rifle_aim", (accessor) -> new GunAimAnimation(
         0.05f, true, accessor, "biped/combat/rifle_aim_mid", "biped/combat/rifle_aim_up", "biped/combat/rifle_aim_down", "biped/combat/rifle_aim_up", bipedArmature
      ));
      BIPED_RIFLE_RELOAD = builder.nextAccessor("biped/combat/rifle_reload", (accessor) -> new LockedAnimation(0.05f, true, accessor, "biped/combat/rifle_reload", bipedArmature));
      
      BIPED_HOLD_BAZOOKA = builder.nextAccessor("biped/living/hold_bazooka", (accessor) -> new GunAimAnimation(
         0.05f, true, accessor, "biped/living/hold_bazooka_mid", "biped/living/hold_bazooka_up", "biped/living/hold_bazooka_down", "biped/living/hold_bazooka_up", bipedArmature
      ));
      BIPED_WALK_BAZOOKA = builder.nextAccessor("biped/living/walk_bazooka", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/walk_bazooka_mid", "biped/living/hold_bazooka_up", "biped/living/hold_bazooka_down", "biped/living/hold_bazooka_up", bipedArmature
      ));
      BIPED_RUN_BAZOOKA = builder.nextAccessor("biped/living/run_bazooka", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/run_bazooka_mid", "biped/living/hold_bazooka_up", "biped/living/hold_bazooka_down", "biped/living/hold_bazooka_up", bipedArmature
      ));
      BIPED_SNEAK_BAZOOKA = builder.nextAccessor("biped/living/sneak_bazooka", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/sneak_bazooka_mid", "biped/living/hold_bazooka_up", "biped/living/hold_bazooka_down", "biped/living/hold_bazooka_up", bipedArmature
      ));
      BIPED_BAZOOKA_AIM = builder.nextAccessor("biped/combat/bazooka_aim", (accessor) -> new GunAimAnimation(
         0.05f, true, accessor, "biped/combat/bazooka_aim_mid", "biped/combat/bazooka_aim_up", "biped/combat/bazooka_aim_down", "biped/combat/bazooka_aim_up", bipedArmature
      ));
      BIPED_BAZOOKA_RELOAD = builder.nextAccessor("biped/combat/bazooka_reload", (accessor) -> new LockedAnimation(0.05f, true, accessor, "biped/combat/bazooka_reload", bipedArmature));
      
      BIPED_HOLD_MINI_GUN = builder.nextAccessor("biped/living/hold_mini_gun", (accessor) -> new GunAimAnimation(
         0.05f, true, accessor, "biped/living/hold_mini_gun_mid", "biped/living/hold_mini_gun_up", "biped/living/hold_mini_gun_down", "biped/living/hold_mini_gun_up", bipedArmature
      ));
      BIPED_WALK_MINI_GUN = builder.nextAccessor("biped/living/walk_mini_gun", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/walk_mini_gun_mid", "biped/living/hold_mini_gun_up", "biped/living/hold_mini_gun_down", "biped/living/hold_mini_gun_up", bipedArmature
      ));
      BIPED_RUN_MINI_GUN = builder.nextAccessor("biped/living/run_mini_gun", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/run_mini_gun_mid", "biped/living/hold_mini_gun_up", "biped/living/hold_mini_gun_down", "biped/living/hold_mini_gun_up", bipedArmature
      ));
      BIPED_SNEAK_MINI_GUN = builder.nextAccessor("biped/living/sneak_mini_gun", (accessor) -> new GunMoveAnimation(
         0.05f, true, accessor, "biped/living/sneak_mini_gun_mid", "biped/living/hold_mini_gun_up", "biped/living/hold_mini_gun_down", "biped/living/hold_mini_gun_up", bipedArmature
      ));
      BIPED_MINI_GUN_RELOAD = builder.nextAccessor("biped/combat/mini_gun_reload", (accessor) -> new LockedAnimation(0.05f, true, accessor, "biped/combat/mini_gun_reload", bipedArmature));
      
      BIPED_GRENADE_ARM = builder.nextAccessor("biped/combat/grenade_arm", (accessor) -> new GunAimAnimation(
         0.05f, false, accessor, "biped/combat/grenade_arm", "biped/combat/grenade_arm", "biped/combat/grenade_arm", "biped/combat/grenade_arm", bipedArmature
      ));
      BIPED_GRENADE_THROW = builder.nextAccessor("biped/combat/grenade_throw", (accessor) -> new ReboundAnimation(
         0.05f, false, accessor, "biped/combat/grenade_throw", "biped/combat/grenade_throw", "biped/combat/grenade_throw", "biped/combat/grenade_throw", bipedArmature
      ));
   }
}
