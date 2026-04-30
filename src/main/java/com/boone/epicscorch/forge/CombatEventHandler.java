package com.boone.epicscorch.forge;

import com.boone.epicscorch.EpicScorch;
import com.boone.epicscorch.config.EpicScorchConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import top.ribs.scguns.util.GunModifierHelper;
import top.ribs.scguns.util.GunEnchantmentHelper;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;

/**
 * Event handler for combat mechanics integration between Epic Fight and Scorched Guns.
 * Handles stamina reduction when firing weapons.
 */
@EventBusSubscriber(modid = EpicScorch.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onGunFirePre(GunFireEvent.Pre event) {
        if (!EpicScorchConfig.ENABLE_STAMINA_REDUCTION.get()) return;

        Player player = event.getEntity();
        if (player.isCreative() || player.isSpectator()) return;

        ItemStack stack = event.getStack();
        if (stack.getItem() instanceof GunItem gunItem) {
            float requiredStamina = getRequiredStamina(player, stack, gunItem);
            if (requiredStamina <= 0.0f) return;

            EpicFightCapabilities.getUnparameterizedEntityPatch(player, PlayerPatch.class).ifPresent(playerPatch -> {
                if (!playerPatch.hasStamina(requiredStamina)) {
                    event.setCanceled(true); // Block the shot if there isn't enough stamina
                }
            });
        }
    }

    @SubscribeEvent
    public static void onGunFirePost(GunFireEvent.Post event) {
        if (event.isClient()) return; // Consume stamina only on the server to ensure synchronization
        if (!EpicScorchConfig.ENABLE_STAMINA_REDUCTION.get()) return;

        Player player = event.getEntity();
        if (player.isCreative() || player.isSpectator()) return;

        ItemStack stack = event.getStack();
        if (stack.getItem() instanceof GunItem gunItem) {
            float requiredStamina = getRequiredStamina(player, stack, gunItem);
            if (requiredStamina <= 0.0f) return;

            EpicFightCapabilities.getUnparameterizedEntityPatch(player, ServerPlayerPatch.class).ifPresent(playerPatch -> {
                float currentStamina = playerPatch.getStamina();
                playerPatch.setStamina(Math.max(0.0f, currentStamina - requiredStamina));
                playerPatch.setStaminaRegenAwaitTicks(EpicScorchConfig.STAMINA_REGEN_DELAY.get());
            });
        }
    }

    private static float getRequiredStamina(Player player, ItemStack stack, GunItem gunItem) {
        String registryName = ForgeRegistries.ITEMS.getKey(gunItem).toString();
        List<? extends String> overrides = EpicScorchConfig.WEAPON_STAMINA_OVERRIDES.get();
        
        for (String override : overrides) {
            String[] parts = override.split("=");
            if (parts.length == 2 && parts[0].trim().equals(registryName)) {
                try {
                    return Float.parseFloat(parts[1].trim());
                } catch (NumberFormatException e) {
                    return 0.0f; // Invalid override, do not consume stamina as a precaution
                }
            }
        }

        Gun modifiedGun = gunItem.getModifiedGun(stack);
        
        // Support both old and new SCG versions by checking both General and Projectile NBT
        CompoundTag generalNbt = modifiedGun.getGeneral().serializeNBT();
        CompoundTag projectileNbt = modifiedGun.getProjectile().serializeNBT();
        
        float recoilAngle = generalNbt.contains("RecoilAngle") ? generalNbt.getFloat("RecoilAngle") 
                          : projectileNbt.getFloat("RecoilAngle");
        
        // Apply recoil modifiers from attachments and enchantments
        float modifier = 1.0f - GunModifierHelper.getRecoilModifier(stack);
        modifier *= GunEnchantmentHelper.getRecoilModifier(player, stack);

        // Add ADS reduction if aiming
        float adsReduction = generalNbt.contains("RecoilAdsReduction") ? generalNbt.getFloat("RecoilAdsReduction") 
                           : projectileNbt.getFloat("RecoilAdsReduction");

        if (player.level().isClientSide) {
            if (AimingHandler.get().isAiming()) {
                modifier *= (1.0f - adsReduction);
            }
        } else {
            if (ModSyncedDataKeys.AIMING.getValue(player)) {
                modifier *= (1.0f - adsReduction);
            }
        }
        
        return recoilAngle * modifier * EpicScorchConfig.STAMINA_MULTIPLIER.get().floatValue();
    }
}
