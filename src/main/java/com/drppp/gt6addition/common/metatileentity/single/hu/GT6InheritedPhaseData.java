package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Last heat(material)/steal(material) calls verified in the GT6 MT.java snapshot. */
final class GT6InheritedPhaseData {
    private static final Map<String, String> SOURCES = new HashMap<>();

    static {
        // MT.java:332,1569-1574: steal() follows the one-component configuration.
        put("redstonia", "redstone");
        put("palis", "lapis");
        put("diamantine", "diamond");
        put("voidcrystal", "coal");
        put("emeradic", "emerald");
        put("enori", "iron");
        // diamond() initially steals Carbon, but MT.java:1358-1368 ends
        // each declaration with heat(4200, C.mBoilingPoint). The final
        // values live in GT6DeclaredPhaseData, not a Carbon inheritance.
        put("brick", "ceramic");
        // BalasRuby's steal(Ruby) is followed by uumMcfg at MT.java:1390;
        // the latter recomputes temperature, so it is not a final inheritance.
        put("coalcoke", "coal");
        put("petcoke", "coal");
        put("chargedcertusquartz", "certusquartz");
        put("deepiron", "iron");
        put("shadowiron", "wroughtiron");
        put("adamantine", "adamantium");
        put("prometheum", "promethium");
        put("orichalcum", "copper");
        put("astralsilver", "silver");
        put("midasium", "gold");
        put("mithril", "platinum");
        put("springsteel", "hsla");
        put("tungstenalloy", "springsteel");
        put("hslaspringsteel", "springsteel"); // Actual internal name, MT.java:1722.
        put("hslatungstenalloy", "tungstenalloy"); // Actual internal name, MT.java:1723.
        put("pigiron", "wroughtiron");
        put("ironcompressed", "iron");
        put("ironcast", "iron");
        put("ironmagnetic", "iron");
        put("steelmagnetic", "steel");
        put("neodymiummagnetic", "neodymium");
        put("tungstensintered", "tungsten");
        put("darkthaumium", "thaumium");
        put("spectreiron", "iron");
        put("ancientdebris", "meteoriciron");
        put("netherite", "meteoricsteel");
        put("netherizeddiamond", "meteoricsteel");
        put("efrine", "meteoricsteel");
        put("meteorite", "meteoriciron");
        put("frozeniron", "iron");
        put("syrmorite", "gold");
        put("refinediron", "wroughtiron");
    }

    private GT6InheritedPhaseData() {}

    private static void put(String material, String source) { SOURCES.put(material, source); }

    static int meltingPoint(String name) {
        long point = inherited(name, false);
        return point < 0 || point > Integer.MAX_VALUE ? -1 : (int) point;
    }

    static long boilingPoint(String name) {
        long point = inherited(name, true);
        return point < 0 ? Long.MAX_VALUE : point;
    }

    private static long inherited(String name, boolean boiling) {
        if (name == null) return -1;
        String source = SOURCES.get(normalize(name));
        return source == null ? -1 : resolve(source, boiling, new HashSet<>());
    }

    private static long resolve(String name, boolean boiling, Set<String> visited) {
        name = normalize(name);
        if (!visited.add(name)) return -1;
        String source = SOURCES.get(name);
        if (source != null) return resolve(source, boiling, visited);
        CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
        long point = rule == null ? -1 : boiling ? rule.boilingPoint : rule.meltingPoint;
        if (point >= 0 && point != Long.MAX_VALUE) return point;
        point = boiling ? GT6ElementPhaseData.boilingPoint(name) : GT6ElementPhaseData.meltingPoint(name);
        if (point >= 0 && point != Long.MAX_VALUE) return point;
        point = boiling ? GT6DeclaredPhaseData.boilingPoint(name) : GT6DeclaredPhaseData.meltingPoint(name);
        return point >= 0 && point != Long.MAX_VALUE ? point : -1;
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
