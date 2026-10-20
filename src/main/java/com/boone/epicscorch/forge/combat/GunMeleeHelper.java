package com.boone.epicscorch.forge.combat;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
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
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.item.BayonetItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.util.GunModifierHelper;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;

public class GunMeleeHelper {

    private static final float ENCHANTMENT_DAMAGE_SCALING_FACTOR = 0.70f;
    private static final Map<Integer, Collider> COLLIDER_CACHE = new HashMap<>();

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
        // In Tool_R for firearms (due to Scorched Guns rendering transform):
        // The local Y axis aligns horizontally with the barrel of the gun.
        // sizeY is the half-extent (box total length = 2 * sizeY).
        // Scorched Guns melee reach is measured in world raycast blocks from eye position (base 3.0F).
        // In local bone space (Tool_R at the hand), 1.0F of reach corresponds to ~0.50m of weapon length.
        // For each deltaReach:
        // - sizeY grows by ~0.25D (total box length grows by ~0.50D)
        // - centerY shifts forward by ~0.28D to advance along the barrel
        // This lands the tip directly at the muzzle/bayonet tip across all gun types.
        double deltaReach = clampedReach - 2.0D;
        double sizeX = 0.35D + Math.min(0.15D, deltaReach * 0.04D);
        double sizeY = 0.40D + (deltaReach * 0.25D);
        double sizeZ = 0.35D + Math.min(0.15D, deltaReach * 0.04D);
        double centerY = -0.32D - (deltaReach * 0.28D);

        return new MultiOBBCollider(3, sizeX, sizeY, sizeZ, 0.0D, centerY, 0.0D);
    }

    public static boolean applyMeleeHit(ServerPlayer player, LivingEntity target, ItemStack heldItem, GunItem gunItem) {
        if (target == null || !target.isAlive() || target == player) {
            return false;
        }

        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float additionalDamage = GunModifierHelper.getAdditionalDamage(heldItem, true);
        DamageSource damageSource = player.serverLevel().damageSources().playerAttack(player);
        float enchantmentDamage = getEnchantmentDamageFromBayonet(heldItem, target, gunItem);

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

    private static float getEnchantmentDamageFromBayonet(ItemStack gunStack, LivingEntity target, GunItem gunItem) {
        float enchantmentDamage = 0.0f;
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = gunItem.getAttachment(gunStack, type);
            if (attachmentStack.getItem() instanceof BayonetItem) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(attachmentStack);
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    float damageBonus = entry.getKey().getDamageBonus(entry.getValue(), target.getMobType());
                    enchantmentDamage += damageBonus * ENCHANTMENT_DAMAGE_SCALING_FACTOR;
                }
            }
        }
        return enchantmentDamage;
    }

    private static void applySpecialEnchantmentsFromBayonet(ItemStack gunStack, LivingEntity target, Player player, GunItem gunItem) {
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = gunItem.getAttachment(gunStack, type);
            if (attachmentStack.getItem() instanceof BayonetItem) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(attachmentStack);
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    applyEnchantmentEffects(entry.getKey(), entry.getValue(), target, player);
                }
            }
        }
    }

    private static void applyEnchantmentEffects(Enchantment enchantment, int level, LivingEntity target, Player player) {
        if (level <= 0) {
            return;
        }

        if (enchantment == Enchantments.FIRE_ASPECT) {
            target.setSecondsOnFire(level * 4);
        } else if (enchantment == Enchantments.KNOCKBACK) {
            Vec3 direction = target.position().subtract(player.position()).normalize();
            target.knockback(level * 0.5F, -direction.x(), -direction.z());
        } else if (enchantment == ModEnchantments.CORRODED.get()) {
            target.knockback(level * 0.3F, -(target.getX() - player.getX()), -(target.getZ() - player.getZ()));
        }
    }

    private static void applyKnockback(Player player, LivingEntity target, ItemStack stack) {
        int knockbackLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, stack);
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
