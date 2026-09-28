package com.drppp.gt6addition.intergations.jei.crucible;

import gregtech.api.GTValues;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrucibleJeiRecipeMakerTest {
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
