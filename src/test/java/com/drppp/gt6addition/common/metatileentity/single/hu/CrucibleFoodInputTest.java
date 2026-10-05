package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;

class CrucibleFoodInputTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        net.minecraft.init.Bootstrap.register();
    }

    @Test
    void grainFormsPreserveTheirOwnMaterialAndSeedCropBaleQuantities() {
        String[][] families = {
                {"Barley", "barley"}, {"Rye", "rye"}, {"Rice", "rice"},
                {"Oats", "oat"}, {"AbyssalOats", "abyssal_oat"}
        };
        for (String[] family : families) {
            CrucibleFoodInput.Rule seed = CrucibleFoodInput.oreName("seed" + family[0]);
            CrucibleFoodInput.Rule crop = CrucibleFoodInput.oreName("crop" + family[0]);
            CrucibleFoodInput.Rule bale = CrucibleFoodInput.oreName("bale" + family[0]);
            assertEquals("gt6addition:" + family[1], crop.materials[0]);
            assertArrayEquals(crop.materials, seed.materials);
            assertArrayEquals(crop.materials, bale.materials);
            assertEquals(GTValues.M, crop.amounts[0]);
            assertEquals(crop.amounts[0], 9 * seed.amounts[0]);
            assertEquals(bale.amounts[0], 9 * crop.amounts[0]);
            assertFalse(crop.sameContents(CrucibleFoodInput.oreName("cropWheat")));
            // A nine-unit bale must not enter the vessel's last eight units.
            assertFalse(CrucibleFluidUnits.incomingBatchFits(8 * GTValues.M, 16 * GTValues.M,
                    bale.amounts, new int[1]));
            assertTrue(CrucibleFluidUnits.incomingBatchFits(7 * GTValues.M, 16 * GTValues.M,
                    bale.amounts, new int[1]));
            assertTrue(crop.resolve(name -> {
                assertEquals("gt6addition:" + family[1], name);
                return null;
            }).isEmpty());
        }
        assertEquals(GTValues.M / 9, CrucibleFoodInput.oreName("seedCorn").amounts[0]);
        assertEquals(GTValues.M, CrucibleFoodInput.oreName("cropCorn").amounts[0]);
        assertNull(CrucibleFoodInput.oreName("baleCorn")); // No such GT6 automatic item data.
        assertTrue(CrucibleFoodInput.vanilla("minecraft:potato", 0).sameContents(
                CrucibleFoodInput.oreName("cropPotato")));
        assertNull(CrucibleFoodInput.vanilla("minecraft:baked_potato", 0));
        assertNull(CrucibleFoodInput.vanilla("minecraft:poisonous_potato", 0));
    }

    @Test
    void registeredGrainsAndPotatoDoNotInheritTechnicalFamilyMeltingFlags() {
        int[] ids = {9704, 9705, 9706, 9707, 9719, 9708, 9709};
        String[] names = {"barley", "rye", "rice", "oat", "abyssal_oat", "corn", "potato"};
        for (int i = 0; i < ids.length; i++) {
            String name = names[i];
            assertEquals("gt6addition:" + name, GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(ids[i])));
            assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(3000L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertEquals(1000D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name));
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name));
            assertFalse(CrucibleSmeltingRule.hasMeltingFlag(name));
            assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name));
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 313, true));
            assertEquals(!"potato".equals(name), GT6MaterialHazardData.shouldBurn(name, 314, true));
        }
        assertEquals("gt6addition:oat", GT6MaterialIdentity.hostRecyclingName("Oats"));
        assertEquals("gt6addition:abyssal_oat", GT6MaterialIdentity.hostRecyclingName("AbyssalOats"));
        assertEquals("gtqtcore:Oats", GT6MaterialIdentity.hostRecyclingName("gtqtcore:Oats"));
    }

    @Test
    void meatIntakeAccountsForBoneWhenCheckingCapacity() {
        CrucibleFoodInput.Rule raw = CrucibleFoodInput.vanilla("minecraft:beef", 0);
        assertArrayEquals(new String[]{"gt6addition:meat_raw", "gregtech:bone"}, raw.materials);
        assertArrayEquals(new long[]{2 * GTValues.M, GTValues.M / 9}, raw.amounts);
        // Room for the meat alone must not consume an item containing bone.
        assertFalse(CrucibleFluidUnits.incomingBatchFits(14 * GTValues.M, 16 * GTValues.M,
                raw.amounts, new int[2]));
        long total = 16 * GTValues.M - 2 * GTValues.M - GTValues.M / 9;
        assertTrue(CrucibleFluidUnits.incomingBatchFits(total, 16 * GTValues.M, raw.amounts, new int[2]));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(total + 1, 16 * GTValues.M, raw.amounts, new int[2]));
        assertEquals("gt6addition:meat_cooked",
                CrucibleFoodInput.vanilla("minecraft:cooked_beef", 0).materials[0]);
        assertEquals("gt6addition:meat_rotten",
                CrucibleFoodInput.vanilla("minecraft:rotten_flesh", 0).materials[0]);
    }

    @Test
    void fishVariantOilAndBoneRemainSeparateMaterials() {
        long[][] expected = {
                {2 * GTValues.M, GTValues.M / 9, 2 * GTValues.M},
                {2 * GTValues.M, GTValues.M / 9, 4 * GTValues.M},
                {2 * GTValues.M, GTValues.M / 9, GTValues.M},
                {GTValues.M, GTValues.M / 3, GTValues.M}
        };
        for (int metadata = 0; metadata < expected.length; metadata++) {
            CrucibleFoodInput.Rule raw = CrucibleFoodInput.vanilla("minecraft:fish", metadata);
            assertArrayEquals(new String[]{"gt6addition:fish_raw", "gregtech:bone", "gregtech:fish_oil"}, raw.materials);
            assertArrayEquals(expected[metadata], raw.amounts);
        }
        // LoaderItemData's unconditional wildcard is distinct from its
        // optional Mariculture-specific metadata table.
        assertArrayEquals(expected[2], CrucibleFoodInput.vanilla("minecraft:fish", 100).amounts);
        CrucibleFoodInput.Rule cooked = CrucibleFoodInput.vanilla("minecraft:cooked_fish", 1);
        assertArrayEquals(new String[]{"gt6addition:fish_cooked", "gregtech:bone"}, cooked.materials);
        assertArrayEquals(new long[]{2 * GTValues.M, GTValues.M / 9}, cooked.amounts);
    }

    @Test
    void seedsCropsAndBalesUsePerItemUnitsAndAliasesMustAgree() {
        CrucibleFoodInput.Rule seed = CrucibleFoodInput.vanilla("minecraft:wheat_seeds", 0);
        CrucibleFoodInput.Rule crop = CrucibleFoodInput.vanilla("minecraft:wheat", 0);
        CrucibleFoodInput.Rule bale = CrucibleFoodInput.vanilla("minecraft:hay_block", 0);
        assertEquals(crop.amounts[0], seed.amounts[0] * 9);
        assertEquals(bale.amounts[0], crop.amounts[0] * 9);
        assertTrue(seed.sameContents(CrucibleFoodInput.oreName("seedWheat")));
        assertTrue(crop.sameContents(CrucibleFoodInput.oreName("cropWheat")));
        assertTrue(crop.sameContents(CrucibleFoodInput.oreName("itemWheat")));
        assertTrue(bale.sameContents(CrucibleFoodInput.oreName("baleWheat")));
        assertFalse(seed.sameContents(crop));
        assertFalse(bale.sameContents(crop));
        assertTrue(CrucibleFoodInput.oreName("foodSilkentofu").sameContents(
                CrucibleFoodInput.oreName("foodFirmtofu")));
        assertEquals(GTValues.M, CrucibleFoodInput.oreName("foodFirmtofu").amounts[0]);
    }

    @Test
    void lookupAndUnresolvedMaterialNeverModifyInput() {
        Item item = new Item().setHasSubtypes(true).setRegistryName("minecraft", "fish");
        ItemStack stack = new ItemStack(item, 64, 1);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("display", "unchanged");
        stack.setTagCompound(tag);
        NBTTagCompound before = tag.copy();
        CrucibleFoodInput.Rule rule = CrucibleFoodInput.find(stack);
        assertNotNull(rule);
        assertEquals(4 * GTValues.M, rule.amounts[2]); // One salmon, not 64.
        assertTrue(rule.resolve(name -> null).isEmpty());
        assertEquals(64, stack.getCount());
        assertEquals(1, stack.getMetadata());
        assertEquals(before, stack.getTagCompound());
        assertNull(CrucibleFoodInput.find(ItemStack.EMPTY));
        assertNull(CrucibleFoodInput.find(null));
        assertNull(CrucibleFoodInput.vanilla("anothermod:beef", 0));
        assertNull(CrucibleFoodInput.vanilla("minecraft:rabbit", 0)); // No GT6 vanilla mapping.
        assertNull(CrucibleFoodInput.oreName("listAllmeatraw")); // Not a fixed composition.
    }

    @Test
    void foodComponentsUseGt6StatisticsAndNotHostChemicalRecipes() {
        assertEquals("gregtech:fish_oil", GT6MaterialIdentity.hostRecyclingName("FishOil"));
        assertEquals(1000D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("fish_oil"));
        assertEquals(192.5D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("bone"));
        assertEquals(1115, CrucibleMaterialPhaseData.knownMeltingPoint("bone"));
        assertEquals(1757L, CrucibleMaterialPhaseData.boilingPoint("bone"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("bone"));
        assertFalse(GT6MaterialHazardData.shouldBurn("bone", 314, true));
        assertFalse(GT6MaterialHazardData.shouldBurn("fish_oil", 313, true));
        assertTrue(GT6MaterialHazardData.shouldBurn("fish_oil", 314, true));
        assertEquals(1000, CrucibleFluidUnits.fluidUnit("fish_oil"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("bone"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("fish_oil"));
    }
}
