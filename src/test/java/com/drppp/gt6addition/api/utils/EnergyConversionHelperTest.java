package com.drppp.gt6addition.api.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnergyConversionHelperTest {

    @Test
    void directConvertersReachAdvertisedHalfAndDoubleOutputWithoutAJump() {
        int nominalOutput = 32;
        double efficiency = 0.8D;
        int minimumInput = EnergyConversionHelper.minimumInputForHalfNominalOutput(
                nominalOutput, efficiency, 1.0D);
        int maximumInput = EnergyConversionHelper.maximumInputForDoubleOutput(
                nominalOutput, efficiency, 1.0D);

        assertEquals(20, minimumInput);
        assertEquals(80, maximumInput);
        assertEquals(nominalOutput / 2, EnergyConversionHelper.scaledOutputFromInput(
                minimumInput, nominalOutput, efficiency));
        assertEquals(63, EnergyConversionHelper.scaledOutputFromInput(
                maximumInput - 1L, nominalOutput, efficiency));
        assertEquals(nominalOutput * 2, EnergyConversionHelper.scaledOutputFromInput(
                maximumInput, nominalOutput, efficiency));
    }

    @Test
    void steamConvertersIncludeGt6SteamToEnergyRatio() {
        int nominalOutput = 64;
        double efficiency = 0.66D;
        int minimumSteam = EnergyConversionHelper.minimumInputForHalfNominalOutput(
                nominalOutput, efficiency, 2.0D);
        int maximumSteam = EnergyConversionHelper.maximumInputForDoubleOutput(
                nominalOutput, efficiency, 2.0D);

        assertEquals(97, minimumSteam);
        assertEquals(388, maximumSteam);
        assertEquals(nominalOutput / 2, EnergyConversionHelper.scaledOutputFromInput(
                minimumSteam, nominalOutput, efficiency, 2.0D));
        assertEquals(127, EnergyConversionHelper.scaledOutputFromInput(
                maximumSteam - 1L, nominalOutput, efficiency, 2.0D));
        assertEquals(nominalOutput * 2, EnergyConversionHelper.scaledOutputFromInput(
                maximumSteam, nominalOutput, efficiency, 2.0D));
    }

    @Test
    void gt6SteamEngineConversionUsesSteamPerEnergyAndEfficiency() {
        assertEquals(80L, EnergyConversionHelper.convertedEnergyFromInput(200L, 0.8D, 2.0D));
        assertEquals(66L, EnergyConversionHelper.convertedEnergyFromInput(200L, 0.66D, 2.0D));
    }
}
