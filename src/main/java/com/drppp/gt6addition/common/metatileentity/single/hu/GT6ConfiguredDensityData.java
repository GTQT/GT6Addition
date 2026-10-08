package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Map;

import static com.drppp.gt6addition.common.metatileentity.single.hu.GT6MineralDensityData.put;

/** Read-only MT.java configurations and statistics copies, not Material registrations.
 * Run after the mineral prerequisites. Preserve nested units, explicit dividers
 * and per-component integer truncation; heat/qual/generifying never sets density.
 */
final class GT6ConfiguredDensityData {
    private GT6ConfiguredDensityData() {}

    static void addTo(Map<String, Double> densities) {
        addChemicals(densities);
        addGems(densities);
        addClaysAndMixtures(densities);
    }

    private static void addChemicals(Map<String, Double> d) {
        // MT.java:1020-1057. The gas/acid factories add tags only: no
        // implicit real gas density or blanket 1500 kg/m^3 acid override.
        put(d, "hydrochloricacid", 0, new long[]{1, 1}, "hydrogen", "chlorine");
        put(d, "hydrogenfluoride", 0, new long[]{1, 1}, "hydrogen", "fluorine");
        // Existing host HF adapter, also used by GT6DeclaredPhaseData.
        // CEu FirstDegreeMaterials:1234 explicitly has H1:F1.
        put(d, "hydrofluoricacid", 0, new long[]{1}, "hydrogenfluoride");
        put(d, "nitrogenmonoxide", 0, new long[]{1, 1}, "nitrogen", "oxygen");
        put(d, "nitrogendioxide", 0, new long[]{1, 2}, "nitrogen", "oxygen");
        put(d, "ammonia", 0, new long[]{1, 3}, "nitrogen", "hydrogen");
        put(d, "carbonmonoxide", 0, new long[]{1, 1}, "carbon", "oxygen");
        put(d, "carbondioxide", 0, new long[]{1, 2}, "carbon", "oxygen");
        put(d, "sulfurdioxide", 0, new long[]{1, 2}, "sulfur", "oxygen");
        put(d, "hydrosulfuricacid", 0, new long[]{2, 1}, "hydrogen", "sulfur");
        put(d, "silveriodide", 0, new long[]{1, 1}, "silver", "iodine");
        put(d, "carborundum", 0, new long[]{1, 1}, "silicon", "carbon");
        put(d, "glass", 0, new long[]{1}, "silicondioxide");
        // :1078-1107. Gypsum's divider 6 leaves one sulfate AND one water,
        // not their average and not a real-world hydration formula.
        put(d, "tungsticacid", 0, new long[]{2, 1, 4}, "hydrogen", "tungsten", "oxygen");
        put(d, "aluminiumfluoride", 0, new long[]{1, 3}, "aluminium", "fluorine");
        put(d, "aluminiumhydroxide", 0, new long[]{1, 3, 3}, "aluminium", "oxygen", "hydrogen");
        put(d, "titaniumtetrachloride", 0, new long[]{1, 4}, "titanium", "chlorine");
        put(d, "manganesechloride", 0, new long[]{1, 2}, "manganese", "chlorine");
        put(d, "ferrouschloride", 0, new long[]{1, 2}, "iron", "chlorine");
        put(d, "ferricchloride", 0, new long[]{1, 3}, "iron", "chlorine");
        put(d, "ferricoxyhydroxide", 0, new long[]{1, 3, 3}, "iron", "oxygen", "hydrogen");
        put(d, "calciumchloride", 0, new long[]{1, 2}, "calcium", "chlorine");
        put(d, "calciumsulfate", 0, new long[]{1, 1, 4}, "calcium", "sulfur", "oxygen");
        put(d, "gypsum", 6, new long[]{6, 6}, "calciumsulfate", "water");
        put(d, "quicklime", 0, new long[]{1, 1}, "calcium", "oxygen");
        // :1121-1158,1293. Register dependencies before Niter and clays.
        put(d, "lithiumchloride", 0, new long[]{1, 1}, "lithium", "chlorine");
        put(d, "lithiumchlorate", 0, new long[]{1, 1, 3}, "lithium", "chlorine", "oxygen");
        put(d, "lithiumperchlorate", 0, new long[]{1, 1, 4}, "lithium", "chlorine", "oxygen");
        put(d, "lithiumhydroxide", 0, new long[]{1, 1, 1}, "lithium", "oxygen", "hydrogen");
        put(d, "sodiumhydroxide", 0, new long[]{1, 1, 1}, "sodium", "oxygen", "hydrogen");
        put(d, "sodiumsulfite", 0, new long[]{2, 1, 3}, "sodium", "sulfur", "oxygen");
        put(d, "sodiumpyrosulfate", 0, new long[]{2, 2, 7}, "sodium", "sulfur", "oxygen");
        put(d, "sodiumaluminate", 0, new long[]{1, 1, 2}, "sodium", "aluminium", "oxygen");
        put(d, "sodiumfluoride", 0, new long[]{1, 1}, "sodium", "fluorine");
        put(d, "cryolite", 0, new long[]{3, 1, 6}, "sodium", "aluminium", "fluorine");
        put(d, "iodinesalt", 0, new long[]{1, 1, 3}, "potassium", "iodine", "oxygen");
        put(d, "sylvite", 0, new long[]{1, 1}, "potassium", "chlorine");
        put(d, "potassiumnitrate", 0, new long[]{1, 1, 3}, "potassium", "nitrogen", "oxygen");
        put(d, "potassiumsulfite", 0, new long[]{2, 1, 3}, "potassium", "sulfur", "oxygen");
        put(d, "potassiumsulfate", 0, new long[]{2, 1, 4}, "potassium", "sulfur", "oxygen");
        put(d, "potassiumpyrosulfate", 0, new long[]{2, 2, 7}, "potassium", "sulfur", "oxygen");
        put(d, "potassiumcarbonate", 0, new long[]{2, 4}, "potassium", "carbontrioxide");
        put(d, "potassiumaluminate", 0, new long[]{1, 1, 2}, "potassium", "aluminium", "oxygen");
        put(d, "potassiumfluoride", 0, new long[]{1, 1}, "potassium", "fluorine");
        put(d, "niter", 0, new long[]{1, 1}, "potassiumnitrate", "sodiumnitrate");
        // :1163-1188. Black Vitriol really uses Fe:S=1:1. Martian
        // Vitriol's 18 is NOT the sum (17) of its configured units.
        put(d, "chloroauricacid", 0, new long[]{1, 4, 1}, "gold", "chlorine", "hydrogen");
        put(d, "chloroplatinicacid", 0, new long[]{1, 6, 2}, "platinum", "chlorine", "hydrogen");
        put(d, "stannicchloride", 0, new long[]{1, 4}, "tin", "chlorine");
        put(d, "blackvitriol", 0, new long[]{1, 1}, "iron", "sulfur");
        String[] vitriols = {"bluevitriol", "greenvitriol", "redvitriol", "pinkvitriol",
                "cyanvitriol", "whitevitriol", "grayvitriol"};
        String[] metals = {"copper", "iron", "cobalt", "magnesium", "nickel", "zinc", "manganese"};
        for (int i = 0; i < vitriols.length; i++)
            put(d, vitriols[i], 0, new long[]{1, 1, 4}, metals[i], "sulfur", "oxygen");
        put(d, "martianvitriol", 18, new long[]{2, 3, 12}, "iron", "sulfur", "oxygen");
        put(d, "vitriolofclay", 0, new long[]{5, 3, 9}, "alumina", "sulfur", "oxygen");
        // U_238 and U_235 explicitly both have 18.95 g/cm^3 (:482-483).
        // Retain the six literal configured identities; no isotope guessing.
        for (String name : new String[]{"uraniumtetrafluoride", "uranium238tetrafluoride", "uranium235tetrafluoride"})
            put(d, name, 0, new long[]{1, 4}, "uranium", "fluorine");
        for (String name : new String[]{"uraniumhexafluoride", "uranium238hexafluoride", "uranium235hexafluoride"})
            put(d, name, 0, new long[]{1, 6}, "uranium", "fluorine");
        put(d, "aquaregia", 0, new long[]{5, 8}, "nitricacid", "hydrochloricacid");
    }

