package com.drppp.gt6addition.common.material;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GT6MachineMaterialsTest {

    @Test
    void customAlloyRatiosMatchGt6() {
        assertEquals("Cu3As", GT6MachineMaterials.FORMULA_ARSENIC_COPPER);
        assertEquals("AsBronze4", GT6MachineMaterials.FORMULA_ARSENIC_BRONZE);
        assertEquals("AncientDebris", GT6MachineMaterials.FORMULA_ANCIENT_DEBRIS);
        assertEquals("Au4AncientDebris4", GT6MachineMaterials.FORMULA_NETHERITE);
        assertEquals("Ta4HfC5", GT6MachineMaterials.FORMULA_TANTALUM_HAFNIUM_CARBIDE);

        assertFormula(GT6MachineMaterials.arsenicCopperFormula(),
                GT6MachineMaterials.Component.COPPER, 3, GT6MachineMaterials.Component.ARSENIC, 1);
        assertFormula(GT6MachineMaterials.arsenicBronzeFormula(),
                GT6MachineMaterials.Component.BRONZE, 4, GT6MachineMaterials.Component.ARSENIC, 1);
        assertFormula(GT6MachineMaterials.netheriteFormula(),
                GT6MachineMaterials.Component.GOLD, 4, GT6MachineMaterials.Component.ANCIENT_DEBRIS, 4);

        GT6MachineMaterials.ComponentPart[] carbide = GT6MachineMaterials.tantalumHafniumCarbideFormula();
        assertEquals(3, carbide.length);
        assertEquals(GT6MachineMaterials.Component.TANTALUM, carbide[0].component);
        assertEquals(4, carbide[0].amount);
        assertEquals(GT6MachineMaterials.Component.HAFNIUM, carbide[1].component);
        assertEquals(1, carbide[1].amount);
        assertEquals(GT6MachineMaterials.Component.CARBON, carbide[2].component);
        assertEquals(5, carbide[2].amount);
    }

    private static void assertFormula(GT6MachineMaterials.ComponentPart[] components,
                                     GT6MachineMaterials.Component firstMaterial, long firstAmount,
                                     GT6MachineMaterials.Component secondMaterial, long secondAmount) {
        assertEquals(2, components.length);
        assertEquals(firstMaterial, components[0].component);
        assertEquals(firstAmount, components[0].amount);
        assertEquals(secondMaterial, components[1].component);
        assertEquals(secondAmount, components[1].amount);
    }
}
