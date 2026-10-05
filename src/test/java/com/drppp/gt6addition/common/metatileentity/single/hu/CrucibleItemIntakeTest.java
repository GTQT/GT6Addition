package com.drppp.gt6addition.common.metatileentity.single.hu;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CrucibleItemIntakeTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        net.minecraft.init.Bootstrap.register();
    }

    @Test
    void automaticSourcesShareOneSuccessfulImportPerTick() {
        AtomicInteger consumed = new AtomicInteger();
        CrucibleItemIntake.Tick tick = new CrucibleItemIntake.Tick();
        assertFalse(tick.tryImport(() -> false)); // A capacity failure does not consume the budget.
        assertTrue(tick.tryImport(() -> { consumed.incrementAndGet(); return true; }));
        assertFalse(tick.tryImport(() -> { consumed.incrementAndGet(); return true; }));
        assertFalse(tick.tryImport(() -> { consumed.incrementAndGet(); return true; }));
        assertEquals(1, consumed.get()); // Rejected sources must not execute a mutating supplier.
        assertTrue(new CrucibleItemIntake.Tick().tryImport(() -> {
            consumed.incrementAndGet();
            return true;
        }));
        assertEquals(2, consumed.get());
    }

    @Test
    void dismantlingReleasesPendingStacksOnceWithTheirOrderMetadataAndTags() {
        Item item = new Item().setHasSubtypes(true).setRegistryName("gt6addition", "intake_test");
        ItemStack first = new ItemStack(item, 32, 1);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("owner", "preserve");
        first.setTagCompound(tag);
        ItemStack second = new ItemStack(item, 40, 2);
        ItemStack last = new ItemStack(item, 8, 1);
        List<ItemStack> pending = new ArrayList<>(Arrays.asList(first, ItemStack.EMPTY, second, last));
        assertEquals(80, CrucibleItemIntake.pendingCount(pending)); // Preserve legacy overflow beyond 64.
        List<ItemStack> drops = CrucibleItemIntake.takePendingDrops(pending);
        assertTrue(pending.isEmpty());
        assertEquals(3, drops.size());
        assertEquals(32, drops.get(0).getCount());
        assertEquals(40, drops.get(1).getCount());
        assertEquals(8, drops.get(2).getCount());
        assertEquals(1, drops.get(0).getMetadata());
        assertEquals(2, drops.get(1).getMetadata());
        assertEquals(1, drops.get(2).getMetadata());
        assertEquals(tag, drops.get(0).getTagCompound());
        assertNotSame(first, drops.get(0));
        drops.get(0).getTagCompound().setString("owner", "changed");
        assertEquals("preserve", first.getTagCompound().getString("owner"));
        assertTrue(CrucibleItemIntake.takePendingDrops(pending).isEmpty());
    }

    @Test
    void queueCapacityCountsItemsAndCannotWrapLegacyOverflow() {
        Item item = new Item().setRegistryName("gt6addition", "intake_count_test");
        List<ItemStack> pending = Arrays.asList(new ItemStack(item, 63), ItemStack.EMPTY);
        assertEquals(1, CrucibleTransferLogic.acceptedQueueItems(
                CrucibleItemIntake.pendingCount(pending), 8, 64));
        List<ItemStack> overflow = Arrays.asList(
                new ItemStack(item, Integer.MAX_VALUE), new ItemStack(item, 64));
        assertEquals(Integer.MAX_VALUE, CrucibleItemIntake.pendingCount(overflow));
        assertEquals(0, CrucibleTransferLogic.acceptedQueueItems(
                CrucibleItemIntake.pendingCount(overflow), 1, 64));
        assertEquals(Integer.MAX_VALUE, overflow.get(0).getCount());
        assertEquals(64, overflow.get(1).getCount());
    }
}
