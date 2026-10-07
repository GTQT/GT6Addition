package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** ANY.java:36-92,97-149. Statistics and processing outputs are independent.
 * steal() copies heat/density, stealLooks() does neither, and
 * setAllToTheOutputOf() copies both target identities AND their quantities.
 * These negative-ID recipe families must not inherit their members' tags.
 */
final class GT6TechnicalMaterialData {
    static final class Profile {
        final String statsSource;
        final String targetsSource;
        final String nativeSelfOutput;

        private Profile(String statsSource, String targetsSource, String nativeSelfOutput) {
            this.statsSource = statsSource;
            this.targetsSource = targetsSource;
            this.nativeSelfOutput = nativeSelfOutput;
        }
    }

    private static final Map<String, Profile> PROFILES;
    static {
        Map<String, Profile> profiles = new LinkedHashMap<>();
        copy(profiles, "anyglowstone", "glowstone", "gregtech:glowstone");
        copy(profiles, "anydiamond", "diamond", "gregtech:diamond");
        copy(profiles, "anysapphire", "sapphire", "gregtech:sapphire");
        copy(profiles, "anyemerald", "emerald", "gregtech:emerald");
        copy(profiles, "anyamethyst", "amethyst", "gregtech:amethyst");
        stats(profiles, "anygarnet", "spessartine");
        stats(profiles, "anyjasper", "jasper");
        stats(profiles, "anytigereye", "tigereye");
        stats(profiles, "anyaventurine", "greenaventurine");
        stats(profiles, "anyamber", "amber");
        copy(profiles, "anyfluorite", "fluorite", "gtqtcore:fluorite");
        // MT.Phosphorus is Ca3(PO4)2, NOT the elemental MT.P/CEu phosphorus.
        copy(profiles, "anyphosphorus", "tricalciumphosphate", "gregtech:tricalcium_phosphate");
        stats(profiles, "anyblaze", "blaze");
        copy(profiles, "anyprismarine", "prismarine", "gt6addition:prismarine");
        copy(profiles, "anygrains", "wheat", "gregtech:wheat");
        copy(profiles, "anyflour", "wheat", "gregtech:wheat");
        copy(profiles, "anyflourorgrains", "wheat", "gregtech:wheat");
        copy(profiles, "anywax", "wax", "gt6addition:wax");
        copy(profiles, "anystone", "stone", "gregtech:stone");
        copy(profiles, "anycalcite", "calcite", "gregtech:calcite");
        copy(profiles, "anyclay", "clay", "gregtech:clay");
        copy(profiles, "anysalt", "salt", "gregtech:salt");
        copy(profiles, "anyiron", "iron", "gregtech:iron");
        copy(profiles, "anyironorsteel", "iron", "gregtech:iron");
        copy(profiles, "anyironsteel", "steel", "gregtech:steel");
        copy(profiles, "anyblacksteel", "blacksteel", "gregtech:black_steel");
        copy(profiles, "anybluesteel", "bluesteel", "gregtech:blue_steel");
        copy(profiles, "anyredsteel", "redsteel", "gregtech:red_steel");
        profiles.put("anymagiciron", new Profile("manasteel", "iron", "gregtech:iron"));
        copy(profiles, "anycopper", "copper", "gregtech:copper");
        copy(profiles, "anyashes", "ash", "gregtech:ash");
        copy(profiles, "anycarbon", "carbon", "gregtech:carbon");
        // ANY.Coal copies Carbon, not native Coal's half-Carbon output.
        copy(profiles, "anycoalcarbon", "carbon", "gregtech:carbon");
        copy(profiles, "anysilicon", "silicon", "gregtech:silicon");
        copy(profiles, "anysilicondioxide", "silicondioxide", "gregtech:silicon_dioxide");
        profiles.put("quartz", new Profile("milkyquartz", "silicondioxide", "gregtech:silicon_dioxide"));
        copy(profiles, "anysand", "sand", "gt6addition:sand");
        copy(profiles, "anytungsten", "tungsten", "gregtech:tungsten");
        stats(profiles, "anythaumiccrystal", "infuseddull");
        stats(profiles, "hexorium", "hexoriumwhite");
        for (String name : new String[]{"anywood", "anydefaultwood", "anynormalwood", "anymagicalwood",
                "anytreatedwood", "anyuntreatedwood"}) {
            copy(profiles, name, "wood", "gregtech:wood");
        }
        stats(profiles, "anywoodorplastic", "wood");
        copy(profiles, "anyrubber", "rubber", "gregtech:rubber");
        copy(profiles, "anyplastic", "plastic", "gregtech:plastic");
        profiles.put("anyhardplastic", new Profile("hardplastic", "plastic", "gregtech:plastic"));
        // _Steel/_Bronze/_Metal ONLY stealLooks. Constructor defaults remain:
        // self/U targets, 1000/3000 K, 1 g/cm^3, no MELTING/UNBURNABLE.
        stats(profiles, "anysteel", null);
        stats(profiles, "anybronze", null);
        stats(profiles, "anymetal", null);
        PROFILES = Collections.unmodifiableMap(profiles);
    }

    private GT6TechnicalMaterialData() {}

    private static void copy(Map<String, Profile> profiles, String name, String source, String output) {
        profiles.put(name, new Profile(source, source, output));
    }

    private static void stats(Map<String, Profile> profiles, String name, String source) {
        profiles.put(name, new Profile(source, null, null));
    }

    static Profile find(String name) {
        if (name == null) return null;
        String normalized = name.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        // Only previously supported explicit aliases; not arbitrary namespaces.
        if ("anycoal/carbon".equals(normalized)) normalized = "anycoalcarbon";
        if ("anyhexorium".equals(normalized)) normalized = "hexorium";
        return PROFILES.get(normalized);
    }

    static boolean contains(String name) { return find(name) != null; }

    /** Exactly 53 source identities; legacy aliases do not count as new families. */
    static Set<String> names() { return PROFILES.keySet(); }

    static int meltingPoint(Profile profile) {
        return profile.statsSource == null ? 1000 : CrucibleMaterialPhaseData.knownMeltingPoint(profile.statsSource);
    }

    static long boilingPoint(Profile profile) {
        return profile.statsSource == null ? 3000L : CrucibleMaterialPhaseData.boilingPoint(profile.statsSource);
    }
}
