package com.drppp.gt6addition.common.metatileentity.single.hu;

/** Exact GT6 OP.java storage/dense ore names, including declared aliases. */
final class CrucibleOreInputForm {
    final String materialName;
    final int multiplier;

    private CrucibleOreInputForm(String materialName, int multiplier) {
        this.materialName = materialName;
        this.multiplier = multiplier;
    }

    /** Use the resolved prefix, not an ore-name startsWith test: oreNetherQuartz
     * can mean ordinary ore + NetherQuartz, rather than oreNether + Quartz. */
    static int registeredOreMultiplier(String prefixName) {
        return "oreNether".equals(prefixName) || "oreEnd".equals(prefixName) ? 2 : 1;
    }

    static CrucibleOreInputForm find(String oreName) {
        if (oreName == null) return null;
        String[] prefixes = {"crateGt64Raw", "crateGt64Ore", "crateGtRaw", "crateGtOre",
                "blockRaw", "blockOre", "oreDense", "denseore"};
        int[] multipliers = {64, 64, 16, 16, 9, 9, 2, 2};
        for (int i = 0; i < prefixes.length; i++) {
            if (oreName.startsWith(prefixes[i]) && oreName.length() > prefixes[i].length()) {
                return new CrucibleOreInputForm(oreName.substring(prefixes[i].length()), multipliers[i]);
            }
        }
        return null;
    }
}
