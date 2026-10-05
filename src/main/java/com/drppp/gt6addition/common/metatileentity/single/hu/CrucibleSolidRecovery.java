package com.drppp.gt6addition.common.metatileentity.single.hu;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

/** GT6 manual/shovel recovery rules, without committing crucible contents. */
final class CrucibleSolidRecovery {
    private CrucibleSolidRecovery() {}

    static int outputCount(long amount, long unit, int stackLimit, boolean shovel) {
        if (amount <= 0 || unit <= 0 || stackLimit <= 0) return 0;
        return (int) Math.min(amount / unit, shovel ? stackLimit : 1);
    }

    /** GT6's ordinary shovel uses 10/100 of a mining use per recovered item. */
    static int shovelDamage(int count) {
        // CEu/vanilla durability is integral: round the whole operation up.
        return count <= 0 ? 0 : 1 + (count - 1) / 10;
    }

    /** ST.add accepts the entire output in one main-inventory slot, or does nothing. */
    static boolean insertWholeStack(List<ItemStack> inventory, int currentSlot, int inventoryLimit,
                                    ItemStack output) {
        if (output.isEmpty() || output.getCount() > Math.min(output.getMaxStackSize(), inventoryLimit)) {
            return false;
        }
        for (int slot = 0; slot < inventory.size(); slot++) {
            if (slot == currentSlot) continue;
            ItemStack existing = inventory.get(slot);
            if (canMerge(existing, output, inventoryLimit)) {
                existing.grow(output.getCount());
                return true;
            }
        }
        for (int slot = 0; slot < inventory.size(); slot++) {
            if (slot != currentSlot && inventory.get(slot).isEmpty()) {
                inventory.set(slot, output.copy());
                return true;
            }
        }
        if (currentSlot >= 0 && currentSlot < inventory.size()) {
            ItemStack existing = inventory.get(currentSlot);
            if (existing.isEmpty()) {
                inventory.set(currentSlot, output.copy());
                return true;
            }
            if (canMerge(existing, output, inventoryLimit)) {
                existing.grow(output.getCount());
                return true;
            }
        }
        return false;
    }

    private static boolean canMerge(ItemStack existing, ItemStack output, int inventoryLimit) {
        return ItemHandlerHelper.canItemStacksStack(existing, output) &&
                output.getCount() <= Math.min(existing.getMaxStackSize(), inventoryLimit) - existing.getCount();
    }
}
