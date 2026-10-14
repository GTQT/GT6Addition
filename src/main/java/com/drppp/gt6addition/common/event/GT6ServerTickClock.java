package com.drppp.gt6addition.common.event;

/**
 * Server-lifetime tick counter matching GT6's {@code SERVER_TIME} lifecycle.
 * This is deliberately not persisted with a world: GT6 resets its counter
 * when a server starts and advances it once at the start of every server tick.
 */
public final class GT6ServerTickClock {
    private static volatile long serverTick;

    private GT6ServerTickClock() {}

    public static void reset() {
        serverTick = 0L;
    }

    public static void advance() {
        serverTick++;
    }

    public static long current() {
        return serverTick;
    }
}
