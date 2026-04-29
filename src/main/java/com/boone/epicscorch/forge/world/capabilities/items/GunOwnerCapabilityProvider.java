package com.boone.epicscorch.forge.world.capabilities.items;

import com.boone.epicscorch.forge.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
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
import top.ribs.scguns.item.GrenadeItem;

@EventBusSubscriber(modid = "epicscorch", bus = Bus.FORGE)
public class GunOwnerCapabilityProvider implements ICapabilityProvider {
   private final LazyOptional<GunOwnerCapabilityProvider.OwnerId> id = LazyOptional.of(GunOwnerCapabilityProvider.OwnerId::new);

   @NotNull
   @Override
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      return cap == ModCapabilities.OWNER_ID ? this.id.cast() : LazyOptional.empty();
   }

   @SubscribeEvent
   public static void onAttachCapabilitiesToItemStack(AttachCapabilitiesEvent<ItemStack> event) {
      ItemStack stack = event.getObject();
      if (stack == null || stack.isEmpty()) return;
      
      Item item = stack.getItem();
      if (item instanceof GunItem || item instanceof GrenadeItem) {
          GunCapabilityProvider weaponProvider = new GunCapabilityProvider(stack);
          if (weaponProvider.hasCapability()) {
              event.addCapability(new ResourceLocation("epicscorch", "weapon_cap"), weaponProvider);
          }
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
