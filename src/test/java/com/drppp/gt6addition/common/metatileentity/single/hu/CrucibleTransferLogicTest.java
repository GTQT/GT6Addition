package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleTransferLogicTest {
    @Test
    void specialTankHasIndependentCapacityAndRejectsFluidMixing() {
        assertEquals(16_000, CrucibleTransferLogic.acceptedFluidAmount(null, 0, "water", 20_000, 16_000));
        assertEquals(0, CrucibleTransferLogic.acceptedFluidAmount("water", 2_000, "lava", 1_000, 16_000));
        assertEquals(1_000, CrucibleTransferLogic.acceptedFluidAmount("water", 15_000, "water", 2_000, 16_000));
    }

    @Test
    void simulatedFluidAcceptanceDoesNotChangeTankOrTemperatureState() {
        String fluid = "water";
        int amount = 12_000;
        long temperature = 500;

        int accepted = CrucibleTransferLogic.acceptedFluidAmount(fluid, amount, "water", 8_000, 16_000);

        assertEquals(4_000, accepted);
        assertEquals("water", fluid);
        assertEquals(12_000, amount);
        assertEquals(500, temperature);
    }

    @Test
    void queueIsBoundedTo64ItemCounts() {
        assertEquals(64, CrucibleTransferLogic.acceptedQueueItems(0, 128, 64));
        assertEquals(4, CrucibleTransferLogic.acceptedQueueItems(60, 20, 64));
        assertEquals(0, CrucibleTransferLogic.acceptedQueueItems(64, 1, 64));
    }

    @Test
    void inputMustHaveAMeltableOutputWithinTheCruciblesTemperatureLimit() {
        assertTrue(CrucibleTransferLogic.canMeltWithinLimit(1_800, 1_300, true));
        assertFalse(CrucibleTransferLogic.canMeltWithinLimit(1_299, 1_300, true));
        assertFalse(CrucibleTransferLogic.canMeltWithinLimit(2_000, 1_300, false));
    }

    @Test
    void phaseChangesRespectGt6TemperatureBoundaries() {
        assertTrue(CrucibleTransferLogic.shouldCondenseLava(1_299));
        assertFalse(CrucibleTransferLogic.shouldCondenseLava(1_300));
        assertFalse(CrucibleTransferLogic.shouldBoilWater(372));
        assertTrue(CrucibleTransferLogic.shouldBoilWater(373));
        assertEquals(1, CrucibleTransferLogic.obsidianUnitsForLava(1_000));
        assertEquals(1, CrucibleTransferLogic.obsidianUnitsForLava(1_999));
        assertEquals(2, CrucibleTransferLogic.obsidianUnitsForLava(2_000));
    }

    @Test
    void fluidHeatTransferIsWeightedAndSimulationCalculationIsPure() {
        long simulated = CrucibleTransferLogic.mixTemperature(1_000, 10.0D, 300, 10.0D);
        assertEquals(650, simulated);
        assertEquals(simulated, CrucibleTransferLogic.mixTemperature(1_000, 10.0D, 300, 10.0D));
    }
}
