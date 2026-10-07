package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GT6NativeScalarDataTest {
    @Test
    void everyIndependentSourceStatementRetainsItsOwnScalarData() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/gt6-native-scalar-data.txt");
        assertNotNull(stream);
        Set<Integer> ids = new HashSet<>();
        int defaults = 0, overrides = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\\|", -1);
                assertEquals(7, fields.length, line);
                int id = Integer.parseInt(fields[0]);
                assertTrue(ids.add(id), line);
                String literal = fields[1];
                assertEquals(normalize(literal), normalize(GT6MaterialIdentity.name(id)), line);
                int melting = Integer.parseInt(fields[4]), boiling = Integer.parseInt(fields[5]);
                GT6NativeScalarData.Profile scalar = GT6NativeScalarData.find(literal);
                assertNotNull(scalar, line);
                assertEquals(melting, scalar.melting, line);
                assertEquals(boiling, scalar.boiling, line);
                assertEquals(melting, CrucibleMaterialPhaseData.knownMeltingPoint(literal), line);
                assertEquals(boiling, CrucibleMaterialPhaseData.boilingPoint(literal), line);
                assertEquals(1000D, scalar.density, line);
                assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(literal), line);
                assertEquals(1000D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(literal, 999999), line);
                if (melting == 1000 && boiling == 3000) defaults++; else overrides++;
            }
        }
        assertEquals(157, ids.size());
        assertEquals(131, defaults);
        assertEquals(26, overrides);
    }

    @Test
    void constructorStatisticsAreNotAReplacementForUnknownOrComposedMaterials() {
        for (String name : new String[]{"unknown_scalar", "unknown:diorite", "duralumin", "volcanic_ashes",
                "vanilla", "milk_powder", "native_stone_like", "osmium", "iron"}) {
            assertNull(GT6NativeScalarData.find(name), name);
        }
        assertEquals(-1, CrucibleMaterialPhaseData.knownMeltingPoint("unknown_scalar"));
        assertEquals(Long.MAX_VALUE, CrucibleMaterialPhaseData.boilingPoint("unknown_scalar"));
        assertFalse(CrucibleTransferLogic.hasKnownGt6MaterialDensity("unknown_scalar"));
        assertEquals(1200D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("unknown_scalar", 0));
        assertEquals(4321D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("unknown_scalar", 4321));
    }

    @Test
    void copyingElementStatisticsDoesNotCopyItsTemperatures() {
        // MT.java:1623-1625 calls stealStatsElement, not steal/heat.
        String[] names = {"sluice_sand", "platinum_group_sludge", "rare_earth"};
        double[] densities = {(2329.6 + 2 * 1.429) / 3, 21460, 7007};
        for (int i = 0; i < names.length; i++) {
            GT6NativeScalarData.Profile scalar = GT6NativeScalarData.find(names[i]);
            assertNotNull(scalar);
            assertTrue(Double.isNaN(scalar.density), "Density must remain separately sourced");
            assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint(names[i]));
            assertEquals(3000, CrucibleMaterialPhaseData.boilingPoint(names[i]));
            assertEquals(densities[i], CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(names[i], 999999), 1e-8);
        }
        assertEquals(1811, CrucibleMaterialPhaseData.knownMeltingPoint("cast_iron"));
        assertEquals(3134, CrucibleMaterialPhaseData.boilingPoint("cast_iron"));
        assertEquals(7874D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("cast_iron", 0));
    }

    @Test
    void defaultThermalValuesDoNotGrantProcessingOrCombustionExemptions() {
        for (String name : new String[]{"paper", "indigo", "peat", "peat_bituminous"}) {
            assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertFalse(CrucibleSmeltingRule.hasMeltingFlag(name), name);
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 313, false));
        }
        // MT.java:1300 gives Paper a burning product, but no FLAMMABLE tag.
        // Constructor temperatures do not grant that tag, even if CEu does.
        assertFalse(GT6MaterialHazardData.shouldBurn("paper", 314, true));
        for (String name : new String[]{"indigo", "peat", "peat_bituminous"}) {
            assertTrue(GT6MaterialHazardData.shouldBurn(name, 314, false), name);
        }
        assertEquals(273, CrucibleMaterialPhaseData.knownMeltingPoint("milk"));
        assertEquals(373, CrucibleMaterialPhaseData.boilingPoint("honey"));
        assertEquals(987, CrucibleMaterialPhaseData.knownMeltingPoint("umber"));
        assertEquals(1974, CrucibleMaterialPhaseData.boilingPoint("umber"));
    }

    private static String normalize(String name) {
        assertNotNull(name);
        return name.toLowerCase(java.util.Locale.ROOT).replace("-", "").replace("_", "").replace(" ", "");
    }
}
