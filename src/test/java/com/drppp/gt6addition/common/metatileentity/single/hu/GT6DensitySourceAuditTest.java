package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Independent source values, not availability or values echoed from the
 * implementation. Covers MT's literal/factory statistics only; negative IDs,
 * arbitrary lifecycle changes and full-pack Material bindings are separate.
 */
class GT6DensitySourceAuditTest {
    @Test
    void all1084LiteralIdentitiesMatchIndependentlyEvaluatedSourceDensities() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/gt6-density-source-values.txt");
        assertNotNull(stream);
        Set<Integer> ids = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\\|", -1);
                assertEquals(4, fields.length, line);
                int id = Integer.parseInt(fields[0]);
                assertTrue(ids.add(id), line);
                assertEquals(normalize(fields[1]), normalize(GT6MaterialIdentity.name(id)), line);
                assertTrue(Integer.parseInt(fields[2]) > 0, line);
                String mechanics = GT6MaterialIdentity.mechanicsName(id);
                assertNotNull(mechanics, line);
                double expected = Double.parseDouble(fields[3]);
                assertTrue(Double.isFinite(expected) && expected >= 0, line);
                assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(mechanics), line);
                assertEquals(expected, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(mechanics),
                        1e-6, line);
                assertEquals(expected, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(mechanics, 999999),
                        1e-6, "A conflicting Forge fluid cannot replace native statistics: " + line);
            }
        }
        assertEquals(1084, ids.size(), "Do not count unresolved source identities or reduce this scope");
    }

    @Test
    void numericNativeStatisticsDoNotCollapseHostElementsAndGt6CompoundsOrAlloys() {
        assertEquals("Phosphor", GT6MaterialIdentity.name(150));
        assertEquals("Phosphorus", GT6MaterialIdentity.name(8208));
        assertEquals("tricalciumphosphate", GT6MaterialIdentity.mechanicsName(8208));
        assertEquals("duraniumalloy", GT6MaterialIdentity.mechanicsName(8751));
        assertEquals("tritaniumalloy", GT6MaterialIdentity.mechanicsName(8752));
        assertEquals("gregtech:phosphorus", GT6MaterialIdentity.hostRecyclingName("Phosphor"));
        assertEquals("gregtech:tricalcium_phosphate", GT6MaterialIdentity.hostRecyclingName("Phosphorus"));
        assertEquals("gt6addition:duranium_alloy", GT6MaterialIdentity.hostRecyclingName("Duranium"));
        assertEquals("gt6addition:tritanium_alloy", GT6MaterialIdentity.hostRecyclingName("Tritanium"));
        assertEquals("gregtech:duranium", GT6MaterialIdentity.hostRecyclingName("Duranium Elemental"));
        assertEquals("gregtech:tritanium", GT6MaterialIdentity.hostRecyclingName("Tritanium Elemental"));
        assertNull(GT6MaterialIdentity.mechanicsName(30012));
        assertNull(GT6MaterialIdentity.mechanicsName(-1));
    }

    private static String normalize(String name) {
        assertNotNull(name);
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
