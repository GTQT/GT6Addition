package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Verifies the generated runtime tables retain one consistent source identity per literal GT6 row. */
class GT6LiteralJavaTablesTest {
    @Test
    void all1084LiteralRowsRemainAlignedAcrossRuntimeTables() {
        assertEquals(1084, GT6LiteralTargetRows.ROWS.size());
        assertEquals(1084, GT6LiteralPhaseRows.ROWS.size());
        assertEquals(1084, GT6LiteralDensityRows.ROWS.size());
        assertEquals(1084, GT6LiteralHazardRows.ROWS.size());
        assertEquals(1084, GT6LiteralTargetData.size());
        assertEquals(1084, GT6LiteralPhaseData.size());
        assertEquals(1084, GT6LiteralDensityData.size());
        assertEquals(1084, GT6LiteralHazardData.size());

        for (GT6LiteralTargetRows.Row source : GT6LiteralTargetRows.ROWS) {
            GT6LiteralTargetData.Profile target = GT6LiteralTargetData.findById(source.id);
            GT6LiteralPhaseData.Profile phase = GT6LiteralPhaseData.findById(source.id);
            GT6LiteralDensityData.Profile density = GT6LiteralDensityData.findById(source.id);
            GT6LiteralHazardData.Profile hazard = GT6LiteralHazardData.findById(source.id);
            assertNotNull(target, "target id=" + source.id);
            assertNotNull(phase, "phase id=" + source.id);
            assertNotNull(density, "density id=" + source.id);
            assertNotNull(hazard, "hazard id=" + source.id);

            String mechanicsName = GT6MaterialIdentity.mechanicsName(source.id);
            assertEquals(source.sourceName, target.sourceName, "target source name id=" + source.id);
            assertEquals(source.sourceLine, target.sourceLine, "target source line id=" + source.id);
            assertEquals(source.sourceName, phase.sourceName, "phase source name id=" + source.id);
            assertEquals(source.sourceLine, phase.sourceLine, "phase source line id=" + source.id);
            assertEquals(source.sourceName, density.sourceName, "density source name id=" + source.id);
            assertEquals(source.sourceLine, density.sourceLine, "density source line id=" + source.id);
            assertEquals(source.sourceName, hazard.sourceName, "hazard source name id=" + source.id);
            assertEquals(source.sourceLine, hazard.sourceLine, "hazard source line id=" + source.id);
            assertEquals(mechanicsName, target.mechanicsName, "target mechanics name id=" + source.id);
            assertEquals(mechanicsName, phase.mechanicsName, "phase mechanics name id=" + source.id);
            assertEquals(mechanicsName, density.mechanicsName, "density mechanics name id=" + source.id);
            assertEquals(mechanicsName, hazard.mechanicsName, "hazard mechanics name id=" + source.id);
        }
    }
}
