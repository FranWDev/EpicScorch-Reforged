package com.boone.epicscorch.forge.gameasset.animation;

import com.boone.epicscorch.forge.gameasset.animation.type.GunAimAnimation;
import com.boone.epicscorch.forge.gameasset.animation.type.GunMoveAnimation;
import com.boone.epicscorch.forge.gameasset.animation.type.LockedAnimation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import yesman.epicfight.api.animation.types.ReboundAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.forgeevent.AnimationRegistryEvent;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.HumanoidArmature;

@EventBusSubscriber(modid = "epicscorch", bus = Bus.MOD)
public class Animations {
   public static StaticAnimation BIPED_HOLD_PISTOL;
   public static StaticAnimation BIPED_WALK_PISTOL;
   public static StaticAnimation BIPED_RUN_PISTOL;
   public static StaticAnimation BIPED_SNEAK_PISTOL;
   public static StaticAnimation BIPED_PISTOL_AIM;
   public static StaticAnimation BIPED_PISTOL_RELOAD;
   public static StaticAnimation BIPED_HOLD_RIFLE;
   public static StaticAnimation BIPED_WALK_RIFLE;
   public static StaticAnimation BIPED_RUN_RIFLE;
   public static StaticAnimation BIPED_SNEAK_RIFLE;
   public static StaticAnimation BIPED_RIFLE_AIM;
   public static StaticAnimation BIPED_RIFLE_RELOAD;
   public static StaticAnimation BIPED_HOLD_BAZOOKA;
   public static StaticAnimation BIPED_WALK_BAZOOKA;
   public static StaticAnimation BIPED_RUN_BAZOOKA;
   public static StaticAnimation BIPED_SNEAK_BAZOOKA;
   public static StaticAnimation BIPED_BAZOOKA_AIM;
   public static StaticAnimation BIPED_BAZOOKA_RELOAD;
   public static StaticAnimation BIPED_HOLD_MINI_GUN;
   public static StaticAnimation BIPED_WALK_MINI_GUN;
   public static StaticAnimation BIPED_RUN_MINI_GUN;
   public static StaticAnimation BIPED_SNEAK_MINI_GUN;
   public static StaticAnimation BIPED_MINI_GUN_AIM;
   public static StaticAnimation BIPED_MINI_GUN_RELOAD;
   public static StaticAnimation BIPED_GRENADE_ARM;
   public static StaticAnimation BIPED_GRENADE_THROW;

   @SubscribeEvent
   public static void registerAnimations(AnimationRegistryEvent event) {
      event.getRegistryMap().put("epicscorch", Animations::buildAnimations);
   }

   private static void buildAnimations() {
      HumanoidArmature bipedArmature = Armatures.BIPED;
      BIPED_HOLD_PISTOL = new GunAimAnimation(
         true, "biped/living/hold_pistol_mid", "biped/living/hold_pistol_up", "biped/living/hold_pistol_down", "biped/living/hold_pistol_up", bipedArmature
      );
      BIPED_WALK_PISTOL = new GunMoveAnimation(
         true, "biped/living/walk_pistol_mid", "biped/living/hold_pistol_up", "biped/living/hold_pistol_down", "biped/living/hold_pistol_up", bipedArmature
      );
      BIPED_RUN_PISTOL = new GunMoveAnimation(
         true, "biped/living/run_pistol_mid", "biped/living/hold_pistol_up", "biped/living/hold_pistol_down", "biped/living/hold_pistol_up", bipedArmature
      );
      BIPED_SNEAK_PISTOL = new GunMoveAnimation(
         true, "biped/living/sneak_pistol_mid", "biped/living/hold_pistol_up", "biped/living/hold_pistol_down", "biped/living/hold_pistol_up", bipedArmature
      );
      BIPED_PISTOL_AIM = new GunAimAnimation(
         true, "biped/combat/pistol_aim_mid", "biped/combat/pistol_aim_up", "biped/combat/pistol_aim_down", "biped/combat/pistol_aim_up", bipedArmature
      );
      BIPED_PISTOL_RELOAD = new LockedAnimation(true, "biped/combat/pistol_reload", bipedArmature);
      BIPED_HOLD_RIFLE = new GunAimAnimation(
         true, "biped/living/hold_rifle_mid", "biped/living/hold_rifle_up", "biped/living/hold_rifle_down", "biped/living/hold_rifle_up", bipedArmature
      );
      BIPED_WALK_RIFLE = new GunMoveAnimation(
         true, "biped/living/walk_rifle_mid", "biped/living/hold_rifle_up", "biped/living/hold_rifle_down", "biped/living/hold_rifle_up", bipedArmature
      );
      BIPED_RUN_RIFLE = new GunMoveAnimation(
         true, "biped/living/run_rifle_mid", "biped/living/hold_rifle_up", "biped/living/hold_rifle_down", "biped/living/hold_rifle_up", bipedArmature
      );
      BIPED_SNEAK_RIFLE = new GunMoveAnimation(
         true, "biped/living/sneak_rifle_mid", "biped/living/hold_rifle_up", "biped/living/hold_rifle_down", "biped/living/hold_rifle_up", bipedArmature
      );
      BIPED_RIFLE_AIM = new GunAimAnimation(
         true, "biped/combat/rifle_aim_mid", "biped/combat/rifle_aim_up", "biped/combat/rifle_aim_down", "biped/combat/rifle_aim_up", bipedArmature
      );
      BIPED_RIFLE_RELOAD = new LockedAnimation(true, "biped/combat/rifle_reload", bipedArmature);
      BIPED_HOLD_BAZOOKA = new GunAimAnimation(
         true, "biped/living/hold_bazooka_mid", "biped/living/hold_bazooka_up", "biped/living/hold_bazooka_down", "biped/living/hold_bazooka_up", bipedArmature
      );
      BIPED_BAZOOKA_AIM = new GunAimAnimation(
         true, "biped/combat/bazooka_aim_mid", "biped/combat/bazooka_aim_up", "biped/combat/bazooka_aim_down", "biped/combat/bazooka_aim_up", bipedArmature
      );
      BIPED_BAZOOKA_RELOAD = new LockedAnimation(true, "biped/combat/bazooka_reload", bipedArmature);
      BIPED_HOLD_MINI_GUN = new GunAimAnimation(
         true,
         "biped/living/hold_mini_gun_mid",
         "biped/living/hold_mini_gun_up",
         "biped/living/hold_mini_gun_down",
         "biped/living/hold_mini_gun_up",
         bipedArmature
      );
      BIPED_GRENADE_ARM = new GunAimAnimation(
         false, "biped/combat/grenade_arm", "biped/combat/grenade_arm", "biped/combat/grenade_arm", "biped/combat/grenade_arm", bipedArmature
      );
      BIPED_GRENADE_THROW = new ReboundAnimation(
         false, "biped/combat/grenade_throw", "biped/combat/grenade_throw", "biped/combat/grenade_throw", "biped/combat/grenade_throw", bipedArmature
      );
   }
}
