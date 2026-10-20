package com.boone.epicscorch.forge.combat;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.util.GunModifierHelper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;

public class GunMeleeHelper {

    private static final float ENCHANTMENT_DAMAGE_SCALING_FACTOR = 0.70f;
    private static final Map<Integer, Collider> COLLIDER_CACHE = new ConcurrentHashMap<>();

    public static float getEffectiveMeleeReach(ItemStack heldItem, GunItem gunItem) {
        float reach = gunItem.getModifiedGun(heldItem).getGeneral().getMeleeReach();
        if (gunItem.hasBayonet(heldItem)) {
            reach += 0.6F;
        }
        if (gunItem.hasExtendedBarrel(heldItem)) {
            reach += 0.3F;
        }
        return reach;
    }

    public static Collider getDynamicCollider(ItemStack heldItem, GunItem gunItem) {
        float reach = getEffectiveMeleeReach(heldItem, gunItem);
        int key = Math.round(reach * 10.0F);
        return COLLIDER_CACHE.computeIfAbsent(key, k -> createColliderForReach(k / 10.0F));
    }

    private static Collider createColliderForReach(float reach) {
        double clampedReach = Math.max(1.5D, Math.min(6.0D, reach));
        // In the swing animation orientation for Tool_R:
        // Local Y projects horizontally forward towards the enemy (reach / 'largo').
        // Local Z projects vertically up/down (height / 'alto').
        // Local X is lateral thickness.
        // We scale sizeY and centerY with reach so the hitbox extends forward along the weapon reach,
        // while sizeZ remains consistent with the attack strike thickness.
        double sizeX = 0.35D + Math.min(0.15D, (clampedReach - 2.0D) * 0.04D);
        double sizeY = 0.45D + (clampedReach - 2.0D) * 0.28D;
        double sizeZ = 0.35D + Math.min(0.15D, (clampedReach - 2.0D) * 0.04D);
        double centerY = 0.20D + (clampedReach - 2.0D) * 0.38D;

        return new MultiOBBCollider(3, sizeX, sizeY, sizeZ, 0.0D, centerY, 0.0D);
    }

    public static boolean applyMeleeHit(ServerPlayer player, LivingEntity target, ItemStack heldItem, GunItem gunItem) {
        if (target == null || !target.isAlive() || target == player) {
            return false;
        }

        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float additionalDamage = GunModifierHelper.getAdditionalDamage(heldItem, true);
        DamageSource damageSource = player.serverLevel().damageSources().playerAttack(player);
        float enchantmentDamage = getEnchantmentDamageFromBayonet(player, heldItem, target, gunItem, damageSource);

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        float meleeDamage = modifiedGun.getGeneral().getMeleeDamage();

        float attackDamage = baseDamage + additionalDamage + enchantmentDamage + meleeDamage;

        if (target.hurt(damageSource, attackDamage)) {
            spawnSuccessfulHitParticles(player, target);

            player.level().playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.PLAYER_ATTACK_WEAK,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
            );

            applyKnockback(player, target, heldItem);
            applySpecialEnchantmentsFromBayonet(heldItem, target, player, gunItem);
            return true;
        }

        return false;
    }

    public static void damageWeapon(ServerPlayer player, ItemStack heldItem) {
        if (heldItem != null && !heldItem.isEmpty()) {
            GunEventBus.damageGun(heldItem, player.level(), player);
            GunEventBus.damageAttachments(heldItem, player.level(), player);
        }
    }

    private static float getEnchantmentDamageFromBayonet(ServerPlayer player, ItemStack gunStack, LivingEntity target, GunItem gunItem, DamageSource damageSource) {
        float enchantmentDamage = 0.0f;
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = gunItem.getAttachment(gunStack, type);
            if (attachmentStack.getItem() instanceof BayonetItem) {
                float damageBonus = EnchantmentHelper.modifyDamage(player.serverLevel(), attachmentStack, target, damageSource, 0.0F);
                enchantmentDamage += damageBonus * ENCHANTMENT_DAMAGE_SCALING_FACTOR;
            }
        }
        return enchantmentDamage;
    }

    private static void applySpecialEnchantmentsFromBayonet(ItemStack gunStack, LivingEntity target, Player player, GunItem gunItem) {
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = gunItem.getAttachment(gunStack, type);
            if (attachmentStack.getItem() instanceof BayonetItem) {
                applyEnchantmentEffects(Enchantments.FIRE_ASPECT, getEnchantmentLevel(attachmentStack, Enchantments.FIRE_ASPECT), target, player);
                applyEnchantmentEffects(Enchantments.KNOCKBACK, getEnchantmentLevel(attachmentStack, Enchantments.KNOCKBACK), target, player);
                applyEnchantmentEffects(Enchantments.SMITE, getEnchantmentLevel(attachmentStack, Enchantments.SMITE), target, player);
                applyEnchantmentEffects(Enchantments.BANE_OF_ARTHROPODS, getEnchantmentLevel(attachmentStack, Enchantments.BANE_OF_ARTHROPODS), target, player);
                applyEnchantmentEffects(Enchantments.SHARPNESS, getEnchantmentLevel(attachmentStack, Enchantments.SHARPNESS), target, player);
            }
        }
    }

    private static void applyEnchantmentEffects(ResourceKey<Enchantment> enchantment, int level, LivingEntity target, Player player) {
        if (level <= 0) {
            return;
        }

        if (enchantment == Enchantments.FIRE_ASPECT) {
            target.igniteForSeconds(level * 4);
        } else if (enchantment == Enchantments.KNOCKBACK) {
            Vec3 direction = target.position().subtract(player.position()).normalize();
            target.knockback(level * 0.5F, -direction.x(), -direction.z());
        }
    }

    private static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        int[] level = {0};
        EnchantmentHelper.runIterationOnItem(stack, (holder, enchantmentLevel) -> {
            if (holder.is(enchantment)) {
                level[0] = Math.max(level[0], enchantmentLevel);
            }
        });
        return level[0];
    }

    private static void applyKnockback(Player player, LivingEntity target, ItemStack stack) {
        int knockbackLevel = getEnchantmentLevel(stack, Enchantments.KNOCKBACK);
        Vec3 direction = target.position().subtract(player.position()).normalize();
        target.knockback(0.4F + (knockbackLevel * 0.5F), -direction.x(), -direction.z());
    }

    private static void spawnSuccessfulHitParticles(ServerPlayer player, LivingEntity target) {
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        ClientboundLevelParticlesPacket sweepPacket = new ClientboundLevelParticlesPacket(
            ParticleTypes.SWEEP_ATTACK,
            true,
            targetPos.x,
            targetPos.y,
            targetPos.z,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            1
        );
        player.connection.send(sweepPacket);

        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = (Math.random() - 0.5) * 0.5;
            double offsetZ = (Math.random() - 0.5) * 0.5;

            ClientboundLevelParticlesPacket critPacket = new ClientboundLevelParticlesPacket(
                ParticleTypes.CRIT,
                true,
                targetPos.x + offsetX,
                targetPos.y + offsetY,
                targetPos.z + offsetZ,
                0.0F,
                0.1F,
                0.0F,
                0.1F,
                1
            );
            player.connection.send(critPacket);
        }
    }
}
