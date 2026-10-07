package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Inventory of implemented source data, NOT a claim of complete GT6 parity.
 * Missing data stays visible in the report; runtime fallbacks cannot count as
 * implemented data. Availability does not prove every table value correct;
 * explicit real-world overrides still require source-by-source comparison.
 * Negative/dynamic IDs and full-pack registration are separate.
 */
class GT6MaterialCoverageAuditTest {
    @Test
    void reportEveryLiteralIdentityWithoutCountingHostFallbacksAsSourceData() throws Exception {
        List<String> rows = new ArrayList<>();
        rows.add("gt6_id,native_name,known_melting_k,known_boiling_k,explicit_density_kg_m3,missing_implemented_data");
        Set<Integer> identities = new HashSet<>();
        int missingMelt = 0, missingBoil = 0, missingDensity = 0, complete = 0;
        for (int id = 1; id <= Short.MAX_VALUE; id++) {
            String name = GT6MaterialIdentity.name(id);
            if (name == null) continue;
            assertTrue(identities.add(id));
            assertFalse(name.isEmpty());
            int melting = CrucibleMaterialPhaseData.knownMeltingPoint(name);
            long boiling = CrucibleMaterialPhaseData.boilingPoint(name);
            boolean densityKnown = CrucibleTransferLogic.hasKnownGt6MaterialDensity(name);
            double density = CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name);
            assertTrue(Double.isFinite(density) && density >= 0, name);
            if (!densityKnown) assertEquals(0D, density, name);
            List<String> missing = new ArrayList<>();
            if (melting < 0) { missing.add("melting"); missingMelt++; }
            if (boiling == Long.MAX_VALUE) { missing.add("boiling"); missingBoil++; }
            if (!densityKnown) { missing.add("density"); missingDensity++; }
            if (missing.isEmpty()) complete++;
            rows.add(id + ",\"" + name.replace("\"", "\"\"") + "\"," +
                    (melting < 0 ? "" : melting) + "," +
                    (boiling == Long.MAX_VALUE ? "" : boiling) + "," +
                    (densityKnown ? Double.toString(density) : "") + "," + String.join(";", missing));
        }
        assertEquals(1084, identities.size(), "Literal source inventory changed; re-audit its scope");
        assertEquals(identities.size() + 1, rows.size());
        Path report = Paths.get("build", "reports", "crucible-parity", "material-coverage.csv");
        Files.createDirectories(report.getParent());
        Files.write(report, rows, StandardCharsets.UTF_8);
        System.out.println("GT6_MATERIAL_COVERAGE literal=" + identities.size() + " allThreeKnown=" + complete +
                " missingMelting=" + missingMelt + " missingBoiling=" + missingBoil +
                " missingDensity=" + missingDensity + " report=" + report.toAbsolutePath());
    }
}
