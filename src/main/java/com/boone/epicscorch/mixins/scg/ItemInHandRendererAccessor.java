package com.boone.epicscorch.mixins.scg;

import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {
    // f_109302_ is the SRG name for mainHandHeight in 1.20.1
    @Accessor(value = "f_109302_", remap = false)
    void setMainHandHeight(float height);

    // f_109303_ is the SRG name for offHandHeight in 1.20.1
    @Accessor(value = "f_109303_", remap = false)
    void setOffHandHeight(float height);
}
