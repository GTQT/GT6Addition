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

/** Independently evaluated MT source values, never generated from the host
 * tables. Scope includes reviewed late MT alloy adjustments, but not arbitrary
 * other-mod lifecycle mutations or negative/dynamic material identities.
 */
class GT6PhaseSourceAuditTest {
    @Test
    void all1084LiteralIdentitiesMatchSourceTemperaturesAfterLateMtAlloyInitialization() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/gt6-phase-source-values.txt");
        assertNotNull(stream);
        Set<Integer> ids = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            assertEquals("# MT_SHA256=CF5BD26C6D6E0D4F4078950C7E74DB3182DFF613A46F720DDE72BA535515DE1A",
                    reader.readLine());
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\\|", -1);
                assertEquals(5, fields.length, line);
                int id = Integer.parseInt(fields[0]);
                assertTrue(ids.add(id), line);
                assertEquals(normalize(fields[1]), normalize(GT6MaterialIdentity.name(id)), line);
                assertTrue(Integer.parseInt(fields[2]) > 0, line);
                String mechanics = GT6MaterialIdentity.mechanicsName(id);
                assertNotNull(mechanics, line);
                int melt = Integer.parseInt(fields[3]);
                long boil = Long.parseLong(fields[4]);
                assertTrue(melt >= 0 && boil >= melt, line);
                assertTrue(CrucibleMaterialPhaseData.hasKnownMeltingPoint(mechanics), line);
                assertEquals(melt, CrucibleMaterialPhaseData.knownMeltingPoint(mechanics), line);
                assertEquals(boil, CrucibleMaterialPhaseData.boilingPoint(mechanics), line);
            }
        }
        assertEquals(1084, ids.size(), "Do not pass unresolved identities or reduce the source scope");
    }

    @Test
    void lateAlloyClampsChangeOnlyOutputMeltingNotEarlierSnapshotsOrBoiling() {
        // MT:3374 As boils at 1090 K. :3352 non-default Glowstones boil at 600 K.
        phase("arsenic", 887, 1090); // :422, melt and boil are distinct.
        phase("arsenic_copper", 1070, 2835);
        phase("energetic_alloy", 580, 1742);
        // Earlier heat/configuration snapshots do not retroactively recompute when
        // the component or another alloy is modified by TECH.init().
        phase("arsenic_bronze", 1357, 2835);
        phase("energetic_silver", 744, 1511);
        // :1753 stealStatsElement(Ag) copies density/atomic statistics only;
        // it must not overwrite this configuration with Ag's heat profile.
        phase("silver", 1234, 2435);
        assertEquals(10501, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("energetic_silver"));
        // The explicit heat(0,0,0) particle contract must remain zero, unlike
        // a configured composition's minimum 1 K/2 K or constructor defaults.
        phase("photon", 0, 0);
        assertEquals(-1, CrucibleMaterialPhaseData.knownMeltingPoint("unknown_energetic_alloy"));
        assertEquals(Long.MAX_VALUE, CrucibleMaterialPhaseData.boilingPoint("unknown_energetic_alloy"));
    }

    private static void phase(String name, int melt, long boil) {
        assertEquals(melt, CrucibleMaterialPhaseData.knownMeltingPoint(name), name);
        assertEquals(boil, CrucibleMaterialPhaseData.boilingPoint(name), name);
    }

    private static String normalize(String name) {
        return name == null ? null : name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
