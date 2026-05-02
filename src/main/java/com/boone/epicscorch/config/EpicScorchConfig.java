package com.boone.epicscorch.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for EpicScorch Reforged.
 * Handles the toggles and multipliers for combat integration between Epic Fight and Scorched Guns.
 */
public class EpicScorchConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_STAMINA_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue STAMINA_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WEAPON_STAMINA_OVERRIDES;
    public static final ForgeConfigSpec.BooleanValue DISABLE_LOCK_ON;
    public static final ForgeConfigSpec.BooleanValue CANCEL_RELOAD_ON_ACTION;
    public static final ForgeConfigSpec.BooleanValue CANCEL_AIM_ON_ACTION;
    public static final ForgeConfigSpec.BooleanValue FORCE_CANCEL_AIM_WHILE_SPRINTING;
    public static final ForgeConfigSpec.IntValue STAMINA_REGEN_DELAY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Combat Mechanics");

        ENABLE_STAMINA_REDUCTION = builder
                .comment("Enables stamina reduction when firing Scorched Guns weapons")
                .define("enableStaminaReduction", true);

        STAMINA_MULTIPLIER = builder
                .comment("Global multiplier for stamina consumed per shot (based on weapon recoil)")
                .defineInRange("staminaMultiplier", 0.75, 0.0, 100.0);

        WEAPON_STAMINA_OVERRIDES = builder
                .comment("List to define fixed stamina consumption per specific weapon.",
                         "This ignores the global multiplier and the weapon's recoil.",
                         "Format: \"modid:item_name=value\" (e.g. \"scguns:forlorn_hope=2.5\", or \"scguns:pyroclastic_flow=0.0\" to disable it).")
                .defineList("weaponStaminaOverrides", new ArrayList<>(), o -> o instanceof String && ((String) o).contains("="));

        DISABLE_LOCK_ON = builder
                .comment("Prevents using Epic Fight's lock-on if the player is holding a gun in their main hand")
                .define("disableLockOn", true);

        CANCEL_RELOAD_ON_ACTION = builder
                .comment("Cancels weapon reload if the player sprints or performs Epic Fight actions (dodging, attacking, etc.)")
                .define("cancelReloadOnAction", true);

        CANCEL_AIM_ON_ACTION = builder
                .comment("Cancels weapon aiming if the player performs Epic Fight actions (dodging, attacking, etc.)")
                .define("cancelAimOnAction", true);

        FORCE_CANCEL_AIM_WHILE_SPRINTING = builder
                .comment("Forces aiming to stop if the sprint key is held, preventing the mechanic where you can aim while running if you started aiming first.")
                .define("forceCancelAimWhileSprinting", true);

        STAMINA_REGEN_DELAY = builder
                .comment("Delay (in ticks) before stamina starts regenerating after firing (20 ticks = 1 second). Default: 30 (1.5s)")
                .defineInRange("staminaRegenDelay", 30, 0, 100);

        builder.pop();
        SPEC = builder.build();
    }
}
