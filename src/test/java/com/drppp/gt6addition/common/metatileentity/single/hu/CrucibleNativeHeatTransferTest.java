package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.capability.IHeatable;
import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class CrucibleNativeHeatTransferTest {
    @Test
    void inputOnlySourceCannotBeDrained() {
        Source source = new Source(1000);
        source.output = false;
        assertEquals(0, CrucibleNativeHeatTransfer.pull(source, EnumFacing.UP, 128,
                amount -> { fail("Input-only heat storage must not be offered"); return amount; },
                amount -> { fail("Input-only heat storage must not be committed"); return amount; }));
        assertEquals(1000, source.stored);
        assertEquals(0, source.debits);
    }

    @Test
    void longStorageIsClampedBeforeTransfer() {
        Source source = new Source(Long.MAX_VALUE);
        assertEquals(128, CrucibleNativeHeatTransfer.pull(source, EnumFacing.UP, 128,
                amount -> { assertEquals(128, amount); return amount; }, amount -> amount));
        assertEquals(Long.MAX_VALUE - 128, source.stored);
    }

    @Test
    void rejectionDoesNotDebitSource() {
        Source source = new Source(1000);
        assertEquals(0, CrucibleNativeHeatTransfer.pull(source, EnumFacing.UP, 128,
                amount -> 0, amount -> { fail("Rejected simulation must not commit"); return amount; }));
        assertEquals(1000, source.stored);
        assertEquals(0, source.debits);
    }

    @Test
    void receiverGetsActualDebitRatherThanSimulatedAmount() {
        Source source = new Source(1000);
        source.debitLimit = 32;
        assertEquals(32, CrucibleNativeHeatTransfer.pull(source, EnumFacing.UP, 128,
                amount -> amount, amount -> { assertEquals(32, amount); return amount; }));
        assertEquals(968, source.stored);
    }

    @Test
    void unusedDebitIsReturnedToSource() {
        Source source = new Source(1000);
        assertEquals(16, CrucibleNativeHeatTransfer.pull(source, EnumFacing.UP, 128,
                amount -> amount, amount -> 16));
        assertEquals(984, source.stored);
        assertEquals(112, source.refunded);
    }

    @Test
    void noOpDebitCannotCreateHeat() {
        Source source = new Source(1000);
        source.debitLimit = 0;
        assertEquals(0, CrucibleNativeHeatTransfer.pull(source, EnumFacing.UP, 128,
                amount -> amount, amount -> { fail("A source that removed no heat cannot supply heat"); return amount; }));
        assertEquals(1000, source.stored);
    }

    @Test
    void emptyAndDisabledInputsDoNotCallReceiver() {
        for (long limit : new long[]{0, -1, 128}) {
            assertEquals(0, CrucibleNativeHeatTransfer.pull(new Source(0), EnumFacing.UP, limit,
                    amount -> { fail("No offer expected"); return amount; }, amount -> amount));
        }
        assertEquals(0, CrucibleNativeHeatTransfer.pull(null, EnumFacing.UP, 128,
                amount -> { fail("No source expected"); return amount; }, amount -> amount));
    }

    private static final class Source implements IHeatable {
        long stored;
        long debitLimit = Long.MAX_VALUE;
        long refunded;
        int debits;
        boolean output = true;

        Source(long stored) { this.stored = stored; }

        @Override public long changeHeat(long delta) {
            if (delta < 0) {
                debits++;
                long removed = Math.min(stored, Math.min(-delta, debitLimit));
                stored -= removed;
                return -removed;
            }
            stored += delta;
            refunded += delta;
            return delta;
        }

        @Override public boolean canOutputHeat() { return output; }
        @Override public long getHeatStored() { return stored; }
        @Override public long transferHeat(long amount, int temperature) { return 0; }
        @Override public int getTemperature() { return 1000; }
        @Override public void setTemperature(int temperature) {}
        @Override public int getMaxTemperature() { return 2000; }
        @Override public void setMaxTemperature(int temperature) {}
        @Override public long getHeatCapacity() { return Long.MAX_VALUE; }
        @Override public boolean canAcceptHeat() { return true; }
    }
}
