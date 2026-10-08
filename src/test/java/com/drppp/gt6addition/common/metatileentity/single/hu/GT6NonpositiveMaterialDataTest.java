package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GT6NonpositiveMaterialDataTest {
    @Test
    void refinedIronUsesOnlyItsExplicitNegativeIdSourceSemantics() {
        assertTrue(GT6NonpositiveMaterialData.contains("Refined_Iron"));
        assertTrue(CrucibleSolidifyingRule.isSnapshotMaterial("refined_iron"));
        assertEquals("iron", CrucibleSmeltingRule.find("refinediron").target);
        assertEquals("iron", CrucibleSolidifyingRule.target("refinediron"));
        assertEquals(2011, CrucibleMaterialPhaseData.knownMeltingPoint("refinediron"));
        assertEquals(3134L, CrucibleMaterialPhaseData.boilingPoint("refinediron"));
        assertEquals(Integer.valueOf(0), GT6MaterialHazardData.knownCombustionFlags("refinediron"));
        assertFalse(GT6MaterialHazardData.shouldBurn("refinediron", 2012, true));
        assertEquals(Boolean.FALSE, GT6MaterialHazardData.knownAcidFlag("refinediron"));
    }

    @Test
    void placeholdersAndTierMarkersAreNotPromotedToPhysicalMaterials() {
        for (String name : new String[]{"NULL", "Empty", "Organic", "Crystal", "Unknown", "Cobblestone",
                "Primitive", "Basic", "Good", "Advanced", "Data", "Elite", "Master", "Ultimate",
                "Quantum", "Superconductor", "Infinite", "Crystal Flux", "Draconic"}) {
            assertFalse(GT6NonpositiveMaterialData.contains(name), name);
        }
        assertFalse(GT6NonpositiveMaterialData.contains("thirdparty:refined_iron"));
        assertFalse(GT6NonpositiveMaterialData.contains("made_up_material"));
    }
}
