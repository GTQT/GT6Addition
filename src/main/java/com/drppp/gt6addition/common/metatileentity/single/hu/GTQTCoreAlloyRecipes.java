package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.registry.MaterialRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Read-only crucible mappings for GTQTCore's explicit material-to-material Mixer recipes.
 *
 * <p>Rows mirror the relevant recipes in
 * {@code CompatibilityMaterialRecipeLoader.init()},
 * {@code NanochipSupplyRecipes.registerMaterials()},
 * {@code CrystalSoCChain.CeriumDopedLutetiumAluminiumOxygenBlend()}, and
 * {@code MagnetoResonaticChain.init()} from the local GTQTCore source. Outputs
 * and Core-produced inputs use their exact {@code gtqtcore:} paths; CEu
 * ingredients use exact {@code gregtech:} paths. Missing identities make a
 * row unavailable rather than allowing a same-named substitute.</p>
 *
 * <p>Only exact solid-material-ratio recipes selected as crucible material
 * mixtures are represented here; unrelated ore-processing and chemical-blend
 * stages are not converted into crucible recipes. Soularium is intentionally
 * not synthesized from its Core component formula: Core's Alloy Smelter recipe
 * consumes the vanilla Soul Sand block, which has no verified crucible
 * MaterialStack mapping in this source table.</p>
 */
