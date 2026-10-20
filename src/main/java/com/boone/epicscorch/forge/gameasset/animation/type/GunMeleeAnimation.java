package com.boone.epicscorch.forge.gameasset.animation.type;

import com.boone.epicscorch.forge.combat.GunMeleeHelper;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.ComboAttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GunMeleeAnimation extends ComboAttackAnimation {

    protected final Joint joint;

    public GunMeleeAnimation(
        float transitionTime,
        float preDelay,
        float contact,
        float recovery,
        Collider collider,
        Joint joint,
        AnimationAccessor<? extends GunMeleeAnimation> accessor,
        String clipPath,
        AssetAccessor<? extends Armature> armature
    ) {
        super(transitionTime, preDelay, contact, recovery, collider, joint, accessor, armature);
        this.joint = joint;
        this.resourceLocation = ResourceLocation.fromNamespaceAndPath("epicfight", "animmodels/animations/" + clipPath + ".json");
        this.addProperty(
            StaticAnimationProperty.PLAY_SPEED_MODIFIER,
            (DynamicAnimation anim, LivingEntityPatch<?> patch, float speed, float prev, float elapsed) -> speed * 0.8F
        );
    }

    @Override
    public void end(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {
        super.end(entitypatch, nextAnimation, isEnd);
        if (entitypatch.isLogicalClient()) {
            ClientAnimator animator = entitypatch.getClientAnimator();
            entitypatch.updateMotion(false);
            LivingMotion motion = entitypatch.currentCompositeMotion;
            if (motion != null && motion != LivingMotions.NONE) {
                AssetAccessor<? extends StaticAnimation> holdAnim = animator.getCompositeLivingMotion(motion);
                if (holdAnim != null && holdAnim.isPresent()) {
                    animator.playAnimation(holdAnim, 0.15F);
                }
            }
        }
    }

    @Override
    protected void hurtCollidingEntities(
        LivingEntityPatch<?> entitypatch,
        float prevElapsedTime,
        float elapsedTime,
        EntityState prevState,
        EntityState state,
        AttackAnimation.Phase phase
    ) {
        if (entitypatch.isLogicalClient()) {
            return;
        }

        LivingEntity original = entitypatch.getOriginal();
        if (!(original instanceof ServerPlayer player)) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }

        Collider gunCollider = GunMeleeHelper.getDynamicCollider(heldItem, gunItem);
        List<Entity> list = gunCollider.updateAndSelectCollideEntity(
            entitypatch,
            this,
            prevElapsedTime,
            elapsedTime,
            this.joint,
            this.getPlaySpeed(entitypatch, this)
        );

        if (list.isEmpty()) {
            return;
        }

        boolean hitAny = false;
        for (Entity entity : list) {
            LivingEntity target = getTrueEntity(entity);
            if (target != null && target.isAlive() && target != player) {
                if (entitypatch.getCurrentlyAttackTriedEntities().contains(target)) {
                    continue;
                }
                entitypatch.getCurrentlyAttackTriedEntities().add(target);

                boolean hit = GunMeleeHelper.applyMeleeHit(player, target, heldItem, gunItem);
                if (hit) {
                    hitAny = true;
                    entitypatch.getCurrentlyActuallyHitEntities().add(target);
                }
            }
        }

        if (hitAny) {
            GunMeleeHelper.damageWeapon(player, heldItem);
        }
    }

    @Override
    @ClientOnly
    public void renderDebugging(
        PoseStack poseStack,
        MultiBufferSource buffer,
        LivingEntityPatch<?> entitypatch,
        float playbackTime,
        float partialTicks
    ) {
        AnimationPlayer animPlayer = entitypatch.getAnimator().getPlayerFor(this.getAccessor());
        if (animPlayer == null) {
            return;
        }

        float prevElapsedTime = animPlayer.getPrevElapsedTime();
        float elapsedTime = animPlayer.getElapsedTime();

        ItemStack heldItem = entitypatch.getOriginal().getMainHandItem();
        Collider collider;
        if (heldItem.getItem() instanceof GunItem gunItem) {
            collider = GunMeleeHelper.getDynamicCollider(heldItem, gunItem);
        } else {
            Phase phase = this.getPhaseByTime(playbackTime);
            JointColliderPair[] colliders = phase.getColliders();
            collider = colliders.length > 0 ? colliders[0].getSecond() : null;
        }

        if (collider != null) {
            collider.draw(poseStack, buffer, entitypatch, this, this.joint, prevElapsedTime, elapsedTime, partialTicks, this.getPlaySpeed(entitypatch, this));
        }
    }
}
