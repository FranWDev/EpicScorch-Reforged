package com.boone.epicscorch.forge.world.capabilities.items;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.item.GrenadeItem;
import top.ribs.scguns.item.GunItem;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class GunCapabilityProvider implements ICapabilityProvider, NonNullSupplier<CapabilityItem> {
   private final LazyOptional<CapabilityItem> optional = LazyOptional.of(this);
   private final CapabilityItem capability;

   public GunCapabilityProvider(ItemStack itemStack) {
      Item item = itemStack.m_41720_();
      if (item instanceof GunItem gunItem) {
         String gripType = String.valueOf(gunItem.getGun().getGeneral().getGripType(itemStack));
         System.out.println("GripType: " + gripType);

         this.capability = switch (gripType) {
            case "ONE_HANDED" -> GunCapabilityPresets.PISTOL.apply(gunItem).build();
            case "TWO_HANDED" -> GunCapabilityPresets.RIFLE.apply(gunItem).build();
            case "BAZOOKA" -> GunCapabilityPresets.BAZOOKA.apply(gunItem).build();
            case "MINI_GUN" -> GunCapabilityPresets.MINI_GUN.apply(gunItem).build();
            default -> null;
         };
      } else if (item instanceof GrenadeItem grenadeItem) {
         this.capability = GunCapabilityPresets.GRENADE.apply(grenadeItem).build();
      } else {
         this.capability = null;
      }
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      return cap == EpicFightCapabilities.CAPABILITY_ITEM ? this.optional.cast() : LazyOptional.empty();
   }

   @NotNull
   public CapabilityItem get() {
      return this.capability;
   }

   public boolean hasCapability() {
      return this.capability != null;
   }
}