public final class GTQTCoreAlloyRecipes {
    private static final GT6AlloyRecipes[] ALL = {
            // CompatibilityMaterialRecipeLoader.java:74-80
            recipe("dark_steel", 2, new String[]{"gregtech:steel", "gregtech:obsidian"}, new long[]{1, 1}),
            // CompatibilityMaterialRecipeLoader.java:116-122
            recipe("electrical_steel", 2, new String[]{"gregtech:steel", "gregtech:silicon"}, new long[]{1, 1}),
            // CompatibilityMaterialRecipeLoader.java:142-148
            recipe("redstone_alloy", 2, new String[]{"gregtech:redstone", "gregtech:silicon"}, new long[]{1, 1}),
            // CompatibilityMaterialRecipeLoader.java:185-191
            recipe("pulsating_iron", 2, new String[]{"gregtech:iron", "gregtech:uraninite"}, new long[]{1, 1}),
            // CompatibilityMaterialRecipeLoader.java:211-217
            recipe("conductive_iron", 2, new String[]{"gregtech:iron", "gregtech:redstone"}, new long[]{1, 1}),
            // CompatibilityMaterialRecipeLoader.java:221-227
            recipe("energetic_alloy", 4,
                    new String[]{"gregtech:gold", "gregtech:redstone", "gregtech:glowstone"}, new long[]{2, 1, 1}),
            // CompatibilityMaterialRecipeLoader.java:232-238
            recipe("vibrant_alloy", 2,
                    new String[]{"gtqtcore:energetic_alloy", "gregtech:ender_pearl"}, new long[]{1, 1}),
            // CompatibilityMaterialRecipeLoader.java:283-289
            recipe("end_steel", 2,
                    new String[]{"gtqtcore:dark_steel", "gregtech:endstone"}, new long[]{1, 1}),
            // CompatibilityMaterialRecipeLoader.java:293-300
            recipe("lumium", 6,
                    new String[]{"gtqtcore:end_steel", "gregtech:blue_alloy", "gtqtcore:vibrant_alloy"},
                    new long[]{1, 2, 3}),
            // CompatibilityMaterialRecipeLoader.java:304-312
            recipe("signalum", 8,
                    new String[]{"gtqtcore:lumium", "gregtech:annealed_copper", "gregtech:blaze", "gregtech:rhodium"},
                    new long[]{3, 2, 1, 2}),
            // CompatibilityMaterialRecipeLoader.java:316-324
            recipe("enderium", 8,
                    new String[]{"gtqtcore:signalum", "gregtech:platinum", "gregtech:osmium", "gregtech:ender_eye"},
                    new long[]{4, 2, 1, 1}),
            // CompatibilityMaterialRecipeLoader.java:360-366
            recipe("construction_alloy", 2,
                    new String[]{"gregtech:iron", "gregtech:clay"}, new long[]{1, 1}),
            // CompatibilityMaterialRecipeLoader.java:370-377
            recipe("enriched_alloy", 4,
                    new String[]{"gtqtcore:construction_alloy", "gregtech:lithium", "gregtech:cobalt"},
                    new long[]{2, 1, 1}),
            // CompatibilityMaterialRecipeLoader.java:381-388
            recipe("energetic_silver", 4,
                    new String[]{"gregtech:silver", "gregtech:redstone", "gregtech:glowstone"}, new long[]{2, 1, 1}),
            // CompatibilityMaterialRecipeLoader.java:392-398
            recipe("vivid_alloy", 2,
                    new String[]{"gtqtcore:energetic_silver", "gregtech:ender_pearl"}, new long[]{1, 1}),
            // CompatibilityMaterialRecipeLoader.java:411-418
            recipe("crystalline_alloy", 4,
                    new String[]{"gtqtcore:vivid_alloy", "gregtech:emerald", "gregtech:plutonium_241"},
                    new long[]{2, 1, 1}),
            // CompatibilityMaterialRecipeLoader.java:422-429
            recipe("crystalline_pink_slime", 6,
                    new String[]{"gtqtcore:crystalline_alloy", "gregtech:red_steel", "gtqtcore:redstone_alloy"},
                    new long[]{3, 1, 2}),
            // CompatibilityMaterialRecipeLoader.java:433-440
            recipe("melodic_alloy", 4,
                    new String[]{"gtqtcore:crystalline_pink_slime", "gregtech:iridium", "gregtech:blue_alloy"},
                    new long[]{2, 1, 1}),
            // CompatibilityMaterialRecipeLoader.java:444-450
            recipe("stellar_alloy", 4,
                    new String[]{"gregtech:nether_star", "gtqtcore:melodic_alloy", "gregtech:naquadah"},
                    new long[]{2, 1, 1}),

            // NanochipSupplyRecipes.java:102-105; dust prefix quantities are equal material units.
            recipe("indalloy_140", 100,
                    new String[]{"gregtech:bismuth", "gregtech:lead", "gregtech:tin", "gregtech:cadmium",
                            "gregtech:indium"},
                    new long[]{47, 25, 13, 10, 5}),
            // NanochipSupplyRecipes.java:32-34,106-128; alloy(first, second) is 3:1 -> 4 dust.
            recipe("precious_metal_alloy", 4,
                    new String[]{"gregtech:rhodium_plated_palladium", "gregtech:platinum"}, new long[]{3, 1}),
            recipe("proto_halkonite", 4,
                    new String[]{"gtqtcore:rhugnor", "gtqtcore:hypogen"}, new long[]{3, 1}),
            recipe("shirabon", 4,
                    new String[]{"gtqtcore:metastable_oganesson", "gtqtcore:rhugnor"}, new long[]{3, 1}),
            recipe("transcendent_metal", 4,
                    new String[]{"gtqtcore:astral_titanium", "gtqtcore:spacetime"}, new long[]{3, 1}),
            recipe("chromatic_glass", 4,
                    new String[]{"gregtech:borosilicate_glass", "gtqtcore:lumium"}, new long[]{3, 1}),
            recipe("white_dwarf_matter", 4,
                    new String[]{"gtqtcore:universium", "gtqtcore:cosmic_neutronium"}, new long[]{3, 1}),
            recipe("black_dwarf_matter", 4,
                    new String[]{"gtqtcore:universium", "gtqtcore:bedrockium"}, new long[]{3, 1}),
            recipe("hexanite", 4,
                    new String[]{"gtqtcore:eternity", "gtqtcore:hikarium"}, new long[]{3, 1}),
            recipe("creon", 4,
                    new String[]{"gtqtcore:rhugnor", "gtqtcore:astralium"}, new long[]{3, 1}),
            recipe("six_phased_copper", 4,
                    new String[]{"gregtech:copper", "gtqtcore:hikarium"}, new long[]{3, 1}),
            recipe("enriched_holmium", 4,
                    new String[]{"gregtech:holmium", "gtqtcore:enriched_naquadah_alloy"}, new long[]{3, 1}),
            recipe("quantum_alloy", 4,
                    new String[]{"gregtech:neutronium", "gtqtcore:astralium"}, new long[]{3, 1}),
            recipe("shijima", 4,
                    new String[]{"gtqtcore:astralium", "gtqtcore:cosmic_neutronium"}, new long[]{3, 1}),
            recipe("tairitsu", 4,
                    new String[]{"gtqtcore:hikarium", "gtqtcore:cosmic_neutronium"}, new long[]{3, 1}),
            recipe("churitsu", 4,
                    new String[]{"gtqtcore:spacetime", "gtqtcore:cosmic_neutronium"}, new long[]{3, 1}),
            recipe("octiron", 4,
                    new String[]{"gtqtcore:void_metal", "gregtech:iron"}, new long[]{3, 1}),
            recipe("draconium_awakened", 4,
                    new String[]{"gtqtcore:astralium", "gtqtcore:adamantium"}, new long[]{3, 1}),
            recipe("quantium", 4,
                    new String[]{"gtqtcore:astralium", "gregtech:titanium"}, new long[]{3, 1}),
            recipe("tengam_purified", 4,
                    new String[]{"gregtech:neodymium", "gregtech:samarium"}, new long[]{3, 1}),
            recipe("tengam_attuned", 4,
                    new String[]{"gtqtcore:tengam_purified", "gtqtcore:magneto_resonatic"}, new long[]{3, 1}),
            recipe("chronomatic_glass", 4,
                    new String[]{"gregtech:borosilicate_glass", "gtqtcore:spacetime"}, new long[]{3, 1}),
            recipe("draconium", 4,
                    new String[]{"gtqtcore:astralium", "gtqtcore:void_metal"}, new long[]{3, 1}),

            // loaders/recipe/circuit/CrystalSoCChain.java:17-44. Three exact solid-input alternatives
            // make the same registered Core material; retain each route.
            recipe("cerium_doped_lutetium_aluminium_oxygen_blend", 9,
                    new String[]{"gregtech:cerium", "gregtech:lutetium", "gregtech:green_sapphire"},
                    new long[]{1, 3, 5}),
            recipe("cerium_doped_lutetium_aluminium_oxygen_blend", 9,
                    new String[]{"gregtech:cerium", "gregtech:lutetium", "gregtech:sapphire"},
                    new long[]{1, 3, 5}),
            recipe("cerium_doped_lutetium_aluminium_oxygen_blend", 9,
                    new String[]{"gregtech:cerium", "gregtech:lutetium", "gregtech:alumina"},
                    new long[]{1, 3, 5}),

            // MagnetoResonaticChain.java:37-43. Use the registered Mixer ratio;
            // it intentionally differs from the material's component declaration.
            recipe("magneto_resonatic", 9,
                    new String[]{"gtqtcore:bismuth_tellurite", "gtqtcore:prasiolite",
                            "gtqtcore:cubic_zirconia", "gregtech:steel_magnetic"}, new long[]{4, 3, 1, 1})
    };

