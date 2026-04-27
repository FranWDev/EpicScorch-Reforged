package com.boone.epicscorch.forge;

import com.boone.epicscorch.forge.world.capabilities.items.GunOwnerCapabilityProvider;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {
   public static final Capability<GunOwnerCapabilityProvider.OwnerId> OWNER_ID = CapabilityManager.get(
      new CapabilityToken<GunOwnerCapabilityProvider.OwnerId>() {}
   );
}
