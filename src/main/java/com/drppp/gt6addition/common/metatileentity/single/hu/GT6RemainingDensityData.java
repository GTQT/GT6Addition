package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Map;

import static com.drppp.gt6addition.common.metatileentity.single.hu.GT6MineralDensityData.put;

/** Read-only final densities of MT.java's remaining literal configurations.
 * This is NOT a material registry. Run after mineral/gem prerequisites and
 * before TECH/ANY copies. Preserve nested components, integer units and the
 * final steal/setDensity overrides, not the host's chemical composition.
 */
final class GT6RemainingDensityData {
    private GT6RemainingDensityData() {}

    static void addTo(Map<String, Double> d) {
        addAlloys(d);
        addRocksAndConvertedGems(d);
        addUnconfiguredFluids(d);
    }

    private static void copy(Map<String, Double> d, String source, String... names) {
        for (String name : names) put(d, name, 1, new long[]{1}, source);
    }

    private static void defaults(Map<String, Double> d, String... names) {
        // Only individually verified declarations without density/configuration
        // changes: constructor 1.0 g/cm^3, NOT the unknown-material fallback.
        for (String name : names) d.put(name, 1000D);
    }

    private static void addAlloys(Map<String, Double> d) {
        // MT.java:1655-1669,1680,1724-1736,1743,1821,1847-1848.
        // heat, qual, generification and processing targets never copy density.
        copy(d, "iron", "wroughtiron", "deepiron", "shadowiron", "pigiron", "ironcompressed",
                "ironmagnetic", "darkiron", "meteoriciron", "meteorite", "frozeniron");
        // MT.java:1958 RefinedIron.steal(WroughtIron) copies element statistics,
        // including density; its later setAllToTheOutputOf(Fe) copies targets only.
        copy(d, "wroughtiron", "refinediron");
        copy(d, "copper", "annealedcopper", "infuscolium", "vyroxeres");
        copy(d, "aluminium", "alduorite");
        copy(d, "tungsten", "rubracium", "tungstensintered");
        copy(d, "nickel", "meutoite", "oureclase");
        copy(d, "magnesium", "lemurite");
        copy(d, "lead", "aredrite");
        copy(d, "antimony", "ceruclase");
        copy(d, "platinum", "kalendrite");
        copy(d, "zinc", "carmot");
        copy(d, "mercury", "sanguinite");
        copy(d, "manganese", "eximite");
        copy(d, "tin", "ignatius");
        copy(d, "steel", "steelmagnetic", "meteoricsteel", "hslasteel", "hslaspringsteel",
                "hslatungstenalloy", "terrasteel");
        copy(d, "neodymium", "neodymiummagnetic");
        // :1705-1723. HSLA/SpringSteel/TungstenAlloy call steal AFTER
        // configuring: their copied density wins, unlike coated materials.
        put(d, "bronze", 0, new long[]{3, 1}, "copper", "tin");
        put(d, "angmallen", 0, new long[]{1, 1}, "gold", "wroughtiron");
        put(d, "goldinductive", 0, new long[]{1, 1}, "gold", "redstone");
        put(d, "cdinagalloy", 0, new long[]{1, 1, 1}, "cadmium", "indium", "silver");
        put(d, "gildediron", 9, new long[]{9, 1}, "iron", "gold");
        put(d, "cobaltbrass", 0, new long[]{7, 1, 1}, "brass", "aluminium", "cobalt");
        put(d, "aluminiumalloy", 45, new long[]{45, 1}, "aluminium", "silicon");
        put(d, "hepatizon", 0, new long[]{1, 1}, "gold", "bronze");
        put(d, "arseniccopper", 0, new long[]{3, 1}, "copper", "arsenic");
        put(d, "arsenicbronze", 0, new long[]{1, 4}, "arsenic", "bronze");
        put(d, "damascussteel", 50, new long[]{50, 1, 1}, "steel", "vanadium", "tungsten");
        put(d, "vanadiumsteel", 0, new long[]{4, 1}, "steel", "vanadium");
        put(d, "tungstensteel", 0, new long[]{1, 1}, "steel", "tungsten");
        put(d, "steelgalvanized", 9, new long[]{9, 1}, "steel", "zinc");
        put(d, "titaniumgold", 0, new long[]{3, 1}, "titanium", "gold");
        put(d, "tantalumhafniumcarbide", 0, new long[]{4, 1, 5}, "tantalum", "hafnium", "carbon");
        // :1743-1763. Explicit final statistics overrides supersede configs.
        copy(d, "copper", "redalloy");
        copy(d, "silver", "bluealloy");
        copy(d, "silicon", "redstonealloy", "nikolinealloy");
        copy(d, "iron", "electrotinealloy", "conductiveiron");
        copy(d, "electrum", "electrumflux");
        put(d, "purplealloy", 1, new long[]{1, 1}, "redalloy", "bluealloy");
        put(d, "mingrade", 0, new long[]{1, 1}, "copper", "redstone");
        put(d, "invar", 0, new long[]{2, 1}, "wroughtiron", "nickel");
        put(d, "constantan", 0, new long[]{1, 1}, "copper", "nickel");
        put(d, "nichrome", 0, new long[]{4, 1}, "nickel", "chromium");
        put(d, "kanthal", 0, new long[]{1, 1, 1}, "wroughtiron", "aluminium", "chromium");
        put(d, "magnalium", 0, new long[]{1, 2}, "magnesium", "aluminium");
        put(d, "stainlesssteel", 0, new long[]{4, 3, 1, 1}, "wroughtiron", "invar", "chromium", "manganese");
        put(d, "ultimet", 0, new long[]{5, 1, 2, 1}, "cobalt", "nickel", "chromium", "molybdenum");
        put(d, "tinalloy", 0, new long[]{1, 1}, "tin", "wroughtiron");
        put(d, "batteryalloy", 0, new long[]{4, 1}, "lead", "antimony");
        put(d, "solderingalloy", 0, new long[]{9, 1}, "tin", "antimony");
        // :1739-1741. Generifying ordinary coloured steel is not stats copying.
        put(d, "meteoricblacksteel", 0, new long[]{1, 1, 3}, "nickel", "blackbronze", "meteoricsteel");
        put(d, "meteoricbluesteel", 0, new long[]{1, 1, 2, 4},
                "sterlingsilver", "bismuthbronze", "meteoricsteel", "meteoricblacksteel");
        put(d, "meteoricredsteel", 0, new long[]{1, 1, 2, 4},
                "rosegold", "brass", "meteoricsteel", "meteoricblacksteel");
        // :1675-1688,1764-1794. Magic is explicitly zero-density. Dividers
        // 1/2/4/18 must not be replaced with sums of coefficients.
        put(d, "prometheum", 0, new long[]{3, 4}, "promethium", "oxygen");
        put(d, "vulcanite", 0, new long[]{1, 1}, "copper", "tellurium");
        put(d, "orichalcum", 4, new long[]{3, 1, 2}, "copper", "zinc", "magic");
        put(d, "astralsilver", 2, new long[]{2, 1}, "silver", "magic");
        put(d, "midasium", 2, new long[]{2, 1}, "gold", "magic");
        put(d, "mithril", 2, new long[]{2, 1}, "platinum", "magic");
        put(d, "celenegil", 0, new long[]{1, 1}, "platinum", "orichalcum");
        put(d, "shadowsteel", 0, new long[]{1, 1}, "shadowiron", "lemurite");
        put(d, "inolashite", 0, new long[]{1, 1}, "alduorite", "ceruclase");
        put(d, "haderoth", 0, new long[]{1, 1}, "mithril", "rubracium");
        put(d, "desichalkos", 0, new long[]{1, 1}, "eximite", "meutoite");
        put(d, "tartarite", 0, new long[]{1, 1}, "adamantine", "atlarus");
        put(d, "amordrine", 0, new long[]{1, 1}, "prometheum", "kalendrite");
        put(d, "ironwood", 18, new long[]{8, 9, 2}, "wroughtiron", "liveroot", "angmallen");
        put(d, "steeleaf", 1, new long[]{1, 1}, "steel", "magic");
        put(d, "knightmetal", 2, new long[]{2, 1}, "steel", "magic");
        put(d, "fierysteel", 1, new long[]{1, 1}, "steel", "magic");
        put(d, "fireleaf", 1, new long[]{1, 2}, "steel", "magic");
        String[] flames = {"meteoflamesteel", "meteoflameblacksteel", "meteoflamebluesteel", "meteoflameredsteel", "flamascussteel"};
        String[] bases = {"meteoricsteel", "meteoricblacksteel", "meteoricbluesteel", "meteoricredsteel", "damascussteel"};
        for (int i = 0; i < flames.length; i++) put(d, flames[i], 1, new long[]{1, 1}, bases[i], "magic");
        put(d, "thaumium", 1, new long[]{1, 1}, "iron", "magic");
        defaults(d, "darkthaumium", "voidmetal", "ardite", "gaiaspirit", "mauftrium", "ancientdebris",
                "efrine", "kreknorite", "syrmorite", "octine", "bedrockium", "draconium", "cosmicneutronium", "infinity");
        put(d, "osmiridium", 0, new long[]{1, 1}, "osmium", "iridium");
        put(d, "chromiumdioxide", 1, new long[]{1, 2}, "chromium", "oxygen");
        put(d, "vanadiumgallium", 0, new long[]{3, 1}, "vanadium", "gallium");
        put(d, "yttriumbariumcuprate", 6, new long[]{1, 2, 3, 7}, "yttrium", "barium", "copper", "oxygen");
        put(d, "niobiumnitride", 0, new long[]{1, 1}, "niobium", "nitrogen");
        put(d, "niobiumtitanium", 0, new long[]{1, 1}, "niobium", "titanium");
        put(d, "aluminiumbrass", 0, new long[]{3, 1}, "aluminium", "copper");
        put(d, "alumite", 9, new long[]{5, 2, 18}, "alumina", "wroughtiron", "obsidian");
        put(d, "manyullyn", 0, new long[]{1, 1}, "cobalt", "ardite");
        put(d, "vibraniumsteel", 0, new long[]{1, 3}, "vibranium", "steel");
        put(d, "vibraniumsilver", 0, new long[]{1, 3}, "vibranium", "silver");
        put(d, "vibramantium", 0, new long[]{1, 3}, "vibranium", "adamantium");
        // :1791-1812. Lumium includes four Glowstone units in divider4;
        // energetic/vibrant alloys and Soularium use sum density, not average.
        put(d, "signalum", 8, new long[]{1, 2, 5}, "copper", "silver", "redalloy");
        put(d, "lumium", 4, new long[]{3, 1, 4}, "tin", "silver", "glowstone");
        put(d, "enderiumbase", 4, new long[]{2, 1, 1}, "tin", "silver", "platinum");
        put(d, "enderium", 1, new long[]{1, 1}, "enderiumbase", "enderpearl");
        put(d, "bedrockhslaalloy", 1, new long[]{4, 1}, "bedrock", "hslasteel");
        put(d, "pulsatingiron", 1, new long[]{1, 1}, "wroughtiron", "enderpearl");
        put(d, "energeticalloy", 1, new long[]{2, 1}, "goldinductive", "glowstone");
        put(d, "vibrantalloy", 1, new long[]{1, 1}, "energeticalloy", "enderpearl");
        put(d, "electricalsteel", 1, new long[]{1, 1}, "steel", "silicon");
        put(d, "soularium", 1, new long[]{9, 1}, "soulsand", "gold");
        put(d, "claycompound", 1, new long[]{2, 1}, "stone", "ceramic");
        put(d, "spectreiron", 1, new long[]{1, 1}, "wroughtiron", "ectoplasm");
        // :1833-1835,1853-1876. These declarations configure large/non-unit
        // dividers; later heat() never changes their computed densities.
        put(d, "netherite", 1, new long[]{4, 4}, "gold", "ancientdebris");
        put(d, "netherizeddiamond", 4, new long[]{1, 4}, "netherite", "diamond");
        // :1840-1841. Literal Duranium/Tritanium are these alloys, whereas
        // CEu uses those unsuffixed registry names for Dn/Tn elements.
        put(d, "duraniumalloy", 0, new long[]{7, 1}, "duraniumelemental", "magnesium");
        put(d, "tritaniumalloy", 0, new long[]{3, 1}, "tritaniumelemental", "duraniumelemental");
        put(d, "hssg", 0, new long[]{5, 1, 2, 1}, "tungstensteel", "chromium", "molybdenum", "vanadium");
        put(d, "hsse", 0, new long[]{6, 1, 1, 1}, "hssg", "cobalt", "manganese", "silicon");
        put(d, "hsss", 0, new long[]{6, 2, 1}, "hssg", "osmiridium", "iridium");
        copy(d, "draconium", "draconiumawakened");
        put(d, "crystalmatrix", 1, new long[]{20, 2}, "diamond", "netherstar");
        put(d, "trinaquadalloy", 0, new long[]{6, 2, 1}, "trinium", "naquadah", "carbon");
        put(d, "trinitanium", 0, new long[]{2, 1}, "trinium", "titanium");
        put(d, "titaniumiridium", 0, new long[]{1, 1}, "iridium", "titanium");
        put(d, "titaniumaluminide", 3, new long[]{3, 7}, "titanium", "aluminium");
    }

