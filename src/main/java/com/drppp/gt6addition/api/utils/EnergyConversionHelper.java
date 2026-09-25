package com.drppp.gt6addition.api.utils;

public final class EnergyConversionHelper {

    private EnergyConversionHelper() {
    }

    public static int minimumInputForNominalOutput(int nominalOutput, double efficiency) {
        if (nominalOutput <= 0 || efficiency <= 0.0D) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(nominalOutput / efficiency));
    }

    /** Minimum input needed to reach half the nominal output. */
    public static int minimumInputForHalfNominalOutput(int nominalOutput, double efficiency,
                                                       double inputPerOutputUnit) {
        if (!isValidConversion(nominalOutput, efficiency, inputPerOutputUnit)) {
            return 0;
        }
        return ceilToPositiveInt(nominalOutput * 0.5D * inputPerOutputUnit / efficiency);
    }

    public static int maximumInputForDoubleOutput(int nominalOutput, double efficiency) {
        return maximumInputForDoubleOutput(nominalOutput, efficiency, 1.0D);
    }

    public static int maximumInputForDoubleOutput(int nominalOutput, double efficiency,
                                                  double inputPerOutputUnit) {
        if (!isValidConversion(nominalOutput, efficiency, inputPerOutputUnit)) {
            return 0;
        }
        return ceilToPositiveInt(nominalOutput * 2.0D * inputPerOutputUnit / efficiency);
    }

    public static int scaledOutputFromInput(long input, int nominalOutput, double efficiency) {
        return scaledOutputFromInput(input, nominalOutput, efficiency, 1.0D);
    }

    public static int scaledOutputFromInput(long input, int nominalOutput, double efficiency,
                                            double inputPerOutputUnit) {
        if (input <= 0L || !isValidConversion(nominalOutput, efficiency, inputPerOutputUnit)) {
            return 0;
        }
        long scaled = convertedEnergyFromInput(input, efficiency, inputPerOutputUnit);
        long clamped = Math.min((long) nominalOutput * 2L, Math.max(0L, scaled));
        return (int) Math.min(Integer.MAX_VALUE, clamped);
    }

    /** Applies conversion loss and source/output unit ratio, without an output-rate clamp. */
    public static long convertedEnergyFromInput(long input, double efficiency, double inputPerOutputUnit) {
        if (input <= 0L || efficiency <= 0.0D || inputPerOutputUnit <= 0.0D
                || Double.isNaN(efficiency) || Double.isInfinite(efficiency)
                || Double.isNaN(inputPerOutputUnit) || Double.isInfinite(inputPerOutputUnit)) {
            return 0L;
        }
        double scaled = Math.floor(input * efficiency / inputPerOutputUnit);
        if (scaled >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, (long) scaled);
    }

    public static int directOutputFromInput(long input, int maxOutput, double efficiency) {
        if (input <= 0L || maxOutput <= 0 || efficiency <= 0.0D) {
            return 0;
        }
        long scaled = convertedEnergyFromInput(input, efficiency, 1.0D);
        long clamped = Math.min(maxOutput, Math.max(0L, scaled));
        return (int) Math.min(Integer.MAX_VALUE, clamped);
    }

    private static boolean isValidConversion(int nominalOutput, double efficiency, double inputPerOutputUnit) {
        return nominalOutput > 0 && efficiency > 0.0D && inputPerOutputUnit > 0.0D
                && !Double.isNaN(efficiency) && !Double.isInfinite(efficiency)
                && !Double.isNaN(inputPerOutputUnit) && !Double.isInfinite(inputPerOutputUnit);
    }

    private static int ceilToPositiveInt(double value) {
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.max(1, (int) Math.ceil(value));
    }
}
