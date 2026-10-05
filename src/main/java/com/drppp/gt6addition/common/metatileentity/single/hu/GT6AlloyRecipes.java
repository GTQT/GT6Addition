package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Collections;

/** Verified GT6 definition-time and additional alloying recipes, in material units. */
public final class GT6AlloyRecipes {
    private static final GT6AlloyRecipes[] FIXED = {
            // MT.java:1055: uumMcfg(...).alloyElectrolyzer(...) also
            // registers its component configuration as a crucible alloy.
            new GT6AlloyRecipes("silicon_carbide", 2, new String[]{"silicon", "carbon"}, new long[]{1, 1}),
            // Separate alloy outputs; inputs are the host's verified elements.
            new GT6AlloyRecipes("duranium_alloy", 8, new String[]{"duranium", "magnesium"}, new long[]{7, 1}),
            new GT6AlloyRecipes("tritanium_alloy", 4, new String[]{"tritanium", "duranium"}, new long[]{3, 1}),
            // MT.java:1871-1872. Duranium/Tritanium are deliberately not
            // substituted for their distinct GT6 elemental input identities.
            new GT6AlloyRecipes("trinaquadalloy", 9, new String[]{"trinium", "naquadah", "carbon"},
                    new long[]{6, 2, 1}),
            new GT6AlloyRecipes("trinitanium", 3, new String[]{"trinium", "titanium"}, new long[]{2, 1}),
            // MT.java:1655-1656: single-component refinement is an alloy
            // recipe too; heat/steal does not replace its material identity.
            new GT6AlloyRecipes("wrought_iron", 1, new String[]{"iron"}, new long[]{1}),
            new GT6AlloyRecipes("annealed_copper", 1, new String[]{"copper"}, new long[]{1}),
            // MT.java:1833,1838,1853-1855,1875-1876.
            new GT6AlloyRecipes("netherite", 1, new String[]{"gold", "ancient_debris"}, new long[]{4, 4}),
            new GT6AlloyRecipes("desh", 9,
                    new String[]{"boron", "lanthanum", "neodymium", "niobium", "cobalt", "cerium", "lithium"},
                    new long[]{2, 2, 1, 1, 1, 1, 1}),
            new GT6AlloyRecipes("hss_g", 9, new String[]{"tungsten_steel", "chrome", "molybdenum", "vanadium"},
                    new long[]{5, 1, 2, 1}),
            new GT6AlloyRecipes("hss_e", 9, new String[]{"hss_g", "cobalt", "manganese", "silicon"},
                    new long[]{6, 1, 1, 1}),
            new GT6AlloyRecipes("hss_s", 9, new String[]{"hss_g", "osmiridium", "iridium"}, new long[]{6, 2, 1}),
            new GT6AlloyRecipes("titanium_iridium", 2, new String[]{"iridium", "titanium"}, new long[]{1, 1}),
            new GT6AlloyRecipes("titanium_aluminide", 3, new String[]{"titanium", "aluminium"}, new long[]{3, 7}),
            // MT.java:1682-1688,1764. Vulcanite and Steeleaf only
            // configure composition, so neither belongs in this list.
            new GT6AlloyRecipes("celenegil", 2, new String[]{"platinum", "orichalcum"}, new long[]{1, 1}),
            new GT6AlloyRecipes("shadow_steel", 2, new String[]{"shadow_iron", "lemurite"}, new long[]{1, 1}),
            new GT6AlloyRecipes("inolashite", 2, new String[]{"alduorite", "ceruclase"}, new long[]{1, 1}),
            new GT6AlloyRecipes("haderoth", 2, new String[]{"mithril", "rubracium"}, new long[]{1, 1}),
            new GT6AlloyRecipes("desichalkos", 2, new String[]{"eximite", "meutoite"}, new long[]{1, 1}),
            new GT6AlloyRecipes("tartarite", 2, new String[]{"adamantine", "atlarus"}, new long[]{1, 1}),
            new GT6AlloyRecipes("amordrine", 2, new String[]{"prometheum", "kalendrite"}, new long[]{1, 1}),
            new GT6AlloyRecipes("ironwood", 18, new String[]{"wrought_iron", "liveroot", "angmallen"},
                    new long[]{8, 9, 2}),
            // MT.java:1753,1786-1790,1811,1819.
            new GT6AlloyRecipes("energetic_silver", 1, new String[]{"silver", "redstone", "glowstone"},
                    new long[]{1, 1, 1}),
            new GT6AlloyRecipes("alumite", 9, new String[]{"alumina", "wrought_iron", "obsidian"},
                    new long[]{5, 2, 18}),
            new GT6AlloyRecipes("manyullyn", 2, new String[]{"cobalt", "ardite"}, new long[]{1, 1}),
            new GT6AlloyRecipes("vibranium_steel", 4, new String[]{"vibranium", "steel"}, new long[]{1, 3}),
            new GT6AlloyRecipes("vibranium_silver", 4, new String[]{"vibranium", "silver"}, new long[]{1, 3}),
            new GT6AlloyRecipes("vibramantium", 4, new String[]{"vibranium", "adamantium"}, new long[]{1, 3}),
            new GT6AlloyRecipes("clay_compound", 1, new String[]{"stone", "ceramic"}, new long[]{2, 1}),
            new GT6AlloyRecipes("spectre_iron", 1, new String[]{"wrought_iron", "ectoplasm"}, new long[]{1, 1}),
            // MT.java:1744-1752,1791-1794,1805-1815. Keep the
            // configured output divider, not the sum of the ingredients.
            new GT6AlloyRecipes("red_alloy", 1, new String[]{"copper", "redstone"}, new long[]{1, 4}),
            new GT6AlloyRecipes("blue_alloy", 1, new String[]{"silver", "nikolite"}, new long[]{1, 4}),
            new GT6AlloyRecipes("purple_alloy", 1, new String[]{"red_alloy", "blue_alloy"}, new long[]{1, 1}),
            new GT6AlloyRecipes("redstone_alloy", 1, new String[]{"silicon", "redstone"}, new long[]{1, 1}),
            new GT6AlloyRecipes("nikoline_alloy", 1, new String[]{"silicon", "nikolite"}, new long[]{1, 1}),
            new GT6AlloyRecipes("electrotine_alloy", 1, new String[]{"wrought_iron", "nikolite"}, new long[]{1, 8}),
            new GT6AlloyRecipes("electrum_flux", 1, new String[]{"electrum", "redstone"}, new long[]{1, 2}),
            new GT6AlloyRecipes("conductive_iron", 1, new String[]{"wrought_iron", "redstone"}, new long[]{1, 1}),
            new GT6AlloyRecipes("signalum", 8, new String[]{"copper", "silver", "red_alloy"}, new long[]{1, 2, 5}),
            new GT6AlloyRecipes("lumium", 4, new String[]{"tin", "silver", "glowstone"}, new long[]{3, 1, 4}),
            new GT6AlloyRecipes("enderium_base", 4, new String[]{"tin", "silver", "platinum"}, new long[]{2, 1, 1}),
            new GT6AlloyRecipes("enderium", 1, new String[]{"enderium_base", "ender_pearl"}, new long[]{1, 1}),
            new GT6AlloyRecipes("obsidian_steel", 1, new String[]{"steel", "obsidian"}, new long[]{1, 9}),
            new GT6AlloyRecipes("pulsating_iron", 1, new String[]{"wrought_iron", "ender_pearl"}, new long[]{1, 1}),
            new GT6AlloyRecipes("energetic_alloy", 1, new String[]{"inductive_alloy", "glowstone"}, new long[]{2, 1}),
            new GT6AlloyRecipes("vibrant_alloy", 1, new String[]{"energetic_alloy", "ender_pearl"}, new long[]{1, 1}),
            new GT6AlloyRecipes("electrical_steel", 1, new String[]{"steel", "silicon"}, new long[]{1, 1}),
            new GT6AlloyRecipes("soularium", 1, new String[]{"soul_sand", "gold"}, new long[]{9, 1}),
            new GT6AlloyRecipes("end_steel", 1, new String[]{"endstone", "obsidian_steel", "obsidian"},
                    new long[]{1, 1, 9}),
            new GT6AlloyRecipes("melodic_alloy", 1, new String[]{"end_steel", "ender_eye"}, new long[]{1, 1}),
            new GT6AlloyRecipes("stellar_alloy", 2, new String[]{"melodic_alloy", "nether_star", "clay"},
                    new long[]{1, 1, 4}),
            new GT6AlloyRecipes("vivid_alloy", 1, new String[]{"energetic_silver", "ender_pearl"}, new long[]{1, 1}),

            // MT.java:1714-1716,1718-1720,1733-1734,1739-1741.
            // Composition-only steel variants below are deliberately absent.
            new GT6AlloyRecipes("black_steel", 5, new String[]{"nickel", "black_bronze", "steel"},
                    new long[]{1, 1, 3}),
            new GT6AlloyRecipes("blue_steel", 8,
                    new String[]{"sterling_silver", "bismuth_bronze", "steel", "black_steel"},
                    new long[]{1, 1, 2, 4}),
            new GT6AlloyRecipes("red_steel", 8, new String[]{"rose_gold", "brass", "steel", "black_steel"},
                    new long[]{1, 1, 2, 4}),
            new GT6AlloyRecipes("vanadium_steel", 5, new String[]{"steel", "vanadium"}, new long[]{4, 1}),
            new GT6AlloyRecipes("tungsten_steel", 2, new String[]{"steel", "tungsten"}, new long[]{1, 1}),
            new GT6AlloyRecipes("tungsten_carbide", 2, new String[]{"tungsten", "carbon"}, new long[]{1, 1}),
            new GT6AlloyRecipes("titanium_gold", 4, new String[]{"titanium", "gold"}, new long[]{3, 1}),
            new GT6AlloyRecipes("tantalum_hafnium_carbide", 10,
                    new String[]{"tantalum", "hafnium", "carbon"}, new long[]{4, 1, 5}),
            new GT6AlloyRecipes("meteoric_black_steel", 5,
                    new String[]{"nickel", "black_bronze", "meteoric_steel"}, new long[]{1, 1, 3}),
            new GT6AlloyRecipes("meteoric_blue_steel", 8,
                    new String[]{"sterling_silver", "bismuth_bronze", "meteoric_steel", "meteoric_black_steel"},
                    new long[]{1, 1, 2, 4}),
            new GT6AlloyRecipes("meteoric_red_steel", 8,
                    new String[]{"rose_gold", "brass", "meteoric_steel", "meteoric_black_steel"},
                    new long[]{1, 1, 2, 4}),

            // MT.java:1691-1708. Only setAloy/uumAloy creates a recipe;
            // GildedIron.setMcfg and AluminiumAlloy.uumMcfg do not.
            new GT6AlloyRecipes("electrum", 2, new String[]{"silver", "gold"}, new long[]{1, 1}),
            new GT6AlloyRecipes("sterling_silver", 5, new String[]{"copper", "silver"}, new long[]{1, 4}),
            new GT6AlloyRecipes("rose_gold", 5, new String[]{"copper", "gold"}, new long[]{1, 4}),
            new GT6AlloyRecipes("angmallen", 2, new String[]{"gold", "wrought_iron"}, new long[]{1, 1}),
            new GT6AlloyRecipes("inductive_alloy", 2, new String[]{"gold", "redstone"}, new long[]{1, 1}),
            new GT6AlloyRecipes("cd_in_ag_alloy", 3, new String[]{"cadmium", "indium", "silver"},
                    new long[]{1, 1, 1}),
            new GT6AlloyRecipes("brass", 4, new String[]{"copper", "zinc"}, new long[]{3, 1}),
            new GT6AlloyRecipes("cobalt_brass", 9, new String[]{"brass", "aluminium", "cobalt"},
                    new long[]{7, 1, 1}),
            new GT6AlloyRecipes("bronze", 4, new String[]{"copper", "tin"}, new long[]{3, 1}),
            new GT6AlloyRecipes("black_bronze", 5, new String[]{"copper", "electrum"}, new long[]{3, 2}),
            new GT6AlloyRecipes("bismuth_bronze", 5, new String[]{"bismuth", "brass"}, new long[]{1, 4}),
            new GT6AlloyRecipes("hepatizon", 2, new String[]{"gold", "bronze"}, new long[]{1, 1}),
            new GT6AlloyRecipes("arsenic_copper", 4, new String[]{"copper", "arsenic"}, new long[]{3, 1}),
            new GT6AlloyRecipes("arsenic_bronze", 5, new String[]{"arsenic", "bronze"}, new long[]{1, 4}),

            // MT.java:1754-1763,1777,1780,1783-1784. uumAloy registers
            // the original configuration as a crucible recipe; divider 0
            // means the sum of the input U coefficients, not one output U.
            new GT6AlloyRecipes("invar", 3, new String[]{"wrought_iron", "nickel"}, new long[]{2, 1}),
            new GT6AlloyRecipes("constantan", 2, new String[]{"copper", "nickel"}, new long[]{1, 1}),
            new GT6AlloyRecipes("nichrome", 5, new String[]{"nickel", "chrome"}, new long[]{4, 1}),
            new GT6AlloyRecipes("kanthal", 3, new String[]{"wrought_iron", "aluminium", "chrome"},
                    new long[]{1, 1, 1}),
            new GT6AlloyRecipes("magnalium", 3, new String[]{"magnesium", "aluminium"}, new long[]{1, 2}),
            new GT6AlloyRecipes("stainless_steel", 9,
                    new String[]{"wrought_iron", "invar", "chrome", "manganese"}, new long[]{4, 3, 1, 1}),
            new GT6AlloyRecipes("ultimet", 9, new String[]{"cobalt", "nickel", "chrome", "molybdenum"},
                    new long[]{5, 1, 2, 1}),
            new GT6AlloyRecipes("tin_alloy", 2, new String[]{"tin", "wrought_iron"}, new long[]{1, 1}),
            new GT6AlloyRecipes("battery_alloy", 5, new String[]{"lead", "antimony"}, new long[]{4, 1}),
            new GT6AlloyRecipes("soldering_alloy", 10, new String[]{"tin", "antimony"}, new long[]{9, 1}),
            new GT6AlloyRecipes("osmiridium", 2, new String[]{"osmium", "iridium"}, new long[]{1, 1}),
            new GT6AlloyRecipes("vanadium_gallium", 4, new String[]{"vanadium", "gallium"}, new long[]{3, 1}),
            new GT6AlloyRecipes("niobium_titanium", 2, new String[]{"niobium", "titanium"}, new long[]{1, 1}),
            new GT6AlloyRecipes("aluminium_brass", 4, new String[]{"aluminium", "copper"}, new long[]{3, 1}),

            // Reductions and carbothermic recipes.
            new GT6AlloyRecipes("iron", 2, new String[]{"hematite", "carbon", "calcite"}, new long[]{5, 1, 1}),
            new GT6AlloyRecipes("iron", 6, new String[]{"magnetite", "carbon"}, new long[]{14, 3}),
            new GT6AlloyRecipes("iron", 6, new String[]{"basaltic_mineral_sand", "carbon"}, new long[]{14, 3}),
            new GT6AlloyRecipes("iron", 6, new String[]{"granitic_mineral_sand", "carbon"}, new long[]{14, 3}),
            new GT6AlloyRecipes("iron", 6, new String[]{"ferrovanadium", "carbon"}, new long[]{28, 3}),
            new GT6AlloyRecipes("silicon", 1, new String[]{"silicon_dioxide", "carbon"}, new long[]{3, 1}),

            // GT6 custom materials and air-assisted alloying.
            new GT6AlloyRecipes("iron", 2, new String[]{"shadow_iron", "ignatius"}, new long[]{1, 1}),
            new GT6AlloyRecipes("iron", 2, new String[]{"deep_iron", "prometheum"}, new long[]{1, 1}),
            new GT6AlloyRecipes("black_steel", 2, new String[]{"deep_iron", "infuscolium"}, new long[]{1, 1}),
            new GT6AlloyRecipes("steel", 1, new String[]{"wrought_iron", "air"}, new long[]{1, 1}),
            new GT6AlloyRecipes("meteoric_steel", 1, new String[]{"meteoric_iron", "air"}, new long[]{1, 1}),
            new GT6AlloyRecipes("ultimet", 36, new String[]{"cobalt", "nichrome", "chrome", "molybdenum"},
                    new long[]{20, 5, 7, 4}),
            new GT6AlloyRecipes("stainless_steel", 36,
                    new String[]{"wrought_iron", "nichrome", "chrome", "manganese"}, new long[]{24, 5, 3, 4}),
            new GT6AlloyRecipes("tungsten_steel", 2, new String[]{"meteoric_steel", "tungsten"}, new long[]{1, 1}),
            new GT6AlloyRecipes("tungsten_steel", 2,
                    new String[]{"meteoric_steel", "tungsten_sintered"}, new long[]{1, 1}),
            new GT6AlloyRecipes("tungsten_steel", 2, new String[]{"steel", "tungsten_sintered"}, new long[]{1, 1}),
            new GT6AlloyRecipes("vanadium_steel", 5, new String[]{"meteoric_steel", "vanadium"}, new long[]{4, 1}),
            new GT6AlloyRecipes("electrical_steel", 1, new String[]{"meteoric_steel", "silicon"}, new long[]{1, 1}),
            new GT6AlloyRecipes("obsidian_steel", 1, new String[]{"meteoric_steel", "lava"}, new long[]{1, 9}),
            new GT6AlloyRecipes("obsidian_steel", 1, new String[]{"steel", "lava"}, new long[]{1, 9}),
            new GT6AlloyRecipes("end_steel", 1, new String[]{"obsidian_steel", "endstone", "lava"},
                    new long[]{1, 1, 9}),
            new GT6AlloyRecipes("alumite", 5, new String[]{"aluminium", "wrought_iron", "lava"},
                    new long[]{5, 2, 18}),
            new GT6AlloyRecipes("hepatizon", 24, new String[]{"bronze", "tin", "rose_gold"},
                    new long[]{8, 1, 15}),

            // GT6 copper and precious-metal alloy recipes.
            new GT6AlloyRecipes("red_alloy", 1, new String[]{"mingrade", "redstone"}, new long[]{2, 3}),
            new GT6AlloyRecipes("red_alloy", 1, new String[]{"annealed_copper", "redstone"}, new long[]{1, 4}),
            new GT6AlloyRecipes("rose_gold", 5, new String[]{"annealed_copper", "gold"}, new long[]{1, 4}),
            new GT6AlloyRecipes("sterling_silver", 5, new String[]{"annealed_copper", "silver"}, new long[]{1, 4}),
            new GT6AlloyRecipes("aluminium_brass", 4, new String[]{"annealed_copper", "aluminium"},
                    new long[]{1, 3}),
            new GT6AlloyRecipes("brass", 4, new String[]{"annealed_copper", "zinc"}, new long[]{3, 1}),
            new GT6AlloyRecipes("bronze", 4, new String[]{"annealed_copper", "tin"}, new long[]{3, 1}),
            new GT6AlloyRecipes("arsenic_copper", 4, new String[]{"annealed_copper", "arsenic"}, new long[]{3, 1}),
            new GT6AlloyRecipes("arsenic_bronze", 5, new String[]{"arsenic_copper", "tin"}, new long[]{4, 1}),
            new GT6AlloyRecipes("black_bronze", 5, new String[]{"annealed_copper", "electrum"}, new long[]{3, 2}),
            new GT6AlloyRecipes("black_bronze", 20,
                    new String[]{"copper", "rose_gold", "silver"}, new long[]{11, 5, 4}),
            new GT6AlloyRecipes("black_bronze", 20,
                    new String[]{"annealed_copper", "rose_gold", "silver"}, new long[]{11, 5, 4}),
            new GT6AlloyRecipes("black_bronze", 20,
                    new String[]{"copper", "sterling_silver", "gold"}, new long[]{11, 5, 4}),
            new GT6AlloyRecipes("black_bronze", 20,
                    new String[]{"annealed_copper", "sterling_silver", "gold"}, new long[]{11, 5, 4}),
            new GT6AlloyRecipes("signalum", 8,
                    new String[]{"annealed_copper", "silver", "red_alloy"}, new long[]{1, 2, 5}),
            new GT6AlloyRecipes("signalum", 16,
                    new String[]{"copper", "sterling_silver", "red_alloy"}, new long[]{1, 5, 10}),
            new GT6AlloyRecipes("signalum", 16,
                    new String[]{"annealed_copper", "sterling_silver", "red_alloy"}, new long[]{1, 5, 10}),
            new GT6AlloyRecipes("constantan", 2, new String[]{"annealed_copper", "nickel"}, new long[]{1, 1}),
            new GT6AlloyRecipes("yttrium_barium_cuprate", 6,
                    new String[]{"annealed_copper", "barium", "yttrium"}, new long[]{3, 2, 1}),
            new GT6AlloyRecipes("yttrium_barium_cuprate", 6,
                    new String[]{"copper", "barium", "yttrium"}, new long[]{3, 2, 1}),
            new GT6AlloyRecipes("ferrite", 8, new String[]{"hematite", "lithium_oxide"}, new long[]{5, 3})
    };

