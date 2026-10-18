package com.boone.epicscorch.compat.shouldersurfing;

import com.github.exopandora.shouldersurfing.api.client.event.ComputeCameraCouplingEvent;
import com.github.exopandora.shouldersurfing.api.client.event.ComputePlayerAimStateEvent;
import com.github.exopandora.shouldersurfing.api.client.event.ComputePlayerUseItemStateEvent;
import com.github.exopandora.shouldersurfing.api.event.IEventBus;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import net.minecraft.world.entity.LivingEntity;

public class EpicScorchShoulderSurfingPlugin implements IShoulderSurfingPlugin {
    @Override
    public void register(IEventBus bus) {
        bus.register(this::onComputeAimState);
        bus.register(this::onComputeUseItemState);
        bus.register(this::onComputeCameraCoupling);
    }

    private void onComputeAimState(ComputePlayerAimStateEvent event) {
        LivingEntity entity = event.getEntity();
        if (ShoulderSurfingCompatHelper.isAimingGun(entity)) {
            event.setResult(true);
        }
    }

    private void onComputeUseItemState(ComputePlayerUseItemStateEvent event) {
        LivingEntity entity = event.getCameraEntity();
        if (ShoulderSurfingCompatHelper.isAimingGun(entity)) {
            event.setResult(true);
        }
    }

    private void onComputeCameraCoupling(ComputeCameraCouplingEvent event) {
        net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null && (ShoulderSurfingCompatHelper.isAimingGun(player) || ShoulderSurfingCompatHelper.isShootingGun(player))) {
            event.setResult(true);
        }
    }
}
