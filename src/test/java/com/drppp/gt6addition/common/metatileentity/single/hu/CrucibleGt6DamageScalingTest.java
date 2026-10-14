package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrucibleGt6DamageScalingTest {

    @Test
    void appliesGt6MultiplierOnlyWhenTfcIsLoadedAndTheOptionIsEnabled() {
        assertEquals(80.0F, CrucibleGt6DamageScaling.scale(1.0F, true, true), 0.0F);
        assertEquals(160.0F, CrucibleGt6DamageScaling.scale(2.0F, true, true), 0.0F);
        assertEquals(1.0F, CrucibleGt6DamageScaling.scale(1.0F, true, false), 0.0F);
        assertEquals(1.0F, CrucibleGt6DamageScaling.scale(1.0F, false, true), 0.0F);
        assertEquals(1.0F, CrucibleGt6DamageScaling.scale(1.0F, false, false), 0.0F);
    }
}