    static final GT6AlloyRecipes[] ALL = withGlowstoneFamily();

    /** Audited original/additional recipe sets; an explicitly empty set is authoritative too. */
    public static boolean hasCompleteRecipeSet(String name) {
        if (name == null) return false;
        int separator = name.lastIndexOf(':');
        String normalized = name.substring(separator + 1).toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        switch (GT6MaterialIdentity.canonicalAlloyName(normalized)) {
            // MT.java:1041 defines composition, not a crucible alloy recipe.
            // Keep the verified CEu C3H5N3O9 identity out of host fallback recipes.
            case "glyceryl":
            case "glyceryltrinitrate":
            // MT.java:1294 only uumMcfg, no alloySimple/added recipe.
            case "tricalciumphosphate":
            case "bluephosphorus":
            case "redphosphorus":
            case "whitephosphorus":
            case "anyphosphorus":
            case "anymagiciron":
            case "anywoodorplastic":
            case "anygarnet":
            case "anyjasper":
            case "anytigereye":
            case "anyaventurine":
            case "anyamber":
            case "anythaumiccrystal":
            case "anyhexorium":
            case "wax":
            case "sand":
            case "stone":
            case "calcite":
            case "silicondioxide":
            case "blaze":
            case "anyblaze":
            case "prismarine":
            case "prismarinedark":
            case "anyprismarine":
            case "wheat":
            case "barley":
            case "rye":
            case "rice":
            case "oat":
            case "abyssaloat":
            case "corn":
            case "potato":
            case "anygrains":
            case "fishraw":
            case "fishcooked":
            case "fishrotten":
            case "fishoil":
            case "bone":
            case "tofu":
            case "siliconcarbide":
            case "duraniumalloy":
            case "tritaniumalloy":
            case "trinaquadalloy":
            case "trinitanium":
            case "wroughtiron":
            case "annealedcopper":
            case "netherite":
            case "desh":
            case "hssg":
            case "hsse":
            case "hsss":
            case "titaniumiridium":
            case "titaniumaluminide":
            // MT.java:1834,1839,1845: composition, not alloy recipes.
            case "netherizeddiamond":
            case "workersalloy":
            case "duralumin":
            case "celenegil":
            case "shadowsteel":
            case "inolashite":
            case "haderoth":
            case "desichalkos":
            case "tartarite":
            case "amordrine":
            case "ironwood":
            // MT.java:1677,1765: no alloy recipe registration.
            case "vulcanite":
            case "steeleaf":
            case "energeticsilver":
            case "alumite":
            case "manyullyn":
            case "vibraniumsteel":
            case "vibraniumsilver":
            case "vibramantium":
            case "claycompound":
            case "spectreiron":
            // MT.java:1796-1803,1820-1827: composition/attributes only.
            case "refinedglowstone":
            case "refinedobsidian":
            case "bedrockhslaalloy":
            case "manasteel":
            case "terrasteel":
            case "elvenelementium":
            case "gaiaspirit":
            case "elvorium":
            case "niflheimpower":
            case "muspelheimpower":
            case "redalloy":
            case "bluealloy":
            case "purplealloy":
            case "redstonealloy":
            case "nikolinealloy":
            case "electrotinealloy":
            case "electrumflux":
            case "conductiveiron":
            case "signalum":
            case "lumium":
            case "enderiumbase":
            case "enderium":
            case "obsidiansteel":
            case "pulsatingiron":
            case "energeticalloy":
            case "vibrantalloy":
            case "electricalsteel":
            case "soularium":
            case "endsteel":
            case "melodicalloy":
            case "stellaralloy":
            case "vividalloy":
            // MT.java:1747,1816-1817: no alloy registration.
            case "mingrade":
            case "crystallinealloy":
            case "crystallinepinkslime":
            case "steel":
            case "blacksteel":
            case "bluesteel":
            case "redsteel":
            case "vanadiumsteel":
            case "tungstensteel":
            case "tungstencarbide":
            case "titaniumgold":
            case "tantalumhafniumcarbide":
            case "meteoricsteel":
            case "meteoricblacksteel":
            case "meteoricbluesteel":
            case "meteoricredsteel":
            // Only composition/heat/steal, with no alloy recipe registration.
            case "damascussteel":
            case "hslasteel":
            case "springsteel":
            case "tungstenalloy":
            case "steelgalvanized":
            case "meteoriciron":
            case "electrum":
            case "sterlingsilver":
            case "rosegold":
            case "angmallen":
            case "inductivealloy":
            case "cdinagalloy":
            case "brass":
            case "cobaltbrass":
            case "bronze":
            case "blackbronze":
            case "bismuthbronze":
            case "hepatizon":
            case "arseniccopper":
            case "arsenicbronze":
            // MT.java:1697/1702: composition declarations, not alloy recipes.
            case "gildediron":
            case "aluminiumalloy":
            case "aluminumalloy":
            case "invar":
            case "constantan":
            case "nichrome":
            case "kanthal":
            case "magnalium":
            case "stainlesssteel":
            case "ultimet":
            case "tinalloy":
            case "batteryalloy":
            case "solderingalloy":
            case "osmiridium":
            case "vanadiumgallium":
            case "niobiumtitanium":
            case "aluminiumbrass": return true;
            default: return false;
        }
    }

