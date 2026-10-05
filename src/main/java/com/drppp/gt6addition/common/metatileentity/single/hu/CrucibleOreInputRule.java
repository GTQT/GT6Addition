package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Locale;

/** MT.java:1972-1983 targetCrushing overrides, each with one material unit per base ore. */
final class CrucibleOreInputRule {
    final String target;
    final int multiplier;

    private CrucibleOreInputRule(String target, int multiplier) {
        this.target = target;
        this.multiplier = multiplier;
    }

    static CrucibleOreInputRule find(String name) {
        if (name == null) return null;
        switch (normalize(name)) {
            case "adamantium": return new CrucibleOreInputRule("adamantine", 2);
            case "iron": return new CrucibleOreInputRule("hematite", 3);
            case "aluminium": return new CrucibleOreInputRule("alumina", 2);
            case "titanium": return new CrucibleOreInputRule("rutile", 2);
            case "tungsten": return new CrucibleOreInputRule("scheelite", 2);
            case "fluorine": return new CrucibleOreInputRule("fluorite", 2);
            case "tantalum": return new CrucibleOreInputRule("tantalite", 2);
            case "niobium": return new CrucibleOreInputRule("columbite", 2);
            case "uranium":
            case "uranium238":
            case "u238": return new CrucibleOreInputRule("uraninite", 2);
            case "nq528":
            case "naquadahenriched":
            case "enrichednaquadah": return new CrucibleOreInputRule("naquadah", 2);
            case "nq522":
            case "naquadria": return new CrucibleOreInputRule("naquadah", 4);
            case "dilithium": return new CrucibleOreInputRule("dolamide", 2);
            default: return null;
        }
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
