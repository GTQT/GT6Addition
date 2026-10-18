package com.drppp.gt6addition.common.metatileentity.single.hu;

/** Exact GT6 OP.java standard/dense ore and storage-form names, including aliases. */
final class CrucibleOreInputForm {
    private static final String[] NON_STANDARD_ORE_PREFIXES = {
            // These GT6 prefixes are DUST_ORE, bedrock, or TFC/Railcraft
            // quantity classes, not STANDARD_ORE/DENSE_ORE inputs.
            "oreStrangesand", "oreRedSand", "oreGravel", "oreBedrock", "oreRich", "oreNormal",
            "oreSmall", "orePoor", "oreSand", "oreMud"
    };

    private static final String[] ACCEPTED_PREFIXES = {
            "crateGt64Raw", "crateGt64Ore", "crateGtRaw", "crateGtOre", "blockRaw", "blockOre",
            "oreLightprismarine", "oreDarkprismarine", "oreVanillagranite", "oreVanillastone",
            "oreBlackgranite", "oreRedgranite", "oreGreenschist", "oreBlueschist", "orePinkschist",
            "oreGrayschist", "oreKepler22b", "oreBetweenstone", "oreLivingrock", "oreQuartzite",
            "oreSandstone", "oreKomatiite", "oreUmberstone", "oreKimberlite", "oreHolystone", "oreDeadrock",
            "oreNetherrack", "oreEndstone", "oreBlackstone", "oreDeepslate", "oreGanymede", "oreCallisto",
            "oreNeptune", "oreEuropa", "oreMercury", "oreJupiter", "oreTitania", "oreAndesite", "oreDiorite",
            "orePhobos", "oreDeimos", "oreVenus", "oreCeres", "oreSaturn", "oreIapetus", "oreUranus",
            "oreOberon", "oreTriton", "orePluto", "oreEris", "oreMoon", "oreMars",
            "oreSpace", "oreRhea", "oreTitan", "oreIo", "orePitstone", "oreBasalt", "oreMarble",
            "oreLimestone", "oreSiltstone", "oreShale", "oreSlate", "oreGneiss", "oreNether", "oreDense",
            "oreGravel", "oreStrangesand", "oreRedSand", "oreSand", "oreMud", "oreBedrock", "oreRich",
            "oreNormal", "oreSmall", "orePoor", "denseore", "oreEnd", "oreRaw", "rawOre", "oreGem", "raw",
            "ore"
    };

    final String materialName;
    final int multiplier;

    private CrucibleOreInputForm(String materialName, int multiplier) {
        this.materialName = materialName;
        this.multiplier = multiplier;
    }

    /** Use a resolved CEu prefix before this name parser: oreNetherQuartz can
     * mean ordinary ore + NetherQuartz, rather than oreNether + Quartz. */
    static int registeredOreMultiplier(String prefixName) {
        return "oreNether".equals(prefixName) || "oreEnd".equals(prefixName) ||
                "oreDense".equals(prefixName) ? 2 : 1;
    }

    static CrucibleOreInputForm find(String oreName) {
        if (oreName == null) return null;
        // GT6's oreNether + Quartz and the ordinary ore + NetherQuartz share
        // this spelling. When CEu has no resolved prefix, refusing is safer
        // than silently applying the dense-ore factor to the wrong material.
        if ("oreNetherQuartz".equals(oreName)) return null;
        String matchedPrefix = null;
        // Longest exact GT6 prefix wins (for example oreNetherrack before ore).
        for (String prefix : ACCEPTED_PREFIXES) {
            if (oreName.startsWith(prefix) && oreName.length() > prefix.length() &&
                    (matchedPrefix == null || prefix.length() > matchedPrefix.length())) {
                matchedPrefix = prefix;
            }
        }
        for (String prefix : NON_STANDARD_ORE_PREFIXES) {
            if (oreName.startsWith(prefix) && oreName.length() > prefix.length() &&
                    (matchedPrefix == null || prefix.length() > matchedPrefix.length())) {
                matchedPrefix = prefix;
            }
        }
        if (matchedPrefix == null) return null;
        for (String prefix : NON_STANDARD_ORE_PREFIXES) {
            if (prefix.equals(matchedPrefix)) return null;
        }

        int multiplier;
        switch (matchedPrefix) {
            case "crateGt64Raw":
            case "crateGt64Ore": multiplier = 64; break;
            case "crateGtRaw":
            case "crateGtOre": multiplier = 16; break;
            case "blockRaw":
            case "blockOre": multiplier = 9; break;
            case "oreNether":
            case "oreDense":
            case "denseore":
            case "oreEnd": multiplier = 2; break;
            default: multiplier = 1;
        }
        return new CrucibleOreInputForm(oreName.substring(matchedPrefix.length()), multiplier);
    }
}
