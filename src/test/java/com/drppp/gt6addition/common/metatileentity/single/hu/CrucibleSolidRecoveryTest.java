package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrucibleSolidRecoveryTest {
    @BeforeAll
    static void bootstrapMinecraft() { Bootstrap.register(); }

    @Test
    void handsRecoverOneAndShovelsRespectWholeUnitsAndStackLimits() {
        long scrap = GTValues.M / 9L;
        assertEquals(1, CrucibleSolidRecovery.outputCount(16L * GTValues.M, scrap, 18, false));
        assertEquals(18, CrucibleSolidRecovery.outputCount(16L * GTValues.M, scrap, 18, true));
        assertEquals(2, CrucibleSolidRecovery.outputCount(3 * scrap - 1, scrap, 18, true));
        assertEquals(0, CrucibleSolidRecovery.outputCount(scrap - 1, scrap, 18, false));
        assertEquals(18, CrucibleSolidRecovery.outputCount(Long.MAX_VALUE, scrap, 18, true));
        assertEquals(0, CrucibleSolidRecovery.outputCount(GTValues.M, 0, 18, true));
        assertEquals(0, CrucibleSolidRecovery.outputCount(GTValues.M, scrap, 0, true));
        assertEquals(0, CrucibleSolidRecovery.outputCount(-1, scrap, 18, true));
    }

    @Test
    void ordinaryShovelDurabilityRoundsWholeOperationUpFromGt6Tenths() {
        assertEquals(0, CrucibleSolidRecovery.shovelDamage(0));
        assertEquals(1, CrucibleSolidRecovery.shovelDamage(1));
        assertEquals(1, CrucibleSolidRecovery.shovelDamage(10));
        assertEquals(2, CrucibleSolidRecovery.shovelDamage(11));
        assertEquals(2, CrucibleSolidRecovery.shovelDamage(18));
        assertEquals(7, CrucibleSolidRecovery.shovelDamage(64));
    }

    @Test
    void wholeStackMergesBeforeAnEmptySlotAndDoesNotConsumeTheOfferedStack() {
        List<ItemStack> inventory = emptyInventory();
        inventory.set(0, new ItemStack(Items.IRON_SHOVEL));
        inventory.set(7, new ItemStack(Items.IRON_INGOT, 40));
        ItemStack output = new ItemStack(Items.IRON_INGOT, 18);
        assertTrue(CrucibleSolidRecovery.insertWholeStack(inventory, 0, 64, output));
        assertEquals(58, inventory.get(7).getCount());
        assertTrue(inventory.get(1).isEmpty());
        assertEquals(18, output.getCount());
        assertEquals(Items.IRON_SHOVEL, inventory.get(0).getItem());
    }

    @Test
    void fullInventoryWithOnlyPartialStackSpaceDoesNotPartiallyInsert() {
        List<ItemStack> inventory = fullInventory();
        inventory.set(2, new ItemStack(Items.IRON_INGOT, 55));
        inventory.set(3, new ItemStack(Items.IRON_INGOT, 55));
        ItemStack output = new ItemStack(Items.IRON_INGOT, 18);
        assertFalse(CrucibleSolidRecovery.insertWholeStack(inventory, 0, 64, output));
        assertEquals(55, inventory.get(2).getCount());
        assertEquals(55, inventory.get(3).getCount());
        assertEquals(18, output.getCount());
    }

    @Test
    void copyingIntoAnEmptySlotPreservesTagsWithoutAliasing() {
        List<ItemStack> inventory = emptyInventory();
        ItemStack output = new ItemStack(Items.IRON_INGOT, 18);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("provenance", "original");
        output.setTagCompound(tag);
        assertTrue(CrucibleSolidRecovery.insertWholeStack(inventory, 0, 64, output));
        assertTrue(inventory.get(0).isEmpty());
        assertNotSame(output, inventory.get(1));
        inventory.get(1).getTagCompound().setString("provenance", "changed");
        assertEquals("original", output.getTagCompound().getString("provenance"));
    }

    @Test
    void mismatchedMetadataTagsAndInventoryLimitsCannotMerge() {
        List<ItemStack> inventory = fullInventory();
        inventory.set(1, new ItemStack(Items.COAL, 1, 1));
        assertFalse(CrucibleSolidRecovery.insertWholeStack(inventory, 0, 64, new ItemStack(Items.COAL, 18, 0)));
        ItemStack tagged = new ItemStack(Items.COAL, 1, 0);
        tagged.setStackDisplayName("different");
        inventory.set(1, tagged);
        assertFalse(CrucibleSolidRecovery.insertWholeStack(inventory, 0, 64, new ItemStack(Items.COAL, 18, 0)));
        assertFalse(CrucibleSolidRecovery.insertWholeStack(emptyInventory(), 0, 16, new ItemStack(Items.COAL, 18)));
        assertEquals(1, inventory.get(1).getCount());
    }

    @Test
    void currentSlotIsOnlyTheLastFallback() {
        List<ItemStack> inventory = fullInventory();
        inventory.set(0, ItemStack.EMPTY);
        assertTrue(CrucibleSolidRecovery.insertWholeStack(inventory, 0, 64, new ItemStack(Items.IRON_INGOT, 18)));
        assertEquals(18, inventory.get(0).getCount());
        inventory.set(0, new ItemStack(Items.IRON_INGOT, 30));
        assertTrue(CrucibleSolidRecovery.insertWholeStack(inventory, 0, 64, new ItemStack(Items.IRON_INGOT, 18)));
        assertEquals(48, inventory.get(0).getCount());
        assertFalse(CrucibleSolidRecovery.insertWholeStack(inventory, 0, 64, ItemStack.EMPTY));
    }

    private static List<ItemStack> emptyInventory() {
        return new ArrayList<>(Collections.nCopies(36, ItemStack.EMPTY));
    }

    private static List<ItemStack> fullInventory() {
        List<ItemStack> inventory = emptyInventory();
        for (int i = 0; i < inventory.size(); i++) inventory.set(i, new ItemStack(Items.GOLD_INGOT, 64));
        return inventory;
    }
}
