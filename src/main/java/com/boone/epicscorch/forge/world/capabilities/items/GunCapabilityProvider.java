package com.boone.epicscorch.forge.world.capabilities.items;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GrenadeItem;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class GunCapabilityProvider implements ICapabilityProvider<ItemStack, Void, CapabilityItem> {
   public static final GunCapabilityProvider INSTANCE = new GunCapabilityProvider();
   private final Map<Item, CapabilityItem> cache = new ConcurrentHashMap<>();

   public static void registerCapabilities(RegisterCapabilitiesEvent event) {
      BuiltInRegistries.ITEM.forEach(item -> {
         if (item instanceof GunItem || item instanceof GrenadeItem) {
            event.registerItem(EpicFightCapabilities.CAPABILITY_ITEM, INSTANCE, item);
         }
      });
   }

   @Override
   public @Nullable CapabilityItem getCapability(ItemStack itemStack, Void context) {
      Item item = itemStack.getItem();
      if (!(item instanceof GunItem || item instanceof GrenadeItem)) {
         return null;
      }

      return cache.computeIfAbsent(item, k -> buildCapability(itemStack, k));
   }

   private CapabilityItem buildCapability(ItemStack itemStack, Item item) {
      String gripType = "UNKNOWN";
      String registryPath = "unknown";

      ResourceLocation loc = BuiltInRegistries.ITEM.getKey(item);
      if (loc != null) {
         registryPath = loc.getPath().toLowerCase();
      }

      try {
         if (item instanceof GunItem gunItem) {
            Gun gun = gunItem.getGun();
            GripType grip = gun.getGeneral().getBaseGripType();
            if (grip == null) {
               grip = gun.getGeneral().getGripType(itemStack);
            }
            if (grip != null) {
               gripType = grip.id().getPath().toUpperCase();
            }
         }
      } catch (Exception ignored) {
      }

      if ("UNKNOWN".equals(gripType)) {
         if (registryPath.contains("pistol") || registryPath.contains("revolver") || registryPath.contains("hand_cannon")) {
            gripType = "ONE_HANDED";
         } else if (registryPath.contains("rifle") || registryPath.contains("musket") || registryPath.contains("carbine") || registryPath.contains("carabine") || registryPath.contains("shotgun") || registryPath.contains("blunderbuss") || registryPath.contains("longarm")) {
            gripType = "TWO_HANDED_SHOTGUN";
         } else if (registryPath.contains("bazooka") || registryPath.contains("rocket") || registryPath.contains("launcher")) {
            gripType = "BAZOOKA";
         } else if (registryPath.contains("minigun") || registryPath.contains("mini_gun") || registryPath.contains("gatling")) {
            gripType = "MINI_GUN";
         } else {
            gripType = "TWO_HANDED_SHOTGUN";
         }
      }

      return switch (gripType) {
         case "ONE_HANDED", "ONE_HANDED_2", "DUAL_WIELD" -> GunCapabilityPresets.PISTOL.apply(item).build();
         case "TWO_HANDED", "TWO_HANDED_SHOTGUN", "TWO_HANDED_SMG", "TWO_HANDED_RELOADABLE" -> GunCapabilityPresets.RIFLE.apply(item).build();
         case "BAZOOKA" -> GunCapabilityPresets.BAZOOKA.apply(item).build();
         case "MINI_GUN", "MINI_GUN_2", "MINI_GUN_3", "MINI_GUN_4", "MINI_GUN_5" -> GunCapabilityPresets.MINI_GUN.apply(item).build();
         default -> {
            if (item instanceof GrenadeItem grenadeItem) {
               yield GunCapabilityPresets.GRENADE.apply(grenadeItem).build();
            }
            yield GunCapabilityPresets.RIFLE.apply(item).build();
         }
      };
   }
}
