package com.boone.epicscorch.mixins.scg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import top.ribs.scguns.client.handler.AimingHandler;

@Mixin(value = AimingHandler.class, remap = false)
public interface AimingHandlerAccessor {
    @Accessor("normalisedAdsProgress")
    void setNormalisedAdsProgress(double progress);
    
    @Accessor("normalisedAdsProgress")
    double getNormalisedAdsProgress();
}
