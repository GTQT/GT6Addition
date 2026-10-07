package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GT6LateMaterialDataTest {
    @Test void everyLiteralWoodnormalIdentityUsesTheActualFactoryContract() {
        Set<String> expected = new HashSet<>();
        for (int id = 9300; id <= 9409; id++) {
            String name = GT6MaterialIdentity.name(id);
            assertNotNull(name, "Missing source wood identity " + id);
            expected.add(name.toLowerCase(java.util.Locale.ROOT));
            assertTrue(GT6WoodMaterialData.contains(name), name);
            assertEquals(400, CrucibleMaterialPhaseData.knownMeltingPoint(name), name);
            assertEquals(500, CrucibleMaterialPhaseData.boilingPoint(name), name);
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(name), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 499, true), name);
            assertFalse(CrucibleSmeltingRule.hasDefaultSelfTarget(name), name);
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            assertNotNull(rule, name);
            assertEquals("ash", rule.target, name);
            assertEquals(1, rule.convert(4), name);
            assertEquals(648648000L / 4, rule.convert(648648000L), name);
            assertNull(CrucibleSolidifyingRule.target(name), name);
            assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name), name);
        }
        assertEquals(110, expected.size());
        assertEquals(expected, GT6WoodMaterialData.names());
        assertThrows(UnsupportedOperationException.class, () -> GT6WoodMaterialData.names().clear());
    }

    @Test void woodNamesAreExplicitAndDoNotReplaceDistinctMaterials() {
        assertTrue(GT6WoodMaterialData.contains("Blue Spruce"));
        assertTrue(GT6WoodMaterialData.contains("wood_compressed"));
        for (String name : new String[]{"wood", "treated_wood", "silverwood", "petrifiedwood",
                "thirdparty_wood", "thirdparty:oak", "oak_planks", "coconut", "gold", "ash", "lime", ""}) {
            assertFalse(GT6WoodMaterialData.contains(name), name);
        }
        assertFalse(GT6WoodMaterialData.contains(null));
        assertEquals(450, CrucibleMaterialPhaseData.knownMeltingPoint("silverwood"));
        assertEquals(0, CrucibleSmeltingRule.find("silverwood").convert(648648000L));
        assertEquals(350, CrucibleMaterialPhaseData.knownMeltingPoint("petrifiedwood"));
    }

    @Test void woodDensityUsesSourceComponentsNotTheHostFallback() {
        // MT.java C = 2.267 g/cm3, H2O = 1, woodnormal divider = 21.
        double expected = (6 * 2267D + 15 * 1000D) / 21;
        for (String name : GT6WoodMaterialData.names()) {
            assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(name), name);
            assertEquals(expected, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 99), 1e-9, name);
            assertEquals(expected / 9, CrucibleTransferLogic.materialWeightKg(648648000L, expected, 648648000L), 1e-9);
        }
        assertEquals(1200, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("unregistered_oak", -1));
    }

    @Test void hydratedSilicatesKeepNestedSourceHeatAndDoNotGainMeltingFlags() {
        checkPhase("wollastonite", 1425, 2301, false);
        checkPhase("zeolite", 1461, 2289, false);
        checkPhase("pollucite", 1455, 2273, false);
        // Two Calcite, one quartz and one clay; source heat is 1612/3000,
        // 1986/3220 and 2000/4000. The later generification is not inheritance.
        checkPhase("shale", 1802, 3305, false);
        checkPhase("redrock", 1802, 3305, false);
        assertEquals(CrucibleMaterialPhaseData.knownMeltingPoint("ferrovanadium"),
                CrucibleMaterialPhaseData.knownMeltingPoint("Vanadium Magnetite"));
        assertEquals(CrucibleMaterialPhaseData.boilingPoint("ferrovanadium"),
                CrucibleMaterialPhaseData.boilingPoint("Vanadium Magnetite"));
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("vanadium_magnetite"));
    }

    @Test void fictionalHalidesUseElementalMetalsRatherThanAlloyHeat() {
        checkPhase("diduranium_trioxide", 512, 1050, false);
        checkPhase("tritanium_dioxide", 702, 1106, false);
        String[] suffixes = {"fluoride", "chloride", "bromide", "iodide", "astatide"};
        int[] melts = {53, 171, 265, 386, 575}, boils = {85, 239, 332, 457, 610};
        for (int i = 0; i < suffixes.length; i++) {
            checkPhase("duraniumhexa" + suffixes[i], (1200 + 6 * melts[i]) / 7, (2491 + 6 * boils[i]) / 7, false);
            checkPhase("tritaniumhexa" + suffixes[i], (2000 + 6 * melts[i]) / 7, (3138 + 6 * boils[i]) / 7, false);
        }
    }

    @Test void lateMineralDensitiesUseNestedSourceWeightsAndExplicitDividers() {
        // Exact GT6 factory constants, not rounded real-world handbook values.
        double silica = (2329.6D + 2 * 1.429D) / 3;
        double alumina = (2 * 2698D + 3 * 1.429D) / 5;
        assertDensity("wollastonite", (1540 + 3 * silica + 1.429) / 5);
        assertDensity("zeolite", (5 * alumina + 2 * 971 + 12 * silica + 6 * 1000 + 1.429) / 26);
        assertDensity("pollucite", (5 * alumina + 2 * 1873 + 12 * silica + 6 * 1000 + 1.429) / 26);
        assertDensity("diduraniumtrioxide", (2 * 20000D + 3 * 1.429) / 5);
        assertDensity("tritaniumdioxide", (25000D + 2 * 1.429) / 3);
        assertDensity("duraniumhexafluoride", (20000D + 6 * 1.696) / 7);
        assertDensity("tritaniumhexafluoride", (25000D + 6 * 1.696) / 7);
        double calcite = (1540 + 2267 + 3 * 1.429) / 5;
        double ceramic = (5 * alumina + 12 * silica) / 18;
        double clay = (2 * ceramic + 1000) / 2;
        double redClay = clay + (862 + 1.429 + .08988) / 54;
        assertDensity("shale", (2 * calcite + silica + clay) / 4);
        assertDensity("redrock", (2 * calcite + silica + redClay) / 4);
    }

    @Test void missingFictionalElementThermalAndDensityDataIsExplicit() {
        checkPhase("Naquadah-Enriched", 1500, 3000, true);
        checkPhase("enriched_naquadah", 1500, 3000, true);
        checkPhase("naquadria", 1500, 3000, true);
        checkPhase("abyssalnite", 1500, 3000, true);
        checkPhase("coralium", 2000, 4000, true);
        checkPhase("dreadium", 2500, 5000, true);
        checkPhase("ethaxium", 3000, 6000, true);
        checkPhase("Mac-Guffium", 200, 1000, false);
        checkPhase("gravitonium", 112, 1275, false);
        assertDensity("enriched_naquadah", 22000);
        assertDensity("abyssalnite", 15000);
        assertDensity("Mac-Guffium", 3122);
        assertDensity("gravitonium", 1768866.761);
        assertTrue(GT6MaterialHazardData.isExplosive("Naquadah-Enriched"));
        assertTrue(GT6MaterialHazardData.isExplosive("naquadria"));
    }

    private static void checkPhase(String name, int melt, long boil, boolean exempt) {
        assertEquals(melt, CrucibleMaterialPhaseData.knownMeltingPoint(name), name);
        assertEquals(boil, CrucibleMaterialPhaseData.boilingPoint(name), name);
        assertEquals(exempt, GT6DeclaredPhaseData.hasBurningExemption(name), name);
    }

    private static void assertDensity(String name, double expected) {
        assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(name), name);
        assertEquals(expected, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 99999), 1e-6, name);
    }
}
