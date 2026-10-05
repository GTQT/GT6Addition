package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** MT.java ore multipliers, distinct from CEu machine-processing yields. */
final class GT6OreMultiplierData {
    private static final Set<String> KNOWN = knownMaterials();

    private GT6OreMultiplierData() {}

    static long multiplier(String name, long hostMultiplier) {
        String key = normalize(name);
        CrucibleOreInputRule crushing = CrucibleOreInputRule.find(key);
        if (crushing != null) return crushing.multiplier;
        switch (key) {
            // MT.java:1984-2014. Targets remain the original material.
            case "meteorite": case "meteoriciron": case "meteoricsteel":
            case "amber": case "goldenamber": case "ambergolden":
            case "dominicanamber": case "amberdominican":
            case "zircon": case "draconium": case "borax": case "cassiterite":
            case "monazite": case "scabyst": case "moonstone": return 2;
            case "bastnasite": case "bluephosphorus": case "redphosphorus": case "whitephosphorus":
            // GT6 Phosphorus is Ca3(PO4)2, not CEu elemental Phosphorus.
            case "tricalciumphosphate":
            case "sodiumnitrate": case "potassiumnitrate":
            case "saltpeter": case "salpeter": case "nitrate": case "chimerite": return 3;
            case "apatite": case "bone": case "sunstone": return 4;
            case "lapis": case "sodalite": case "lazurite": case "malachite":
            case "azurite": case "eudialyte": return 5;
            case "perlite": return 8;
            // MT.java:164-165,1540-1550: glowstone/redstone factories.
            case "redstone": case "nikolite": case "electrotine": case "teslatite":
            case "glowstone": case "glowstoneceres": case "glowstoneio":
            case "glowstoneenceladus": case "glowstoneproteus": case "glowstonepluto":
            case "gloomstone": return 4;
            // MT.java:179,1553-1566: quartz factory, including Fluix.
            case "milkyquartz": case "netherquartz": case "voidquartz": case "sunnyquartz":
            case "lavenderquartz": case "redquartz": case "blazequartz":
            case "smokeyquartz": case "smokyquartz": case "quartzsmoky":
            case "manaquartz": case "elvenquartz": case "quartzblack": case "blackquartz":
            case "certusquartz": case "chargedcertusquartz": case "fluix": return 2;
            // MT.java:202,1585-1593: Thaumcraft crystal factory.
            case "infuseddull": case "infusedvis": case "infusedair": case "infusedfire":
            case "infusedearth": case "infusedwater": case "infusedentropy":
            case "infusedorder": case "infusedbalance": return 2;
            // MT.java:203,1596-1611: only the five non-commented declarations.
            case "hexoriumblack": case "hexoriumwhite": return 3;
            case "hexoriumred": case "hexoriumgreen": case "hexoriumblue": return 4;
            default:
                // OreDictMaterial.java:223 defaults to one. Known GT6 materials
                // must not silently inherit a different CEu ore multiplier.
                return KNOWN.contains(key) ? 1 : Math.max(1, hostMultiplier);
        }
    }

    private static Set<String> knownMaterials() {
        Set<String> result = new HashSet<>();
        // The reference material array has 32767 slots; identity data contains
        // only verified literal declarations, not CEu IDs or guessed names.
        for (int id = 1; id < 32767; id++) {
            String name = GT6MaterialIdentity.name(id);
            if (name != null) result.add(normalize(name));
        }
        return result;
    }

    private static String normalize(String name) {
        return name == null ? "" : GT6MaterialIdentity.canonicalOxideName(
                name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ""));
    }
}
