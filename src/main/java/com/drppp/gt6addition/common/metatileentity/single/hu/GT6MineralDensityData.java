package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Map;

/** MT.java's native mineral and alloy configurations. Density uses the explicit common
 * divider and nested material units, NOT a flattened CEu molecular average.
 * OreDictConfigurationComponent divides each amount with integer truncation
 * before OreDictMaterial.setMoleculeConfiguration sums the densities.
 */
final class GT6MineralDensityData {
    private static final long U = 648648000L;

    private GT6MineralDensityData() {}

    static void addTo(Map<String, Double> densities) {
        // MT.java:1036,1065,1071,1077,1086,1100,1137,1139. Dependencies
        // precede consumers; their density divider need not be their atom sum.
        put(densities, "carbontrioxide", 0, new long[]{1, 3}, "carbon", "oxygen");
        put(densities, "sulfurtrioxide", 0, new long[]{1, 3}, "sulfur", "oxygen"); // :1045
        put(densities, "potassiumhydroxide", 0, new long[]{1, 1, 1}, "potassium", "oxygen", "hydrogen"); // :1149
        put(densities, "vanadiumpentoxide", 0, new long[]{2, 5}, "vanadium", "oxygen");
        put(densities, "tantalumpentoxide", 0, new long[]{2, 5}, "tantalum", "oxygen");
        put(densities, "tungstentrioxide", 0, new long[]{1, 3}, "tungsten", "oxygen");
        put(densities, "rutile", 1, new long[]{1, 2}, "titanium", "oxygen");
        put(densities, "magnesiumchloride", 0, new long[]{1, 2}, "magnesium", "chlorine");
        put(densities, "sodiumsulfate", 0, new long[]{2, 1, 4}, "sodium", "sulfur", "oxygen");
        put(densities, "sodiumcarbonate", 0, new long[]{2, 4}, "sodium", "carbontrioxide");
        // :1292,1524,1530,1534-1535. Anthracite has C2/divider1 and no
        // Coal's .929 density override; copying smelting targets is unrelated.
        densities.put("graphite", densities.get("carbon"));
        densities.put("graphene", densities.get("carbon"));
        put(densities, "anthracite", 1, new long[]{2}, "carbon");
        put(densities, "hydratedcoal", 8, new long[]{8, 1}, "coal", "water");
        densities.put("peat", 1000D); // No configuration or density override.
        // :1511-1512,1553,1564-1565,1625-1626.
        put(densities, "zircon", 0, new long[]{1, 3, 2}, "zirconium", "silicondioxide", "oxygen");
        put(densities, "azurite", 0, new long[]{3, 8, 1, 3}, "copper", "carbontrioxide", "oxygen", "water");
        densities.put("certusquartz", densities.get("silicondioxide"));
        densities.put("chargedcertusquartz", densities.get("certusquartz"));
        densities.put("rareearth", densities.get("neodymium")); // stealStatsElement(Nd)
        put(densities, "monazite", 0, new long[]{1, 1}, "rareearth", "phosphate");
        // :3700-3705. Explicit divider1 for the first three is a sum,
        // not an average; Uraninite's source element is U_238 (Uranium).
        put(densities, "cassiterite", 1, new long[]{1, 2}, "tin", "oxygen");
        put(densities, "garnierite", 1, new long[]{1, 1}, "nickel", "oxygen");
        put(densities, "uraninite", 1, new long[]{1, 2}, "uranium", "oxygen");
        put(densities, "magnetite", 0, new long[]{3, 4}, "iron", "oxygen");
        densities.put("basalticmineralsand", densities.get("magnetite"));
        densities.put("graniticmineralsand", densities.get("magnetite"));
        // :3707-3722. Dividers of zero use the sum of component units.
        put(densities, "realgar", 0, new long[]{1, 1}, "arsenic", "sulfur");
        put(densities, "cinnabar", 0, new long[]{1, 1}, "mercury", "sulfur");
        put(densities, "molybdenite", 0, new long[]{1, 2}, "molybdenum", "sulfur");
        put(densities, "sphalerite", 0, new long[]{1, 1}, "zinc", "sulfur");
        put(densities, "stibnite", 0, new long[]{2, 3}, "antimony", "sulfur");
        put(densities, "pentlandite", 0, new long[]{9, 8}, "nickel", "sulfur");
        put(densities, "chalcopyrite", 0, new long[]{1, 1, 2}, "copper", "iron", "sulfur");
        put(densities, "arsenopyrite", 0, new long[]{1, 1, 1}, "iron", "arsenic", "sulfur");
        put(densities, "cobaltite", 0, new long[]{1, 1, 1}, "cobalt", "arsenic", "sulfur");
        put(densities, "galena", 0, new long[]{3, 3, 2}, "lead", "silver", "sulfur");
        put(densities, "cooperite", 0, new long[]{3, 1, 1, 1}, "platinum", "nickel", "palladium", "sulfur");
        put(densities, "tetrahedrite", 0, new long[]{3, 1, 1, 3}, "copper", "antimony", "iron", "sulfur");
        put(densities, "kesterite", 0, new long[]{2, 1, 1, 4}, "copper", "zinc", "tin", "sulfur");
        put(densities, "stannite", 0, new long[]{2, 1, 1, 4}, "copper", "iron", "tin", "sulfur");
        put(densities, "barite", 0, new long[]{1, 1, 4}, "barium", "sulfur", "oxygen");
        put(densities, "celestine", 0, new long[]{1, 1, 4}, "strontium", "sulfur", "oxygen");
        // :3724-3732. WO3 is a nested material, not elemental tungsten.
        put(densities, "scheelite", 0, new long[]{1, 4, 1}, "calcium", "tungstentrioxide", "oxygen");
        put(densities, "wolframite", 0, new long[]{1, 4, 1}, "magnesium", "tungstentrioxide", "oxygen");
        put(densities, "ferberite", 0, new long[]{1, 4, 1}, "iron", "tungstentrioxide", "oxygen");
        put(densities, "huebnerite", 0, new long[]{1, 4, 1}, "manganese", "tungstentrioxide", "oxygen");
        put(densities, "tungstate", 0, new long[]{2, 4, 1}, "lithium", "tungstentrioxide", "oxygen");
        put(densities, "stolzite", 0, new long[]{1, 4, 1}, "lead", "tungstentrioxide", "oxygen");
        put(densities, "russellite", 0, new long[]{2, 4, 3}, "bismuth", "tungstentrioxide", "oxygen");
        put(densities, "pinalite", 0, new long[]{3, 4, 2, 2}, "lead", "tungstentrioxide", "chlorine", "oxygen");
        // :3739-3757. Some existing thermal overrides do not affect density.
        put(densities, "brownlimonite", 0, new long[]{1, 1, 2}, "iron", "hydrogen", "oxygen");
        densities.put("yellowlimonite", densities.get("brownlimonite"));
        put(densities, "tantalite", 0, new long[]{7, 1}, "tantalumpentoxide", "pyrolusite");
        put(densities, "coltan", 0, new long[]{1, 1}, "tantalite", "columbite");
        put(densities, "ilmenite", 0, new long[]{1, 1, 3}, "iron", "titanium", "oxygen");
        put(densities, "bauxite", 0, new long[]{1, 2, 2}, "rutile", "ilmenite", "alumina");
        put(densities, "chromite", 0, new long[]{1, 2, 4}, "iron", "chromium", "oxygen");
        put(densities, "powellite", 0, new long[]{1, 1, 4}, "calcium", "molybdenum", "oxygen");
        put(densities, "wulfenite", 0, new long[]{1, 1, 4}, "lead", "molybdenum", "oxygen");
        put(densities, "bastnasite", 0, new long[]{1, 1, 1, 3}, "cerium", "carbon", "fluorine", "oxygen");
        put(densities, "pitchblende", 0, new long[]{3, 1, 1}, "uraninite", "thorium", "lead");
        put(densities, "malachite", 0, new long[]{2, 4, 2, 2}, "copper", "carbontrioxide", "hydrogen", "oxygen");
        put(densities, "bromargyrite", 0, new long[]{1, 1}, "silver", "bromine");
        put(densities, "smithsonite", 0, new long[]{1, 1, 3}, "zinc", "carbon", "oxygen");
        put(densities, "sperrylite", 0, new long[]{1, 2}, "platinum", "arsenic");
        // :3759-3764. Hydration dividers retain more than one component unit.
        put(densities, "perlite", 1, new long[]{1, 1}, "obsidian", "water");
        put(densities, "trona", 6, new long[]{6, 6}, "sodiumcarbonate", "water");
        put(densities, "mirabilite", 7, new long[]{7, 30}, "sodiumsulfate", "water");
        put(densities, "bischofite", 3, new long[]{3, 6}, "magnesiumchloride", "water");
        put(densities, "borax", 0, new long[]{2, 4, 30, 7}, "sodium", "boron", "water", "oxygen");
        // :3767-3774. These are the actual nested source configurations,
        // including deliberately approximate minerals, not real-world formulas.
        put(densities, "spodumene", 0, new long[]{5, 2, 12, 1}, "alumina", "lithium", "silicondioxide", "oxygen");
        put(densities, "lepidolite", 0, new long[]{10, 1, 3, 2, 6}, "alumina", "potassium", "lithium", "fluorine", "oxygen");
        put(densities, "glauconite", 0, new long[]{10, 1, 2, 3, 7}, "alumina", "potassium", "magnesium", "water", "oxygen");
        put(densities, "vermiculite", 0, new long[]{10, 3, 12, 12, 2}, "alumina", "iron", "silicondioxide", "water", "hydrogen");
        put(densities, "mica", 0, new long[]{15, 2, 18, 4}, "alumina", "potassium", "silicondioxide", "fluorine");
        put(densities, "kyanite", 0, new long[]{5, 3}, "alumina", "silicondioxide");
        put(densities, "alunite", 0, new long[]{15, 6, 16, 15, 9}, "alumina", "potassiumhydroxide", "sulfurtrioxide", "water", "oxygen");
        // :3777,3819-3820,3827. Quartzite has no configuration, despite its
        // SiO2 processing target; its density is the constructor's 1 g/cm^3.
        put(densities, "quartzsand", 0, new long[]{1, 1}, "certusquartz", "milkyquartz");
        put(densities, "marble", 0, new long[]{1, 7}, "magnesium", "calcite");
        densities.put("limestone", densities.get("calcite"));
        densities.put("quartzite", 1000D);

        // MT.java:1024,1037-1039,1061-1062,1124-1159,1189-1191.
        // The common divider controls density, not the thermal atom average.
        put(densities, "heliumneon", 0, new long[]{1, 1}, "helium", "neon");
        put(densities, "methane", 0, new long[]{1, 4}, "carbon", "hydrogen");
        put(densities, "sugar", 0, new long[]{12, 22, 11}, "carbon", "hydrogen", "oxygen");
        put(densities, "vanilla", 0, new long[]{8, 8, 3}, "carbon", "hydrogen", "oxygen");
        put(densities, "hydrogenborate", 0, new long[]{3, 1, 3}, "hydrogen", "boron", "oxygen");
        put(densities, "datolite", 0, new long[]{2, 2, 2, 2, 10}, "hydrogen", "calcium", "boron", "silicon", "oxygen");
        put(densities, "lithiumoxide", 0, new long[]{2, 1}, "lithium", "oxygen");
        put(densities, "ferrite", 0, new long[]{2, 2, 4}, "lithium", "iron", "oxygen");
        put(densities, "sodiumnitrate", 0, new long[]{1, 1, 3}, "sodium", "nitrogen", "oxygen");
        put(densities, "sodiumhydrogencarbonate", 0, new long[]{1, 1, 1, 3}, "sodium", "hydrogen", "carbon", "oxygen");
        put(densities, "sodiumbisulfate", 0, new long[]{1, 1, 1, 4}, "sodium", "hydrogen", "sulfur", "oxygen");
        put(densities, "sodiumpersulfate", 0, new long[]{1, 1, 4}, "sodium", "sulfur", "oxygen");
        put(densities, "sodiumsulfide", 0, new long[]{2, 1}, "sodium", "sulfur");
        put(densities, "potassiumbisulfate", 0, new long[]{1, 1, 1, 4}, "potassium", "hydrogen", "sulfur", "oxygen");
        put(densities, "potassiumpersulfate", 0, new long[]{1, 1, 4}, "potassium", "sulfur", "oxygen");
        put(densities, "potassiumsulfide", 0, new long[]{2, 1}, "potassium", "sulfur");
        put(densities, "potassiumheptafluorotantalate", 0, new long[]{2, 1, 7}, "potassium", "tantalum", "fluorine");
        put(densities, "cobalthexahydrate", 0, new long[]{1, 6}, "cobalt", "water");
        put(densities, "methaneice", 2, new long[]{1, 2}, "methane", "ice");
        put(densities, "nitrocarbon", 0, new long[]{1, 1}, "nitrogen", "carbon");
        // Dependencies used below; each has its own explicit GT6 configuration.
        densities.put("flint", densities.get("silicondioxide")); // :1058
        put(densities, "magnesiumcarbonate", 0, new long[]{1, 4}, "magnesium", "carbontrioxide"); // :1101
        put(densities, "pyrite", 0, new long[]{1, 2}, "iron", "sulfur"); // :1237
        put(densities, "ruby", 6, new long[]{5, 1}, "alumina", "chromium"); // :1381
        put(densities, "sodalite", 0, new long[]{3, 3, 4, 1}, "alumina", "silicondioxide", "sodium", "chlorine"); // :1517
        put(densities, "redstone", 0, new long[]{5, 3, 1, 1}, "pyrite", "mercury", "silicondioxide", "ruby"); // :1540
        put(densities, "nikolite", 0, new long[]{5, 3, 1, 1}, "sodalite", "copper", "silicondioxide", "argon"); // :1541
        // :1230,1233-1239,1298,1308-1310. Explicit 1/8 calcium is NOT
        // a one-component normalized density (Bone and Slimy Bone).
        put(densities, "volcanicashes", 0, new long[]{6, 1, 1}, "flint", "hematite", "magnesium");
        densities.put("chalk", densities.get("calcite"));
        put(densities, "dolomite", 0, new long[]{1, 1}, "calcite", "magnesiumcarbonate");
        put(densities, "asbestos", 0, new long[]{3, 6, 6, 3}, "magnesium", "silicondioxide", "water", "oxygen");
        put(densities, "talc", 0, new long[]{3, 12, 3, 3}, "magnesium", "silicondioxide", "water", "oxygen");
        put(densities, "potassiumfeldspar", 0, new long[]{2, 5, 18, 1}, "potassium", "alumina", "silicondioxide", "oxygen");
        put(densities, "biotite", 0, new long[]{2, 6, 15, 4, 18}, "potassium", "magnesium", "alumina", "fluorine", "silicondioxide");
        put(densities, "apatite", 0, new long[]{5, 3, 1}, "calcium", "phosphate", "chlorine");
        put(densities, "bone", 8, new long[]{1}, "calcium");
        put(densities, "slimybone", 8, new long[]{1}, "calcium");
        put(densities, "gunpowder", 4, new long[]{2, 1, 1}, "carbon", "sulfur", "sodiumnitrate");
        put(densities, "dynamite", 0, new long[]{1, 1}, "glyceryl", "wood");
        // :1336,1350-1351. heat(...) overrides temperatures only, not density.
        put(densities, "chocolate", 0, new long[]{1, 1}, "cocoa", "sugar");
        densities.put("butter", densities.get("milk"));
        put(densities, "saltedbutter", 1, new long[]{1, 1}, "milk", "salt");
        densities.put("vinteum", 0D); // :1478-1479 configuration of Magic alone.
        densities.put("vinteumpurified", 0D);
        put(densities, "eudialyte", 0, new long[]{18, 3, 15, 6, 2, 75, 12},
                "zircon", "pyrolusite", "sodium", "calcium", "chlorine", "silicondioxide", "oxygen");
        put(densities, "prismane", 1, new long[]{4}, "carbon");
        put(densities, "lonsdaleite", 1, new long[]{8}, "carbon");
        put(densities, "energiumred", 0, new long[]{4, 5}, "sapphire", "redstone");
        put(densities, "energiumcyan", 0, new long[]{4, 5}, "sapphire", "nikolite");
        put(densities, "duralumin", 0, new long[]{1, 1}, "aluminium", "copper");
        // :1623-1625 copy elemental statistics, with default thermal values.
        densities.put("sluicesand", densities.get("silicondioxide"));
        densities.put("platinumgroupsludge", densities.get("platinum"));
        densities.put("castiron", densities.get("iron")); // :1726 steal(Fe), not field spelling.
        // :1499-1500,1635,1753,1802,1805,1812-1815. Preserve nested
        // configs and statistics copying; alloy density is not an atom average.
        densities.put("endstone", densities.get("silicondioxide")); // stealStatsElement(SiO2)
        put(densities, "enderpearl", 10, new long[]{1, 4, 5, 6}, "beryllium", "potassium", "nitrogen", "magic");
        put(densities, "endereye", 9, new long[]{9, 1}, "enderpearl", "blaze");
        densities.put("energeticsilver", densities.get("silver")); // stealStatsElement(Ag), not heat.
        put(densities, "yellorite", 1, new long[]{1, 2}, "yellorium", "oxygen");
        put(densities, "obsidiansteel", 1, new long[]{1, 9}, "steel", "obsidian");
        put(densities, "endsteel", 1, new long[]{1, 1, 9}, "endstone", "obsidiansteel", "obsidian");
        put(densities, "melodicalloy", 1, new long[]{1, 1}, "endsteel", "endereye");
        put(densities, "stellaralloy", 2, new long[]{1, 1, 4}, "melodicalloy", "netherstar", "clay");
        put(densities, "vividalloy", 1, new long[]{1, 1}, "energeticsilver", "enderpearl");
        // :1822,1826-1828. Elementium copies Steel statistics; Elvorium
        // adds Dragonstone at divider1. Its power variants copy that config.
        densities.put("elvenelementium", densities.get("steel"));
        put(densities, "elvorium", 1, new long[]{1, 1}, "elvenelementium", "elvendragonstone");
        put(densities, "niflheimpower", 1, new long[]{1}, "elvorium");
        put(densities, "muspelheimpower", 1, new long[]{1}, "elvorium");
        // :1838-1839. WorkersAlloy is the native name of field DeshAlloy.
        put(densities, "desh", 0, new long[]{2, 2, 1, 1, 1, 1, 1},
                "boron", "lanthanum", "neodymium", "niobium", "cobalt", "cerium", "lithium");
        put(densities, "workersalloy", 4, new long[]{4, 1}, "desh", "mercury");
    }

    static void put(Map<String, Double> densities, String name, long divider,
                    long[] weights, String... components) {
        if (divider == 0) for (long weight : weights) divider += weight;
        double density = 0;
        for (int i = 0; i < components.length; i++) {
            Double component = densities.get(components[i]);
            if (component == null) throw new IllegalStateException("Missing GT6 density: " + components[i]);
            long amount = weights[i] * U / divider;
            density += component * amount / (double) U;
        }
        densities.put(name, density);
    }
}
