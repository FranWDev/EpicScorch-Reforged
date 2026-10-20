package com.boone.epicscorch.client;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class RenderContextHolder {
    private static final ThreadLocal<LivingEntity> CURRENT_ENTITY = new ThreadLocal<>();

    public static void setRenderingEntity(@Nullable LivingEntity entity) {
        CURRENT_ENTITY.set(entity);
    }

    @Nullable
    public static LivingEntity getRenderingEntity() {
        return CURRENT_ENTITY.get();
    }

    public static void clear() {
        CURRENT_ENTITY.remove();
    }
}
