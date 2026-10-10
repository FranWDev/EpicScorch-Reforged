package com.boone.epicscorch.forge.world.capabilities.items;

import com.boone.epicscorch.forge.gameasset.animation.Animations;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Builder;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.RangedWeaponCapability;

public class GunCapabilityPresets {
   public static final Function<Item, Builder<?>> PISTOL = item -> RangedWeaponCapability.builder()
      .addAnimationsModifier(LivingMotions.IDLE, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.KNEEL, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.WALK, Animations.BIPED_WALK_PISTOL)
      .addAnimationsModifier(LivingMotions.RUN, Animations.BIPED_RUN_PISTOL)
      .addAnimationsModifier(LivingMotions.SNEAK, Animations.BIPED_SNEAK_PISTOL)
      .addAnimationsModifier(LivingMotions.SWIM, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.FLOAT, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.FLY, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.MOUNT, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.SIT, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.CREATIVE_FLY, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.CREATIVE_IDLE, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.FALL, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.JUMP, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.RELOAD, Animations.BIPED_PISTOL_RELOAD)
      .addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_HOLD_PISTOL)
      .addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_PISTOL_AIM)
      .category(WeaponCategories.RANGED);

   public static final Function<Item, Builder<?>> RIFLE = item -> RangedWeaponCapability.builder()
      .addAnimationsModifier(LivingMotions.IDLE, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.KNEEL, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.WALK, Animations.BIPED_WALK_RIFLE)
      .addAnimationsModifier(LivingMotions.RUN, Animations.BIPED_RUN_RIFLE)
      .addAnimationsModifier(LivingMotions.SNEAK, Animations.BIPED_SNEAK_RIFLE)
      .addAnimationsModifier(LivingMotions.SWIM, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.FLOAT, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.FLY, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.MOUNT, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.SIT, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.CREATIVE_FLY, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.CREATIVE_IDLE, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.FALL, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.JUMP, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.RELOAD, Animations.BIPED_RIFLE_RELOAD)
      .addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_HOLD_RIFLE)
      .addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_RIFLE_AIM)
      .category(WeaponCategories.RANGED);

   public static final Function<Item, Builder<?>> BAZOOKA = item -> RangedWeaponCapability.builder()
      .addAnimationsModifier(LivingMotions.IDLE, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.KNEEL, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.WALK, Animations.BIPED_WALK_BAZOOKA)
      .addAnimationsModifier(LivingMotions.RUN, Animations.BIPED_RUN_BAZOOKA)
      .addAnimationsModifier(LivingMotions.SNEAK, Animations.BIPED_SNEAK_BAZOOKA)
      .addAnimationsModifier(LivingMotions.SWIM, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.FLOAT, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.FLY, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.MOUNT, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.SIT, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.CREATIVE_FLY, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.CREATIVE_IDLE, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.FALL, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.JUMP, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.RELOAD, Animations.BIPED_BAZOOKA_RELOAD)
      .addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_HOLD_BAZOOKA)
      .addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_BAZOOKA_AIM)
      .category(WeaponCategories.RANGED);

   public static final Function<Item, Builder<?>> MINI_GUN = item -> RangedWeaponCapability.builder()
      .addAnimationsModifier(LivingMotions.IDLE, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.KNEEL, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.WALK, Animations.BIPED_WALK_MINI_GUN)
      .addAnimationsModifier(LivingMotions.RUN, Animations.BIPED_RUN_MINI_GUN)
      .addAnimationsModifier(LivingMotions.SNEAK, Animations.BIPED_SNEAK_MINI_GUN)
      .addAnimationsModifier(LivingMotions.SWIM, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.FLOAT, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.FLY, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.MOUNT, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.SIT, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.CREATIVE_FLY, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.CREATIVE_IDLE, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.FALL, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.JUMP, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.RELOAD, Animations.BIPED_MINI_GUN_RELOAD)
      .addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_HOLD_MINI_GUN)
      .addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_HOLD_MINI_GUN)
      .category(WeaponCategories.RANGED);

   public static final Function<Item, Builder<?>> GRENADE = item -> RangedWeaponCapability.builder()
      .addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_GRENADE_ARM)
      .addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_GRENADE_THROW)
      .category(WeaponCategories.RANGED);

   public static void register() {
      EpicFightEventHooks.Registry.WEAPON_CAPABILITY_PRESET.registerEvent(GunCapabilityPresets::onWeaponCapabilityPresetRegister);
   }

   public static void onWeaponCapabilityPresetRegister(WeaponCapabilityPresetRegistryEvent event) {
      event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath("epicfight", "pistol"), PISTOL);
      event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath("epicfight", "rifle"), RIFLE);
      event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath("epicfight", "bazooka"), BAZOOKA);
      event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath("epicfight", "minigun"), MINI_GUN);
      event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath("epicfight", "grenade"), GRENADE);
   }
}

