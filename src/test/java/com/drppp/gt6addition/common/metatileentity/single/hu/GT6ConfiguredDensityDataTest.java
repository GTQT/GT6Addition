package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Independent source formulas; neither host molecular mass nor the table
 * under test is used to construct an expected component density.
 */
class GT6ConfiguredDensityDataTest {
    private static final long U = 648648000L;
    private static final double H = .08988, O = 1.429, N = 1.2506, F = 1.696, CL = 3.214;
    private static final double C = 2267, FE = 7874, S = 2067, AL = 2698, K = 862, NA = 971;
    private static final double SILICA = (2329.6 + 2 * O) / 3, ALUMINA = (2 * AL + 3 * O) / 5;
    private static final double CERAMIC = mixture(18, new long[]{5, 12}, ALUMINA, SILICA);
    private static final double CLAY = CERAMIC + 500;
    private static final double RUBY = (5 * ALUMINA + 7150) / 6;
    private static final double PYRITE = (FE + 2 * S) / 3;
    private static final double REDSTONE = (5 * PYRITE + 3 * 13533.6 + SILICA + RUBY) / 10;
    private static final double KNO3 = (K + N + 3 * O) / 5;
    private static final double CALCITE = (1540 + C + 3 * O) / 5;

    private static double mixture(long divider, long[] weights, double... densities) {
        if (divider == 0) for (long weight : weights) divider += weight;
        double result = 0;
        for (int i = 0; i < weights.length; i++) {
            long amount = BigInteger.valueOf(U).multiply(BigInteger.valueOf(weights[i]))
                    .divide(BigInteger.valueOf(divider)).longValueExact();
            result += densities[i] * amount / U;
        }
        return result;
    }

