package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Run this class alone to initialize transfer data before any MTE or phase
 * lookup. Reproduces the first-use path that formerly poisoned the class at
 * world load/placement with "Missing GT6 density: pyrite".
 */
class GT6MaterialInitializationTest {
    @Test
    void coldFirstUseResolvesAllPrerequisitesWithoutPoisoningTheTransferClass() throws Exception {
        Class<?> transfer = assertDoesNotThrow(() -> Class.forName(CrucibleTransferLogic.class.getName(), true,
                CrucibleTransferLogic.class.getClassLoader()));
        Field table = transfer.getDeclaredField("GT6_MATERIAL_DENSITIES");
        table.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String, Double> densities = (Map<String, Double>) table.get(null);
        assertFalse(densities.isEmpty());
        densities.forEach((name, density) -> {
            assertNotNull(density, "Null copied prerequisite: " + name);
            assertTrue(Double.isFinite(density) && density >= 0, name);
        });
        double silica = (2329.6 + 2 * 1.429) / 3;
        double alumina = (2 * 2698 + 3 * 1.429) / 5;
        double pyrite = (7874 + 2 * 2067D) / 3;
        double ruby = (5 * alumina + 7150) / 6;
        double sodalite = mixture(11, new long[]{3, 3, 4, 1}, alumina, silica, 971, 3.214);
        double redstone = (5 * pyrite + 3 * 13533.6 + silica + ruby) / 10;
        double nikolite = (5 * sodalite + 3 * 8960 + silica + 1.7837) / 10; // MT.java:406 Argon.
        density("pyrite", pyrite);
        density("ruby", ruby);
        density("sodalite", sodalite);
        density("redstone", redstone);
        density("nikolite", nikolite);
        density("energium_red", (4 * (5 * alumina / 6) + 5 * redstone) / 9);
        density("energium_cyan", (4 * (5 * alumina / 6) + 5 * nikolite) / 9);
        // Repeated calls after initial world load remain usable for placement,
        // sync, heating and density-based display selection.
        for (int i = 0; i < 3; i++) density("pyrite", pyrite);
    }

    @Test
    void nonNormalizedConfigurationsAndHeatOnlyOverridesKeepTheirQuantities() {
        density("slimy_bone", 1540D / 8);
        density("prismane", 4 * 2267D);
        density("lonsdaleite", 8 * 2267D);
        density("milk", 1000);
        density("salted_butter", 1000 + (971 + 3.214) / 2);
        density("vinteum", 0);
        density("vinteum_purified", 0);
        assertEquals(1, CrucibleMaterialPhaseData.knownMeltingPoint("vinteum"));
        assertEquals(2, CrucibleMaterialPhaseData.boilingPoint("vinteum"));
        assertEquals(313, CrucibleMaterialPhaseData.knownMeltingPoint("salted_butter"));
        assertEquals(500, CrucibleMaterialPhaseData.boilingPoint("salted_butter"));
    }

    private static double mixture(long divider, long[] weights, double... densities) {
        double result = 0;
        for (int i = 0; i < weights.length; i++) {
            long amount = 648648000L * weights[i] / divider;
            result += densities[i] * amount / 648648000D;
        }
        return result;
    }

    private static void density(String name, double expected) {
        assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(name), name);
        assertEquals(expected, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 999999), 1e-6, name);
    }
}
