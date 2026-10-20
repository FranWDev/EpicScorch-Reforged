package com.boone.epicscorch.mixins;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class EpicScorchMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            // Check if it's a client mixin
            if (mixinClassName.contains("GunItemStackRendererMixin")
                    || mixinClassName.contains("PlayerModelHandlerMixin")
                    || mixinClassName.contains("ControlEngineMixin")
                    || mixinClassName.contains("LocalPlayerPatchMixin")
                    || mixinClassName.contains("DiscreteInputActionTriggerMixin")
                    || mixinClassName.contains("LocalPlayerSwingMixin")
                    || mixinClassName.contains("SkillContainerMixin")
                    || mixinClassName.contains("RenderEngineMixin")
                    || mixinClassName.contains("ShootingHandlerMixin")
                    || mixinClassName.contains("ItemInHandRendererAccessor")
                    || mixinClassName.contains("AimingHandlerAccessor")
                    || mixinClassName.contains("AimingHandlerMixin")
                    || mixinClassName.contains("EpicFightCameraAPIMixin")
                    || mixinClassName.contains("ReloadHandlerMixin")
                    || mixinClassName.contains("GunRenderingHandlerMixin")
                    || mixinClassName.contains("ClientMeleeAttackHandlerMixin")
                    || mixinClassName.contains("ItemInHandLayerEntityTrackerMixin")
                    || mixinClassName.contains("RenderItemBaseEntityTrackerMixin")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
