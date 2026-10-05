package com.drppp.gt6addition.intergations.jei.crucible;

import gregtech.api.GTValues;
import com.drppp.gt6addition.common.metatileentity.single.hu.GT6AlloyRecipes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleJeiRecipeMakerTest {
    @Test
    void registeredPhaseUnitsAreUsedWithoutRoundingJeiVolumes() {
        assertEquals(20736, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M, 20736));
        assertEquals(5184, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M / 4, 20736));
        assertEquals(1000, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M, 1000));
        assertEquals(250, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M, 250));
        assertEquals(160000, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M, 160000));
        assertEquals(0, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M / 9, 250));
        assertEquals(1296, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M, 1296));
        assertEquals(0, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M, 0));
        assertEquals(0, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(Long.MAX_VALUE, 20736));
    }

    @Test
    void displayedBatchMustFitBothInputAndOutputCapacity() {
        assertEquals(false, CrucibleJeiRecipeMaker.batchFitsCapacity(new long[] {14, 3}, 6, 1));
        assertTrue(CrucibleJeiRecipeMaker.batchFitsCapacity(new long[] {14, 3}, 6, 4));
        assertTrue(CrucibleJeiRecipeMaker.batchFitsCapacity(new long[] {14, 3}, 6, 9));
        assertEquals(false, CrucibleJeiRecipeMaker.batchFitsCapacity(new long[] {1}, 17, 1));
        assertEquals(false, CrucibleJeiRecipeMaker.batchFitsCapacity(new long[] {Long.MAX_VALUE}, 1, 1));
        assertEquals(false, CrucibleJeiRecipeMaker.batchFitsCapacity(new long[] {1, 0}, 1, 1));
        assertEquals(false, CrucibleJeiRecipeMaker.batchFitsCapacity(new long[] {1}, 1, 0));
        // Exact ingredient rows alone are insufficient: M/11 is not an integer
        // runtime conversion, even though scaling coefficients of 11 produces whole units.
        assertEquals(false, CrucibleJeiRecipeMaker.batchFitsCapacity(new long[] {11, 11}, 11, 11));
        assertEquals(14 * (GTValues.M / 4), CrucibleJeiRecipeMaker.scaledMaterialAmount(14, 4));
        assertEquals(6 * (GTValues.M / 9), CrucibleJeiRecipeMaker.scaledMaterialAmount(6, 9));
    }

    @Test
    void fractionalFluidIngredientsAndOutputsMustNotBeRounded() {
        assertEquals(250, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M / 4, "water"));
        assertEquals(0, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M / 9, "water"));
        assertEquals(126, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(GTValues.M / 4, "alumina"));
        assertEquals(96, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(6 * (GTValues.M / 9), "iron"));
        assertEquals(0, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(Long.MAX_VALUE, "water"));
        assertEquals(0, CrucibleJeiRecipeMaker.exactDisplayFluidAmount(0, "iron"));
    }

    @Test
    void singleInputRefinementRequiresBothProductAndInputMeltingPoints() {
        assertEquals(2011, CrucibleJeiRecipeMaker.requiredAlloyTemperature(2011,
                java.util.Collections.singletonList(1811)));
        assertEquals(2800, CrucibleJeiRecipeMaker.requiredAlloyTemperature(2800,
                java.util.Collections.singletonList(1358)));
        assertEquals(2000, CrucibleJeiRecipeMaker.requiredAlloyTemperature(1000,
                java.util.Collections.singletonList(2000)));
        java.util.List<Integer> inputs = java.util.Arrays.asList(3000, 1000, 2000);
        assertEquals(2000, CrucibleJeiRecipeMaker.requiredAlloyTemperature(1500, inputs));
        assertEquals(java.util.Arrays.asList(3000, 1000, 2000), inputs);
        assertThrows(IllegalArgumentException.class, () ->
                CrucibleJeiRecipeMaker.requiredAlloyTemperature(1000, java.util.Collections.emptyList()));
    }

    @Test
    void reductionCoefficientsAllowFractionalBatchesInsideSixteenUnitVessel() {
        long portion = GTValues.M / 9;
        long magnetite = 14 * portion;
        long carbon = 3 * portion;
        // The 17-part recipe is a ratio, not a minimum 17-ingot input batch.
        assertTrue(magnetite + carbon < 16 * GTValues.M);
        long conversions = Math.min(magnetite / 14, carbon / 3);
        assertEquals(portion, conversions);
        assertEquals(96, CrucibleJeiRecipeMaker.toFluidAmount(6 * conversions, "iron"));
        assertEquals(0, magnetite - 14 * conversions);
        assertEquals(0, carbon - 3 * conversions);
    }

    @Test
    void displayFluidAmountsUseMaterialUnitsAndDoNotOverflow() {
        assertEquals(1000, CrucibleJeiRecipeMaker.toFluidAmount(GTValues.M, "water"));
        assertEquals(504, CrucibleJeiRecipeMaker.toFluidAmount(GTValues.M, "alumina"));
        assertEquals(144, CrucibleJeiRecipeMaker.toFluidAmount(GTValues.M, "iron"));
        assertEquals(160000, CrucibleJeiRecipeMaker.toFluidAmount(GTValues.M, "steam"));
        assertEquals(144, CrucibleJeiRecipeMaker.toFluidAmount(GTValues.M, "unknown_material"));
        assertEquals(0, CrucibleJeiRecipeMaker.toFluidAmount(-1, "water"));
        assertEquals(Integer.MAX_VALUE, CrucibleJeiRecipeMaker.toFluidAmount(Long.MAX_VALUE, "water"));
    }

    @Test
    void displayCatalogueCannotChangeRuntimeRecipesAndRetainsCommonDivider() {
        assertThrows(UnsupportedOperationException.class, () -> GT6AlloyRecipes.getRecipes().clear());
        boolean found = false;
        for (GT6AlloyRecipes recipe : GT6AlloyRecipes.getRecipes()) {
            if (!"stainless_steel".equals(recipe.getOutputName()) || recipe.getOutputUnits() != 9) continue;
            String[] inputs = recipe.getInputNames();
            long[] units = recipe.getInputUnits();
            assertEquals("wrought_iron", inputs[0]);
            assertEquals("invar", inputs[1]);
            assertEquals(4, units[0]);
            assertEquals(3, units[1]);
            inputs[0] = "incorrect_material";
            units[0] = 99;
            assertEquals("wrought_iron", recipe.getInputNames()[0]);
            assertEquals(4, recipe.getInputUnits()[0]);
            assertEquals(1296, CrucibleJeiRecipeMaker.toFluidAmount(recipe.getOutputUnits() * GTValues.M));
            found = true;
        }
        assertTrue(found);
    }

    @Test
    void threeToOneBatchProducesFourIngotsInsteadOfZeroFluid() {
        long divisor = CrucibleJeiRecipeMaker.gcd(3, 1);
        long amount = (3 / divisor + 1 / divisor) * GTValues.M;
        assertEquals(576, CrucibleJeiRecipeMaker.toFluidAmount(amount));
    }

    @Test
    void redundantRatioIsReducedWithoutChangingProportions() {
        long divisor = CrucibleJeiRecipeMaker.gcd(6, 2);
        assertEquals(2, divisor);
        assertEquals(3, 6 / divisor);
        assertEquals(1, 2 / divisor);
        assertEquals(144, CrucibleJeiRecipeMaker.toFluidAmount(GTValues.M));
    }

    @Test
    void multiComponentRatioAccumulatesCommonDivisor() {
        long divisor = 0;
        for (long part : new long[] {8, 2, 10}) {
            divisor = CrucibleJeiRecipeMaker.gcd(divisor, part);
        }
        assertEquals(2, divisor);
        assertEquals(1440, CrucibleJeiRecipeMaker.toFluidAmount(10 * GTValues.M));
    }
}