    private static GT6AlloyRecipes[] withGlowstoneFamily() {
        List<GT6AlloyRecipes> recipes = new ArrayList<>(Arrays.asList(FIXED));
        // MT.java glowstone factory registers these exact family members.
        // The alloy loop explicitly excludes ordinary Glowstone. Missing
        // registered materials are skipped by the runtime recipe resolver.
        String[] variants = {"glowstone_ceres", "glowstone_io", "glowstone_enceladus",
                "glowstone_proteus", "glowstone_pluto", "gloomstone"};
        for (String variant : variants) {
            recipes.add(new GT6AlloyRecipes("energetic_alloy", 1,
                    new String[]{"inductive_alloy", variant}, new long[]{2, 1}));
            recipes.add(new GT6AlloyRecipes("lumium", 4,
                    new String[]{"tin", "silver", variant}, new long[]{3, 1, 4}));
        }
        return recipes.toArray(new GT6AlloyRecipes[0]);
    }

    final String output;
    final long outputUnits;
    final String[] inputs;
    final long[] inputUnits;

    /** Read-only recipe catalogue; array getters return copies for display consumers. */
    public static List<GT6AlloyRecipes> getRecipes() {
        return Collections.unmodifiableList(Arrays.asList(ALL));
    }

    public String getOutputName() { return output; }
    public long getOutputUnits() { return outputUnits; }
    public String[] getInputNames() { return inputs.clone(); }
    public long[] getInputUnits() { return inputUnits.clone(); }

    private GT6AlloyRecipes(String output, long outputUnits, String[] inputs, long[] inputUnits) {
        this.output = output;
        this.outputUnits = outputUnits;
        this.inputs = inputs;
        this.inputUnits = inputUnits;
    }
}
