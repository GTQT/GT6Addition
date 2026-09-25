package com.drppp.gt6addition.common.metatileentity.single.hu;

/** Pure boundary calculations shared by the crucible transfer code and tests. */
public final class CrucibleTransferLogic {
    public static final int OBSIDIAN_MELTING_TEMPERATURE = 1_300;

    private CrucibleTransferLogic() {}

    public static int acceptedFluidAmount(String currentFluid, int currentAmount,
                                          String offeredFluid, int offeredAmount, int capacity) {
        if (offeredFluid == null || offeredAmount <= 0 || capacity <= 0 || currentAmount < 0 ||
                (currentAmount > 0 && !offeredFluid.equals(currentFluid))) {
            return 0;
        }
        return Math.min(offeredAmount, Math.max(0, capacity - currentAmount));
    }

    public static int acceptedQueueItems(int queuedItems, int requestedItems, int capacity) {
        return Math.min(Math.max(0, requestedItems), Math.max(0, capacity - queuedItems));
    }

    public static boolean canMeltWithinLimit(int maxTemperature, int meltingTemperature,
                                             boolean hasFluidOutput) {
        return hasFluidOutput && meltingTemperature > 0 && meltingTemperature <= maxTemperature;
    }

    public static boolean shouldCondenseLava(long temperature) {
        return temperature < 1300L;
    }

    public static boolean shouldBoilWater(long temperature) {
        return temperature >= 373L;
    }

    public static int obsidianUnitsForLava(int millibuckets) {
        return Math.max(0, millibuckets) / 1000;
    }

    public static long mixTemperature(long currentTemperature, double currentMass,
                                      long incomingTemperature, double incomingMass) {
        double totalMass = currentMass + incomingMass;
        if (totalMass <= 0.0D) return Math.max(0L, currentTemperature);
        return Math.max(0L, Math.round((currentTemperature * currentMass +
                incomingTemperature * incomingMass) / totalMass));
    }
}
