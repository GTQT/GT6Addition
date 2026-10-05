package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.capability.IHeatable;
import gregtech.api.capability.impl.HeatContainerHandler;
import net.minecraft.util.EnumFacing;

import java.util.function.LongUnaryOperator;

/** Native CEu HU fallback; the add-on's HU/CU/KU buffers have separate direct inputs. */
final class CrucibleNativeHeatTransfer {
    private CrucibleNativeHeatTransfer() {}

    static long pull(IHeatable source, EnumFacing sourceSide, long inputLimit,
                     LongUnaryOperator simulate, LongUnaryOperator commit) {
        if (source == null || inputLimit <= 0L || !source.canOutputHeat()) return 0L;
        long limit = inputLimit;
        if (source instanceof HeatContainerHandler) {
            HeatContainerHandler container = (HeatContainerHandler) source;
            if (!container.canOutputHeat(sourceSide)) return 0L;
            limit = Math.min(limit, container.getMaxOutputHeatFlow());
        }
        // Clamp in long before any conversion: native storage can exceed int.
        long offered = Math.min(Math.max(0L, source.getHeatStored()), Math.max(0L, limit));
        if (offered <= 0L) return 0L;
        long requested = Math.min(offered, simulate.applyAsLong(offered));
        if (requested <= 0L) return 0L;

        // The actual dependency returns the signed change, not the new balance.
        long delta = source.changeHeat(-requested);
        long extracted = delta >= 0L ? 0L : delta < -requested ? requested : -delta;
        if (extracted <= 0L) return 0L;
        long received = Math.max(0L, Math.min(extracted, commit.applyAsLong(extracted)));
        // A receiver can change during source callbacks. Return the unused
        // debit to the source rather than consuming unaccepted heat.
        if (received < extracted) source.changeHeat(extracted - received);
        return received;
    }
}
