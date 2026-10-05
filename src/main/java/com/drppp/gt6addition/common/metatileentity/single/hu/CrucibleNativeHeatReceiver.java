package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.capability.IHeatable;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.LongUnaryOperator;

/**
 * Input-only CEu HU view of the crucible's existing signed thermal buffer.
 * The native source owns its output budget; this view owns one side's input
 * budget. Source temperature must not replace the GT6 thermal-mass calculation.
 */
final class CrucibleNativeHeatReceiver implements IHeatable {
    private final LongSupplier tick;
    private final LongSupplier inputLimit;
    private final LongSupplier buffer;
    private final LongSupplier temperature;
    private final IntSupplier maxTemperature;
    private final LongUnaryOperator simulate;
    private final LongUnaryOperator commit;
    private long budgetTick = Long.MIN_VALUE;
    private long inputThisTick;

    CrucibleNativeHeatReceiver(LongSupplier tick, LongSupplier inputLimit, LongSupplier buffer,
                              LongSupplier temperature, IntSupplier maxTemperature,
                              LongUnaryOperator simulate, LongUnaryOperator commit) {
        this.tick = tick;
        this.inputLimit = inputLimit;
        this.buffer = buffer;
        this.temperature = temperature;
        this.maxTemperature = maxTemperature;
        this.simulate = simulate;
        this.commit = commit;
    }

    private long remaining(long now) {
        long limit = Math.max(0L, inputLimit.getAsLong());
        long used = now == budgetTick ? inputThisTick : 0L;
        return limit > used ? limit - used : 0L;
    }

    @Override
    public long transferHeat(long amount, int sourceTemperature) {
        if (amount <= 0L) return 0L;
        long now = tick.getAsLong();
        long offered = Math.min(amount, remaining(now));
        if (offered <= 0L) return 0L;
        long requested = Math.max(0L, Math.min(offered, simulate.applyAsLong(offered)));
        if (requested <= 0L) return 0L;
        long accepted = Math.max(0L, Math.min(requested, commit.applyAsLong(requested)));
        if (accepted > 0L) {
            if (now != budgetTick) {
                budgetTick = now;
                inputThisTick = 0L;
            }
            inputThisTick += accepted;
        }
        return accepted;
    }

    @Override
    public long changeHeat(long amount) {
        // Input-only: native callers cannot remove the pending buffer or turn
        // negative HU into CU. Explicit CU uses ICrucibleEnergyReceiver instead.
        return amount > 0L ? transferHeat(amount, getTemperature()) : 0L;
    }

    @Override
    public boolean canAcceptHeat() {
        return remaining(tick.getAsLong()) > 0L && simulate.applyAsLong(1L) > 0L;
    }

    @Override public boolean canOutputHeat() { return false; }
    @Override public long getHeatStored() { return Math.max(0L, buffer.getAsLong()); }
    @Override public long getHeatCapacity() { return Long.MAX_VALUE; }
    @Override public int getTemperature() {
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, temperature.getAsLong()));
    }
    @Override public int getMaxTemperature() { return maxTemperature.getAsInt(); }
    // A capability consumer cannot overwrite physical temperature or a vessel's
    // material limit. Only accepted HU and the crucible's phase logic change it.
    @Override public void setTemperature(int value) {}
    @Override public void setMaxTemperature(int value) {}
}
