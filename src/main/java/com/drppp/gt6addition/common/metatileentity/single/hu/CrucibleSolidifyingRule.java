package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

/** MT.java explicit one-U solidifying targets and setAllToTheOutputOf copies.
 * Unknown materials retain GT6's default self target; smelting is not reversible.
 * Water/lava use the vessel's dedicated special-fluid process.
 */
final class CrucibleSolidifyingRule {
    private static final Set<String> SNAPSHOT_MATERIALS = snapshotMaterials();
    private CrucibleSolidifyingRule() {}

    private static Set<String> snapshotMaterials() {
        Set<String> names = new HashSet<>();
        // Reuse the source-verified literal ID/name table, not a second list
        // inferred from whichever fluids happen to be registered by CEu.
        for (int id = 1; id <= Short.MAX_VALUE; id++) {
            String name = GT6MaterialIdentity.name(id);
            if (name == null) continue;
            names.add(normalize(name));
            names.add(GT6MaterialIdentity.canonicalAlloyName(normalize(name)));
            names.add(GT6MaterialIdentity.canonicalElementName(normalize(name)));
            String hostName = GT6MaterialIdentity.hostRecyclingName(name);
            int separator = hostName.indexOf(':');
            names.add(normalize(separator < 0 ? hostName : hostName.substring(separator + 1)));
        }
        // ANY.WoodPlastic has default self targets, not MT.Wood's ash target.
        names.add("anywoodorplastic");
        java.util.Collections.addAll(names, "anygarnet", "anyjasper", "anytigereye", "anyaventurine", "anyamber");
        names.add("anyblaze");
        names.add("anythaumiccrystal");
        names.add("anyhexorium");
        return names;
    }

    private static String normalize(String name) {
        return name == null ? "" : GT6MaterialIdentity.canonicalOxideName(GT6MaterialIdentity.canonicalElementName(
                name.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "")));
    }

    static boolean hasAuthoritativeSelfTarget(String name) {
        return name != null && target(name) == null && isSnapshotMaterial(name);
    }

    /** Shared identity membership, not a statement about hot or cold targets. */
    static boolean isSnapshotMaterial(String name) {
        return name != null && SNAPSHOT_MATERIALS.contains(normalize(name));
    }

    static String target(String name) {
        if (name == null) return null;
        switch (name.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "")) {
            case "ironiiioxide": return "hematite";
            case "anyphosphorus": return "gregtech:tricalcium_phosphate";
            case "anymagiciron": return "gregtech:iron";
            case "anyprismarine": return "gt6addition:prismarine";
            case "anygrains": return "gregtech:wheat";
            // gem_aa copies targetSolidifying independently of targetSmelting.
            case "redstonia": return "redstone";
            case "palis": return "lapis";
            case "diamantine": return "diamond";
            case "voidcrystal": return "coal";
            case "emeradic": return "emerald";
            case "enori": return "iron";
            case "woodtreated":
            case "treatedwood":
            case "woodpolished": return "wood"; // MT.java:1245-1246
            case "brick":
            case "claybrick": return "ceramic"; // MT.java:1280 internal name + ore alias
            case "tungstensintered":
            case "sinteredtungsten": return "tungsten"; // MT.java:1732
            case "refinedglowstone":
            case "glowstonerefined": return "glowstone"; // MT.java:1796
            case "refinedobsidian":
            case "obsidianrefined": return "obsidian"; // MT.java:1797
            case "refinediron": return "iron"; // MT.java:1958
            default: return null;
        }
    }
}
