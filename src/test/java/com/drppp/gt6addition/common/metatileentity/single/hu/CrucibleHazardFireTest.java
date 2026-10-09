package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleHazardFireTest {
    @Test
    void onlyGregTechOwnedBlocksWithItemFormsAreProtectedFromSyntheticFire() {
        assertTrue(CrucibleHazardFire.isGregTechItemBlock(
                new ResourceLocation(GTValues.MODID, "machine"), true));
        assertFalse(CrucibleHazardFire.isGregTechItemBlock(
                new ResourceLocation("minecraft", "chest"), true));
        assertFalse(CrucibleHazardFire.isGregTechItemBlock(
                new ResourceLocation(GTValues.MODID, "machine"), false));
        assertFalse(CrucibleHazardFire.isGregTechItemBlock(null, true));
    }

    @Test
    void optionalNodeCompatibilityChecksTheActualTileEntityApiType() {
        assertTrue(CrucibleHazardFire.implementsOptionalNodeApi(new TestNodeTile(), TestNode.class));
        assertFalse(CrucibleHazardFire.implementsOptionalNodeApi(new PlainTile(), TestNode.class));
        assertFalse(CrucibleHazardFire.implementsOptionalNodeApi(new TestNodeTile(), null));
        assertFalse(CrucibleHazardFire.implementsOptionalNodeApi(null, TestNode.class));
    }

    private interface TestNode {}

    private static final class PlainTile extends TileEntity {}

    private static final class TestNodeTile extends TileEntity implements TestNode {}
}
