package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleFluidExtractionTest {
    @Test
    void moltenMetalMustReachBothItsMeltingPointAndRegisteredFluidTemperature() {
        assertFalse(CrucibleTransferLogic.canExtractFluidAtTemperature(1810, 1811, 1873));
        assertFalse(CrucibleTransferLogic.canExtractFluidAtTemperature(1811, 1811, 1873));
        assertFalse(CrucibleTransferLogic.canExtractFluidAtTemperature(1872, 1811, 1873));
        assertTrue(CrucibleTransferLogic.canExtractFluidAtTemperature(1873, 1811, 1873));
        assertTrue(CrucibleTransferLogic.canExtractFluidAtTemperature(1874, 1811, 1873));
        // A cooler registered fluid does not lower the material's own melting gate.
        assertFalse(CrucibleTransferLogic.canExtractFluidAtTemperature(1810, 1811, 1000));
        assertTrue(CrucibleTransferLogic.canExtractFluidAtTemperature(1811, 1811, 1000));
    }

    @Test
    void coolFluidsUseMeltingGateButThe320KelvinThresholdIsInclusive() {
        assertTrue(CrucibleTransferLogic.canExtractFluidAtTemperature(273, 273, 300));
        assertFalse(CrucibleTransferLogic.canExtractFluidAtTemperature(272, 273, 300));
        assertTrue(CrucibleTransferLogic.canExtractFluidAtTemperature(300, 273, 319));
        assertFalse(CrucibleTransferLogic.canExtractFluidAtTemperature(319, 273, 320));
        assertTrue(CrucibleTransferLogic.canExtractFluidAtTemperature(320, 273, 320));
        assertFalse(CrucibleTransferLogic.canExtractFluidAtTemperature(400, -1, 300));
    }

    @Test
    void smallSharedSpaceCannotBeJudgedOnlyInMetalVolumeUnits() {
        long waterSpace = CrucibleFluidUnits.materialAmount(1, 1000);
        assertEquals(3629, waterSpace);
        assertEquals(0, CrucibleTransferLogic.materialFluidAmount(waterSpace, GTValues.M, GTValues.L));
        assertEquals(1, CrucibleTransferLogic.acceptedSharedFluidAmount(1000,
                CrucibleTransferLogic.materialFluidAmount(waterSpace, GTValues.M, 1000)));
        assertEquals(0, CrucibleTransferLogic.acceptedSharedFluidAmount(1000,
                CrucibleTransferLogic.materialFluidAmount(waterSpace - 1, GTValues.M, 1000)));
    }
}
