package com.example.mahitosoulregen;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class RegenConfig {
    public static final ForgeConfigSpec SPEC;

    // --- Who counts as Mahito (values confirmed from Jujutsu Craft v51) ---
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MAHITO_IDS;
    public static final ForgeConfigSpec.DoubleValue MAHITO_TECHNIQUE_ID;
    public static final ForgeConfigSpec.ConfigValue<String> MAHITO_TAG;

    // --- Who can observe the soul ---
    public static final ForgeConfigSpec.ConfigValue<String> OBSERVER_ENTITY_TYPE_TAG;
    public static final ForgeConfigSpec.ConfigValue<String> OBSERVER_ADVANCEMENT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_OBSERVER_IDS;
    public static final ForgeConfigSpec.ConfigValue<String> OBSERVER_TAG;

    // --- Regen tuning ---
    public static final ForgeConfigSpec.DoubleValue INSTANT_FRACTION;
    public static final ForgeConfigSpec.DoubleValue REGEN_PER_TICK;
    public static final ForgeConfigSpec.BooleanValue ENVIRONMENT_UNOBSERVED;
    public static final ForgeConfigSpec.BooleanValue MIRROR_JJC_EXEMPTIONS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXEMPT_VICTIM_EFFECTS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Which entities count as Mahito").push("mahito");
        MAHITO_IDS = b.defineList("mahitoEntityIds", List.of("jujutsucraft:mahito"), o -> o instanceof String);
        MAHITO_TECHNIQUE_ID = b.comment("Players whose PlayerCurseTechnique / PlayerCurseTechnique2 equals this are Mahito (15 = Mahito in JJC v51). Set -1 to disable.")
                .defineInRange("mahitoTechniqueId", 15.0, -1.0, 1000.0);
        MAHITO_TAG = b.comment("Optional manual marker: /tag <name> add mahito_regen").define("mahitoTag", "mahito_regen");
        b.pop();

        b.comment("Who can observe the soul (these attackers do NOT trigger the regen)").push("soulObservers");
        OBSERVER_ENTITY_TYPE_TAG = b.comment("Entity type tag JJC itself uses for soul perception")
                .define("observerEntityTypeTag", "forge:soul_perception");
        OBSERVER_ADVANCEMENT = b.comment("Players who have completed this advancement can observe the soul")
                .define("observerAdvancement", "jujutsucraft:observation_of_the_soul");
        EXTRA_OBSERVER_IDS = b.defineList("extraObserverEntityIds", List.of(), o -> o instanceof String);
        OBSERVER_TAG = b.comment("Optional manual marker: /tag <name> add soul_observer").define("observerTag", "soul_observer");
        b.pop();

        b.push("regen");
        INSTANT_FRACTION = b.comment("OPTIONAL extra instant reduction on unobserved hits, on top of the reduction Jujutsu Craft already applies. 0 = off (recommended).").defineInRange("instantFraction", 0.0, 0.0, 1.0);
        REGEN_PER_TICK = b.comment("Fraction of the still-unhealed damage from unobserved attackers that is healed back each tick. Healing starts fast and tapers off: 0.05 is roughly half back in 0.7s and 95% in 3s; 0.15 is roughly half in 0.2s and 95% in 1s. Raise it for faster regen.").defineInRange("regenPerTick", 0.05, 0.0, 1.0);
        ENVIRONMENT_UNOBSERVED = b.comment("Damage with no attacker (fire, falling, etc.) also gets regen. Off by default: Jujutsu Craft only reduces damage from attackers, so the regen follows the same rule.")
                .define("environmentCountsAsUnobserved", false);
        MIRROR_JJC_EXEMPTIONS = b.comment("Match Jujutsu Craft: no regen when its own Mahito reduction is skipped (victim has one of the effects below, or the attack is flagged 'ignore').")
                .define("mirrorJjcExemptions", true);
        EXEMPT_VICTIM_EFFECTS = b.comment("While Mahito has any of these effects, Jujutsu Craft skips its reduction (read from the v51 jar)")
                .defineList("exemptVictimEffects", List.of("jujutsucraft:unstable", "jujutsucraft:domain_amplification"), o -> o instanceof String);
        b.pop();

        SPEC = b.build();
    }
}
