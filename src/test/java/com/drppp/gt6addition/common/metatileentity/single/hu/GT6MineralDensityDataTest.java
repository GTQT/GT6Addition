package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class GT6MineralDensityDataTest {
    private static final long U = 648648000L;
    private static final double O = 1.429, H = .08988, C = 2267, FE = 7874;
    private static final double SILICA = (2329.6 + 2 * O) / 3;
    private static final double ALUMINA = (2 * 2698 + 3 * O) / 5;
    private static final double CO3 = (C + 3 * O) / 4;
    private static final double WO3 = (19250 + 3 * O) / 4;
    private static final double CALCITE = (1540 + 4 * CO3) / 5;
    private static final double OBSIDIAN = (1738 + FE + 6 * SILICA + 4 * O) / 64;

    private static double sourceDensity(long divider, long[] weights, double... densities) {
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
                "Registered fluid density must not override source data: " + name);
    }

    @Test
    void explicitDividersAreNotReplacedWithAtomicAverages() {
        density("cassiterite", 7287 + 2 * O);
        density("garnierite", 8912 + O);
        density("uraninite", 18950 + 2 * O);
        density("rutile", 4540 + 2 * O);
        density("anthracite", 2 * C);
    }

    @Test
    void coalFamilyKeepsIndividualOverridesAndHydrationRatio() {
        for (String name : new String[]{"coal", "charcoal", "coal_coke", "coke"}) density(name, 929);
        for (String name : new String[]{"lignite", "lignite_coke"}) density(name, 865);
        for (String name : new String[]{"graphite", "graphene"}) density(name, C);
        density("hydrated_coal", 929 + 1000.0 / 8);
        density("peat", 1000);
        assertEquals("carbon", CrucibleSmeltingRule.find("anthracite").target);
        assertEquals(U / 2, CrucibleSmeltingRule.find("anthracite").convert(U));
    }

    @Test
    void nativeSulfidesUseSourceComponentsAndPerComponentIntegerDivision() {
        density("realgar", (5776 + 2067.0) / 2);
        density("cinnabar", (13533.6 + 2067) / 2);
        density("molybdenite", (10220 + 2 * 2067.0) / 3);
        density("sphalerite", (7134 + 2067.0) / 2);
        density("stibnite", (2 * 6685 + 3 * 2067.0) / 5);
        density("pentlandite", sourceDensity(17, new long[]{9, 8}, 8912, 2067));
        assertNotEquals((9 * 8912 + 8 * 2067.0) / 17,
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("pentlandite"), 1e-8);
        density("chalcopyrite", (8960 + FE + 2 * 2067) / 4);
        density("arsenopyrite", (FE + 5776 + 2067) / 3);
        density("cobaltite", (8860 + 5776 + 2067.0) / 3);
        density("galena", (3 * 11342 + 3 * 10501 + 2 * 2067.0) / 8);
        density("cooperite", (3 * 21460 + 8912 + 12020 + 2067.0) / 6);
        density("tetrahedrite", (3 * 8960 + 6685 + FE + 3 * 2067) / 8);
        density("kesterite", (2 * 8960 + 7134 + 7287 + 4 * 2067.0) / 8);
        density("stannite", (2 * 8960 + FE + 7287 + 4 * 2067) / 8);
    }

    @Test
    void tungstatesKeepNestedWO3AndLiteralMagnesiumInWolframite() {
        density("tungsten_trioxide", WO3);
        density("scheelite", (1540 + 4 * WO3 + O) / 6);
        density("wolframite", (1738 + 4 * WO3 + O) / 6);
        density("ferberite", (FE + 4 * WO3 + O) / 6);
        density("huebnerite", (7440 + 4 * WO3 + O) / 6);
        density("tungstate", (2 * 534 + 4 * WO3 + O) / 7);
        density("stolzite", (11342 + 4 * WO3 + O) / 6);
        density("russellite", (2 * 9807 + 4 * WO3 + 3 * O) / 9);
        density("pinalite", (3 * 11342 + 4 * WO3 + 2 * 3.214 + 2 * O) / 11);
    }

    @Test
    void nestedCarbonatesAndOtherOreDensitiesFollowSourceNotHostStoichiometry() {
        density("carbon_trioxide", CO3);
        density("zircon", (6506 + 3 * SILICA + 2 * O) / 6);
        density("azurite", (3 * 8960 + 8 * CO3 + O + 3 * 1000) / 15);
        density("malachite", (2 * 8960 + 4 * CO3 + 2 * H + 2 * O) / 10);
        density("pitchblende", (3 * (18950 + 2 * O) + 11720 + 11342) / 5);
        density("bromargyrite", (10501 + 3122.0) / 2);
        density("smithsonite", (7134 + C + 3 * O) / 5);
        density("sperrylite", (21460 + 2 * 5776.0) / 3);
        density("chromite", (FE + 2 * 7150 + 4 * O) / 7);
        density("powellite", (1540 + 10220 + 4 * O) / 6);
        density("wulfenite", (11342 + 10220 + 4 * O) / 6);
        density("bastnasite", (6770 + C + 1.696 + 3 * O) / 6);
        density("barite", (3594 + 2067 + 4 * O) / 6);
        density("celestine", (2640 + 2067 + 4 * O) / 6);
    }

    @Test
    void nestedOxideMixturesAndSandsKeepIndependentSourceIdentities() {
        double ta2o5 = (2 * 16654 + 5 * O) / 7;
        double nb2o5 = (2 * 8570 + 5 * O) / 7;
        double mno2 = 7440 + 2 * O;
        double tantalite = (7 * ta2o5 + mno2) / 8;
        density("tantalum_pentoxide", ta2o5);
        density("tantalite", tantalite);
        density("coltan", (tantalite + (7 * nb2o5 + mno2) / 8) / 2);
        double ilmenite = (FE + 4540 + 3 * O) / 5;
        density("ilmenite", ilmenite);
        density("bauxite", (4540 + 2 * O + 2 * ilmenite + 2 * ALUMINA) / 5);
        for (String name : new String[]{"magnetite", "basaltic_mineral_sand", "granitic_mineral_sand"})
            density(name, (3 * FE + 4 * O) / 7);
        for (String name : new String[]{"brown_limonite", "yellow_limonite"}) density(name, (FE + H + 2 * O) / 4);
        density("rare_earth", 7007);
        density("monazite", (7007 + (1820 + 4 * O) / 5) / 2);
        for (String name : new String[]{"certus_quartz", "charged_certus_quartz", "quartz_sand"}) density(name, SILICA);
        density("marble", (1738 + 7 * CALCITE) / 8);
        density("limestone", CALCITE);
        density("quartzite", 1000); // Processing output is not the density configuration.
    }

    @Test
    void hydratedMineralsKeepCommonDividersAndDefaultsAreNotUsedAsUnknownFallbacks() {
        double carbonate = (2 * 971 + 4 * CO3) / 6;
        double sulfate = (2 * 971 + 2067 + 4 * O) / 7;
        double mgcl2 = (1738 + 2 * 3.214) / 3;
        density("perlite", OBSIDIAN + 1000);
        density("trona", carbonate + 1000);
        density("mirabilite", sourceDensity(7, new long[]{7, 30}, sulfate, 1000));
        density("bischofite", mgcl2 + 2000);
        density("borax", sourceDensity(43, new long[]{2, 4, 30, 7}, 971, 2340, 1000, O));
    }

    @Test
    void aluminosilicatesPreserveNestedComponentsAndRounding() {
        density("spodumene", (5 * ALUMINA + 2 * 534 + 12 * SILICA + O) / 20);
        density("lepidolite", (10 * ALUMINA + 862 + 3 * 534 + 2 * 1.696 + 6 * O) / 22);
        density("glauconite", sourceDensity(23, new long[]{10, 1, 2, 3, 7}, ALUMINA, 862, 1738, 1000, O));
        density("vermiculite", sourceDensity(39, new long[]{10, 3, 12, 12, 2}, ALUMINA, FE, SILICA, 1000, H));
        density("mica", sourceDensity(39, new long[]{15, 2, 18, 4}, ALUMINA, 862, SILICA, 1.696));
        density("kyanite", (5 * ALUMINA + 3 * SILICA) / 8);
        double koh = (862 + O + H) / 3, so3 = (2067 + 3 * O) / 4;
        density("alunite", sourceDensity(61, new long[]{15, 6, 16, 15, 9}, ALUMINA, koh, so3, 1000, O));
    }

    @Test
    void sourceOreAliasesShareTargetsPhasesFlagsDensityAndRecyclingIdentity() {
        String[][] aliases = {{"cassiterite_sand", "cassiterite"}, {"sheldonite", "cooperite"},
                {"calcium_tungstate", "scheelite"}, {"gyubnera", "huebnerite"}, {"raspite", "stolzite"},
                {"bog_iron", "yellowlimonite"}, {"ferrovanadium", "vanadiummagnetite"},
                {"illmenite", "ilmenite"}, {"titanium_iron", "ilmenite"},
                {"glauconite_sand", "glauconite"}, {"coke", "coalcoke"}};
        for (String[] pair : aliases) {
            String alias = pair[0], nativeName = pair[1];
            assertTrue(CrucibleSolidifyingRule.isSnapshotMaterial(alias), alias);
            assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(alias), alias);
            assertEquals(CrucibleMaterialPhaseData.knownMeltingPoint(nativeName),
                    CrucibleMaterialPhaseData.knownMeltingPoint(alias), alias);
            assertEquals(CrucibleMaterialPhaseData.boilingPoint(nativeName),
                    CrucibleMaterialPhaseData.boilingPoint(alias), alias);
            assertEquals(GT6MaterialHazardData.knownCombustionFlags(nativeName),
                    GT6MaterialHazardData.knownCombustionFlags(alias), alias);
            assertEquals(Boolean.FALSE, GT6MaterialHazardData.knownAcidFlag(alias), alias);
            assertEquals(nativeName, GT6MaterialIdentity.hostRecyclingName(alias), alias);
            density(alias, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(nativeName));
        }
        assertEquals("platinum", CrucibleSmeltingRule.find("sheldonite").target);
        assertEquals(U / 3, CrucibleSmeltingRule.find("sheldonite").convert(U));
        assertEquals("tungsten_trioxide", CrucibleSmeltingRule.find("raspite").target);
        assertEquals(4 * U / 6, CrucibleSmeltingRule.find("raspite").convert(U));
        assertEquals("hematite", CrucibleSmeltingRule.find("bog_iron").target);
        assertEquals(U / 2, CrucibleSmeltingRule.find("bog_iron").convert(U));
        for (String name : new String[]{"calcium_tungstate", "gyubnera", "titanium_iron"}) {
            assertNull(CrucibleSmeltingRule.find(name));
            assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget(name), name);
        }
    }

    @Test
    void sourceOreAliasesNeverInheritConflictingHostOreMultipliers() {
        assertEquals(2, GT6OreMultiplierData.multiplier("cassiterite_sand", 99));
        for (String name : new String[]{"sheldonite", "raspite", "bog_iron", "calcium_tungstate",
                "gyubnera", "ferrovanadium", "illmenite", "titanium_iron", "glauconite_sand", "coke"})
            assertEquals(1, GT6OreMultiplierData.multiplier(name, 99), name);
        assertEquals(99, GT6OreMultiplierData.multiplier("thirdparty:sheldonite", 99));
        assertEquals(99, GT6OreMultiplierData.multiplier("unknown_sheldonite_like", 99));
    }

    @Test
    void aliasMatchingDoesNotInventNamesOrDiscardRegistryNamespaces() {
        for (String name : new String[]{"gyubnera_like", "sheldonite_unknown", "thirdparty:sheldonite",
                "thirdparty:bog_iron", "raspite_mixture"}) {
            assertNull(CrucibleSmeltingRule.find(name), name);
            assertFalse(CrucibleSolidifyingRule.isSnapshotMaterial(name), name);
            assertNull(GT6MaterialHazardData.knownAcidFlag(name), name);
            assertEquals(name, GT6MaterialIdentity.hostRecyclingName(name), name);
        }
        assertFalse(GT6MaterialIdentity.allowsRegistryNamespace("thirdparty:sheldonite", "gregtech:cooperite"));
        assertTrue(GT6MaterialIdentity.allowsRegistryNamespace("gregtech:sheldonite", "gregtech:cooperite"));
    }
}
