package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GT6RemainingElementDataTest {
    @Test
    void explicitElementDensityDeclarationsIncludeZeroAndNativeSavedNames() {
        // Independent MT.java literals (:465,519-520,696,722,744,793).
        int[] ids = {760, 1190, 1200, 1260, 1520, 1740, 2210};
        String[] names = {"OsmiumElemental", "Ununennium", "Unbinilium", "Trinium", "Vibranium", "Naquadah", "Atlarus"};
        double[] densities = {22610, 0, 0, 1068.74, 3239.78365, 21000, 21246.25421};
        for (int i = 0; i < ids.length; i++) {
            assertEquals(names[i], GT6MaterialIdentity.name(ids[i]));
            assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(names[i]), names[i]);
            assertEquals(densities[i], CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(names[i]), 1e-8, names[i]);
            assertEquals(densities[i], CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(names[i], 7874), 1e-8, names[i]);
        }
        assertEquals(22610, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("osmium"));
        assertEquals("germanium", GT6MaterialIdentity.hostRecyclingName("Osmium"));
    }

    @Test
    void particlesKeepTheirExplicitZeroPhaseDataWithoutInventedMeltingFlags() {
        String[] names = {"Photon", "Neutrino", "Neutron", "Proton", "Electron"};
        for (int i = 0; i < names.length; i++) {
            assertEquals(names[i], GT6MaterialIdentity.name(i + 1));
            assertEquals(0, CrucibleMaterialPhaseData.knownMeltingPoint(names[i]));
            assertEquals(0, CrucibleMaterialPhaseData.boilingPoint(names[i]));
            assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(names[i]));
            assertEquals(0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(names[i]));
            assertTrue(CrucibleTransferLogic.isAirDensity(CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(names[i])));
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(names[i]));
            assertFalse(CrucibleSmeltingRule.hasMeltingFlag(names[i]));
            assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget(names[i]));
        }
    }

    @Test
    void literalElementAliasesShareDensitiesButUnknownMaterialsStayUnknown() {
        String[][] aliases = {{"Unbihexium", "Trinium"}, {"Unpentbium", "Vibranium"},
                {"Unseptquadium", "Naquadah"}, {"Bibiunium", "Atlarus"}};
        for (String[] alias : aliases) {
            assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(alias[0]));
            assertEquals(CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(alias[1]),
                    CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(alias[0]));
        }
        assertEquals(-1, CrucibleMaterialPhaseData.knownMeltingPoint("unknown_particle"));
        assertEquals(Long.MAX_VALUE, CrucibleMaterialPhaseData.boilingPoint("unknown_particle"));
        assertFalse(CrucibleTransferLogic.hasKnownGt6MaterialDensity("unknown_particle"));
        assertEquals(1200D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("unknown_particle", 0));
    }
}
