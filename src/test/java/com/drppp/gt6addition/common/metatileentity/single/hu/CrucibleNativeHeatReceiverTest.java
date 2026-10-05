package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrucibleNativeHeatReceiverTest {
    @Test
    void nativePushAddsHuWithoutAssigningSourceTemperature() {
        Vessel vessel = new Vessel();
        CrucibleNativeHeatReceiver input = vessel.input();
        assertEquals(128, input.transferHeat(128, 5000));
        assertEquals(128, vessel.buffer);
        assertEquals(288, input.getTemperature());
        assertEquals(128, input.getHeatStored());
        assertEquals(4133, input.getMaxTemperature());
    }

    @Test
    void repeatedTransfersShareOneSideBudgetUntilWorldTickChanges() {
        Vessel vessel = new Vessel();
        CrucibleNativeHeatReceiver input = vessel.input();
        assertEquals(100, input.transferHeat(100, 1000));
        assertEquals(28, input.transferHeat(100, 1000));
        assertFalse(input.canAcceptHeat());
        assertEquals(0, input.transferHeat(100, 1000));
        // A machine update cannot reset the budget twice within one world tick.
        assertEquals(128, vessel.buffer);
        vessel.tick++;
        assertTrue(input.canAcceptHeat());
        assertEquals(128, input.transferHeat(Long.MAX_VALUE, 1000));
        assertEquals(256, vessel.buffer);
    }

    @Test
    void sideViewsHaveSeparateBudgetsAndOnePhysicalBuffer() {
        Vessel vessel = new Vessel();
        CrucibleNativeHeatReceiver north = vessel.input();
        CrucibleNativeHeatReceiver south = vessel.input();
        assertEquals(128, north.transferHeat(200, 1000));
        assertEquals(128, south.transferHeat(200, 1000));
        assertEquals(256, north.getHeatStored());
        assertEquals(256, south.getHeatStored());
        assertEquals(0, north.transferHeat(1, 1000));
        assertEquals(0, south.transferHeat(1, 1000));
    }

    @Test
    void nativeCapabilityCannotExtractHuOrForgeTemperatureAndMaterialLimits() {
        Vessel vessel = new Vessel();
        vessel.buffer = 500;
        CrucibleNativeHeatReceiver input = vessel.input();
        assertFalse(input.canOutputHeat());
        assertEquals(0, input.changeHeat(-100));
        assertEquals(0, input.changeHeat(Long.MIN_VALUE));
        input.setTemperature(5000);
        input.setMaxTemperature(1);
        assertEquals(500, vessel.buffer);
        assertEquals(288, input.getTemperature());
        assertEquals(4133, input.getMaxTemperature());
        assertEquals(128, input.changeHeat(200));
        assertEquals(628, vessel.buffer);
    }

    @Test
    void simulationAndCapabilityQueriesDoNotModifyBufferOrSpendInputBudget() {
        Vessel vessel = new Vessel();
        CrucibleNativeHeatReceiver input = vessel.input();
        for (int i = 0; i < 10; i++) assertTrue(input.canAcceptHeat());
        assertEquals(0, vessel.buffer);
        vessel.allowInput = false;
        assertFalse(input.canAcceptHeat());
        assertEquals(0, input.transferHeat(128, 1000));
        assertEquals(0, vessel.buffer);
        vessel.allowInput = true;
        assertEquals(128, input.transferHeat(128, 1000));
    }

    @Test
    void failedAndPartialCommitsUseOnlyTheActuallyAcceptedBudget() {
        Vessel vessel = new Vessel();
        CrucibleNativeHeatReceiver input = vessel.input();
        vessel.commitLimit = 0;
        assertEquals(0, input.transferHeat(128, 1000));
        vessel.commitLimit = 16;
        assertEquals(16, input.transferHeat(128, 1000));
        vessel.commitLimit = Long.MAX_VALUE;
        assertEquals(112, input.transferHeat(128, 1000));
        assertEquals(128, vessel.buffer);
    }

    @Test
    void signedColdBufferAndOverflowBoundaryKeepOneHuAccount() {
        Vessel vessel = new Vessel();
        vessel.buffer = -200;
        CrucibleNativeHeatReceiver input = vessel.input();
        assertEquals(0, input.getHeatStored());
        assertEquals(128, input.transferHeat(128, 2000));
        assertEquals(-72, vessel.buffer);
        assertEquals(0, input.getHeatStored());
        vessel.tick++;
        vessel.buffer = Long.MAX_VALUE - 2;
        assertEquals(2, input.transferHeat(128, 2000));
        assertEquals(Long.MAX_VALUE, vessel.buffer);
        assertFalse(input.canAcceptHeat());
        assertEquals(0, input.transferHeat(128, 2000));
    }

    @Test
    void invalidAmountsAndDisabledLimitCannotProduceHeat() {
        Vessel vessel = new Vessel();
        CrucibleNativeHeatReceiver input = vessel.input();
        assertEquals(0, input.transferHeat(0, 1000));
        assertEquals(0, input.transferHeat(-1, 1000));
        vessel.limit = 0;
        assertFalse(input.canAcceptHeat());
        assertEquals(0, input.transferHeat(Long.MAX_VALUE, 1000));
        assertEquals(0, vessel.buffer);
    }

    private static final class Vessel {
        long tick;
        long buffer;
        long limit = 128;
        long commitLimit = Long.MAX_VALUE;
        boolean allowInput = true;

        long simulate(long amount) {
            if (!allowInput || amount <= 0) return 0;
            return buffer > 0 ? Math.min(amount, Long.MAX_VALUE - buffer) : amount;
        }

        CrucibleNativeHeatReceiver input() {
            return new CrucibleNativeHeatReceiver(() -> tick, () -> limit, () -> buffer,
                    () -> 288, () -> 4133, this::simulate, amount -> {
                long accepted = Math.min(simulate(amount), commitLimit);
                buffer += accepted;
                return accepted;
            });
        }
    }
}
