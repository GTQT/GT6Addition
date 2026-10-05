package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleFractionalHeatTest {
    @Test
    void exactWaterIntakeMassDoesNotUseRoundedCapacityAmount() {
        CrucibleFluidUnits.Quantity water = CrucibleFluidUnits.storedFluidAmount(1, 1000);
        double exactMass = weight(water.amount, water.remainder, 1000);
        assertEquals(1.0D / 9.0D, exactMass, 1.0E-12D);
        double integerOnly = weight(water.amount, 0, 1000);
        double roundedCapacityMass = weight(CrucibleFluidUnits.materialAmount(1, 1000), 0, 1000);
        assertTrue(integerOnly < exactMass);
        assertTrue(exactMass < roundedCapacityMass);
    }

    @Test
    void fluidSplitsAndMergesPreserveTheSameTotalHeatMass() {
        for (int fluidUnit : new int[]{144, 504, 1000, 250, 160000, 20736}) {
            CrucibleFluidUnits.Quantity first = CrucibleFluidUnits.storedFluidAmount(1, fluidUnit);
            CrucibleFluidUnits.Quantity second = CrucibleFluidUnits.storedFluidAmount(2, fluidUnit);
            CrucibleFluidUnits.Quantity combined = CrucibleFluidUnits.merge(first.amount, first.remainder,
                    second.amount, second.remainder, CrucibleFluidUnits.STORAGE_UNIT);
            double splitMass = weight(first.amount, first.remainder, 7874) +
                    weight(second.amount, second.remainder, 7874);
            assertEquals(splitMass, weight(combined.amount, combined.remainder, 7874), 1.0E-10D);
            assertEquals(3.0D / fluidUnit * 7874.0D / 9.0D,
                    weight(combined.amount, combined.remainder, 7874), 1.0E-10D);
        }
    }

    @Test
    void fractionalOnlyContentsHavePositiveMassWhileEmptyContentsHaveNone() {
        double fractionMass = weight(0, CrucibleFluidUnits.STORAGE_UNIT / 2, 22610);
        assertTrue(fractionMass > 0);
        assertEquals(0.5D * 22610 / (GTValues.M * 9.0D), fractionMass, 1.0E-15D);
        assertEquals(0, weight(0, 0, 22610));
        assertEquals(22610D / 9D, weight(GTValues.M, 0, 22610), 1.0E-10D);
    }

    @Test
    void migratedFractionsRetainTheirOriginalPhysicalWeight() {
        CrucibleFluidUnits.Quantity migrated = CrucibleFluidUnits.migrateStoredQuantity(500, 1, 250);
        assertEquals((500.0D + 1.0D / 250) * 7874 / (GTValues.M * 9.0D),
                weight(migrated.amount, migrated.remainder, 7874), 1.0E-12D);
    }

    @Test
    void invalidFractionOrDensityCannotPoisonTemperatureMath() {
        assertEquals(0, weight(-1, 0, 1000));
        assertEquals(0, weight(1, -1, 1000));
        assertEquals(0, weight(1, CrucibleFluidUnits.STORAGE_UNIT, 1000));
        assertEquals(0, weight(1, 0, Double.NaN));
        assertEquals(0, weight(1, 0, Double.POSITIVE_INFINITY));
        assertEquals(0, weight(1, 0, -1));
    }

    private static double weight(long amount, int remainder, double density) {
        return CrucibleTransferLogic.materialWeightKg(amount, remainder, CrucibleFluidUnits.STORAGE_UNIT,
                density, GTValues.M);
    }
}