    private static void addGems(Map<String, Double> d) {
        // :1371-1377. Each has the same explicit beryl configuration;
        // this does not copy a processing target or invent a generic family.
        for (String name : new String[]{"aquamarine", "morganite", "heliodor", "goshenite", "bixbite", "maxixe"})
            put(d, name, 0, new long[]{5, 3, 18, 3}, "alumina", "beryllium", "silicondioxide", "oxygen");
        // :1382-1390. Yellow Sapphire uses TiO2's divider-1 density.
        String[] sapphires = {"bluesapphire", "greensapphire", "purplesapphire", "yellowsapphire", "orangesapphire"};
        String[] traces = {"iron", "magnesium", "vanadium", "rutile", "copper"};
        for (int i = 0; i < sapphires.length; i++)
            put(d, sapphires[i], 6, new long[]{5, 1}, "alumina", traces[i]);
        put(d, "spinel", 0, new long[]{5, 1, 1}, "alumina", "magnesium", "oxygen");
        // BalasRuby calls steal(Ruby) BEFORE its own config: config wins.
        put(d, "balasruby", 0, new long[]{2, 1, 4}, "chromium", "magnesium", "oxygen");
        // :1393-1398. Spessartine is already explicitly registered upstream.
        put(d, "almandine", 0, new long[]{5, 3, 9, 3}, "alumina", "iron", "silicondioxide", "oxygen");
        put(d, "grossular", 0, new long[]{5, 3, 9, 3}, "alumina", "calcium", "silicondioxide", "oxygen");
        put(d, "pyrope", 0, new long[]{5, 3, 9, 3}, "alumina", "magnesium", "silicondioxide", "oxygen");
        put(d, "andradite", 0, new long[]{3, 2, 9, 6}, "calcium", "iron", "silicondioxide", "oxygen");
        put(d, "uvarovite", 0, new long[]{3, 2, 9, 6}, "calcium", "chromium", "silicondioxide", "oxygen");
        // :212-215,1402-1422: factory configs, not the colours or gem quality.
        for (String name : new String[]{"oceanjasper", "rainforestjasper", "bluejasper", "greenjasper", "yellowjasper"})
            put(d, name, 0, new long[]{2, 1}, "silicondioxide", "iron");
        for (String name : new String[]{"catseye", "dragoneye", "hawkseye", "blackeye", "tigeriron",
                "brownaventurine", "yellowaventurine", "blackaventurine", "blueaventurine", "redaventurine"})
            put(d, name, 0, new long[]{1}, "silicondioxide");
        for (String name : new String[]{"redfluorite", "pinkfluorite", "bluefluorite", "greenfluorite",
                "blackfluorite", "whitefluorite", "yellowfluorite", "orangefluorite", "magentafluorite"})
            put(d, name, 0, new long[]{1, 2}, "calcium", "fluorine");
        // :1425-1437. These nested gem configurations are not flattened.
        for (String name : new String[]{"topaz", "bluetopaz"})
            put(d, name, 0, new long[]{5, 3, 2, 3}, "alumina", "silicondioxide", "fluorine", "water");
        for (String name : new String[]{"tanzanite", "zanite"})
            put(d, name, 0, new long[]{15, 18, 4, 3, 4}, "alumina", "silicondioxide", "calcium", "water", "oxygen");
        put(d, "amazonite", 0, new long[]{5, 18, 2, 1}, "alumina", "silicondioxide", "potassium", "oxygen");
        put(d, "alexandrite", 0, new long[]{1, 1, 1}, "alumina", "beryllium", "oxygen");
        for (String name : new String[]{"opal", "onyxred", "onyxblack"})
            put(d, name, 0, new long[]{1}, "silicondioxide");
        put(d, "sugilite", 0, new long[]{1, 2, 2, 3, 36, 2},
                "potassium", "sodium", "pyrolusite", "lithium", "silicondioxide", "oxygen");
        put(d, "peridot", 0, new long[]{2, 1, 2}, "silicondioxide", "iron", "magnesium");
        put(d, "dioptase", 0, new long[]{3, 1, 1, 3}, "silicondioxide", "copper", "oxygen", "water");
        // :1516-1518,1545-1550. Lapis consumes the configured minerals.
        put(d, "lazurite", 0, new long[]{6, 6, 8, 8}, "alumina", "silicondioxide", "calcium", "sodium");
        put(d, "lapis", 0, new long[]{12, 2, 1, 1}, "lazurite", "sodalite", "pyrite", "calcite");
        for (String name : new String[]{"glowstoneceres", "glowstoneio", "glowstoneenceladus",
                "glowstoneproteus", "glowstonepluto", "gloomstone"})
            put(d, name, 0, new long[]{5, 3, 1, 1}, "phosphorite", "gold", "silicondioxide", "helium");
        // :1554-1565. Black Quartz and Fluix keep explicit sum dividers.
        for (String name : new String[]{"netherquartz", "voidquartz", "sunnyquartz", "lavenderquartz",
                "redquartz", "blazequartz", "smokeyquartz", "manaquartz", "elvenquartz"})
            put(d, name, 0, new long[]{1}, "silicondioxide");
        put(d, "quartzblack", 1, new long[]{1, 1}, "silicondioxide", "carbon");
        // Preserve the already verified phase-table spelling adapter.
        put(d, "blackquartz", 0, new long[]{1}, "quartzblack");
        put(d, "fluix", 2, new long[]{2, 1}, "silicondioxide", "redstone");
    }

