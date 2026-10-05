package com.drppp.gt6addition.common.metatileentity.single.hu;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Shared automatic input budget and lifecycle of unprocessed items. */
final class CrucibleItemIntake {
    private CrucibleItemIntake() {}

    /** GT6 Smeltery processes at most one item from its input slot per tick. */
    static final class Tick {
        private boolean imported;

        boolean tryImport(BooleanSupplier action) {
            if (imported) return false;
            if (!action.getAsBoolean()) return false;
            imported = true;
            return true;
        }
    }

    static int pendingCount(List<ItemStack> pending) {
        long count = 0;
        for (ItemStack stack : pending) {
            if (stack.isEmpty()) continue;
            count += stack.getCount();
            if (count >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) count;
    }

    /** Move the pending inventory into CEu's drop buffer exactly once. */
    static List<ItemStack> takePendingDrops(List<ItemStack> pending) {
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack stack : pending) {
            if (!stack.isEmpty()) drops.add(stack.copy());
        }
        pending.clear();
        return drops;
    }
}
