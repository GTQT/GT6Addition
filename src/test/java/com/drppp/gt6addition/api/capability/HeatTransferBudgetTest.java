package com.drppp.gt6addition.api.capability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeatTransferBudgetTest {
    @Test void branchesShareOneSourceOffer() {
        HeatTransferBudget budget = new HeatTransferBudget(128);
        assertEquals(128, budget.transfer(128, amount -> amount));
        assertEquals(0, budget.transfer(128, amount -> fail("Exhausted budget called the second receiver")));
        assertEquals(0, budget.remaining());
    }

    @Test void partialAcceptanceLeavesOnlyTheRemainderForLaterBranches() {
        HeatTransferBudget budget = new HeatTransferBudget(128);
        assertEquals(40, budget.transfer(128, amount -> 40));
        assertEquals(88, budget.remaining());
        assertEquals(88, budget.transfer(128, amount -> { assertEquals(88, amount); return amount; }));
    }

    @Test void refusedBranchesDoNotConsumeTheBudget() {
        HeatTransferBudget budget = new HeatTransferBudget(128);
        assertEquals(0, budget.transfer(128, amount -> 0));
        assertEquals(128, budget.transfer(128, amount -> amount));
    }

    @Test void existingRouteLossOfferIsNotIncreased() {
        HeatTransferBudget budget = new HeatTransferBudget(128);
        assertEquals(120, budget.transfer(120, amount -> { assertEquals(120, amount); return amount; }));
        assertEquals(8, budget.transfer(120, amount -> amount));
    }

    @Test void invalidAndOverflowingAmountsDoNotExpandTheBudget() {
        HeatTransferBudget budget = new HeatTransferBudget(128);
        assertEquals(128, budget.transfer(Long.MAX_VALUE, amount -> Long.MAX_VALUE));
        assertEquals(0, budget.remaining());
        HeatTransferBudget invalid = new HeatTransferBudget(Long.MIN_VALUE);
        assertEquals(0, invalid.transfer(128, amount -> fail("Negative budget reached a receiver")));
        assertEquals(0, new HeatTransferBudget(128).transfer(-1, amount -> fail("Negative offer reached a receiver")));
    }

    @Test void negativeReceiverResultDoesNotDebitOrGrowTheBudget() {
        HeatTransferBudget budget = new HeatTransferBudget(128);
        assertEquals(0, budget.transfer(128, amount -> -100));
        assertEquals(128, budget.remaining());
    }

    @Test void maximumLongBudgetDoesNotOverflowAcrossPartialTransfers() {
        HeatTransferBudget budget = new HeatTransferBudget(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE - 1, budget.transfer(Long.MAX_VALUE, amount -> amount - 1));
        assertEquals(1, budget.transfer(Long.MAX_VALUE, amount -> amount));
        assertEquals(0, budget.remaining());
    }
}
