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

    public static int maximumInputForDoubleOutput(int nominalOutput, double efficiency) {
        if (nominalOutput <= 0 || efficiency <= 0.0D) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil((nominalOutput * 2.0D) / efficiency));
    }

    public static int scaledOutputFromInput(long input, int nominalOutput, double efficiency) {
        if (input <= 0L || nominalOutput <= 0 || efficiency <= 0.0D) {
            return 0;
        }
        long scaled = (long) Math.floor(input * efficiency / 2.0D);
        long clamped = Math.min((long) nominalOutput * 2L, Math.max(0L, scaled));
        return (int) Math.min(Integer.MAX_VALUE, clamped);
    }

    public static int directOutputFromInput(long input, int maxOutput, double efficiency) {
        if (input <= 0L || maxOutput <= 0 || efficiency <= 0.0D) {
            return 0;
        }
        long scaled = (long) Math.floor(input * efficiency);
        long clamped = Math.min(maxOutput, Math.max(0L, scaled));
        return (int) Math.min(Integer.MAX_VALUE, clamped);
    }
}
