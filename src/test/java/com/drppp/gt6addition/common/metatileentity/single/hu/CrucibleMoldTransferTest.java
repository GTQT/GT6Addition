package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CrucibleMoldTransferTest {
    @Test
    void lavaCastingUsesOneBucketPerSolidUnitInsteadOfOneMetalIngotVolume() {
        long required = CrucibleTransferLogic.requiredMoldInput(GTValues.M, 1000, GTValues.L);
        assertEquals(25_200_000L, required);
        assertEquals(1000, CrucibleTransferLogic.materialFluidAmount(required, GTValues.M, GTValues.L));
        assertEquals(0, CrucibleTransferLogic.transferToMold(GTValues.M,
                amount -> amount >= required ? required : 0, amount -> {
                    fail("144 mB lava cannot produce a full obsidian plate");
                    return amount;
                }));
        assertEquals(required, CrucibleTransferLogic.transferToMold(required,
                amount -> amount >= required ? required : 0, amount -> amount));
        assertEquals(GTValues.M, CrucibleTransferLogic.requiredMoldInput(GTValues.M, 1, 1));
        assertEquals(2_800_000L, CrucibleTransferLogic.requiredMoldInput(GTValues.M / 9, 1000, GTValues.L));
    }

    @Test
    void sourceConversionRoundsUpAndRejectsInvalidOrOverflowingRequirements() {
        assertEquals(7, CrucibleTransferLogic.requiredMoldInput(1, 1000, 144));
        assertEquals(Long.MAX_VALUE, CrucibleTransferLogic.requiredMoldInput(Long.MAX_VALUE, 1, 1));
        assertEquals(0, CrucibleTransferLogic.requiredMoldInput(Long.MAX_VALUE, 1000, 144));
        assertEquals(0, CrucibleTransferLogic.requiredMoldInput(0, 1000, 144));
        assertEquals(0, CrucibleTransferLogic.requiredMoldInput(-1, 1000, 144));
        assertEquals(0, CrucibleTransferLogic.requiredMoldInput(1, 0, 144));
        assertEquals(0, CrucibleTransferLogic.requiredMoldInput(1, 1000, 0));
    }

    @Test
    void temperatureBoundaryAndAuthoritativeHotTargetControlPouring() {
        assertFalse(CrucibleTransferLogic.canPourIntoMold(100, 1810, 1811, true));
        assertTrue(CrucibleTransferLogic.canPourIntoMold(100, 1811, 1811, true));
        assertTrue(CrucibleTransferLogic.canPourIntoMold(100, 1812, 1811, true));
        assertFalse(CrucibleTransferLogic.canPourIntoMold(100, 1812, 1811, false));
        assertFalse(CrucibleTransferLogic.canPourIntoMold(0, 1812, 1811, true));
        assertFalse(CrucibleTransferLogic.canPourIntoMold(-1, 1812, 1811, true));
        assertFalse(CrucibleTransferLogic.canPourIntoMold(100, 1812, -1, true));
    }

    @Test
    void receiverSeesWholeAvailableAmountAndChoosesItsActualDemand() {
        long[] received = {0, 0};
        long filled = CrucibleTransferLogic.transferToMold(1000, amount -> {
            received[0] = amount;
            return amount >= 700 ? 700 : 0;
        }, amount -> {
            received[1] = amount;
            return amount;
        });
        assertEquals(1000, received[0]);
        assertEquals(700, received[1]);
        assertEquals(700, filled);
        assertEquals(300, 1000 - filled);
    }

    @Test
    void rejectedSimulationNeverCommitsOrConsumesSource() {
        for (long refused : new long[]{0, -1}) {
            assertEquals(0, CrucibleTransferLogic.transferToMold(1000, amount -> refused, amount -> {
                fail("A rejected simulation must not perform a real fill");
                return amount;
            }));
        }
    }

    @Test
    void failedAndPartialCommitDebitOnlyActualCommittedUnits() {
        assertEquals(0, CrucibleTransferLogic.transferToMold(1000, amount -> 700, amount -> 0));
        assertEquals(0, CrucibleTransferLogic.transferToMold(1000, amount -> 700, amount -> -1));
        assertEquals(250, CrucibleTransferLogic.transferToMold(1000, amount -> 700, amount -> {
            assertEquals(700, amount);
            return 250;
        }));
    }

    @Test
    void callbackResultsCannotMakeSourceDebitMoreThanOfferedUnits() {
        assertEquals(1000, CrucibleTransferLogic.transferToMold(1000, amount -> Long.MAX_VALUE, amount -> {
            assertEquals(1000, amount);
            return Long.MAX_VALUE;
        }));
        assertEquals(700, CrucibleTransferLogic.transferToMold(1000, amount -> 700, amount -> Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, CrucibleTransferLogic.transferToMold(Long.MAX_VALUE,
                amount -> amount, amount -> amount));
    }

    @Test
    void emptySourceDoesNotCallReceiver() {
        for (long available : new long[]{0, -1}) {
            assertEquals(0, CrucibleTransferLogic.transferToMold(available, amount -> {
                fail("An empty source must not simulate a fill");
                return amount;
            }, amount -> {
                fail("An empty source must not commit a fill");
                return amount;
            }));
        }
    }
}
