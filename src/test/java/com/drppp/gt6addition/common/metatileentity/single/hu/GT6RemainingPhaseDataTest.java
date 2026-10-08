package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Independent expected values from MT.java's last twenty missing phase
 * declarations. This registers no material and never reads host properties.
 */
class GT6RemainingPhaseDataTest {
    private static final long U = 648648000L;

    @Test
    void metalFactoryDefaultsAreExplicitIdentitiesNotUnknownMaterialFallbacks() {
        String[] names = {"sunnarium", "yellorium", "blutonium", "cyanite", "ludicrite", "endium",
                "oriharukon", "adamantite", "unstable", "crystalline_alloy", "crystalline_pink_slime"};
        for (String name : names) {
            phase(name, 1000, 3000);
            density(name, 1000);
            assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget(name), name);
            assertEquals(Integer.valueOf(0), GT6MaterialHazardData.knownCombustionFlags(name), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 2000, true), name);
        }
        // MT.Unstable's crafting-grid explosion tag is not EXPLOSIVE.
        assertFalse(GT6MaterialHazardData.isExplosive("unstable"));
    }

    @Test
    void nativeConfigurationsUseNestedFinalHeatAndDoNotFlattenTheirComponents() {
        phase("hydrated_coal", 1541, 3863); // :1534, Coal 1700/4300 + Water 273/373, 8:1.
        phase("methane_ice", 215, 315); // :1190, CH4 100/200 + Ice 273/373, 1:2.
        phase("yellorite", 369, 1060); // :1802, constructor Yellorium + Oxygen, 1:2.
        phase("obsidian_steel", 1374, 3913);
        phase("end_steel", 1297, 3846);
        phase("melodic_alloy", 2372, 4412);
        phase("stellar_alloy", 2378, 4256);
        // EnergeticSilver is Ag + Redstone + Glowstone (744/1511), not
        // EnergeticAlloy's original 778/1742 or its later 580 K clamp.
        phase("energetic_silver", 744, 1511);
        phase("vivid_alloy", 1733, 2648);
        phase("desh", 1602, 3634);
        phase("workers_alloy", 1328, 3033);
        phase("niflheim_power", 2811, 5134);
        phase("muspelheim_power", 2811, 5134);
        // A configuration divider affects amounts before thermal averaging;
        // it is not the denominator of the final weighted temperature.
        assertEquals(2378, (2372 + 3896 + 4 * 2000) / 6);
        assertEquals(1328, (4 * 1602 + 234) / 5);
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("hydrated_coal"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("methane_ice"));
        assertTrue(CrucibleSmeltingRule.hasMeltingFlag("yellorite"));
        assertEquals(U / 3, CrucibleSmeltingRule.find("yellorite").convert(U));
    }

    @Test
    void alloyDensityUsesCommonDividersAndStatisticsCopyingIndependentlyOfHeat() {
        double silica = (2329.6 + 2 * 1.429) / 3;
        double alumina = (2 * 2698 + 3 * 1.429) / 5;
        double ceramic = sourceDensity(18, new long[]{5, 12}, alumina, silica);
        double clay = sourceDensity(2, new long[]{2, 1}, ceramic, 1000);
        double obsidian = sourceDensity(64, new long[]{1, 1, 6, 4}, 1738, 7874, silica, 1.429);
        double pearl = sourceDensity(10, new long[]{1, 4, 5, 6}, 1850, 862, 1.2506, 0);
        double eye = sourceDensity(9, new long[]{9, 1}, pearl, 1000);
        double darkSteel = sourceDensity(1, new long[]{1, 9}, 7874, obsidian);
        double endSteel = sourceDensity(1, new long[]{1, 1, 9}, silica, darkSteel, obsidian);
        double melodic = sourceDensity(1, new long[]{1, 1}, endSteel, eye);
        double stellar = sourceDensity(2, new long[]{1, 1, 4}, melodic, 1000, clay);
        double desh = sourceDensity(9, new long[]{2, 2, 1, 1, 1, 1, 1},
                2340, 6145, 7007, 8570, 8860, 6770, 534);
        density("ender_pearl", pearl);
        density("ender_eye", eye);
        density("endstone", silica);
        density("energetic_silver", 10501); // :1753 stealStatsElement(Ag), not config density.
        density("obsidian_steel", darkSteel);
        density("dark_steel", darkSteel); // Source ore-name alias.
        density("end_steel", endSteel);
        density("melodic_alloy", melodic);
        density("stellar_alloy", stellar);
        density("vivid_alloy", 10501 + pearl);
        density("yellorite", 1000 + 2 * 1.429);
        density("elven_elementium", 7874); // :1822 steal(Steel).
        density("elementium", 7874);
        for (String name : new String[]{"elvorium", "niflheim_power", "muspelheim_power"})
            density(name, 7874 + 3530);
        density("desh", desh);
        density("workers_alloy", desh + 13533.6 / 4);
        assertNotEquals((melodic + 1000 + 4 * clay) / 6, stellar, 1);
    }

    @Test
    void unknownNamesRemainUnknownAndPhysicsLookupsDoNotChangeRegistryIdentity() {
        for (String name : new String[]{"unknown_sunnarium", "stellar_alloy_like", "thirdparty:yellorium",
                "workers_alloy_unknown", "thirdparty:endium"}) {
            assertEquals(-1, CrucibleMaterialPhaseData.knownMeltingPoint(name), name);
            assertEquals(Long.MAX_VALUE, CrucibleMaterialPhaseData.boilingPoint(name), name);
            if (name.indexOf(':') < 0) {
                assertFalse(CrucibleTransferLogic.hasKnownGt6MaterialDensity(name), name);
                assertEquals(1200, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 0), name);
            } else {
                // The existing physical-density API accepts namespaced host
                // spellings. It does not authorize a registry-name migration.
                density(name, 1000);
                assertEquals(name, GT6MaterialIdentity.hostRecyclingName(name));
                assertFalse(GT6MaterialIdentity.allowsRegistryNamespace(name, "gregtech:" + name.split(":")[1]));
            }
            assertFalse(CrucibleSmeltingRule.hasDefaultSelfTarget(name), name);
        }
    }

    private static void phase(String name, int melting, long boiling) {
        assertEquals(melting, CrucibleMaterialPhaseData.knownMeltingPoint(name), name);
        assertEquals(boiling, CrucibleMaterialPhaseData.boilingPoint(name), name);
    }

    private static void density(String name, double expected) {
        assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(name), name);
        assertEquals(expected, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name), 1e-6, name);
        assertEquals(expected, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 999999), 1e-6, name);
    }

    private static double sourceDensity(long divider, long[] weights, double... densities) {
        double result = 0;
        for (int i = 0; i < weights.length; i++) {
            long amount = BigInteger.valueOf(U).multiply(BigInteger.valueOf(weights[i]))
                    .divide(BigInteger.valueOf(divider)).longValueExact();
            result += densities[i] * amount / U;
        }
        return result;
    }
}