    private static void density(String name, double expected) {
        assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(name), name);
        assertEquals(expected, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name), 1e-6, name);
        assertEquals(expected, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 999999), 1e-6,
                "Host fluid density must not replace explicit GT6 data: " + name);
    }

    @Test void gasesKeepActualConfigurationsIncludingTheAirDensityBoundary() {
        density("hydrochloric_acid", (H + CL) / 2);
        density("hydrogen_fluoride", (H + F) / 2);
        density("hydrofluoric_acid", (H + F) / 2);
        density("ammonia", (N + 3 * H) / 4);
        density("nitrogen_monoxide", (N + O) / 2);
        density("nitrogen_dioxide", (N + 2 * O) / 3);
        density("carbon_monoxide", (C + O) / 2);
        density("carbon_dioxide", (C + 2 * O) / 3);
        density("sulfur_dioxide", (S + 2 * O) / 3);
        density("hydrosulfuric_acid", (2 * H + S) / 3);
        assertTrue(CrucibleTransferLogic.isAirDensity((H + F) / 2));
        assertTrue(CrucibleTransferLogic.isAirDensity((N + 3 * H) / 4));
        assertFalse(CrucibleTransferLogic.isAirDensity((H + CL) / 2));
        assertFalse(CrucibleTransferLogic.isAirDensity((N + O) / 2));
    }

    @Test void chloridesOxidesAndGypsumKeepLiteralAndNestedRatios() {
        density("silver_iodide", (10501 + 4930D) / 2);
        density("carborundum", (2329.6 + C) / 2);
        density("glass", SILICA);
        density("tungstic_acid", (2 * H + 19250 + 4 * O) / 7);
        density("aluminium_fluoride", (AL + 3 * F) / 4);
        density("aluminium_hydroxide", (AL + 3 * O + 3 * H) / 7);
        density("titanium_tetrachloride", (4540 + 4 * CL) / 5);
        density("manganese_chloride", (7440 + 2 * CL) / 3);
        density("ferrous_chloride", (FE + 2 * CL) / 3);
        density("ferric_chloride", (FE + 3 * CL) / 4);
        density("ferric_oxyhydroxide", (FE + 3 * O + 3 * H) / 7);
        density("calcium_chloride", (1540 + 2 * CL) / 3);
        double sulfate = (1540 + S + 4 * O) / 6;
        density("calcium_sulfate", sulfate);
        density("gypsum", sulfate + 1000);
        density("quicklime", (1540 + O) / 2);
    }

    @Test void alkaliChemicalsUseIntegerUnitsAndNiterUsesTwoNitrates() {
        density("lithium_chloride", (534 + CL) / 2);
        density("lithium_chlorate", (534 + CL + 3 * O) / 5);
        density("lithium_perchlorate", (534 + CL + 4 * O) / 6);
        density("lithium_hydroxide", (534 + O + H) / 3);
        density("sodium_hydroxide", (NA + O + H) / 3);
        density("sodium_sulfite", (2 * NA + S + 3 * O) / 6);
        density("sodium_pyrosulfate", mixture(11, new long[]{2, 2, 7}, NA, S, O));
        density("sodium_aluminate", (NA + AL + 2 * O) / 4);
        density("sodium_fluoride", (NA + F) / 2);
        density("cryolite", (3 * NA + AL + 6 * F) / 10);
        density("iodine_salt", (K + 4930 + 3 * O) / 5);
        density("sylvite", (K + CL) / 2);
        density("potassium_nitrate", KNO3);
        density("potassium_sulfite", (2 * K + S + 3 * O) / 6);
        density("potassium_sulfate", (2 * K + S + 4 * O) / 7);
        density("potassium_pyrosulfate", mixture(11, new long[]{2, 2, 7}, K, S, O));
        density("potassium_carbonate", (2 * K + C + 3 * O) / 6);
        density("potassium_aluminate", (K + AL + 2 * O) / 4);
        density("potassium_fluoride", (K + F) / 2);
        density("niter", (KNO3 + (NA + N + 3 * O) / 5) / 2);
    }

    @Test void acidsAndVitriolsNeverReceiveAnInventedBlanketAcidDensity() {
        density("chloroauric_acid", (19282 + 4 * CL + H) / 6);
        density("chloroplatinic_acid", (21460 + 6 * CL + 2 * H) / 9);
        density("stannic_chloride", (7287 + 4 * CL) / 5);
        density("black_vitriol", (FE + S) / 2);
        String[] names = {"blue_vitriol", "green_vitriol", "red_vitriol", "pink_vitriol",
                "cyan_vitriol", "white_vitriol", "gray_vitriol"};
        double[] metals = {8960, FE, 8860, 1738, 8912, 7134, 7440};
        for (int i = 0; i < names.length; i++) density(names[i], (metals[i] + S + 4 * O) / 6);
        density("martian_vitriol", mixture(18, new long[]{2, 3, 12}, FE, S, O));
        assertNotEquals((2 * FE + 3 * S + 12 * O) / 17,
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("martian_vitriol"), 1e-6);
        density("vitriol_of_clay", mixture(17, new long[]{5, 3, 9}, ALUMINA, S, O));
        density("aqua_regia", mixture(13, new long[]{5, 8}, 1500, (H + CL) / 2));
        // Explicit setDensity(1.5) is retained for these, not for all acids.
        for (String name : new String[]{"nitric_acid", "sulfuric_acid", "disulfuric_acid",
                "hexafluorosilicic_acid"}) density(name, 1500);
    }

    @Test void onlyDeclaredUraniumHalidesUseTheVerifiedIsotopeDensities() {
        for (String name : new String[]{"uranium_tetrafluoride", "uranium_238_tetrafluoride", "uranium_235_tetrafluoride"})
            density(name, (18950 + 4 * F) / 5);
        for (String name : new String[]{"uranium_hexafluoride", "uranium_238_hexafluoride", "uranium_235_hexafluoride"})
            density(name, (18950 + 6 * F) / 7);
        assertFalse(CrucibleTransferLogic.hasKnownGt6MaterialDensity("uranium_999_hexafluoride"));
    }

    @Test void berylVariantsKeepNestedUnitsAndRoundingRatherThanHostAtoms() {
        double expected = mixture(29, new long[]{5, 3, 18, 3}, ALUMINA, 1850, SILICA, O);
        for (String name : new String[]{"aquamarine", "morganite", "heliodor", "goshenite", "bixbite", "maxixe"})
            density(name, expected);
        assertNotEquals((5 * ALUMINA + 3 * 1850 + 18 * SILICA + 3 * O) / 29, expected, 1e-8);
    }

    @Test void sapphiresAndBalasRubyKeepTheirOwnFinalConfigurations() {
        String[] names = {"blue_sapphire", "green_sapphire", "purple_sapphire", "yellow_sapphire", "orange_sapphire"};
        double[] traces = {FE, 1738, 6110, 4540 + 2 * O, 8960};
        for (int i = 0; i < names.length; i++) density(names[i], (5 * ALUMINA + traces[i]) / 6);
        density("spinel", (5 * ALUMINA + 1738 + O) / 7);
        density("balas_ruby", (2 * 7150 + 1738 + 4 * O) / 7);
        assertNotEquals(RUBY, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("balas_ruby"), 1e-6);
    }

    @Test void garnetsAndHydratedGemsKeepLiteralNestedOxides() {
        density("almandine", (5 * ALUMINA + 3 * FE + 9 * SILICA + 3 * O) / 20);
        density("grossular", (5 * ALUMINA + 3 * 1540 + 9 * SILICA + 3 * O) / 20);
        density("pyrope", (5 * ALUMINA + 3 * 1738 + 9 * SILICA + 3 * O) / 20);
        density("andradite", (3 * 1540 + 2 * FE + 9 * SILICA + 6 * O) / 20);
        density("uvarovite", (3 * 1540 + 2 * 7150 + 9 * SILICA + 6 * O) / 20);
        for (String name : new String[]{"topaz", "blue_topaz"})
            density(name, mixture(13, new long[]{5, 3, 2, 3}, ALUMINA, SILICA, F, 1000));
        for (String name : new String[]{"tanzanite", "zanite"})
            density(name, mixture(44, new long[]{15, 18, 4, 3, 4}, ALUMINA, SILICA, 1540, 1000, O));
        density("amazonite", (5 * ALUMINA + 18 * SILICA + 2 * K + O) / 26);
        density("alexandrite", (ALUMINA + 1850 + O) / 3);
        density("peridot", (2 * SILICA + FE + 2 * 1738) / 5);
        density("dioptase", (3 * SILICA + 8960 + O + 3000) / 8);
        density("sugilite", mixture(46, new long[]{1, 2, 2, 3, 36, 2},
                K, NA, 7440 + 2 * O, 534, SILICA, O));
    }

    @Test void colouredFactoryGemsUseTheirFactoryConfigurationsNotColours() {
        for (String name : new String[]{"ocean_jasper", "rainforest_jasper", "blue_jasper", "green_jasper", "yellow_jasper"})
            density(name, (2 * SILICA + FE) / 3);
        for (String name : new String[]{"Cat's Eye", "Dragon Eye", "Hawk's Eye", "Black Eye", "tiger_iron",
                "brown_aventurine", "yellow_aventurine", "black_aventurine", "blue_aventurine", "red_aventurine",
                "opal", "onyx_red", "onyx_black"}) density(name, SILICA);
        for (String name : new String[]{"red_fluorite", "pink_fluorite", "blue_fluorite", "green_fluorite",
                "black_fluorite", "white_fluorite", "yellow_fluorite", "orange_fluorite", "magenta_fluorite"})
            density(name, (1540 + 2 * F) / 3);
    }

    @Test void lapisAndQuartzKeepNonNormalizedVariantsAndNestedMinerals() {
        double lazurite = (6 * ALUMINA + 6 * SILICA + 8 * 1540 + 8 * NA) / 28;
        double sodalite = mixture(11, new long[]{3, 3, 4, 1}, ALUMINA, SILICA, NA, CL);
        density("lazurite", lazurite);
        density("lapis", (12 * lazurite + 2 * sodalite + PYRITE + CALCITE) / 16);
        for (String name : new String[]{"nether_quartz", "void_quartz", "sunny_quartz", "lavender_quartz",
                "red_quartz", "blaze_quartz", "smokey_quartz", "mana_quartz", "elven_quartz"}) density(name, SILICA);
        density("quartz_black", SILICA + C);
        density("black_quartz", SILICA + C);
        density("fluix", SILICA + REDSTONE / 2);
    }

    @Test void glowstoneVariantsKeepIdenticalExplicitConfigsNotGenerificationStats() {
        double phosphate = (1820 + 4 * O) / 5;
        double phosphorite = (5 * 1540 + 3 * phosphate + F) / 9;
        double expected = (5 * phosphorite + 3 * 19282 + SILICA + .1785) / 10;
        for (String name : new String[]{"glowstone_ceres", "glowstone_io", "glowstone_enceladus",
                "glowstone_proteus", "glowstone_pluto", "gloomstone"}) density(name, expected);
    }

    @Test void claysAndMixturesKeepTraceAdditionsAndSourceDividers() {
        density("live_root", (6 * C + 15 * 1000D) / 21);
        for (String name : new String[]{"wax_bee", "wax_refractory", "wax_paraffin", "wax_plant",
                "wax_magic", "wax_amnesic", "wax_soulful"}) density(name, 1000);
        density("clay_brick", CERAMIC);
        String[] clays = {"clay_brown", "clay_red", "bentonite", "palygorskite", "kaolinite"};
        double[] traces = {(534 + O + H) / 3, (K + O + H) / 3, (NA + O + H) / 3, 1738, 1540};
        for (int i = 0; i < clays.length; i++) density(clays[i], mixture(18, new long[]{1, 18}, traces[i], CLAY));
        double feldspar = (2 * K + 5 * ALUMINA + 18 * SILICA + O) / 26;
        density("porcelain", (2 * CERAMIC + SILICA + feldspar) / 4);
        for (String name : new String[]{"red_sand", "white_end_sand", "black_end_sand", "soulsand",
                "concrete", "netherrack", "nether_brick"}) density(name, SILICA);
        density("bedrock", 13356.24762);
        density("oil_shale", (2 * CALCITE + SILICA + CLAY) / 4);
        double obsidian = (1738 + FE + 6 * SILICA + 4 * O) / 64;
        density("petrotheum", mixture(18, new long[]{9, 9, 9, 1}, CLAY, obsidian, REDSTONE, 1000));
        density("aerotheum", mixture(18, new long[]{9, 9, 9, 1}, SILICA, KNO3, REDSTONE, 1000));
        density("pyrotheum", mixture(18, new long[]{9, 9, 9, 1}, 929, S, REDSTONE, 1000));
        density("cryotheum", mixture(18, new long[]{2, 9, 9, 1}, 1000, KNO3, REDSTONE, 1000));
    }

    @Test void explicitAliasesShareStatisticsButUnknownNamesStayUnknown() {
        density("Nitre", (KNO3 + (NA + N + 3 * O) / 5) / 2);
        density("RockSalt", (K + CL) / 2);
        density("ScarletEmerald", mixture(29, new long[]{5, 3, 18, 3}, ALUMINA, 1850, SILICA, O));
        density("FoolsRuby", (2 * 7150 + 1738 + 4 * O) / 7);
        density("CalciumSulphate", (1540 + S + 4 * O) / 6);
        for (String name : new String[]{"unknown_wax", "topaz_like", "ammonia_mix", "some_vitriol"}) {
            assertFalse(CrucibleTransferLogic.hasKnownGt6MaterialDensity(name), name);
            assertEquals(0, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name), name);
            assertEquals(1200, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 0), name);
        }
    }
}
