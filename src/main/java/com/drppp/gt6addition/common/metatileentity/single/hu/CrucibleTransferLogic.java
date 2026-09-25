package com.drppp.gt6addition.common.metatileentity.single.hu;

/** Pure boundary calculations shared by the crucible transfer code and tests. */
public final class CrucibleTransferLogic {
    public static final int OBSIDIAN_MELTING_TEMPERATURE = 1_300;
    public static final double GT6_TUNGSTEN_CARBIDE_DENSITY_KG_PER_CUBIC_METER = 15_600.0D;

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

    public static long requiredEnergyPerKelvin(double thermalMassKg) {
        return 1L + (long) (Math.max(0.0D, thermalMassKg) / 100.0D);
    }

    public static long temperatureGainForHeat(long availableHeat, double thermalMassKg) {
        return Math.max(0L, availableHeat) / requiredEnergyPerKelvin(thermalMassKg);
    }

    public static int nextThermalCooldown(int currentCooldown, boolean heatedThisTick,
                                          int heatCooldownTicks, int passiveCooldownTicks) {
        if (heatedThisTick) {
            return heatCooldownTicks;
        }
        int cooldown = Math.max(0, currentCooldown - 1);
        return cooldown == 0 ? passiveCooldownTicks : cooldown;
    }

    public static boolean shouldPassivelyAdjustTemperature(int currentCooldown, boolean heatedThisTick) {
        return !heatedThisTick && currentCooldown <= 1;
    }

    public static long moveTemperatureTowardAmbient(long temperature, long ambientTemperature) {
        if (temperature > ambientTemperature) {
            return temperature - 1L;
        }
        if (temperature < ambientTemperature) {
            return temperature + 1L;
        }
        return temperature;
    }

    public static long accumulateHeat(long storedHeat, long incomingHeat) {
        long nonNegativeStoredHeat = Math.max(0L, storedHeat);
        long acceptedHeat = Math.max(0L, incomingHeat);
        return Long.MAX_VALUE - nonNegativeStoredHeat < acceptedHeat
                ? Long.MAX_VALUE : nonNegativeStoredHeat + acceptedHeat;
    }

    public static double materialWeightKg(long materialAmount, double densityKgPerCubicMeter,
                                          long materialUnitsPerIngot) {
        if (materialAmount <= 0L || densityKgPerCubicMeter <= 0.0D || materialUnitsPerIngot <= 0L) {
            return 0.0D;
        }
        // GT6 uses 9 material units per cubic metre. Material amounts in this
        // mod are normalized to one ingot (GTValues.M), so this keeps the same
        // density-to-mass conversion without relying on the material having a
        // registered fluid.
        return materialAmount * densityKgPerCubicMeter / (materialUnitsPerIngot * 9.0D);
    }

    public static double gt6MaterialDensityKgPerCubicMeter(String materialName,
                                                           double registeredFluidDensity) {
        if (materialName != null) {
            String normalizedName = materialName.replace("_", "").replace(":", "")
                    .toLowerCase(java.util.Locale.ROOT);
            if (normalizedName.endsWith("tungstencarbide")) {
                return GT6_TUNGSTEN_CARBIDE_DENSITY_KG_PER_CUBIC_METER;
            }
        }
        return registeredFluidDensity > 0.0D ? registeredFluidDensity : 1_000.0D;
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
