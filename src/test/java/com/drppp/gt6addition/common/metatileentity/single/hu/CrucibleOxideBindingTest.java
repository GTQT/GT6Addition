package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrucibleOxideBindingTest {
    @Test void columbiteDensityHonorsTheNestedExplicitMnO2Divider() {
        double oxygen = CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("oxygen");
        double niobiumOxide = (2 * CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("niobium") +
                5 * oxygen) / 7;
        double manganeseOxide = CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("manganese") +
                2 * oxygen;
        assertEquals(niobiumOxide,
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtqtcore:niobium_pentoxide"), 1e-9);
        assertEquals(manganeseOxide, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("pyrolusite"), 1e-9);
        assertEquals((7 * niobiumOxide + manganeseOxide) / 8,
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("columbite"), 1e-9);
    }

    @Test void onlyVerifiedFe2O3NamesAreCanonicalized() {
        assertEquals("hematite", GT6MaterialIdentity.canonicalOxideName("bandediron"));
        assertEquals("hematite", GT6MaterialIdentity.canonicalOxideName("ironiiioxide"));
        assertEquals("magnetite", GT6MaterialIdentity.canonicalOxideName("magnetite"));
        assertEquals("ironoxide", GT6MaterialIdentity.canonicalOxideName("ironoxide"));
    }

    @Test void aliasHeatUsesGt6HematiteRatherThanHostFluidTemperature() {
        for (String alias : new String[]{"hematite", "banded_iron", "iron_iii_oxide"}) {
            assertEquals(1207, CrucibleMaterialPhaseData.knownMeltingPoint(alias), alias);
            assertEquals(2414, CrucibleMaterialPhaseData.boilingPoint(alias), alias);
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(alias), alias);
            assertEquals(1, GT6OreMultiplierData.multiplier(alias, 99), alias);
        }
    }

    @Test void oxideDensityIsSharedButMagnetiteIsNotRenamed() {
        double expected = (2 * CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("iron") +
                3 * CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("oxygen")) / 5;
        for (String alias : new String[]{"hematite", "banded_iron", "gtqtcore:iron_iii_oxide"}) {
            assertEquals(expected, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(alias), 1e-9, alias);
        }
    }

    @Test void coreOxideIsConvertedOneToOneToTheSharedHematiteIdentity() {
        CrucibleSmeltingRule rule = CrucibleSmeltingRule.find("iron_iii_oxide");
        assertNotNull(rule);
        assertEquals("hematite", rule.target);
        assertEquals(1234567, rule.convert(1234567));
        assertEquals("hematite", CrucibleSolidifyingRule.target("iron_iii_oxide"));
        assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget("banded_iron"));
        assertNull(CrucibleSmeltingRule.find("banded_iron"));
    }

    @Test void fictionalElementAndOxideHaveDistinctDensityButSharedHeat() {
        assertEquals(5225, CrucibleMaterialPhaseData.knownMeltingPoint("adamantium"));
        assertEquals(5225, CrucibleMaterialPhaseData.knownMeltingPoint("adamantine"));
        assertEquals(14528, CrucibleMaterialPhaseData.boilingPoint("adamantine"));
        assertEquals(13356.24762, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("adamantium"), 1e-9);
        assertEquals((3 * 13356.24762 + 4 * CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("oxygen")) / 7,
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("adamantine"), 1e-9);
    }

    @Test void dolamideRetainsActualDefaultStatisticsWithoutGrantingMeltingTag() {
        assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint("dolamide"));
        assertEquals(3000, CrucibleMaterialPhaseData.boilingPoint("dolamide"));
        assertEquals(1000, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("dolamide"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("dolamide"));
        assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget("dolamide"));
    }
}