    private static final Set<String> EXPLICIT_OUTPUTS;

    static {
        Set<String> outputs = new HashSet<>();
        for (GT6AlloyRecipes recipe : ALL) {
            outputs.add(normalize(recipe.getOutputName()));
        }
        EXPLICIT_OUTPUTS = Collections.unmodifiableSet(outputs);
    }

    private GTQTCoreAlloyRecipes() {}

    private static GT6AlloyRecipes recipe(String outputPath, long outputUnits, String[] inputs, long[] inputUnits) {
        return new GT6AlloyRecipes("gtqtcore:" + outputPath, outputUnits, inputs, inputUnits);
    }

    /** Immutable exact-material recipe catalogue. */
    public static List<GT6AlloyRecipes> getRecipes() {
        return Collections.unmodifiableList(Arrays.asList(ALL));
    }

    /**
     * Optional integration guard based on the host registry identity, not a
     * compile-time link or a guessed material name. CEu's material manager
     * falls unknown namespaces back to its GregTech registry, whose mod id
     * differs from {@code gtqtcore}.
     */
    public static boolean isCoreMaterialRegistryAvailable() {
        if (GregTechAPI.materialManager == null) return false;
        MaterialRegistry registry = GregTechAPI.materialManager.getRegistry("gtqtcore");
        return registry != null && "gtqtcore".equals(registry.getModid());
    }

    /** Core owns an explicit recipe set for this normalized material path. */
    public static boolean hasRecipeOutput(String name) {
        if (name == null || name.isEmpty()) return false;
        int separator = name.indexOf(':');
        if (separator >= 0) {
            if (separator == 0 || separator == name.length() - 1 ||
                    name.indexOf(':', separator + 1) >= 0 ||
                    !"gtqtcore".equals(name.substring(0, separator))) {
                return false;
            }
            name = name.substring(separator + 1);
        }
        return EXPLICIT_OUTPUTS.contains(normalize(name));
    }

    /** Do not let a Core material inherit a different recipe via component fallback. */
    static boolean blocksHostCompositionFallback(String name) {
        return hasRecipeOutput(name);
    }

    private static String normalize(String name) {
        int separator = name.lastIndexOf(':');
        String path = separator < 0 ? name : name.substring(separator + 1);
        return path.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
