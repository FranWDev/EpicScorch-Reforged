package com.boone.epicscorch.forge.world.capabilities.items;

import com.boone.epicscorch.forge.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.item.GunItem;

@EventBusSubscriber(modid = "epiccompat_cgm", bus = Bus.FORGE)
public class GunOwnerCapabilityProvider implements ICapabilityProvider {
   private final LazyOptional<GunOwnerCapabilityProvider.OwnerId> id = LazyOptional.of(GunOwnerCapabilityProvider.OwnerId::new);

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      return cap == ModCapabilities.OWNER_ID ? this.id.cast() : LazyOptional.empty();
   }

   @SubscribeEvent
   public static void onAttachCapabilitiesToItemStack(AttachCapabilitiesEvent<ItemStack> event) {
      if (((ItemStack)event.getObject()).getItem() instanceof GunItem) {
         event.addCapability(new ResourceLocation("epiccompat_cgm", "owner_id"), new GunOwnerCapabilityProvider());
      }
   }

   @SubscribeEvent
   public static void onLivingTick(LivingTickEvent event) {
      LivingEntity entity = event.getEntity();
      entity.getCapability(ModCapabilities.OWNER_ID).ifPresent(cap -> cap.value = entity.getId());
   }

   public static class OwnerId {
      public int value;
   }
}