    private static void addClaysAndMixtures(Map<String, Double> d) {
        // :1258,1262-1268: Magic has zero density; wax has no config and
        // keeps the verified constructor default, not an unknown fallback.
        put(d, "liveroot", 3, new long[]{3, 1}, "wood", "magic");
        for (String name : new String[]{"waxbee", "waxrefractory", "waxparaffin", "waxplant",
                "waxmagic", "waxamnesic", "waxsoulful"}) d.put(name, 1000D);
        // :217,1280-1289. Trace/U18 adds to Clay, rather than averaging it.
        put(d, "claybrick", 1, new long[]{1}, "ceramic");
        String[] clays = {"claybrown", "clayred", "bentonite", "palygorskite", "kaolinite"};
        String[] traces = {"lithiumhydroxide", "potassiumhydroxide", "sodiumhydroxide", "magnesium", "calcium"};
        for (int i = 0; i < clays.length; i++)
            put(d, clays[i], 18, new long[]{1, 18}, traces[i], "clay");
        put(d, "porcelain", 0, new long[]{2, 1, 1}, "ceramic", "silicondioxide", "potassiumfeldspar");
        // :1619-1622,1632-1634,1637. Explicit stealStatsElement calls.
        for (String name : new String[]{"redsand", "whiteendsand", "blackendsand", "soulsand",
                "concrete", "netherrack", "netherbrick"})
            put(d, name, 0, new long[]{1}, "silicondioxide");
        put(d, "bedrock", 0, new long[]{1}, "adamantium");
        // :1646,1649-1652. Basalz/Blitz/Blizz's verified default data
        // is provided before this table. No missing-prerequisite fallback.
        put(d, "oilshale", 0, new long[]{2, 1, 1}, "calcite", "milkyquartz", "clay");
        put(d, "petrotheum", 18, new long[]{9, 9, 9, 1}, "clay", "obsidian", "redstone", "basalz");
        put(d, "aerotheum", 18, new long[]{9, 9, 9, 1}, "sand", "potassiumnitrate", "redstone", "blitz");
        put(d, "pyrotheum", 18, new long[]{9, 9, 9, 1}, "coal", "sulfur", "redstone", "blaze");
        put(d, "cryotheum", 18, new long[]{2, 9, 9, 1}, "snow", "potassiumnitrate", "redstone", "blizz");
    }
}
