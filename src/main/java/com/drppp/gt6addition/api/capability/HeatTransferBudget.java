package com.drppp.gt6addition.api.capability;

import java.util.function.LongUnaryOperator;

/** One transfer's shared accepted-HU budget, independent of the number of routes. */
public final class HeatTransferBudget {
    private long remaining;

    public HeatTransferBudget(long offered) {
        remaining = Math.max(0L, offered);
    }

    public long transfer(long routeOffer, LongUnaryOperator receiver) {
        long offered = Math.min(remaining, Math.max(0L, routeOffer));
        if (offered <= 0L) return 0L;
        long accepted = Math.max(0L, Math.min(offered, receiver.applyAsLong(offered)));
        remaining -= accepted;
        return accepted;
    }

    public long remaining() { return remaining; }
}