    private static void addRocksAndConvertedGems(Map<String, Double> d) {
        // gem_aa (:332-334) configures and steals the named source. No
        // identity or smelting target is folded by this statistics table.
        copy(d, "redstone", "redstonia");
        copy(d, "lapis", "palis");
        copy(d, "diamond", "diamantine");
        copy(d, "emerald", "emeradic");
        copy(d, "iron", "enori");
        // :3765,3776,3802,3815-3818,3828-3830. Flint is SiO2, not Stone's
        // configured smelting product. DarkAshes keeps the constructor 1000.
        put(d, "diatomite", 0, new long[]{8, 1, 1}, "flint", "hematite", "sapphire");
        put(d, "garnetsand", 0, new long[]{1, 1, 1, 1, 1, 1},
                "almandine", "andradite", "grossular", "pyrope", "spessartine", "uvarovite");
        put(d, "skystone", 0, new long[]{2, 1, 1, 5}, "peridot", "rareearth", "meteoriciron", "obsidian");
        put(d, "komatiite", 0, new long[]{1, 2, 6, 3}, "peridot", "magnesiumcarbonate", "flint", "darkashes");
        put(d, "pumice", 0, new long[]{3, 2, 4, 2}, "peridot", "magnesiumcarbonate", "flint", "darkashes");
        for (String name : new String[]{"gabbro", "basalt"})
            put(d, name, 0, new long[]{1, 3, 8, 4}, "peridot", "calcite", "flint", "darkashes");
        for (String name : new String[]{"granitered", "graniteblack", "granite"})
            put(d, name, 0, new long[]{1, 1, 1}, "biotite", "potassiumfeldspar", "flint");
    }

    private static void addUnconfiguredFluids(Map<String, Double> d) {
        // :1195-1231. No component/density setter in these declarations or
        // their lqud/gas factories. Forge fluid.setDensity does NOT modify MT.
        defaults(d, "biomass", "biofuel", "ethanol", "oil", "fuel", "kerosine", "diesel", "petrol",
                "propane", "butane", "propylene", "ethylene", "creosote", "whaleoil", "seedoil", "hempoil",
                "linoil", "sunfloweroil", "nutoil", "oliveoil", "fryingoilhot", "glue", "lubricant",
                "constructionfoam", "uuamplifier", "uumatter", "latex");
        put(d, "nitrofuel", 0, new long[]{1, 4}, "glyceryl", "fuel");
    }
}
