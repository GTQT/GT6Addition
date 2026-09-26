package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleTransferLogicTest {
    @Test
    void specialTankHasIndependentCapacityAndRejectsFluidMixing() {
        assertEquals(16_000, CrucibleTransferLogic.acceptedFluidAmount(null, 0, "water", 20_000, 16_000));
        assertEquals(0, CrucibleTransferLogic.acceptedFluidAmount("water", 2_000, "lava", 1_000, 16_000));
        assertEquals(1_000, CrucibleTransferLogic.acceptedFluidAmount("water", 15_000, "water", 2_000, 16_000));
    }

    @Test
    void simulatedFluidAcceptanceDoesNotChangeTankOrTemperatureState() {
        String fluid = "water";
        int amount = 12_000;
        long temperature = 500;

        int accepted = CrucibleTransferLogic.acceptedFluidAmount(fluid, amount, "water", 8_000, 16_000);

        assertEquals(4_000, accepted);
        assertEquals("water", fluid);
        assertEquals(12_000, amount);
        assertEquals(500, temperature);
    }

    @Test
    void queueIsBoundedTo64ItemCounts() {
        assertEquals(64, CrucibleTransferLogic.acceptedQueueItems(0, 128, 64));
        assertEquals(4, CrucibleTransferLogic.acceptedQueueItems(60, 20, 64));
        assertEquals(0, CrucibleTransferLogic.acceptedQueueItems(64, 1, 64));
    }

    @Test
    void inputMustHaveAMeltableOutputWithinTheCruciblesTemperatureLimit() {
        assertTrue(CrucibleTransferLogic.canMeltWithinLimit(1_800, 1_300, true));
        assertFalse(CrucibleTransferLogic.canMeltWithinLimit(1_299, 1_300, true));
        assertFalse(CrucibleTransferLogic.canMeltWithinLimit(2_000, 1_300, false));
    }

    @Test
    void heatCapacityUsesTheGt6SingleBlockSmelteryVesselMassAndDensity() {
        assertEquals(1L, CrucibleTransferLogic.requiredEnergyPerKelvin(99.9D));
        assertEquals(2L, CrucibleTransferLogic.requiredEnergyPerKelvin(100.0D));
        assertEquals(3L, CrucibleTransferLogic.requiredEnergyPerKelvin(200.0D));
        double gt6DefaultDensityWallMass = CrucibleTransferLogic.materialWeightKg(7L * GTValues.M,
                1_000.0D, GTValues.M);
        assertEquals(777.777D, gt6DefaultDensityWallMass, 0.001D);
        assertEquals(8L, CrucibleTransferLogic.requiredEnergyPerKelvin(gt6DefaultDensityWallMass));
        assertEquals(8L, CrucibleTransferLogic.temperatureGainForHeat(64L, gt6DefaultDensityWallMass));

        double carbideDensity = CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("gregtech:tungsten_carbide", 1_000.0D);
        assertEquals(15_600.0D, carbideDensity);
        double carbideWallMass = CrucibleTransferLogic.materialWeightKg(7L * GTValues.M,
                carbideDensity, GTValues.M);
        assertEquals(12_133.333D, carbideWallMass, 0.001D);
        assertEquals(122L, CrucibleTransferLogic.requiredEnergyPerKelvin(carbideWallMass));
        assertEquals(0L, CrucibleTransferLogic.temperatureGainForHeat(64L, carbideWallMass));
        assertEquals(1L, CrucibleTransferLogic.temperatureGainForHeat(122L, carbideWallMass));
        assertEquals(112L, CrucibleTransferLogic.accumulateHeat(64L, 48L));
        long accumulatedHeat = CrucibleTransferLogic.accumulateHeat(64L, 64L);
        assertEquals(1L, CrucibleTransferLogic.temperatureGainForHeat(accumulatedHeat, carbideWallMass));
        assertEquals(6L, accumulatedHeat - 122L);
    }

    @Test
    void osmiumCrucibleWithSixteenIronUnitsMatchesGt6HeatRate() {
        double osmiumDensity = CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(
                "gtceu:osmium", 1_000.0D);
        double ironDensity = CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(
                "gtceu:iron", 1_000.0D);
        double osmiumVesselMass = CrucibleTransferLogic.materialWeightKg(
                7L * GTValues.M, osmiumDensity, GTValues.M);
        double ironMeltMass = CrucibleTransferLogic.materialWeightKg(
                16L * GTValues.M, ironDensity, GTValues.M);
        double totalThermalMass = osmiumVesselMass + ironMeltMass;

        assertEquals(22_610.0D, osmiumDensity);
        assertEquals(7_874.0D, ironDensity);
        assertEquals(31_583.778D, totalThermalMass, 0.001D);
        assertEquals(316L, CrucibleTransferLogic.requiredEnergyPerKelvin(totalThermalMass));

        long storedHeat = 0L;
        long temperatureGain = 0L;
        long requiredHeatPerKelvin = CrucibleTransferLogic.requiredEnergyPerKelvin(totalThermalMass);
        for (int tick = 0; tick < 20; tick++) {
            storedHeat = CrucibleTransferLogic.accumulateHeat(storedHeat, 128L);
            long tickGain = CrucibleTransferLogic.temperatureGainForHeat(storedHeat, totalThermalMass);
            storedHeat -= tickGain * requiredHeatPerKelvin;
            temperatureGain += tickGain;
        }

        assertEquals(8L, temperatureGain);
        assertEquals(32L, storedHeat);
    }

    @Test
    void materialWeightConversionUsesEachMaterialsMolecularMassAndGt6ReferenceWeight() {
        double ironDensity = CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:iron");
        double osmiumDensity = CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:osmium");
        assertEquals(7_874.0D, ironDensity);
        assertEquals(22_610.0D, osmiumDensity);

        double ironIngotWeight = CrucibleTransferLogic.materialWeightKgFromMolecularMass(
                GTValues.M, GTValues.M, 55.845D, ironDensity);
        double osmiumIngotWeight = CrucibleTransferLogic.materialWeightKgFromMolecularMass(
                GTValues.M, GTValues.M, 190.23D, osmiumDensity);
        assertEquals(ironDensity / 9.0D, ironIngotWeight, 0.0001D);
        assertEquals(osmiumDensity / 9.0D, osmiumIngotWeight, 0.0001D);

        assertEquals(2_698.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:aluminum"));
        assertEquals(1_873.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:cesium"));
        assertEquals(1_000.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:water"));
        assertEquals(3_530.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:diamond"));
        assertEquals(929.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:coal"));
        assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity("gtceu:fermium"));
        assertEquals(0.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("gtceu:fermium", 1_000.0D));
    }

    @Test
    void compoundDensityUsesTheCeuComponentMoleculeRatiosLikeGt6() {
        double density = CrucibleTransferLogic.gt6MoleculeDensityKgPerCubicMeter(
                new double[]{3.0D, 1.0D}, new double[]{8_960.0D, 5_776.0D});

        // GT6 computes a compound's density as the component amount-weighted average.
        assertEquals(8_164.0D, density, 0.0001D);
        assertEquals(6_720.0D, CrucibleTransferLogic.gt6MoleculeDensityKgPerCubicMeter(
                new double[]{3.0D, 1.0D}, new double[]{8_960.0D, 0.0D}), 0.0001D);
        assertEquals(density / 9.0D, CrucibleTransferLogic.materialWeightKgFromMolecularMass(
                GTValues.M, GTValues.M, 66.0D, density), 0.0001D);
    }

    @Test
    void gt6CoolingResetsAfterHeatingThenMovesOneKelvinEveryTenTicks() {
        int cooldown = CrucibleTransferLogic.nextThermalCooldown(1, true, 100, 10);
        assertEquals(100, cooldown);
        assertFalse(CrucibleTransferLogic.shouldPassivelyAdjustTemperature(1, true));

        boolean cooled = false;
        for (int tick = 0; tick < 99; tick++) {
            cooled |= CrucibleTransferLogic.shouldPassivelyAdjustTemperature(cooldown, false);
            cooldown = CrucibleTransferLogic.nextThermalCooldown(cooldown, false, 100, 10);
        }
        assertFalse(cooled);
        assertTrue(CrucibleTransferLogic.shouldPassivelyAdjustTemperature(cooldown, false));
        cooldown = CrucibleTransferLogic.nextThermalCooldown(cooldown, false, 100, 10);
        assertEquals(10, cooldown);
        assertEquals(499L, CrucibleTransferLogic.moveTemperatureTowardAmbient(500L, 293L));
        assertEquals(294L, CrucibleTransferLogic.moveTemperatureTowardAmbient(293L, 294L));
    }

    @Test
    void phaseChangesRespectGt6TemperatureBoundaries() {
        assertTrue(CrucibleTransferLogic.shouldCondenseLava(1_299));
        assertFalse(CrucibleTransferLogic.shouldCondenseLava(1_300));
        assertFalse(CrucibleTransferLogic.shouldBoilWater(372));
        assertTrue(CrucibleTransferLogic.shouldBoilWater(373));
        assertEquals(1, CrucibleTransferLogic.obsidianUnitsForLava(1_000));
        assertEquals(1, CrucibleTransferLogic.obsidianUnitsForLava(1_999));
        assertEquals(2, CrucibleTransferLogic.obsidianUnitsForLava(2_000));
    }

    @Test
    void fluidHeatTransferIsWeightedAndSimulationCalculationIsPure() {
        long simulated = CrucibleTransferLogic.mixTemperature(1_000, 10.0D, 300, 10.0D);
        assertEquals(650, simulated);
        assertEquals(simulated, CrucibleTransferLogic.mixTemperature(1_000, 10.0D, 300, 10.0D));
    }
}
