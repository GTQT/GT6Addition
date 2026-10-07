package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleTransferLogicTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        net.minecraft.init.Bootstrap.register();
    }

    @Test
    void gt6DisplayScaleKeepsTinyContentsVisibleAndSaturatesBeforeMultiplication() {
        long capacity = 16L * GTValues.M;
        assertEquals(0, CrucibleTransferLogic.displayHeight(0, capacity));
        assertEquals(0, CrucibleTransferLogic.displayHeight(-1, capacity));
        assertEquals(1, CrucibleTransferLogic.displayHeight(1, capacity));
        assertEquals(128, CrucibleTransferLogic.displayHeight(capacity / 2, capacity));
        assertEquals(254, CrucibleTransferLogic.displayHeight(capacity - 1, capacity));
        assertEquals(255, CrucibleTransferLogic.displayHeight(capacity, capacity));
        assertEquals(255, CrucibleTransferLogic.displayHeight(Long.MAX_VALUE, capacity));
        assertEquals(254, CrucibleTransferLogic.displayHeight(Long.MAX_VALUE - 1, Long.MAX_VALUE));
        assertEquals(0, CrucibleTransferLogic.displayHeight(1, 0));
    }

    @Test
    void thaumicAndHexoriumFamiliesKeepDefaultTargetsInsteadOfMemberTargets() {
        String[] originals = {"Any Thaumic Crystal", "AnyThaumicCrystal", "Hexorium"};
        String[] materialNames = {"any_thaumic_crystal", "any_thaumic_crystal", "any_hexorium"};
        for (int i = 0; i < originals.length; i++) {
            String materialName = materialNames[i];
            assertEquals("gt6addition:" + materialName, GT6MaterialIdentity.hostRecyclingName(originals[i]));
            assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(materialName));
            assertNull(CrucibleSmeltingRule.find(materialName)); // GT6 default self, not disabled.
            assertFalse(CrucibleSmeltingRule.hasMeltingFlag(materialName));
            assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint(materialName));
            assertEquals(3000L, CrucibleMaterialPhaseData.boilingPoint(materialName));
            assertEquals(1000D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(materialName));
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(materialName));
            assertFalse(GT6MaterialHazardData.shouldBurn(materialName, 314, true));
            assertFalse(GT6MaterialHazardData.isExplosive(materialName));
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(materialName));
        }
        // The real white member explicitly disables smelting. Its technical
        // family only calls steal(), so copying that rule would lose material.
        CrucibleSmeltingRule member = CrucibleSmeltingRule.find("hexoriumwhite");
        assertNotNull(member);
        assertEquals(0, member.convert(GTValues.M));
        for (String untouched : new String[]{"InfusedDull", "InfusedBalance", "HexoriumWhite",
                "AnyHexorium", "AnyThaumCrystal", "gtqtcore:Hexorium"}) {
            assertEquals(untouched, GT6MaterialIdentity.hostRecyclingName(untouched));
        }
        assertNull(GT6MaterialIdentity.name(30012));
        assertNull(GT6MaterialIdentity.name(30013));
    }

    @Test
    void flourAndGrainFamiliesDoNotInheritBaseWheatsMissingMeltingFlag() {
        for (String name : new String[]{"Any Grains", "AnyGrains", "Any Flour", "AnyFlour",
                "Any Flour Or Grains", "AnyFlourOrGrains"}) {
            assertEquals("gt6addition:any_grains", GT6MaterialIdentity.hostRecyclingName(name));
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("m", name);
            entry.setLong("a", 2 * 648648000L);
            NBTTagCompound list = new NBTTagCompound();
            list.setInteger("size", 1);
            list.setTag("0", entry);
            NBTTagCompound root = new NBTTagCompound();
            root.setTag(CrucibleRecyclingOverride.TAG, list);
            NBTTagCompound before = root.copy();
            int[] calls = {0};
            assertTrue(CrucibleRecyclingOverride.parse(root, resolved -> {
                assertEquals("gt6addition:any_grains", resolved);
                calls[0]++;
                return null;
            }).isEmpty());
            assertEquals(1, calls[0]);
            assertEquals(2 * GTValues.M, CrucibleRecyclingOverride.convertAmount(entry.getLong("a")));
            assertEquals(before, root);
        }
        assertEquals("gregtech:wheat", GT6MaterialIdentity.hostRecyclingName("Flour"));
        assertEquals("gregtech:wheat", GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(9702)));
        for (String name : new String[]{"wheat", "any_grains"}) {
            assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(3000L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertEquals(1000D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name));
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
        }
        assertFalse(GT6MaterialHazardData.shouldBurn("wheat", 313, false));
        assertTrue(GT6MaterialHazardData.shouldBurn("wheat", 314, false));
        assertFalse(GT6MaterialHazardData.shouldBurn("any_grains", 314, false));
        assertFalse(CrucibleSmeltingRule.hasMeltingFlag("wheat"));
        CrucibleSmeltingRule rule = CrucibleSmeltingRule.find("any_grains");
        assertNotNull(rule);
        assertEquals("gregtech:wheat", rule.target);
        assertEquals(2 * GTValues.M, rule.convert(2 * GTValues.M));
        assertEquals(rule.target, CrucibleSolidifyingRule.target("any_grains"));
        for (String name : new String[]{"AnyFlourAndGrains", "gregtech:Rye", "gtqtcore:Barley", "minecraft:Potato",
                "gregtech:Wheat", "gtqtcore:AnyFlour"}) {
            assertEquals(name, GT6MaterialIdentity.hostRecyclingName(name));
        }
    }

    @Test
    void fishAndTofuIdentitiesResolveRegisteredTargetsWithoutChangingYield() {
        int[] ids = {9711, 9712, 9713, 9778};
        String[] names = {"fish_cooked", "fish_raw", "fish_rotten", "tofu"};
        for (int i = 0; i < ids.length; i++) {
            assertEquals("gt6addition:" + names[i], GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(ids[i])));
            assertEquals(1000D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(names[i]));
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(names[i]));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(names[i]));
            assertFalse(GT6MaterialHazardData.shouldBurn(names[i], 314, true));
        }
        CrucibleSmeltingRule raw = CrucibleSmeltingRule.find("fish_raw");
        assertNotNull(raw);
        assertEquals("fish_cooked", raw.target);
        assertEquals(GTValues.M, raw.convert(GTValues.M));
        for (String name : new String[]{"fish_raw", "fish_cooked", "fish_rotten"}) {
            assertEquals(477, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(550L, CrucibleMaterialPhaseData.boilingPoint(name));
        }
        assertEquals(422, CrucibleMaterialPhaseData.knownMeltingPoint("tofu"));
        assertEquals(500L, CrucibleMaterialPhaseData.boilingPoint("tofu"));
    }

    @Test
    void blazeAndPrismarineFamiliesKeepTheirDistinctTargetsAndDefaultStatistics() {
        String[][] mappings = {
                {"Any Blaze", "gt6addition:any_blaze"}, {"AnyBlaze", "gt6addition:any_blaze"},
                {"Any Prismarine", "gt6addition:any_prismarine"}, {"AnyPrismarine", "gt6addition:any_prismarine"},
                {"Prismarine", "gt6addition:prismarine"}, {"PrismarineDark", "gt6addition:prismarine_dark"}
        };
        for (String[] mapping : mappings) {
            assertEquals(mapping[1], GT6MaterialIdentity.hostRecyclingName(mapping[0]));
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("m", mapping[0]);
            entry.setLong("a", 648648000L);
            NBTTagCompound list = new NBTTagCompound();
            list.setInteger("size", 1);
            list.setTag("0", entry);
            NBTTagCompound root = new NBTTagCompound();
            root.setTag(CrucibleRecyclingOverride.TAG, list);
            NBTTagCompound before = root.copy();
            int[] calls = {0};
            assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
                assertEquals(mapping[1], name);
                calls[0]++;
                return null;
            }).isEmpty());
            assertEquals(1, calls[0]);
            assertEquals(before, root);
        }
        assertEquals("gt6addition:prismarine", GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(9219)));
        assertEquals("gt6addition:prismarine_dark", GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(9220)));
        assertEquals(4000, CrucibleMaterialPhaseData.knownMeltingPoint("any_blaze"));
        assertEquals(8000L, CrucibleMaterialPhaseData.boilingPoint("any_blaze"));
        assertNull(CrucibleSmeltingRule.find("any_blaze"));
        assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget("any_blaze"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("any_blaze"));
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("blaze"));
        for (String name : new String[]{"prismarine", "prismarine_dark", "any_prismarine"}) {
            assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(3000L, CrucibleMaterialPhaseData.boilingPoint(name));
        }
        CrucibleSmeltingRule family = CrucibleSmeltingRule.find("any_prismarine");
        assertNotNull(family);
        assertEquals("gt6addition:prismarine", family.target);
        assertEquals(GTValues.M, family.convert(GTValues.M));
        assertEquals(family.target, CrucibleSolidifyingRule.target("any_prismarine"));
        assertTrue(CrucibleSmeltingRule.hasMeltingFlag("any_prismarine"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("prismarine"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("prismarine_dark"));
        for (String name : new String[]{"blaze", "any_blaze", "prismarine", "prismarine_dark", "any_prismarine"}) {
            assertEquals(1000D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name));
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, true));
            assertFalse(GT6MaterialHazardData.isExplosive(name));
        }
        for (String name : new String[]{"gregtech:blaze", "gtqtcore:AnyPrismarine", "gt6addition:prismarine_dark"}) {
            assertEquals(name, GT6MaterialIdentity.hostRecyclingName(name));
        }
    }

    @Test
    void commonNonMetalFamiliesUseTheirVerifiedBaseTargets() {
        String[][] mappings = {
                {"Any Wax", "gt6addition:wax"}, {"AnyWax", "gt6addition:wax"},
                {"Any Sand", "gt6addition:sand"}, {"AnySand", "gt6addition:sand"},
                {"Any Stone", "gregtech:stone"}, {"AnyStone", "gregtech:stone"},
                {"Any Calcite", "gregtech:calcite"}, {"AnyCalcite", "gregtech:calcite"},
                {"Quartz", "gregtech:silicon_dioxide"}
        };
        for (String[] mapping : mappings) {
            assertEquals(mapping[1], GT6MaterialIdentity.hostRecyclingName(mapping[0]));
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("m", mapping[0]);
            entry.setLong("a", 648648000L);
            NBTTagCompound list = new NBTTagCompound();
            list.setInteger("size", 1);
            list.setTag("0", entry);
            NBTTagCompound root = new NBTTagCompound();
            root.setTag(CrucibleRecyclingOverride.TAG, list);
            NBTTagCompound before = root.copy();
            int[] calls = {0};
            assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
                assertEquals(mapping[1], name);
                calls[0]++;
                return null;
            }).isEmpty());
            assertEquals(1, calls[0]);
            assertEquals(GTValues.M, CrucibleRecyclingOverride.convertAmount(entry.getLong("a")));
            assertEquals(before, root);
        }
        assertEquals("gt6addition:sand", GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(8100)));
        assertEquals("gt6addition:wax", GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(8235)));
        CrucibleSmeltingRule wax = CrucibleSmeltingRule.find("wax");
        assertNotNull(wax);
        assertNull(wax.target);
        assertEquals(GTValues.M, wax.convert(GTValues.M));
        assertTrue(CrucibleSmeltingRule.hasMeltingFlag("wax"));
        assertEquals(350, CrucibleMaterialPhaseData.knownMeltingPoint("wax"));
        assertEquals(700L, CrucibleMaterialPhaseData.boilingPoint("wax"));
        CrucibleSmeltingRule sand = CrucibleSmeltingRule.find("sand");
        assertEquals("glass", sand.target);
        assertEquals(GTValues.M, sand.convert(GTValues.M));
        assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint("sand"));
        assertEquals(3000L, CrucibleMaterialPhaseData.boilingPoint("sand"));
        double silica = (2329.6D + 2 * 1.429D) / 3;
        for (String name : new String[]{"sand", "stone", "silicon_dioxide"}) {
            assertEquals(silica, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name), 0.00001D);
        }
        assertEquals(1000D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("wax"));
        assertEquals((1540D + 2267D + 3 * 1.429D) / 5,
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("calcite"), 0.00001D);
        for (String name : new String[]{"wax", "sand", "stone", "calcite", "silicon_dioxide"}) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, true));
            assertFalse(GT6MaterialHazardData.isExplosive(name));
            assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name));
        }
        for (String untouched : new String[]{"MilkyQuartz", "NetherQuartz", "RedSand", "WaxBee", "Marble",
                "gregtech:quartz", "gtqtcore:AnyWax", "AnyQuartz"}) {
            assertEquals(untouched, GT6MaterialIdentity.hostRecyclingName(untouched));
        }
    }

    @Test
    void additionalGt6RecyclingFamiliesUseVerifiedBaseMaterialIdentity() {
        String[][] families = {
                {"Any Wood", "gregtech:wood"},
                {"Any Default Wood", "gregtech:wood"},
                {"Any Normal Wood", "gregtech:wood"},
                {"Any Magical Wood", "gregtech:wood"},
                {"Any Treated Wood", "gregtech:wood"},
                {"Any Untreated Wood", "gregtech:wood"},
                {"Any Silicon", "gregtech:silicon"},
                {"Any Silicon Dioxide", "gregtech:silicon_dioxide"},
                {"Any Rubber", "gregtech:rubber"},
                {"Any Ashes", "gregtech:ash"},
                {"Any Clay", "gregtech:clay"},
                {"Any Salt", "gregtech:salt"}
        };
        for (String[] family : families) {
            assertEquals(family[1], GT6MaterialIdentity.hostRecyclingName(family[0]));
            assertEquals(family[1], GT6MaterialIdentity.hostRecyclingName(family[0].replace(" ", "")));
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("m", family[0]);
            entry.setLong("a", 2 * 648648000L);
            NBTTagCompound list = new NBTTagCompound();
            list.setInteger("size", 1);
            list.setTag("0", entry);
            NBTTagCompound root = new NBTTagCompound();
            root.setTag(CrucibleRecyclingOverride.TAG, list);
            NBTTagCompound before = root.copy();
            int[] calls = {0};
            assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
                assertEquals(family[1], name);
                calls[0]++;
                return null;
            }).isEmpty());
            assertEquals(1, calls[0]);
            assertEquals(2 * GTValues.M, CrucibleRecyclingOverride.convertAmount(entry.getLong("a")));
            assertEquals(before, root);
        }
        for (String member : new String[]{"WoodTreated", "WoodPolished", "WoodRubber", "Greatwood",
                "Silverwood", "RawRubber", "SiliconeRubber", "HardPlastic",
                "gtqtcore:AnyRubber", "gregtech:AnyWood"}) {
            assertEquals(member, GT6MaterialIdentity.hostRecyclingName(member));
        }
        // MT.java:1229-1230,1147,1306 explicitly registers identical names;
        // unlike an ANY membership, they are aliases of the native identity.
        for (String[] alias : new String[][]{{"DarkAsh", "darkashes"}, {"VolcanicAsh", "volcanicashes"},
                {"RockSalt", "sylvite"}, {"Polycarbonate", "hardplastic"}})
            assertEquals(alias[1], GT6MaterialIdentity.hostRecyclingName(alias[0]));
    }

    @Test
    void plasticFamiliesUseActualHostRegistryAndKeepGt6SelfSmeltingQuantity() {
        for (String family : new String[]{"Any Plastic", "AnyPlastic", "Any Hard Plastic", "AnyHardPlastic"}) {
            assertEquals("gregtech:plastic", GT6MaterialIdentity.hostRecyclingName(family));
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("m", family);
            entry.setLong("a", 9 * 648648000L);
            NBTTagCompound list = new NBTTagCompound();
            list.setInteger("size", 1);
            list.setTag("0", entry);
            NBTTagCompound root = new NBTTagCompound();
            root.setTag(CrucibleRecyclingOverride.TAG, list);
            assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
                assertEquals("gregtech:plastic", name);
                return null;
            }).isEmpty());
            long amount = CrucibleRecyclingOverride.convertAmount(entry.getLong("a"));
            assertEquals(9 * GTValues.M, amount); // Do not reduce it during identity resolution.
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find("plastic");
            assertNotNull(rule);
            assertNull(rule.target);
            assertEquals(6 * GTValues.M, rule.convertStored(amount, 0).amount);
            assertEquals(423, CrucibleMaterialPhaseData.knownMeltingPoint("plastic"));
            assertEquals(846, CrucibleMaterialPhaseData.boilingPoint("plastic"));
        }
        for (String member : new String[]{"HardPlastic", "Bakelite",
                "gregtech:AnyPlastic", "gtqtcore:AnyHardPlastic"}) {
            assertEquals(member, GT6MaterialIdentity.hostRecyclingName(member));
        }
        assertEquals("hardplastic", GT6MaterialIdentity.hostRecyclingName("Polycarbonate"));
        assertEquals("gregtech:polyvinyl_chloride", GT6MaterialIdentity.hostRecyclingName("PVC"));
        assertEquals("gregtech:polytetrafluoroethylene", GT6MaterialIdentity.hostRecyclingName("PTFE"));
    }

    @Test
    void amethystFamilyCopiesBaseIdentityWithoutFlatteningOtherGemFamilies() {
        for (String family : new String[]{"Any Amethyst", "AnyAmethyst"}) {
            assertEquals("gregtech:amethyst", GT6MaterialIdentity.hostRecyclingName(family));
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("m", family);
            entry.setLong("a", 2 * 648648000L);
            NBTTagCompound list = new NBTTagCompound();
            list.setInteger("size", 1);
            list.setTag("0", entry);
            NBTTagCompound root = new NBTTagCompound();
            root.setTag(CrucibleRecyclingOverride.TAG, list);
            NBTTagCompound before = root.copy();
            int[] calls = {0};
            assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
                assertEquals("gregtech:amethyst", name);
                calls[0]++;
                return null;
            }).isEmpty());
            assertEquals(1, calls[0]);
            assertEquals(2 * GTValues.M, CrucibleRecyclingOverride.convertAmount(entry.getLong("a")));
            assertEquals(before, root);
        }
        CrucibleSmeltingRule rule = CrucibleSmeltingRule.find("amethyst");
        assertNotNull(rule);
        assertEquals("", rule.target); // GT6 valgemelec -> valgemdcmp disables smelting.
        for (String independent : new String[]{"AmethystEnder", "EnderAmethyst",
                "gregtech:AnyAmethyst", "gtqtcore:AnyAmethyst"}) {
            assertEquals(independent, GT6MaterialIdentity.hostRecyclingName(independent));
        }
    }

    @Test
    void independentGemFamiliesInheritStatisticsButNotDisabledMemberTargets() {
        String[][] families = {
                {"Any Garnet", "any_garnet", "spessartine"},
                {"Any Jasper", "any_jasper", "jasper"},
                {"Any Tiger Eye", "any_tiger_eye", "tigereye"},
                {"Any Aventurine", "any_aventurine", "greenaventurine"},
                {"Any Amber", "any_amber", "amber"}
        };
        for (String[] family : families) {
            for (String name : new String[]{family[0], family[0].replace(" ", "")}) {
                String expected = "gt6addition:" + family[1];
                assertEquals(expected, GT6MaterialIdentity.hostRecyclingName(name));
                NBTTagCompound entry = new NBTTagCompound();
                entry.setString("m", name);
                entry.setLong("a", 2 * 648648000L);
                NBTTagCompound list = new NBTTagCompound();
                list.setInteger("size", 1);
                list.setTag("0", entry);
                NBTTagCompound root = new NBTTagCompound();
                root.setTag(CrucibleRecyclingOverride.TAG, list);
                NBTTagCompound before = root.copy();
                int[] calls = {0};
                assertTrue(CrucibleRecyclingOverride.parse(root, resolved -> {
                    assertEquals(expected, resolved);
                    calls[0]++;
                    return null;
                }).isEmpty());
                assertEquals(1, calls[0]);
                assertEquals(2 * GTValues.M, CrucibleRecyclingOverride.convertAmount(entry.getLong("a")));
                assertEquals(before, root);
            }
            assertEquals(CrucibleMaterialPhaseData.knownMeltingPoint(family[2]),
                    CrucibleMaterialPhaseData.knownMeltingPoint(family[1]));
            assertTrue(CrucibleMaterialPhaseData.knownMeltingPoint(family[1]) > 0);
            assertEquals(CrucibleMaterialPhaseData.boilingPoint(family[2]),
                    CrucibleMaterialPhaseData.boilingPoint(family[1]));
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(family[1]));
            assertNull(CrucibleSmeltingRule.find(family[1])); // GT6 default self, one U.
            assertFalse(CrucibleSmeltingRule.hasMeltingFlag(family[1]));
            assertNull(CrucibleSolidifyingRule.target(family[1]));
            assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(family[1]));
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(family[1]));
            assertFalse(GT6MaterialHazardData.shouldBurn(family[1], 314, true));
            assertFalse(GT6MaterialHazardData.isExplosive(family[1]));
        }
        double silica = (2329.6D + 2 * 1.429D) / 3;
        double alumina = (2 * 2698D + 3 * 1.429D) / 5;
        assertEquals((5 * alumina + 3 * 7440D + 9 * silica + 3 * 1.429D) / 20,
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("any_garnet"), 0.00001D);
        assertEquals((2 * silica + 7874D) / 3,
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("any_jasper"), 0.00001D);
        assertEquals(silica, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("any_tiger_eye"), 0.00001D);
        assertEquals(silica, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("any_aventurine"), 0.00001D);
        assertEquals(1000D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("any_amber"));
        assertEquals(473, CrucibleMaterialPhaseData.knownMeltingPoint("any_amber"));
        assertEquals(946L, CrucibleMaterialPhaseData.boilingPoint("any_amber"));
        for (String member : new String[]{"spessartine", "jasper", "tigereye", "greenaventurine"}) {
            assertEquals("", CrucibleSmeltingRule.find(member).target);
        }
        for (String unchanged : new String[]{"Spessartine", "TigerEye", "GreenAventurine", "Amber",
                "gregtech:AnyGarnet", "gtqtcore:AnyJasper", "gt6addition:any_amber"}) {
            assertEquals(unchanged, GT6MaterialIdentity.hostRecyclingName(unchanged));
        }
        assertEquals("redjasper", GT6MaterialIdentity.hostRecyclingName("Jasper")); // MT.java:1401 literal alias.
    }

    @Test
    void electricAccountBindsConcreteItemAndMetadataWithoutModifyingStack() {
        Item item = new Item().setRegistryName("gt6addition", "provenance_test");
        ItemStack stack = new ItemStack(item, 1, 7);
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("version", 2);
        data.setString("item", "gt6addition:provenance_test");
        data.setInteger("metadata", 7);
        root.setTag(CrucibleElectricProvenance.TAG, data);
        stack.setTagCompound(root);
        NBTTagCompound before = root.copy();
        assertTrue(CrucibleElectricProvenance.matchesItem(stack));
        assertEquals(before, root);
        data.setString("item", "othermod:provenance_test");
        assertFalse(CrucibleElectricProvenance.matchesItem(stack));
        data.setString("item", "gt6addition:provenance_test");
        data.setInteger("metadata", 8);
        assertFalse(CrucibleElectricProvenance.matchesItem(stack));
        data.setString("metadata", "7");
        assertFalse(CrucibleElectricProvenance.matchesItem(stack));
        data.setInteger("version", 1);
        assertTrue(CrucibleElectricProvenance.matchesItem(stack));
        data.setInteger("version", 3);
        assertFalse(CrucibleElectricProvenance.matchesItem(stack));
        assertFalse(CrucibleElectricProvenance.matchesItem(ItemStack.EMPTY));
    }

    @Test
    void breakInheritanceDoesNotChangeNonElectricOrNormalToolReturns() {
        ItemStack plain = new ItemStack(new Item());
        NBTTagCompound plainData = new NBTTagCompound();
        plainData.setString("unchanged", "host result");
        plain.setTagCompound(plainData);
        ItemStack before = plain.copy();
        assertTrue(plain == CrucibleElectricProvenance.inheritBrokenPowerUnit(plain, ItemStack.EMPTY));
        assertEquals(before.getTagCompound(), plain.getTagCompound());
        ItemStack tool = new ItemStack(new Item());
        NBTTagCompound toolData = new NBTTagCompound();
        toolData.setTag("GT.Tool", new NBTTagCompound());
        tool.setTagCompound(toolData);
        assertTrue(tool == CrucibleElectricProvenance.inheritBrokenPowerUnit(tool, ItemStack.EMPTY));
        assertEquals(toolData, tool.getTagCompound());
    }

    @Test
    void exhaustedToolOnlyAllowsUnscaledAccountForExistingPowerUnitBreakResult() {
        assertTrue(CrucibleElectricProvenance.validToolDurability(1000, 999, false));
        assertFalse(CrucibleElectricProvenance.validToolDurability(1000, 1000, false));
        assertFalse(CrucibleElectricProvenance.validToolDurability(1000, 1001, false));
        assertTrue(CrucibleElectricProvenance.validToolDurability(1000, 1000, true));
        assertTrue(CrucibleElectricProvenance.validToolDurability(1000, 1001, true));
        assertTrue(CrucibleElectricProvenance.validToolDurability(1000, Integer.MAX_VALUE, true));
        assertFalse(CrucibleElectricProvenance.validToolDurability(0, 0, true));
        assertFalse(CrucibleElectricProvenance.validToolDurability(1000, -1, true));
        assertEquals(0, CrucibleTransferLogic.remainingDurabilityMaterial(GTValues.M, 1001, 1000));
        assertEquals(ItemStack.EMPTY, CrucibleElectricProvenance.inheritBrokenPowerUnit(
                ItemStack.EMPTY, ItemStack.EMPTY));
    }

    @Test
    void headReplacementCannotInferPowerMaterialsFromAnOldUndividedAccount() {
        NBTTagCompound data = new NBTTagCompound();
        net.minecraft.nbt.NBTTagList full = new net.minecraft.nbt.NBTTagList();
        NBTTagCompound head = new NBTTagCompound();
        head.setString("material", "gregtech:steel");
        head.setLong("amount", 4 * GTValues.M);
        full.appendTag(head);
        data.setTag("materials", full);
        NBTTagCompound before = data.copy();
        assertTrue(CrucibleElectricProvenance.parseList(data, "powerMaterials", name -> {
            throw new AssertionError("The full account is not a power unit account");
        }).isEmpty());
        assertEquals(before, data);
        data.setString("powerMaterials", "gregtech:steel");
        assertTrue(CrucibleElectricProvenance.parseList(data, "powerMaterials", name -> null).isEmpty());
        net.minecraft.nbt.NBTTagList power = new net.minecraft.nbt.NBTTagList();
        NBTTagCompound battery = new NBTTagCompound();
        battery.setString("material", "gregtech:lithium");
        battery.setLong("amount", GTValues.M / 9);
        power.appendTag(battery);
        data.setTag("powerMaterials", power);
        int[] lookups = {0};
        assertTrue(CrucibleElectricProvenance.parseList(data, "powerMaterials", name -> {
            assertEquals("gregtech:lithium", name);
            lookups[0]++;
            return null;
        }).isEmpty());
        assertEquals(1, lookups[0]);
        battery.setLong("amount", 0);
        assertTrue(CrucibleElectricProvenance.parseList(data, "powerMaterials", name -> null).isEmpty());
    }

    @Test
    void electricToolProvenanceIsBoundToItsActualToolMaterial() {
        assertFalse(CrucibleElectricProvenance.hasMatchingToolMaterial(null));
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound tool = new NBTTagCompound();
        tool.setString("Material", "gregtech:steel");
        tool.setInteger("MaxDurability", 1000);
        tool.setInteger("Durability", 250);
        root.setTag("GT.Tool", tool);
        assertTrue(CrucibleToolRecycling.hasSafeToolTags(root));
        assertFalse(CrucibleElectricProvenance.hasMatchingToolMaterial(root));
        NBTTagCompound account = new NBTTagCompound();
        account.setString("toolMaterial", "gregtech:steel");
        root.setTag(CrucibleElectricProvenance.TAG, account);
        NBTTagCompound before = root.copy();
        assertTrue(CrucibleElectricProvenance.hasMatchingToolMaterial(root));
        assertEquals(before, root);
        tool.setString("Material", "gregtech:titanium");
        assertFalse(CrucibleElectricProvenance.hasMatchingToolMaterial(root));
        tool.setString("Material", "othermod:steel");
        assertFalse(CrucibleElectricProvenance.hasMatchingToolMaterial(root));
        account.setString("toolMaterial", "");
        tool.setString("Material", "");
        assertFalse(CrucibleElectricProvenance.hasMatchingToolMaterial(root));
    }

    @Test
    void sharedToolMetadataValidationDoesNotAdmitHiddenInventory() {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound tool = new NBTTagCompound();
        tool.setString("Material", "gregtech:steel");
        root.setTag("GT.Tool", tool);
        assertTrue(CrucibleToolRecycling.hasSafeToolTags(root));
        root.setTag("Inventory", new NBTTagCompound());
        assertFalse(CrucibleToolRecycling.hasSafeToolTags(root));
        root.removeTag("Inventory");
        tool.setTag("Inventory", new NBTTagCompound());
        assertFalse(CrucibleToolRecycling.hasSafeToolTags(root));
    }

    @Test
    void electricAssemblyProvenanceRequiresVersionedTypedMaterialsAndDoesNotMutate() {
        NBTTagCompound root = new NBTTagCompound();
        root.setLong("Charge", 12345);
        NBTTagCompound data = new NBTTagCompound();
        root.setTag(CrucibleElectricProvenance.TAG, data);
        assertTrue(CrucibleElectricRecycling.hasStaticElectricTags(root));
        assertTrue(CrucibleElectricProvenance.parse(root, name -> null).isEmpty());
        data.setInteger("version", 1);
        net.minecraft.nbt.NBTTagList entries = new net.minecraft.nbt.NBTTagList();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("material", "gregtech:copper");
        entry.setLong("amount", GTValues.M / 9);
        entries.appendTag(entry);
        data.setTag("materials", entries);
        NBTTagCompound before = root.copy();
        int[] lookups = {0};
        assertTrue(CrucibleElectricProvenance.parse(root, name -> {
            assertEquals("gregtech:copper", name);
            lookups[0]++;
            return null;
        }).isEmpty());
        assertEquals(1, lookups[0]);
        assertEquals(before, root);
        entry.setInteger("amount", 1); // Wrong numeric type must not truncate silently.
        assertTrue(CrucibleElectricProvenance.parse(root, name -> {
            throw new AssertionError("Malformed quantity must not resolve materials");
        }).isEmpty());
        data.setInteger("version", 2);
        assertTrue(CrucibleElectricProvenance.parse(root, name -> null).isEmpty());
    }

    @Test
    void lavaCondensationRetainsAnEntireSubUnitRemainderAfterTheLastBucket() {
        CrucibleFluidUnits.Quantity bucket = CrucibleFluidUnits.storedFluidAmount(1000, GTValues.L);
        assertNotNull(bucket);
        assertEquals(0, bucket.remainder);
        for (int remainder : new int[]{1, CrucibleFluidUnits.STORAGE_UNIT / 2, CrucibleFluidUnits.STORAGE_UNIT - 1}) {
            CrucibleFluidUnits.LavaCondensation result = CrucibleFluidUnits.condenseLava(
                    bucket.amount, remainder, GTValues.L);
            assertNotNull(result);
            assertEquals(1, result.obsidianCount);
            assertEquals(0, result.remaining.amount);
            assertEquals(remainder, result.remaining.remainder);
            assertNull(CrucibleFluidUnits.condenseLava(result.remaining.amount,
                    result.remaining.remainder, GTValues.L));
        }
    }

    @Test
    void lavaCondensationUsesWholeBucketsAndExactStoredVolume() {
        for (int volume : new int[]{999, 1000, 1999, 2000, 2500}) {
            CrucibleFluidUnits.Quantity quantity = CrucibleFluidUnits.storedFluidAmount(volume, GTValues.L);
            assertNotNull(quantity);
            CrucibleFluidUnits.LavaCondensation result = CrucibleFluidUnits.condenseLava(
                    quantity.amount, quantity.remainder, GTValues.L);
            if (volume < 1000) {
                assertNull(result);
                continue;
            }
            assertNotNull(result);
            assertEquals(volume / 1000, result.obsidianCount);
            assertEquals(volume % 1000, CrucibleFluidUnits.storedFluidVolume(result.remaining.amount,
                    result.remaining.remainder, GTValues.L));
            assertEquals(0, result.remaining.remainder);
        }
        assertNull(CrucibleFluidUnits.condenseLava(-1, 0, GTValues.L));
        assertNull(CrucibleFluidUnits.condenseLava(1, -1, GTValues.L));
        assertNull(CrucibleFluidUnits.condenseLava(1, CrucibleFluidUnits.STORAGE_UNIT, GTValues.L));
        assertNull(CrucibleFluidUnits.condenseLava(1, 0, 0));
        CrucibleFluidUnits.LavaCondensation huge = CrucibleFluidUnits.condenseLava(Long.MAX_VALUE, 0, GTValues.L);
        assertNotNull(huge);
        assertEquals(Integer.MAX_VALUE / 1000, huge.obsidianCount);
        assertTrue(huge.remaining.amount > 0);
    }

    @Test
    void surfaceSelectionDoesNotSkipAnUndrainableLighterMaterial() {
        double[] densities = {1200, 7874, 22587};
        boolean[] drainable = {false, true, true};
        Integer surface = CrucibleSurfaceSelection.lightest(java.util.Arrays.asList(0, 1, 2),
                index -> true, index -> densities[index]);
        assertEquals(Integer.valueOf(0), surface);
        assertFalse(drainable[surface]);
        // Filtering drain eligibility first would incorrectly expose iron.
        assertEquals(Integer.valueOf(1), CrucibleSurfaceSelection.lightest(java.util.Arrays.asList(0, 1, 2),
                index -> drainable[index], index -> densities[index]));
        densities[0] = 9000;
        assertEquals(Integer.valueOf(1), CrucibleSurfaceSelection.lightest(java.util.Arrays.asList(0, 1, 2),
                index -> true, index -> densities[index]));
    }

    @Test
    void surfaceSelectionRetainsStableTiesAndExplicitMicroFluidException() {
        double[] densities = {1000, 1000, 7874};
        assertEquals(Integer.valueOf(0), CrucibleSurfaceSelection.lightest(java.util.Arrays.asList(0, 1, 2),
                index -> true, index -> densities[index]));
        assertEquals(Integer.valueOf(1), CrucibleSurfaceSelection.lightest(java.util.Arrays.asList(1, 0, 2),
                index -> true, index -> densities[index]));
        // Only an explicitly excluded sub-mB liquid can be skipped by the
        // Forge view; render/scrape still include its real retained quantity.
        assertEquals(Integer.valueOf(2), CrucibleSurfaceSelection.lightest(java.util.Arrays.asList(0, 2),
                index -> index != 0, index -> densities[index]));
        assertEquals(Integer.valueOf(0), CrucibleSurfaceSelection.lightest(java.util.Arrays.asList(0, 2),
                index -> true, index -> densities[index]));
        assertNull(CrucibleSurfaceSelection.lightest(java.util.Arrays.asList(0, 1, 2),
                index -> false, index -> densities[index]));
        assertNull(CrucibleSurfaceSelection.lightest(java.util.Collections.<Integer>emptyList(),
                index -> true, index -> densities[index]));
        assertEquals(Integer.valueOf(0), CrucibleSurfaceSelection.lightest(java.util.Arrays.asList(null, 0),
                index -> true, index -> densities[index]));
    }

    @Test
    void preparedMultiMaterialIntakeChecksAllExpandedComponentsBeforeCommit() {
        long unit = GTValues.M;
        assertTrue(CrucibleFluidUnits.incomingBatchFits(12 * unit, 16 * unit,
                new long[]{2 * unit, 2 * unit}, new int[]{0, 0}));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(13 * unit, 16 * unit,
                new long[]{2 * unit, 2 * unit}, new int[]{0, 0}));
        // Each component alone fits, but the batch must not commit the first
        // and then discover that the second has no space.
        assertFalse(CrucibleFluidUnits.incomingBatchFits(15 * unit, 16 * unit,
                new long[]{unit, unit}, new int[]{0, 0}));
        assertTrue(CrucibleFluidUnits.incomingBatchFits(0, 2,
                new long[]{0, 0}, new int[]{1, 1}));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(0, 1,
                new long[]{0, 0}, new int[]{1, 1}));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(0, Long.MAX_VALUE,
                new long[]{Long.MAX_VALUE, 1}, new int[]{0, 0}));
        assertTrue(CrucibleFluidUnits.incomingBatchFits(0, Long.MAX_VALUE,
                new long[]{Long.MAX_VALUE - 1, 1}, new int[]{0, 0}));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(17, 16, new long[]{1}, new int[]{0}));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(0, 16, new long[]{0}, new int[]{0}));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(0, 16, new long[]{1}, new int[]{-1}));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(0, 16, new long[]{1},
                new int[]{CrucibleFluidUnits.STORAGE_UNIT}));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(0, 16, new long[]{1}, new int[0]));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(0, 16, new long[0], new int[0]));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(0, 16, null, null));
    }

    @Test
    void multiMaterialIncomingHeatUsesOneCombinedMassAndOneTemperatureRounding() {
        long firstOrder = CrucibleTransferLogic.smelteryIntakeTemperature(1300, 20, 288, 3 + 7);
        long reverseOrder = CrucibleTransferLogic.smelteryIntakeTemperature(1300, 20, 288, 7 + 3);
        assertEquals(962L, firstOrder);
        assertEquals(firstOrder, reverseOrder);
        assertEquals(CrucibleTransferLogic.IntakePhase.MELT,
                CrucibleTransferLogic.intakePhase(288, firstOrder, 808));
        assertEquals(CrucibleTransferLogic.IntakePhase.NONE,
                CrucibleTransferLogic.intakePhase(288, firstOrder, 1811));
        CrucibleFluidUnits.Quantity diamond = CrucibleSmeltingRule.find("diamond").convertStored(GTValues.M, 0);
        CrucibleFluidUnits.Quantity wood = CrucibleSmeltingRule.find("wood").convertStored(GTValues.M, 0);
        assertNotNull(diamond);
        assertNotNull(wood);
        assertEquals(2 * GTValues.M, diamond.amount);
        assertTrue(CrucibleFluidUnits.incomingBatchFits(0, 3 * GTValues.M,
                new long[]{diamond.amount, wood.amount}, new int[]{diamond.remainder, wood.remainder}));
        assertFalse(CrucibleFluidUnits.incomingBatchFits(GTValues.M, 3 * GTValues.M,
                new long[]{diamond.amount, wood.amount}, new int[]{diamond.remainder, wood.remainder}));
    }

    @Test
    void smelteryMixingMatchesSourceIntegerWeightsAndDirectionalTruncation() {
        assertEquals(626L, CrucibleTransferLogic.smelteryIntakeTemperature(288, 20, 1300, 10));
        assertEquals(940L, CrucibleTransferLogic.smelteryIntakeTemperature(1300, 20.9, 288, 10.9));
        assertEquals(1300L, CrucibleTransferLogic.smelteryIntakeTemperature(288, 0.4, 1300, 0.3));
        assertEquals(500L, CrucibleTransferLogic.smelteryIntakeTemperature(500, 20, 500, 10));
        assertEquals(Long.MAX_VALUE / 2,
                CrucibleTransferLogic.smelteryIntakeTemperature(Long.MAX_VALUE, 1, 0, 1));
        assertEquals(Long.MAX_VALUE / 2 + 1,
                CrucibleTransferLogic.smelteryIntakeTemperature(0, 1, Long.MAX_VALUE, 1));
        // The source's rounding determines whether the intake crosses a melt boundary.
        long mixed = CrucibleTransferLogic.smelteryIntakeTemperature(1300, 20, 288, 10);
        assertEquals(CrucibleTransferLogic.IntakePhase.NONE, CrucibleTransferLogic.intakePhase(288, mixed, 963));
        assertEquals(CrucibleTransferLogic.IntakePhase.MELT, CrucibleTransferLogic.intakePhase(288, mixed, 962));
        // Other project machines retain their existing shared weighted-average behavior.
        assertEquals(963L, CrucibleTransferLogic.mixTemperature(1300, 20, 288, 10));
    }

    @Test
    void intakeConvertsOnlyWhenMixingCrossesTheSourceMeltingBoundary() {
        assertEquals(CrucibleTransferLogic.IntakePhase.NONE, CrucibleTransferLogic.intakePhase(500, 450, 423));
        assertEquals(CrucibleTransferLogic.IntakePhase.NONE, CrucibleTransferLogic.intakePhase(286, 400, 423));
        assertEquals(CrucibleTransferLogic.IntakePhase.MELT, CrucibleTransferLogic.intakePhase(422, 423, 423));
        assertEquals(CrucibleTransferLogic.IntakePhase.SOLIDIFY, CrucibleTransferLogic.intakePhase(423, 422, 423));
        assertEquals(CrucibleTransferLogic.IntakePhase.NONE, CrucibleTransferLogic.intakePhase(423, 423, 423));
        assertEquals(CrucibleTransferLogic.IntakePhase.NONE, CrucibleTransferLogic.intakePhase(500, 286, -1));
    }

    @Test
    void hotTargetsApplyOnNewContentOrTemperatureCrossingRatherThanEveryTick() {
        assertTrue(CrucibleTransferLogic.shouldApplyHotTarget(500, 500, 423, true, true));
        assertFalse(CrucibleTransferLogic.shouldApplyHotTarget(500, 500, 423, true, false));
        assertTrue(CrucibleTransferLogic.shouldApplyHotTarget(423, 422, 423, true, false));
        assertFalse(CrucibleTransferLogic.shouldApplyHotTarget(422, 423, 423, true, true));
        assertTrue(CrucibleTransferLogic.shouldApplyHotTarget(500, 500, 423, false, false));
        assertFalse(CrucibleTransferLogic.shouldApplyHotTarget(500, 286, -1, false, true));
        CrucibleSmeltingRule plastic = CrucibleSmeltingRule.find("plastic");
        assertNotNull(plastic);
        // Already-hot intake keeps its amount; one update event applies 2/3
        // once. Steady ticks must not repeatedly shrink it to 4/9, 8/27, etc.
        long source = 9 * GTValues.M;
        assertEquals(CrucibleTransferLogic.IntakePhase.NONE, CrucibleTransferLogic.intakePhase(500, 500, 423));
        CrucibleFluidUnits.Quantity converted = plastic.convertStored(source, 0);
        assertNotNull(converted);
        assertEquals(6 * GTValues.M, converted.amount);
        assertEquals(0, converted.remainder);
        assertFalse(CrucibleTransferLogic.shouldApplyHotTarget(500, 500, 423, true, false));
    }

    @Test
    void freshColdContentsApplySolidificationTargetsWithoutPreviouslyMelting() {
        for (String name : new String[]{"refined_iron", "tungsten_sintered", "wood_treated",
                "redstonia", "palis", "diamantine", "voidcrystal", "emeradic", "enori"}) {
            int point = CrucibleMaterialPhaseData.knownMeltingPoint(name);
            assertTrue(point > 286, name);
            assertNotNull(CrucibleSolidifyingRule.target(name));
            assertTrue(CrucibleTransferLogic.shouldApplyColdTarget(286, point, false, true, true), name);
            assertFalse(CrucibleTransferLogic.shouldApplyColdTarget(286, point, false, false, true), name);
            assertFalse(CrucibleTransferLogic.shouldApplyColdTarget(point, point, false, true, true), name);
            assertFalse(CrucibleTransferLogic.shouldApplyColdTarget(point + 1L, point, true, true, true), name);
        }
        assertTrue(CrucibleTransferLogic.shouldApplyColdTarget(1810, 1811, true, false, false));
        assertFalse(CrucibleTransferLogic.shouldApplyColdTarget(286, 1811, false, true, false));
        assertFalse(CrucibleTransferLogic.shouldApplyColdTarget(286, -1, true, true, true));
    }

    @Test
    void gt6NegativeIdRecyclingFamiliesResolveTheirVerifiedBaseMaterial() {
        String[][] mappings = {
                {"AnyIron", "gregtech:iron"}, {"AnyIronOrSteel", "gregtech:iron"},
                {"AnyIronSteel", "gregtech:steel"}, {"AnyBlackSteel", "gregtech:black_steel"},
                {"AnyBlueSteel", "gregtech:blue_steel"}, {"AnyRedSteel", "gregtech:red_steel"},
                {"AnyCopper", "gregtech:copper"}, {"AnyTungsten", "gregtech:tungsten"},
                {"AnyCarbon", "gregtech:carbon"}, {"AnyCoalCarbon", "gregtech:carbon"},
                {"AnyDiamond", "gregtech:diamond"}, {"AnySapphire", "gregtech:sapphire"},
                {"AnyEmerald", "gregtech:emerald"}, {"AnyGlowstone", "gregtech:glowstone"},
                {"AnyFluorite", "gtqtcore:fluorite"}
        };
        for (String[] mapping : mappings) {
            assertEquals(mapping[1], GT6MaterialIdentity.hostRecyclingName(mapping[0]));
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("m", mapping[0]);
            entry.setLong("a", 2 * 648648000L);
            NBTTagCompound list = new NBTTagCompound();
            list.setInteger("size", 1);
            list.setTag("0", entry);
            NBTTagCompound root = new NBTTagCompound();
            root.setTag(CrucibleRecyclingOverride.TAG, list);
            int[] calls = {0};
            assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
                assertEquals(mapping[1], name);
                calls[0]++;
                return null; // Missing installed material must not consume the item.
            }).isEmpty());
            assertEquals(1, calls[0]);
            assertEquals(2 * GTValues.M, CrucibleRecyclingOverride.convertAmount(entry.getLong("a")));
        }
        assertEquals("gregtech:steel", GT6MaterialIdentity.hostRecyclingName("Any Iron-Steel"));
        assertEquals("gregtech:carbon", GT6MaterialIdentity.hostRecyclingName("Any Coal/Carbon"));
    }

    @Test
    void recyclingFamiliesDoNotNormalizeMembersPlaceholdersOrOtherNamespaces() {
        for (String name : new String[]{"AnySteel", "AnyBronze", "AnyMetal",
                "AnyUnknown", "gregtech:anyiron", "gtqtcore:AnyIron",
                "MeteoricIron", "WroughtIron", "Graphite", "Charcoal"}) {
            assertEquals(name, GT6MaterialIdentity.hostRecyclingName(name));
        }
        assertEquals("gregtech:iron", GT6MaterialIdentity.hostRecyclingName("ANY_IRON"));
        assertEquals("gregtech:iron", GT6MaterialIdentity.hostRecyclingName("any-iron"));
    }

    @Test
    void phosphorusElementAndCompoundHaveSeparateGt6Identities() {
        assertEquals("gregtech:phosphorus",
                GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(150)));
        assertEquals("gregtech:tricalcium_phosphate",
                GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(8208)));
        assertEquals("gregtech:tricalcium_phosphate", GT6MaterialIdentity.hostRecyclingName("Phosphorous"));
        assertEquals("gregtech:phosphorus", GT6MaterialIdentity.hostRecyclingName("gregtech:phosphorus"));
        assertEquals("gtqtcore:phosphorus", GT6MaterialIdentity.hostRecyclingName("gtqtcore:phosphorus"));
        for (int id : new int[]{150, 8208}) {
            String wanted = id == 150 ? "gregtech:phosphorus" : "gregtech:tricalcium_phosphate";
            for (boolean numeric : new boolean[]{false, true}) {
                NBTTagCompound entry = new NBTTagCompound();
                if (numeric) entry.setShort("i", (short) id);
                else entry.setString("m", GT6MaterialIdentity.name(id));
                entry.setLong("a", 648648000L);
                NBTTagCompound list = new NBTTagCompound();
                list.setInteger("size", 1);
                list.setTag("0", entry);
                NBTTagCompound root = new NBTTagCompound();
                root.setTag(CrucibleRecyclingOverride.TAG, list);
                int[] calls = {0};
                assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
                    assertEquals(wanted, name);
                    calls[0]++;
                    return null;
                }).isEmpty());
                assertEquals(1, calls[0]);
            }
        }
    }

    @Test
    void magicIronAndWoodPlasticFamiliesKeepSeparateStatisticsAndProcessingTargets() {
        String[][] families = {
                {"Any Magic Iron", "gt6addition:any_magic_iron"},
                {"Any Wood Or Plastic", "gt6addition:any_wood_or_plastic"}
        };
        for (String[] family : families) {
            for (String name : new String[]{family[0], family[0].replace(" ", "")}) {
                assertEquals(family[1], GT6MaterialIdentity.hostRecyclingName(name));
                NBTTagCompound entry = new NBTTagCompound();
                entry.setString("m", name);
                entry.setLong("a", 3 * 648648000L);
                NBTTagCompound list = new NBTTagCompound();
                list.setInteger("size", 1);
                list.setTag("0", entry);
                NBTTagCompound root = new NBTTagCompound();
                root.setTag(CrucibleRecyclingOverride.TAG, list);
                NBTTagCompound before = root.copy();
                int[] calls = {0};
                assertTrue(CrucibleRecyclingOverride.parse(root, resolved -> {
                    assertEquals(family[1], resolved);
                    calls[0]++;
                    return null;
                }).isEmpty());
                assertEquals(1, calls[0]);
                assertEquals(3 * GTValues.M, CrucibleRecyclingOverride.convertAmount(entry.getLong("a")));
                assertEquals(before, root);
            }
        }
        String magic = "any_magic_iron";
        assertEquals(2311, CrucibleMaterialPhaseData.knownMeltingPoint(magic));
        assertEquals(4134L, CrucibleMaterialPhaseData.boilingPoint(magic));
        assertEquals(7874D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(magic));
        CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(magic);
        assertNotNull(rule);
        assertEquals("gregtech:iron", rule.target);
        assertEquals(3 * GTValues.M, rule.convert(3 * GTValues.M));
        assertEquals(rule.target, CrucibleSolidifyingRule.target(magic));
        assertTrue(CrucibleSmeltingRule.hasMeltingFlag(magic));
        String woodPlastic = "any_wood_or_plastic";
        assertEquals(400, CrucibleMaterialPhaseData.knownMeltingPoint(woodPlastic));
        assertEquals(500L, CrucibleMaterialPhaseData.boilingPoint(woodPlastic));
        assertEquals(1362D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(woodPlastic), 0.00001D);
        assertNull(CrucibleSmeltingRule.find(woodPlastic)); // Default self, not Wood -> Ash/4.
        assertNull(CrucibleSolidifyingRule.target(woodPlastic));
        assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(woodPlastic));
        assertFalse(CrucibleSmeltingRule.hasMeltingFlag(woodPlastic));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption(woodPlastic));
        for (String family : new String[]{magic, woodPlastic}) {
            assertFalse(GT6MaterialHazardData.shouldBurn(family, 314, true));
            assertFalse(GT6MaterialHazardData.isExplosive(family));
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(family));
        }
        for (String unchanged : new String[]{"Manasteel", "Thaumium", "PetrifiedWood", "Plastic",
                "gregtech:AnyMagicIron", "gtqtcore:AnyWoodOrPlastic"}) {
            assertEquals(unchanged, GT6MaterialIdentity.hostRecyclingName(unchanged));
        }
        assertNull(GT6MaterialIdentity.name(30002));
        assertNull(GT6MaterialIdentity.name(30003));
    }

    @Test
    void coloredPhosphorusAndTechnicalFamilyKeepTheirOwnRegistryAndHazards() {
        String[][] identities = {
                {"Blue Phosphorus", "gt6addition:blue_phosphorus", "8458"},
                {"Red Phosphorus", "gt6addition:red_phosphorus", "8459"},
                {"White Phosphorus", "gt6addition:white_phosphorus", "8460"},
                {"Any Phosphorus", "gt6addition:any_phosphorus", "0"}
        };
        for (String[] identity : identities) {
            assertEquals(identity[1], GT6MaterialIdentity.hostRecyclingName(identity[0]));
            assertEquals(identity[1], GT6MaterialIdentity.hostRecyclingName(identity[0].replace(" ", "")));
            int id = Integer.parseInt(identity[2]);
            for (boolean numeric : new boolean[]{false, true}) {
                if (numeric && id == 0) continue; // Negative-ID families are saved by name.
                NBTTagCompound entry = new NBTTagCompound();
                if (numeric) entry.setShort("i", (short) id);
                else entry.setString("m", identity[0]);
                entry.setLong("a", 648648000L);
                NBTTagCompound list = new NBTTagCompound();
                list.setInteger("size", 1);
                list.setTag("0", entry);
                NBTTagCompound root = new NBTTagCompound();
                root.setTag(CrucibleRecyclingOverride.TAG, list);
                NBTTagCompound before = root.copy();
                int[] calls = {0};
                assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
                    assertEquals(identity[1], name);
                    calls[0]++;
                    return null;
                }).isEmpty());
                assertEquals(1, calls[0]);
                assertEquals(before, root);
            }
        }
        String family = "any_phosphorus";
        assertEquals(829, CrucibleMaterialPhaseData.knownMeltingPoint(family));
        assertEquals(1374L, CrucibleMaterialPhaseData.boilingPoint(family));
        assertEquals(1070.05728D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(family), 0.00001D);
        assertTrue(GT6MaterialHazardData.isExplosive(family));
        assertFalse(GT6MaterialHazardData.shouldBurn(family, 314, true));
        assertTrue(CrucibleSmeltingRule.hasMeltingFlag(family));
        CrucibleSmeltingRule smelting = CrucibleSmeltingRule.find(family);
        assertEquals("gregtech:tricalcium_phosphate", smelting.target);
        assertEquals(2 * GTValues.M, smelting.convert(2 * GTValues.M));
        assertEquals(smelting.target, CrucibleSolidifyingRule.target(family));
        for (String color : new String[]{"blue_phosphorus", "red_phosphorus", "white_phosphorus"}) {
            assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(color));
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(color));
            assertFalse(CrucibleSmeltingRule.hasMeltingFlag(color));
            assertTrue(GT6MaterialHazardData.shouldBurn(color, 314, false));
        }
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(family));
        for (String explicit : new String[]{"gregtech:AnyPhosphorus", "gtqtcore:RedPhosphorus",
                "gt6addition:any_phosphorus", "gregtech:phosphorus"}) {
            assertEquals(explicit, GT6MaterialIdentity.hostRecyclingName(explicit));
        }
    }

    @Test
    void phosphorusCompoundUsesNestedGt6PropertiesRatherThanElementOrFlattenedAtoms() {
        assertEquals(317, CrucibleMaterialPhaseData.knownMeltingPoint("phosphorus"));
        assertEquals(550L, CrucibleMaterialPhaseData.boilingPoint("phosphorus"));
        assertEquals(365.1432D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("phosphate"), 0.00001D);
        for (String name : new String[]{"tricalcium_phosphate", "blue_phosphorus", "red_phosphorus", "white_phosphorus"}) {
            assertEquals(829, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(1374L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertEquals(1070.05728D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name), 0.00001D);
            assertTrue(GT6MaterialHazardData.isExplosive(name));
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 313, false));
            assertTrue(GT6MaterialHazardData.shouldBurn(name, 314, false));
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name));
        }
        assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget("tricalcium_phosphate"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("tricalcium_phosphate"));
        assertFalse(GT6AlloyRecipes.hasCompleteRecipeSet("phosphorus"));
        assertEquals(1820D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("phosphorus"), 0.00001D);
    }

    @Test
    void actualDefaultFluidUnitHasSafeMissingMaterialFallback() {
        assertEquals(GTValues.L, CrucibleFluidUnits.defaultFluidUnit(null));
        assertNull(CrucibleFluidInput.forMaterial(null, null));
        assertNull(CrucibleFluidInput.resolve(null));
    }

    @Test
    void generatedGt6LiquidsAndLiteralAliasesUseBucketUnitsNotMetalUnits() {
        for (String name : new String[]{"Bromine", "Semiheavy Water", "Heavy Water", "Tritiated Water",
                "Sea Water", "WaterDirty", "Hydrogen Peroxide", "Nitric Acid", "Glycerol", "Glyceryl",
                "Sulfuric Acid", "Disulfuric Acid", "Hexafluorosilicic Acid", "Titanium Tetrachloride",
                "Saltwater", "Salted Water", "Chloroauric Acid", "Chloroplatinic Acid", "Stannic Chloride",
                "Black Vitriol", "Blue Vitriol", "Green Vitriol", "Red Vitriol", "Pink Vitriol",
                "Cyan Vitriol", "White Vitriol", "Gray Vitriol", "Martian Vitriol", "Vitriol Of Clay",
                "Aqua Regia", "Creosote", "Ectoplasm", "SulphuricAcid", "Brine", "RomanVitriol",
                "CyprusVitriol", "SolutionBlueVitriol", "SolutionNickelSulfate", "SolutionNickelSulphate",
                "Creosote Oil", "glyceryl_trinitrate"}) {
            assertEquals(1000, CrucibleFluidUnits.fluidUnit(name), name);
            assertEquals(GTValues.L, CrucibleFluidUnits.legacyFluidUnit(name), name);
            CrucibleFluidUnits.Quantity bucket = CrucibleFluidUnits.storedFluidAmount(1000,
                    CrucibleFluidUnits.fluidUnit(name));
            assertNotNull(bucket);
            assertEquals(GTValues.M, bucket.amount, name);
            assertEquals(0, bucket.remainder, name);
        }
    }

    @Test
    void newLiquidUnitsDoNotReclassifyMercuryMetalsOrTheProjectLavaRule() {
        for (String name : new String[]{"mercury", "iron", "tin", "lava", "unknown_liquid"}) {
            assertEquals(GTValues.L, CrucibleFluidUnits.fluidUnit(name), name);
        }
        assertEquals(504, CrucibleFluidUnits.fluidUnit("alumina"));
        assertEquals(1296, CrucibleFluidUnits.fluidUnit("blaze"));
        assertEquals(160000, CrucibleFluidUnits.gasUnit("water"));
        assertEquals(20736, CrucibleFluidUnits.plasmaUnit("iron"));
    }

    @Test
    void generatedLiquidFractionsCanMergeAndDrainWithoutLosingMillibuckets() {
        int unit = CrucibleFluidUnits.fluidUnit("sulfuric_acid");
        CrucibleFluidUnits.Quantity half = CrucibleFluidUnits.storedFluidAmount(500, unit);
        CrucibleFluidUnits.Quantity small = CrucibleFluidUnits.storedFluidAmount(1, unit);
        CrucibleFluidUnits.Quantity merged = CrucibleFluidUnits.merge(half.amount, half.remainder,
                small.amount, small.remainder, CrucibleFluidUnits.STORAGE_UNIT);
        assertNotNull(merged);
        assertEquals(501, CrucibleFluidUnits.storedFluidVolume(merged.amount, merged.remainder, unit));
        CrucibleFluidUnits.Quantity remaining = CrucibleFluidUnits.drainStored(merged.amount,
                merged.remainder, 500, unit);
        assertNotNull(remaining);
        assertEquals(small.amount, remaining.amount);
        assertEquals(small.remainder, remaining.remainder);
        remaining = CrucibleFluidUnits.drainStored(remaining.amount, remaining.remainder, 1, unit);
        assertNotNull(remaining);
        assertEquals(0, remaining.amount);
        assertEquals(0, remaining.remainder);
    }

    @Test
    void preExpansionLiquidRemaindersKeepTheirOldDenominatorAndMaterialAmount() {
        for (String name : new String[]{"sulfuric_acid", "creosote", "glyceryl_trinitrate"}) {
            // Artificial nonzero remainder tests migration even when the
            // host's M happens to divide evenly by the old metal denominator.
            CrucibleFluidUnits.Quantity migrated = CrucibleFluidUnits.migrateStoredQuantity(
                    GTValues.M, 1, CrucibleFluidUnits.legacyFluidUnit(name));
            assertNotNull(migrated);
            assertEquals(GTValues.M, migrated.amount);
            assertEquals(CrucibleFluidUnits.STORAGE_UNIT / GTValues.L, migrated.remainder);
            assertEquals(1000, CrucibleFluidUnits.storedFluidVolume(migrated.amount, migrated.remainder,
                    CrucibleFluidUnits.fluidUnit(name)));
        }
    }

    @Test
    void verifiedNitroglycerinAliasSharesGt6HeatDensityHazardsAndDefaultTargets() {
        String name = "glyceryl_trinitrate";
        assertEquals("gregtech:glyceryl_trinitrate",
                GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(9821)));
        assertEquals("thirdparty:glyceryl", GT6MaterialIdentity.hostRecyclingName("thirdparty:glyceryl"));
        assertEquals(287, CrucibleMaterialPhaseData.knownMeltingPoint(name));
        assertEquals(323, CrucibleMaterialPhaseData.boilingPoint(name));
        assertEquals(1500D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name), 0.00001D);
        assertTrue(GT6MaterialHazardData.isExplosive(name));
        assertFalse(GT6MaterialHazardData.shouldBurn(name, 313, false));
        assertTrue(GT6MaterialHazardData.shouldBurn(name, 314, false));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name));
        assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget(name));
        assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
        assertEquals("glycerol", GT6MaterialIdentity.canonicalCompoundName("glycerol"));
        assertEquals(291, CrucibleMaterialPhaseData.knownMeltingPoint("glycerol"));
        assertFalse(GT6MaterialHazardData.isExplosive("glycerol"));
    }

    @Test
    void explicitGt6LiquidAndGasUnitsDoNotUseMetalDefaults() {
        assertEquals(1296, CrucibleFluidUnits.fluidUnit("blaze"));
        for (String name : new String[]{"fish_oil", "whale_oil", "seed_oil", "hemp_oil", "lin_oil",
                "sunflower_oil", "nut_oil", "olive_oil", "honey", "honeydew", "holy_water", "milk",
                "glue", "lubricant", "kerosene", "fuel_oil", "gasoline"}) {
            assertEquals(1000, CrucibleFluidUnits.fluidUnit(name), name);
            assertEquals(144, CrucibleFluidUnits.legacyFluidUnit(name), name);
        }
        assertEquals(144, CrucibleFluidUnits.legacyFluidUnit("blaze"));
        assertEquals(250, CrucibleFluidUnits.gasUnit("aerotheum"));
        assertEquals(250, CrucibleFluidUnits.gasUnit("glowstone"));
        assertEquals(160000, CrucibleFluidUnits.gasUnit("steam"));
        assertEquals(160000, CrucibleFluidUnits.gasUnit("water"));
        assertEquals(160000, CrucibleFluidUnits.gasUnit("ice"));
        assertEquals(1000, CrucibleFluidUnits.gasUnit("iron"));
        CrucibleFluidUnits.Quantity blaze = CrucibleFluidUnits.storedFluidAmount(1296, 1296);
        assertNotNull(blaze);
        assertEquals(GTValues.M, blaze.amount);
        assertEquals(0, blaze.remainder);
        CrucibleFluidUnits.Quantity old = CrucibleFluidUnits.exactFluidAmount(1, 144);
        CrucibleFluidUnits.Quantity migrated = CrucibleFluidUnits.migrateStoredQuantity(old.amount, old.remainder,
                CrucibleFluidUnits.legacyFluidUnit("blaze"));
        assertEquals(9, CrucibleFluidUnits.storedFluidVolume(migrated.amount, migrated.remainder, 1296));
        assertEquals(144, CrucibleFluidUnits.fluidUnit("unknown_material"));
    }

    @Test
    void gt6DefaultSolidificationCannotBeOverriddenByHostFluidReverseLookup() {
        for (String name : new String[]{"iron", "tin", "carbon", "beryllium", "bronze",
                "wrought_iron", "annealed_copper", "duranium_alloy", "tritanium_alloy",
                "osmium", "silicon_carbide", "constantan", "tungsten_carbide"}) {
            assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name), name);
        }
        for (String name : new String[]{"redstonia", "diamantine", "emeradic", "clay_brick",
                "wood_treated", "refined_glowstone", "refined_obsidian", "refined_iron"}) {
            assertFalse(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name), name);
            assertNotNull(CrucibleSolidifyingRule.target(name), name);
        }
        assertFalse(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget("unregistered_custom_alloy"));
        assertFalse(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(null));
        assertFalse(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(""));
    }

    @Test
    void smeltingRatiosConvertIntegerAndFractionalMaterialTogether() {
        CrucibleFluidUnits.Quantity source = CrucibleFluidUnits.storedFluidAmount(1, 1000);
        CrucibleFluidUnits.Quantity half = CrucibleSmeltingRule.find("void_crystal")
                .convertStored(source.amount, source.remainder);
        assertNotNull(half);
        CrucibleFluidUnits.Quantity restored = CrucibleFluidUnits.scaleStored(half.amount, half.remainder, 2, 1);
        assertNotNull(restored);
        assertEquals(source.amount, restored.amount);
        assertEquals(source.remainder, restored.remainder);
        CrucibleFluidUnits.Quantity beryllium = CrucibleSmeltingRule.find("emeradic")
                .convertStored(source.amount, source.remainder);
        restored = CrucibleFluidUnits.scaleStored(beryllium.amount, beryllium.remainder, 36, 1);
        assertEquals(source.amount, restored.amount);
        assertEquals(source.remainder, restored.remainder);
        CrucibleFluidUnits.Quantity micro = CrucibleSmeltingRule.find("void_crystal").convertStored(1, 0);
        assertEquals(0, micro.amount);
        assertEquals(CrucibleFluidUnits.STORAGE_UNIT / 2, micro.remainder);
        CrucibleFluidUnits.Quantity disabled = CrucibleSmeltingRule.find("silverwood").convertStored(1, 0);
        assertEquals(0, disabled.amount);
        assertEquals(0, disabled.remainder);
        CrucibleFluidUnits.Quantity obsidian = CrucibleFluidUnits.scaleStored(GTValues.M, 0, 1000, 144);
        assertNotNull(obsidian);
        assertEquals(1000, CrucibleFluidUnits.storedFluidVolume(obsidian.amount, obsidian.remainder, 144));
        assertNull(CrucibleFluidUnits.scaleStored(Long.MAX_VALUE, 0, 2, 1));
        assertNull(CrucibleFluidUnits.scaleStored(1, 0, 1, 0));
        assertNull(CrucibleFluidUnits.scaleStored(1, -1, 1, 1));
    }

    @Test
    void phaseExpansionChecksCapacityBeforeReplacingContent() {
        assertTrue(CrucibleFluidUnits.replacementFits(100, 100, 20, 0, 20, 0, true));
        assertFalse(CrucibleFluidUnits.replacementFits(100, 100, 20, 0, 21, 0, true));
        assertFalse(CrucibleFluidUnits.replacementFits(100, 100, 20, 0, 20, 1, true));
        assertTrue(CrucibleFluidUnits.replacementFits(99, 100, 20, 0, 20, 1, true));
        assertTrue(CrucibleFluidUnits.replacementFits(80, 100, 0, 0, 20, 0, false));
        assertFalse(CrucibleFluidUnits.replacementFits(80, 100, 0, 0, 20, 1, false));
        assertTrue(CrucibleFluidUnits.replacementFits(120, 100, 20, 0, 10, 0, true));
        assertFalse(CrucibleFluidUnits.replacementFits(120, 100, 20, 0, 21, 0, true));
        assertFalse(CrucibleFluidUnits.replacementFits(10, 100, 20, 0, 10, 0, true));
        assertFalse(CrucibleFluidUnits.replacementFits(100, 100, 20, 0, Long.MAX_VALUE, 1, true));
        assertEquals(21, CrucibleFluidUnits.occupiedUnits(20, 1));
        assertEquals(-1, CrucibleFluidUnits.occupiedUnits(Long.MAX_VALUE, 1));
    }

    @Test
    void multiphaseFluidUnitsShareExactMaterialFractions() {
        for (int unit : new int[]{144, 504, 1000, 250, 160000, 20736}) {
            CrucibleFluidUnits.Quantity stored = CrucibleFluidUnits.storedFluidAmount(unit, unit);
            assertNotNull(stored);
            assertEquals(GTValues.M, stored.amount);
            assertEquals(0, stored.remainder);
            assertEquals(unit, CrucibleFluidUnits.storedFluidVolume(stored.amount, stored.remainder, unit));
        }
        CrucibleFluidUnits.Quantity gas = CrucibleFluidUnits.storedFluidAmount(1, 1000);
        CrucibleFluidUnits.Quantity plasma = CrucibleFluidUnits.storedFluidAmount(1, 20736);
        CrucibleFluidUnits.Quantity mixed = CrucibleFluidUnits.merge(gas.amount, gas.remainder,
                plasma.amount, plasma.remainder, CrucibleFluidUnits.STORAGE_UNIT);
        assertNotNull(mixed);
        CrucibleFluidUnits.Quantity remaining = CrucibleFluidUnits.drainStored(mixed.amount, mixed.remainder, 1, 1000);
        assertNotNull(remaining);
        assertEquals(plasma.amount, remaining.amount);
        assertEquals(plasma.remainder, remaining.remainder);
        remaining = CrucibleFluidUnits.drainStored(remaining.amount, remaining.remainder, 1, 20736);
        assertNotNull(remaining);
        assertEquals(0, remaining.amount);
        assertEquals(0, remaining.remainder);
        assertNull(CrucibleFluidUnits.drainStored(gas.amount, gas.remainder, 2, 1000));
        assertNull(CrucibleFluidUnits.storedFluidAmount(-1, 1000));
        assertNull(CrucibleFluidUnits.storedFluidAmount(1, 11));
    }

    @Test
    void commonFluidStorageMigratesOldRemaindersWithoutRounding() {
        for (int unit : new int[]{144, 504, 1000, 250, 160000, 20736}) {
            CrucibleFluidUnits.Quantity old = CrucibleFluidUnits.exactFluidAmount(17, unit);
            CrucibleFluidUnits.Quantity migrated = CrucibleFluidUnits.migrateStoredQuantity(old.amount, old.remainder, unit);
            CrucibleFluidUnits.Quantity direct = CrucibleFluidUnits.storedFluidAmount(17, unit);
            assertNotNull(migrated);
            assertEquals(direct.amount, migrated.amount);
            assertEquals(direct.remainder, migrated.remainder);
            assertEquals(17, CrucibleFluidUnits.storedFluidVolume(migrated.amount, migrated.remainder, unit));
        }
        assertNull(CrucibleFluidUnits.migrateStoredQuantity(1, 1000, 1000));
        assertNull(CrucibleFluidUnits.migrateStoredQuantity(-1, 0, 1000));
        assertEquals(1000, CrucibleFluidUnits.plasmaUnit("helium"));
        assertEquals(1000, CrucibleFluidUnits.plasmaUnit("nitrogen"));
        assertEquals(20736, CrucibleFluidUnits.plasmaUnit("iron"));
        assertEquals(20736, CrucibleFluidUnits.plasmaUnit("helium_3"));
        assertEquals(0, CrucibleFluidUnits.storedFluidVolume(1, CrucibleFluidUnits.STORAGE_UNIT, 1000));
    }

    @Test
    void electricRecyclingRecognizesChargeWithoutMutatingNbt() {
        NBTTagCompound root = new NBTTagCompound();
        root.setLong("Charge", 12345);
        root.setLong("MaxCharge", 120000);
        root.setBoolean("Infinite", true);
        root.setTag("display", new NBTTagCompound());
        NBTTagCompound before = root.copy();
        assertTrue(CrucibleElectricRecycling.hasStaticElectricTags(root));
        assertEquals(before, root);
        assertTrue(CrucibleElectricRecycling.hasStaticElectricTags(null));
        root.setInteger("Charge", 12);
        assertFalse(CrucibleElectricRecycling.hasStaticElectricTags(root));
        root.setLong("Charge", -1);
        assertFalse(CrucibleElectricRecycling.hasStaticElectricTags(root));
        root.setLong("Charge", 0);
        root.setLong("MaxCharge", 0);
        assertFalse(CrucibleElectricRecycling.hasStaticElectricTags(root));
        root.setLong("MaxCharge", 120000);
        root.setByte("Infinite", (byte) 2);
        assertFalse(CrucibleElectricRecycling.hasStaticElectricTags(root));
        root.setBoolean("Infinite", false);
        root.setTag("Inventory", new NBTTagCompound());
        assertFalse(CrucibleElectricRecycling.hasStaticElectricTags(root));
    }

    @Test
    void batteryCapacityOverridesRequireExplicitMaterialIdentity() {
        assertTrue(CrucibleElectricRecycling.hasResolvedCapacity(120000, 120000, false));
        assertFalse(CrucibleElectricRecycling.hasResolvedCapacity(120000, 100000, false));
        assertTrue(CrucibleElectricRecycling.hasResolvedCapacity(120000, 100000, true));
        assertFalse(CrucibleElectricRecycling.hasResolvedCapacity(0, 100000, true));
        assertFalse(CrucibleElectricRecycling.hasResolvedCapacity(120000, 0, true));
        ItemStack unknown = new ItemStack(new Item());
        assertTrue(CrucibleElectricRecycling.resolve(unknown, name -> {
            throw new AssertionError("Unknown non-electric items must not resolve materials");
        }).isEmpty());
        assertFalse(unknown.hasTagCompound());
    }

    @Test
    void shapedToolRecyclingCountsOnlyOccupiedPatternSlots() {
        Object[] recipe = {"AA ", " B", "A h", 'A', "plateIron", 'B', "stickWood", 'h', "toolHammer"};
        java.util.Map<Character, Integer> counts = CrucibleToolRecycling.shapedSymbolCounts(recipe);
        assertNotNull(counts);
        assertEquals(Integer.valueOf(3), counts.get('A'));
        assertEquals(Integer.valueOf(1), counts.get('B'));
        assertEquals(Integer.valueOf(1), counts.get('h'));
        assertFalse(counts.containsKey(' '));
        assertEquals(3, counts.size());
        assertEquals("AA ", recipe[0]);
        assertNull(CrucibleToolRecycling.shapedSymbolCounts(null));
        assertNull(CrucibleToolRecycling.shapedSymbolCounts(new Object[]{}));
        assertNull(CrucibleToolRecycling.shapedSymbolCounts(new Object[]{'A', "plateIron"}));
        assertNull(CrucibleToolRecycling.shapedSymbolCounts(new Object[]{"   "}));
        assertNull(CrucibleToolRecycling.shapedSymbolCounts(new Object[]{"AAAA", 'A', "plateIron"}));
        assertNull(CrucibleToolRecycling.shapedSymbolCounts(new Object[]{"A", "A", "A", "A"}));
    }

    @Test
    void toolRecipeBatchDivisionDoesNotSilentlyLoseMaterial() {
        assertEquals(50, CrucibleToolRecycling.exactPerOutputAmount(100, 2));
        assertEquals(0, CrucibleToolRecycling.exactPerOutputAmount(101, 2));
        assertEquals(0, CrucibleToolRecycling.exactPerOutputAmount(1, 2));
        assertEquals(0, CrucibleToolRecycling.exactPerOutputAmount(100, 0));
        assertEquals(0, CrucibleToolRecycling.exactPerOutputAmount(-1, 1));
        assertEquals(Long.MAX_VALUE, CrucibleToolRecycling.exactPerOutputAmount(Long.MAX_VALUE, 1));
    }

    @Test
    void combustionFlagsAndIgnitionThresholdFollowGt6NotFactoryNames() {
        for (String name : new String[]{"phosphor", "phosphorus", "phosphorous", "hafnium", "glyceryl",
                "phosphate", "nitro_carbon", "blue_phosphorus", "red_phosphorus", "white_phosphorus",
                "gunpowder", "dynamite", "TNT", "naquadah_enriched", "enriched_naquadah", "naquadria",
                "fuel", "fuel_oil", "nitro_fuel", "kerosine", "diesel", "petrol", "gasoline"}) {
            assertTrue(GT6MaterialHazardData.isExplosive(name), name);
        }
        for (String name : new String[]{"gunpowder", "dynamite", "niter", "apatite", "phosphorite",
                "sodium_nitrate", "potassium_nitrate", "wheat", "indigo", "petrified_wood", "peat"}) {
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 313, false), name);
            assertTrue(GT6MaterialHazardData.shouldBurn(name, 314, false), name);
        }
        for (String name : new String[]{"propane", "butane", "propylene", "ethylene"}) {
            assertFalse(GT6MaterialHazardData.isExplosive(name), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, true), name);
        }
        assertFalse(GT6MaterialHazardData.isExplosive(null));
        assertFalse(GT6MaterialHazardData.isExplosiveMaterial(null));
        assertFalse(GT6MaterialHazardData.shouldBurn((gregtech.api.unification.material.Material) null, 314));
        assertFalse(GT6MaterialHazardData.shouldBurn("unknown", 314, false));
        assertTrue(GT6MaterialHazardData.shouldBurn("unknown", 314, true));
        assertFalse(GT6MaterialHazardData.shouldBurn("naquadria", 314, true));
    }

    @Test
    void meltingTagsFollowExplicitFactoriesNotTheWoodFactoryName() {
        for (String name : new String[]{"carbon", "carbon_13", "carbon_14", "sulfur", "sulphur"}) {
            assertTrue(GT6ElementPhaseData.hasMeltingFlag(name), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, true), name);
        }
        for (String name : new String[]{"marshmallow", "wood", "coal",
                "rubber", "ptfe", "polymer", "sugar", "netherrack", "hafnium"}) {
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, true), name);
        }
        assertEquals(0, CrucibleSmeltingRule.find("silverwood").convert(GTValues.M));
        assertFalse(GT6InheritedBurningExemptions.contains("silverwood"));
        assertTrue(GT6MaterialHazardData.shouldBurn("silverwood", 314, false));
        assertTrue(GT6MaterialHazardData.shouldBurn("peanutwood", 314, false));
        assertNull(CrucibleSmeltingRule.find("peanutwood"));
        assertNull(CrucibleSmeltingRule.find("marshmallow"));
        assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget("peanutwood"));
        assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget("marshmallow"));
        assertEquals(2 * GTValues.M / 3, CrucibleSmeltingRule.find("ptfe").convert(GTValues.M));
    }

    @Test
    void acidHazardsUseExplicitGt6FactoryTagsNotNameFragments() {
        for (String name : new String[]{"hydrochloric_acid", "hydrogen fluoride", "ammonia", "nitric_acid",
                "hydrosulfuric_acid", "sulfuric_acid", "sulphuric_acid", "disulfuric_acid",
                "hexafluorosilicic_acid", "hydrofluoric_acid", "hydrogen_sulfide", "carbon_trioxide",
                "tungstic_acid", "aluminium_fluoride", "aluminum_fluoride", "titanium_tetrachloride",
                "chloroauric_acid", "chloroplatinic_acid", "stannic_chloride", "black_vitriol",
                "blue_vitriol", "RomanVitriol", "CyprusVitriol", "SolutionBlueVitriol", "green_vitriol",
                "red_vitriol", "pink_vitriol", "cyan_vitriol", "SolutionNickelSulfate",
                "SolutionNickelSulphate", "white_vitriol", "gray_vitriol", "martian_vitriol",
                "vitriol_of_clay", "aqua_regia", "fluorite", "Red Fluorite", "pink_fluorite",
                "blue_fluorite", "green_fluorite", "black_fluorite", "white_fluorite",
                "yellow_fluorite", "orange_fluorite", "magenta_fluorite"}) {
            assertTrue(GT6MaterialHazardData.isAcid(name), name);
        }
        assertFalse(GT6MaterialHazardData.isAcid(null));
        assertFalse(GT6MaterialHazardData.isAcidMaterial(null));
        assertFalse(GT6MaterialHazardData.isAcid(""));
        assertFalse(GT6MaterialHazardData.isAcid("unknown_acid"));
        assertFalse(GT6MaterialHazardData.isAcid("acidproof_alloy"));
        assertFalse(GT6MaterialHazardData.isAcid("iron"));
        assertFalse(GT6MaterialHazardData.isAcid("water"));
    }

    @Test
    void clayAndFluoriteFactoriesKeepTheirExplicitTargetsAndTemperatures() {
        for (String name : new String[]{"clay", "clay_brown", "clay_red", "bentonite",
                "palygorskite", "fullers_earth", "kaolinite"}) {
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            assertNotNull(rule);
            assertEquals("ceramic", rule.target);
            assertEquals(GTValues.M, rule.convert(GTValues.M));
            assertEquals(2000, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(4000L, CrucibleMaterialPhaseData.boilingPoint(name));
        }
        assertNull(CrucibleSmeltingRule.find("fluorite").target);
        for (String color : new String[]{"red", "pink", "blue", "green", "black", "white",
                "yellow", "orange", "magenta"}) {
            String name = color + "_fluorite";
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            assertNotNull(rule);
            assertEquals("fluorite", rule.target);
            assertEquals(GTValues.M, rule.convert(GTValues.M));
            assertEquals(1633, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(3266L, CrucibleMaterialPhaseData.boilingPoint(name));
        }
        assertEquals("ceramic", CrucibleSmeltingRule.find("clay_brick").target);
        assertEquals("ceramic", CrucibleSolidifyingRule.target("clay_brick"));
    }

    @Test
    void strengthenedGemsCopyIndependentSmeltingAndCoolingTargets() {
        String[] names = {"redstonia", "palis", "diamantine", "void_crystal", "emeradic", "enori"};
        String[] smelting = {"redstone", "lapis", "carbon", "carbon", "beryllium", "iron"};
        String[] cooling = {"redstone", "lapis", "diamond", "coal", "emerald", "iron"};
        String[] heatSources = {"redstone", "lapis", "diamond", "coal", "emerald", "iron"};
        long[] outputs = {GTValues.M, GTValues.M, 2 * GTValues.M, GTValues.M / 2,
                GTValues.M / 36, GTValues.M};
        for (int i = 0; i < names.length; i++) {
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(names[i]);
            assertNotNull(rule);
            assertEquals(smelting[i], rule.target);
            assertEquals(outputs[i], rule.convert(GTValues.M));
            assertEquals(cooling[i], CrucibleSolidifyingRule.target(names[i]));
            int melting = CrucibleMaterialPhaseData.knownMeltingPoint(heatSources[i]);
            assertTrue(melting >= 0);
            assertEquals(melting, CrucibleMaterialPhaseData.knownMeltingPoint(names[i]));
            assertEquals(CrucibleMaterialPhaseData.boilingPoint(heatSources[i]),
                    CrucibleMaterialPhaseData.boilingPoint(names[i]));
            assertTrue(CrucibleSmeltingRule.hasMeltingFlag(names[i]));
        }
        assertEquals(4200, CrucibleMaterialPhaseData.knownMeltingPoint("diamond"));
        assertEquals(4300L, CrucibleMaterialPhaseData.boilingPoint("diamond"));
        // These are cooling targets of the copied material, not an instruction
        // to restore a strengthened gem after its conversion into carbon/beryllium.
        assertNull(CrucibleSolidifyingRule.target("carbon"));
        assertNull(CrucibleSolidifyingRule.target("beryllium"));
    }

    @Test
    void onlyDefinedHexoriumVariantsReceiveExplicitDisabledSmelting() {
        for (String color : new String[]{"black", "red", "green", "blue", "white"}) {
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find("hexorium_" + color);
            assertNotNull(rule);
            assertEquals("", rule.target);
            assertEquals(0, rule.convert(GTValues.M));
            assertFalse(CrucibleSmeltingRule.hasMeltingFlag("hexorium_" + color));
        }
        assertNull(CrucibleSmeltingRule.find("hexorium_brown"));
        assertNull(CrucibleSmeltingRule.find("hexorium_turquoise"));
    }

    @Test
    void carborundumElectrolyzerFactoryAlsoRegistersCrucibleAlloy() {
        assertGt6AlloyRecipe("silicon_carbide", 2, new String[]{"silicon", "carbon"}, new long[]{1, 1});
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("silicon_carbide"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("Carborundum"));
        assertEquals("siliconcarbide", GT6MaterialIdentity.canonicalAlloyName("carborundum"));
        assertEquals("Carborundum", GT6MaterialIdentity.name(8003));
        for (String name : new String[]{"carborundum", "silicon_carbide"}) {
            assertEquals(3000, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(3100L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(name));
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            assertNotNull(rule);
            assertNull(rule.target);
            assertEquals(GTValues.M, rule.convert(GTValues.M));
        }
    }

    @Test
    void explicitNamespaceCannotFallBackToAnotherModsSameNamedMaterial() {
        assertTrue(GT6MaterialIdentity.allowsRegistryNamespace("gt6addition:duranium_alloy",
                "gt6addition:duranium_alloy"));
        assertTrue(GT6MaterialIdentity.allowsRegistryNamespace("gt6addition:DuraniumAlloy",
                "gt6addition:duranium_alloy"));
        assertFalse(GT6MaterialIdentity.allowsRegistryNamespace("gt6addition:duranium_alloy",
                "gtqtcore:duranium_alloy"));
        assertFalse(GT6MaterialIdentity.allowsRegistryNamespace("missing:Iron", "gregtech:iron"));
        assertFalse(GT6MaterialIdentity.allowsRegistryNamespace("gt6addition:DarkSteel", "gregtech:dark_steel"));
        assertTrue(GT6MaterialIdentity.allowsRegistryNamespace("Iron", "gregtech:iron"));
        assertTrue(GT6MaterialIdentity.allowsRegistryNamespace("DuraniumAlloy", "gt6addition:duranium_alloy"));
        assertFalse(GT6MaterialIdentity.allowsRegistryNamespace(":iron", "gregtech:iron"));
        assertFalse(GT6MaterialIdentity.allowsRegistryNamespace("gregtech:", "gregtech:iron"));
        assertFalse(GT6MaterialIdentity.allowsRegistryNamespace("gregtech:other:iron", "gregtech:iron"));
        assertFalse(GT6MaterialIdentity.allowsRegistryNamespace(null, "gregtech:iron"));
        assertFalse(GT6MaterialIdentity.allowsRegistryNamespace("gregtech:iron", null));
    }

    @Test
    void duraniteElementAndAlloyRecyclingIdentitiesStaySeparate() {
        assertGt6AlloyRecipe("duranium_alloy", 8, new String[]{"duranium", "magnesium"}, new long[]{7, 1});
        assertGt6AlloyRecipe("tritanium_alloy", 4, new String[]{"tritanium", "duranium"}, new long[]{3, 1});
        int[] ids = {1450, 1250, 8751, 8752};
        String[] expected = {"gregtech:duranium", "gregtech:tritanium",
                "gt6addition:duranium_alloy", "gt6addition:tritanium_alloy"};
        for (int i = 0; i < ids.length; i++) {
            String wanted = expected[i];
            assertEquals(wanted, GT6MaterialIdentity.hostRecyclingName(GT6MaterialIdentity.name(ids[i])));
            NBTTagCompound entry = new NBTTagCompound();
            entry.setShort("i", (short) ids[i]);
            entry.setLong("a", 648648000L);
            NBTTagCompound list = new NBTTagCompound();
            list.setInteger("size", 1);
            list.setTag("0", entry);
            NBTTagCompound root = new NBTTagCompound();
            root.setTag(CrucibleRecyclingOverride.TAG, list);
            int[] lookups = {0};
            assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
                assertEquals(wanted, name);
                lookups[0]++;
                return null; // Missing target still retains the source item.
            }).isEmpty());
            assertEquals(1, lookups[0]);
        }
        assertEquals("gregtech:duranium", GT6MaterialIdentity.hostRecyclingName("gregtech:duranium"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("duranium_alloy"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("tritanium_alloy"));
        assertEquals(1200, CrucibleMaterialPhaseData.knownMeltingPoint("duranium"));
        assertEquals(2000, CrucibleMaterialPhaseData.knownMeltingPoint("tritanium"));
        assertEquals(1165, CrucibleMaterialPhaseData.knownMeltingPoint("duranium_alloy"));
        assertEquals(2350L, CrucibleMaterialPhaseData.boilingPoint("duranium_alloy"));
        assertEquals(1800, CrucibleMaterialPhaseData.knownMeltingPoint("tritanium_alloy"));
        assertEquals(2976L, CrucibleMaterialPhaseData.boilingPoint("tritanium_alloy"));
        assertEquals(20000.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("duranium"), 0.001D);
        assertEquals(25000.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("tritanium"), 0.001D);
        assertNull(CrucibleSmeltingRule.find("duranium_alloy").target);
        assertNull(CrucibleSmeltingRule.find("tritanium_alloy").target);
    }

    @Test
    void triniumAlloysKeepGt6IdentityRatiosAndSharedElementTemperatures() {
        assertGt6AlloyRecipe("trinaquadalloy", 9, new String[]{"trinium", "naquadah", "carbon"},
                new long[]{6, 2, 1});
        assertGt6AlloyRecipe("trinitanium", 3, new String[]{"trinium", "titanium"}, new long[]{2, 1});
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("trinaquadalloy"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("trinitanium"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("gtqtcore:NaquadahAlloy"));
        assertEquals("trinaquadalloy", GT6MaterialIdentity.canonicalAlloyName("naquadahalloy"));
        // Same-looking element/alloy names are not established aliases.
        assertEquals("duraniumelemental", GT6MaterialIdentity.canonicalAlloyName("duraniumelemental"));
        assertEquals("tritaniumelemental", GT6MaterialIdentity.canonicalAlloyName("tritaniumelemental"));
        assertEquals(2645, CrucibleMaterialPhaseData.knownMeltingPoint("trinium"));
        assertEquals(4523L, CrucibleMaterialPhaseData.boilingPoint("trinium"));
        assertEquals(1500, CrucibleMaterialPhaseData.knownMeltingPoint("naquadah"));
        assertEquals(3000L, CrucibleMaterialPhaseData.boilingPoint("naquadah"));
        assertEquals(GT6DeclaredPhaseData.compositionPoint(new long[]{2645, 1500, 3800},
                new long[]{6, 2, 1}, 0), CrucibleMaterialPhaseData.knownMeltingPoint("trinaquadalloy"));
        assertEquals(GT6DeclaredPhaseData.compositionPoint(new long[]{4523, 3000, 4300},
                new long[]{6, 2, 1}, 0), CrucibleMaterialPhaseData.boilingPoint("trinaquadalloy"));
        assertEquals(CrucibleMaterialPhaseData.knownMeltingPoint("trinaquadalloy"),
                CrucibleMaterialPhaseData.knownMeltingPoint("NaquadahAlloy"));
        assertEquals(CrucibleMaterialPhaseData.boilingPoint("trinaquadalloy"),
                CrucibleMaterialPhaseData.boilingPoint("NaquadahAlloy"));
        assertEquals(GT6DeclaredPhaseData.compositionPoint(new long[]{2645, 1941}, new long[]{2, 1}, 0),
                CrucibleMaterialPhaseData.knownMeltingPoint("trinitanium"));
    }

    @Test
    void singleComponentRefinementKeepsSelfSmeltingTargetsAndExactAmount() {
        assertGt6AlloyRecipe("wrought_iron", 1, new String[]{"iron"}, new long[]{1});
        assertGt6AlloyRecipe("annealed_copper", 1, new String[]{"copper"}, new long[]{1});
        for (String name : new String[]{"wrought_iron", "WrougtIron", "annealed_copper"}) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            assertNotNull(rule);
            assertNull(rule.target);
            assertEquals(GTValues.M, rule.convert(GTValues.M));
        }
        assertEquals(2011, CrucibleMaterialPhaseData.knownMeltingPoint("WrougtIron"));
        assertEquals(2800, CrucibleMaterialPhaseData.knownMeltingPoint("annealed_copper"));
    }

    @Test
    void highSpeedSteelAndLateIndustrialRecipesKeepGt6NestedRatios() {
        assertGt6AlloyRecipe("netherite", 1, new String[]{"gold", "ancient_debris"}, new long[]{4, 4});
        assertGt6AlloyRecipe("desh", 9,
                new String[]{"boron", "lanthanum", "neodymium", "niobium", "cobalt", "cerium", "lithium"},
                new long[]{2, 2, 1, 1, 1, 1, 1});
        assertGt6AlloyRecipe("hss_g", 9, new String[]{"tungsten_steel", "chrome", "molybdenum", "vanadium"},
                new long[]{5, 1, 2, 1});
        assertGt6AlloyRecipe("hss_e", 9, new String[]{"hss_g", "cobalt", "manganese", "silicon"},
                new long[]{6, 1, 1, 1});
        assertGt6AlloyRecipe("hss_s", 9, new String[]{"hss_g", "osmiridium", "iridium"}, new long[]{6, 2, 1});
        assertGt6AlloyRecipe("titanium_iridium", 2, new String[]{"iridium", "titanium"}, new long[]{1, 1});
        assertGt6AlloyRecipe("titanium_aluminide", 3, new String[]{"titanium", "aluminium"}, new long[]{3, 7});
        for (String name : new String[]{"netherite", "desh", "hss_g", "hss_e", "hss_s",
                "titanium_iridium", "titanium_aluminide", "Iritanium"}) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("gtqtcore:" + name));
            assertTrue(CrucibleMaterialPhaseData.knownMeltingPoint(name) > 0);
            assertTrue(CrucibleMaterialPhaseData.boilingPoint(name) < Long.MAX_VALUE);
        }
        for (String name : new String[]{"netherized_diamond", "workers_alloy", "duralumin"}) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
            for (GT6AlloyRecipes recipe : GT6AlloyRecipes.ALL) assertFalse(name.equals(recipe.output));
        }
        assertEquals(1602, CrucibleMaterialPhaseData.knownMeltingPoint("desh"));
        assertEquals(3634L, CrucibleMaterialPhaseData.boilingPoint("desh"));
        assertEquals(2330, CrucibleMaterialPhaseData.knownMeltingPoint("Iritanium"));
        assertEquals(4130L, CrucibleMaterialPhaseData.boilingPoint("Iritanium"));
        assertEquals(1235, CrucibleMaterialPhaseData.knownMeltingPoint("titanium_aluminide"));
        assertEquals(3022L, CrucibleMaterialPhaseData.boilingPoint("titanium_aluminide"));
        assertEquals(GT6DeclaredPhaseData.compositionPoint(new long[]{2870, 2180, 2896, 2183},
                new long[]{5, 1, 2, 1}, 0), CrucibleMaterialPhaseData.knownMeltingPoint("hss_g"));
        assertEquals(GT6DeclaredPhaseData.compositionPoint(new long[]{4481, 2944, 4912, 3680},
                new long[]{5, 1, 2, 1}, 0), CrucibleMaterialPhaseData.boilingPoint("hss_g"));
        long hssG = CrucibleMaterialPhaseData.knownMeltingPoint("hss_g");
        assertEquals(GT6DeclaredPhaseData.compositionPoint(new long[]{hssG, 1768, 1519, 1687},
                new long[]{6, 1, 1, 1}, 0), CrucibleMaterialPhaseData.knownMeltingPoint("hss_e"));
        assertEquals(GT6DeclaredPhaseData.compositionPoint(new long[]{hssG, 3012, 2719},
                new long[]{6, 2, 1}, 0), CrucibleMaterialPhaseData.knownMeltingPoint("hss_s"));
    }

    @Test
    void fantasyAndIronwoodRecipesPreserveConfiguredComponentsAndYield() {
        assertGt6AlloyRecipe("celenegil", 2, new String[]{"platinum", "orichalcum"}, new long[]{1, 1});
        assertGt6AlloyRecipe("shadow_steel", 2, new String[]{"shadow_iron", "lemurite"}, new long[]{1, 1});
        assertGt6AlloyRecipe("inolashite", 2, new String[]{"alduorite", "ceruclase"}, new long[]{1, 1});
        assertGt6AlloyRecipe("haderoth", 2, new String[]{"mithril", "rubracium"}, new long[]{1, 1});
        assertGt6AlloyRecipe("desichalkos", 2, new String[]{"eximite", "meutoite"}, new long[]{1, 1});
        assertGt6AlloyRecipe("tartarite", 2, new String[]{"adamantine", "atlarus"}, new long[]{1, 1});
        assertGt6AlloyRecipe("amordrine", 2, new String[]{"prometheum", "kalendrite"}, new long[]{1, 1});
        assertGt6AlloyRecipe("ironwood", 18, new String[]{"wrought_iron", "liveroot", "angmallen"},
                new long[]{8, 9, 2});
        for (String name : new String[]{"celenegil", "shadow_steel", "inolashite", "haderoth", "desichalkos",
                "tartarite", "amordrine", "ironwood"}) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("gtqtcore:" + name));
            assertTrue(CrucibleMaterialPhaseData.knownMeltingPoint(name) > 0);
            assertTrue(CrucibleMaterialPhaseData.boilingPoint(name) < Long.MAX_VALUE);
        }
        for (String name : new String[]{"vulcanite", "steeleaf"}) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
            for (GT6AlloyRecipes recipe : GT6AlloyRecipes.ALL) assertFalse(name.equals(recipe.output));
        }
        assertEquals("steel", CrucibleSmeltingRule.find("steeleaf").target);
        assertEquals(GTValues.M / 4, CrucibleSmeltingRule.find("steeleaf").convert(GTValues.M));
    }

    @Test
    void specialAlloysKeepDistinctOxideAndLavaRecipes() {
        assertGt6AlloyRecipe("energetic_silver", 1, new String[]{"silver", "redstone", "glowstone"},
                new long[]{1, 1, 1});
        assertGt6AlloyRecipe("alumite", 9, new String[]{"alumina", "wrought_iron", "obsidian"},
                new long[]{5, 2, 18});
        assertGt6AlloyRecipe("alumite", 5, new String[]{"aluminium", "wrought_iron", "lava"},
                new long[]{5, 2, 18});
        assertGt6AlloyRecipe("manyullyn", 2, new String[]{"cobalt", "ardite"}, new long[]{1, 1});
        assertGt6AlloyRecipe("vibranium_steel", 4, new String[]{"vibranium", "steel"}, new long[]{1, 3});
        assertGt6AlloyRecipe("vibranium_silver", 4, new String[]{"vibranium", "silver"}, new long[]{1, 3});
        assertGt6AlloyRecipe("vibramantium", 4, new String[]{"vibranium", "adamantium"}, new long[]{1, 3});
        assertGt6AlloyRecipe("clay_compound", 1, new String[]{"stone", "ceramic"}, new long[]{2, 1});
        assertGt6AlloyRecipe("spectre_iron", 1, new String[]{"wrought_iron", "ectoplasm"}, new long[]{1, 1});
        for (String name : new String[]{"energetic_silver", "alumite", "manyullyn", "vibranium_steel",
                "vibranium_silver", "vibramantium", "clay_compound", "spectre_iron", "CrudeSteel"})
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
        for (String name : new String[]{"refined_glowstone", "refined_obsidian", "bedrock_hsla_alloy",
                "manasteel", "terrasteel", "elven_elementium", "gaia_spirit", "elvorium",
                "niflheim_power", "muspelheim_power"}) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
            for (GT6AlloyRecipes recipe : GT6AlloyRecipes.ALL) assertFalse(name.equals(recipe.output));
        }
        for (String name : new String[]{"GlowstoneRefined", "ObsidianRefined", "Elementium"})
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("gtqtcore:" + name));
        for (String name : new String[]{"manyullyn", "vibranium_steel", "vibranium_silver", "vibramantium"}) {
            assertTrue(CrucibleMaterialPhaseData.knownMeltingPoint(name) > 0);
            assertTrue(CrucibleMaterialPhaseData.boilingPoint(name) < Long.MAX_VALUE);
        }
        assertEquals(1384, CrucibleMaterialPhaseData.knownMeltingPoint("manyullyn"));
        assertEquals(3100L, CrucibleMaterialPhaseData.boilingPoint("manyullyn"));
        // MT Steel is explicitly 2046 K; Vb is 4852 K, weighted 3:1 and truncated.
        assertEquals(2747, CrucibleMaterialPhaseData.knownMeltingPoint("vibranium_steel"));
        assertEquals(4704L, CrucibleMaterialPhaseData.boilingPoint("vibranium_steel"));
        assertEquals(2138, CrucibleMaterialPhaseData.knownMeltingPoint("vibranium_silver"));
        assertEquals(4180L, CrucibleMaterialPhaseData.boilingPoint("vibranium_silver"));
        assertEquals(5131, CrucibleMaterialPhaseData.knownMeltingPoint("vibramantium"));
        assertEquals(13249L, CrucibleMaterialPhaseData.boilingPoint("vibramantium"));
    }

    @Test
    void redstoneAndEnderAlloysUseGt6DividersNotHostComponentSums() {
        assertGt6AlloyRecipe("red_alloy", 1, new String[]{"copper", "redstone"}, new long[]{1, 4});
        assertGt6AlloyRecipe("blue_alloy", 1, new String[]{"silver", "nikolite"}, new long[]{1, 4});
        assertGt6AlloyRecipe("purple_alloy", 1, new String[]{"red_alloy", "blue_alloy"}, new long[]{1, 1});
        assertGt6AlloyRecipe("redstone_alloy", 1, new String[]{"silicon", "redstone"}, new long[]{1, 1});
        assertGt6AlloyRecipe("nikoline_alloy", 1, new String[]{"silicon", "nikolite"}, new long[]{1, 1});
        assertGt6AlloyRecipe("electrotine_alloy", 1, new String[]{"wrought_iron", "nikolite"}, new long[]{1, 8});
        assertGt6AlloyRecipe("electrum_flux", 1, new String[]{"electrum", "redstone"}, new long[]{1, 2});
        assertGt6AlloyRecipe("conductive_iron", 1, new String[]{"wrought_iron", "redstone"}, new long[]{1, 1});
        assertGt6AlloyRecipe("signalum", 8, new String[]{"copper", "silver", "red_alloy"}, new long[]{1, 2, 5});
        assertGt6AlloyRecipe("lumium", 4, new String[]{"tin", "silver", "glowstone"}, new long[]{3, 1, 4});
        assertGt6AlloyRecipe("enderium_base", 4, new String[]{"tin", "silver", "platinum"}, new long[]{2, 1, 1});
        assertGt6AlloyRecipe("enderium", 1, new String[]{"enderium_base", "ender_pearl"}, new long[]{1, 1});
        assertGt6AlloyRecipe("obsidian_steel", 1, new String[]{"steel", "obsidian"}, new long[]{1, 9});
        assertGt6AlloyRecipe("pulsating_iron", 1, new String[]{"wrought_iron", "ender_pearl"}, new long[]{1, 1});
        assertGt6AlloyRecipe("energetic_alloy", 1, new String[]{"inductive_alloy", "glowstone"}, new long[]{2, 1});
        assertGt6AlloyRecipe("vibrant_alloy", 1, new String[]{"energetic_alloy", "ender_pearl"}, new long[]{1, 1});
        assertGt6AlloyRecipe("electrical_steel", 1, new String[]{"steel", "silicon"}, new long[]{1, 1});
        assertGt6AlloyRecipe("soularium", 1, new String[]{"soul_sand", "gold"}, new long[]{9, 1});
        assertGt6AlloyRecipe("end_steel", 1, new String[]{"endstone", "obsidian_steel", "obsidian"}, new long[]{1, 1, 9});
        assertGt6AlloyRecipe("melodic_alloy", 1, new String[]{"end_steel", "ender_eye"}, new long[]{1, 1});
        assertGt6AlloyRecipe("stellar_alloy", 2, new String[]{"melodic_alloy", "nether_star", "clay"}, new long[]{1, 1, 4});
        assertGt6AlloyRecipe("vivid_alloy", 1, new String[]{"energetic_silver", "ender_pearl"}, new long[]{1, 1});
        String[] outputs = {"red_alloy", "blue_alloy", "purple_alloy", "redstone_alloy", "nikoline_alloy",
                "electrotine_alloy", "electrum_flux", "conductive_iron", "signalum", "lumium", "enderium_base",
                "enderium", "obsidian_steel", "pulsating_iron", "energetic_alloy", "vibrant_alloy",
                "electrical_steel", "soularium", "end_steel", "melodic_alloy", "stellar_alloy", "vivid_alloy"};
        for (String output : outputs) assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("gtqtcore:" + output));
        for (String alias : new String[]{"TeslatineAlloy", "DarkSteel", "PhasedIron", "PhasedGold", "Vibrant"})
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(alias));
        for (String name : new String[]{"mingrade", "crystalline_alloy", "crystalline_pink_slime"}) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
            for (GT6AlloyRecipes recipe : GT6AlloyRecipes.ALL) assertFalse(name.equals(recipe.output));
        }
    }

    @Test
    void specialSteelRecipesKeepNestedAlloysAndTheirGt6Yields() {
        assertGt6AlloyRecipe("black_steel", 5, new String[]{"nickel", "black_bronze", "steel"},
                new long[]{1, 1, 3});
        assertGt6AlloyRecipe("blue_steel", 8,
                new String[]{"sterling_silver", "bismuth_bronze", "steel", "black_steel"},
                new long[]{1, 1, 2, 4});
        assertGt6AlloyRecipe("red_steel", 8, new String[]{"rose_gold", "brass", "steel", "black_steel"},
                new long[]{1, 1, 2, 4});
        assertGt6AlloyRecipe("vanadium_steel", 5, new String[]{"steel", "vanadium"}, new long[]{4, 1});
        assertGt6AlloyRecipe("tungsten_steel", 2, new String[]{"steel", "tungsten"}, new long[]{1, 1});
        assertGt6AlloyRecipe("tungsten_carbide", 2, new String[]{"tungsten", "carbon"}, new long[]{1, 1});
        assertGt6AlloyRecipe("titanium_gold", 4, new String[]{"titanium", "gold"}, new long[]{3, 1});
        assertGt6AlloyRecipe("tantalum_hafnium_carbide", 10,
                new String[]{"tantalum", "hafnium", "carbon"}, new long[]{4, 1, 5});
        assertGt6AlloyRecipe("meteoric_black_steel", 5,
                new String[]{"nickel", "black_bronze", "meteoric_steel"}, new long[]{1, 1, 3});
        assertGt6AlloyRecipe("meteoric_blue_steel", 8,
                new String[]{"sterling_silver", "bismuth_bronze", "meteoric_steel", "meteoric_black_steel"},
                new long[]{1, 1, 2, 4});
        assertGt6AlloyRecipe("meteoric_red_steel", 8,
                new String[]{"rose_gold", "brass", "meteoric_steel", "meteoric_black_steel"},
                new long[]{1, 1, 2, 4});
        assertGt6AlloyRecipe("steel", 1, new String[]{"wrought_iron", "air"}, new long[]{1, 1});
        assertGt6AlloyRecipe("meteoric_steel", 1, new String[]{"meteoric_iron", "air"}, new long[]{1, 1});
        assertGt6AlloyRecipe("black_steel", 2, new String[]{"deep_iron", "infuscolium"}, new long[]{1, 1});
        assertGt6AlloyRecipe("vanadium_steel", 5, new String[]{"meteoric_steel", "vanadium"}, new long[]{4, 1});
        assertGt6AlloyRecipe("tungsten_steel", 2, new String[]{"meteoric_steel", "tungsten"}, new long[]{1, 1});
        assertGt6AlloyRecipe("tungsten_steel", 2,
                new String[]{"meteoric_steel", "tungsten_sintered"}, new long[]{1, 1});
        assertGt6AlloyRecipe("tungsten_steel", 2, new String[]{"steel", "tungsten_sintered"}, new long[]{1, 1});
    }

    @Test
    void steelCompositionDeclarationsDoNotImplyRecipesOrRecipeInheritance() {
        String[] noRecipes = {"damascus_steel", "hsla_steel", "spring_steel", "tungsten_alloy",
                "steel_galvanized", "meteoric_iron"};
        for (String name : noRecipes) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
            for (GT6AlloyRecipes recipe : GT6AlloyRecipes.ALL) assertFalse(name.equals(recipe.output));
        }
        String[] aliases = {"WolframSteel", "WolframCarbide", "Carbide", "HSLA", "HSLA-Spring-Steel",
                "HSLA-Tungsten-Alloy", "GalvanizedSteel"};
        for (String name : aliases) assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("gtqtcore:" + name));
        String[] actualAlloys = {"steel", "black_steel", "blue_steel", "red_steel", "vanadium_steel",
                "tungsten_steel", "tungsten_carbide", "titanium_gold", "tantalum_hafnium_carbide",
                "meteoric_steel", "meteoric_black_steel", "meteoric_blue_steel", "meteoric_red_steel"};
        for (String name : actualAlloys) assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
        String[] inheritedAliases = {"HSLA", "HSLA-Steel", "HSLA-Spring-Steel", "HSLA-Tungsten-Alloy"};
        for (String name : inheritedAliases) {
            assertEquals(1873, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(3134L, CrucibleMaterialPhaseData.boilingPoint(name));
        }
        assertEquals(3070, CrucibleMaterialPhaseData.knownMeltingPoint("WolframCarbide"));
        assertEquals(6270L, CrucibleMaterialPhaseData.boilingPoint("Carbide"));
        assertEquals(1910, CrucibleMaterialPhaseData.knownMeltingPoint("GalvanizedSteel"));
        assertEquals("steel", CrucibleSmeltingRule.find("GalvanizedSteel").target);
        assertEquals(GTValues.M, CrucibleSmeltingRule.find("GalvanizedSteel").convert(GTValues.M));
    }

    @Test
    void copperAndPreciousAlloysKeepGt6DefinitionRecipesAndAdditionalRecipes() {
        assertGt6AlloyRecipe("electrum", 2, new String[]{"silver", "gold"}, new long[]{1, 1});
        assertGt6AlloyRecipe("sterling_silver", 5, new String[]{"copper", "silver"}, new long[]{1, 4});
        assertGt6AlloyRecipe("rose_gold", 5, new String[]{"copper", "gold"}, new long[]{1, 4});
        assertGt6AlloyRecipe("angmallen", 2, new String[]{"gold", "wrought_iron"}, new long[]{1, 1});
        assertGt6AlloyRecipe("inductive_alloy", 2, new String[]{"gold", "redstone"}, new long[]{1, 1});
        assertGt6AlloyRecipe("cd_in_ag_alloy", 3, new String[]{"cadmium", "indium", "silver"},
                new long[]{1, 1, 1});
        assertGt6AlloyRecipe("brass", 4, new String[]{"copper", "zinc"}, new long[]{3, 1});
        assertGt6AlloyRecipe("cobalt_brass", 9, new String[]{"brass", "aluminium", "cobalt"},
                new long[]{7, 1, 1});
        assertGt6AlloyRecipe("bronze", 4, new String[]{"copper", "tin"}, new long[]{3, 1});
        assertGt6AlloyRecipe("black_bronze", 5, new String[]{"copper", "electrum"}, new long[]{3, 2});
        assertGt6AlloyRecipe("bismuth_bronze", 5, new String[]{"bismuth", "brass"}, new long[]{1, 4});
        assertGt6AlloyRecipe("hepatizon", 2, new String[]{"gold", "bronze"}, new long[]{1, 1});
        assertGt6AlloyRecipe("arsenic_copper", 4, new String[]{"copper", "arsenic"}, new long[]{3, 1});
        assertGt6AlloyRecipe("arsenic_bronze", 5, new String[]{"arsenic", "bronze"}, new long[]{1, 4});
        assertGt6AlloyRecipe("hepatizon", 24, new String[]{"bronze", "tin", "rose_gold"}, new long[]{8, 1, 15});
        assertGt6AlloyRecipe("rose_gold", 5, new String[]{"annealed_copper", "gold"}, new long[]{1, 4});
        assertGt6AlloyRecipe("sterling_silver", 5, new String[]{"annealed_copper", "silver"}, new long[]{1, 4});
        assertGt6AlloyRecipe("brass", 4, new String[]{"annealed_copper", "zinc"}, new long[]{3, 1});
        assertGt6AlloyRecipe("bronze", 4, new String[]{"annealed_copper", "tin"}, new long[]{3, 1});
        assertGt6AlloyRecipe("arsenic_copper", 4, new String[]{"annealed_copper", "arsenic"}, new long[]{3, 1});
        assertGt6AlloyRecipe("arsenic_bronze", 5, new String[]{"arsenic_copper", "tin"}, new long[]{4, 1});
        assertGt6AlloyRecipe("black_bronze", 5, new String[]{"annealed_copper", "electrum"}, new long[]{3, 2});
        assertGt6AlloyRecipe("black_bronze", 20, new String[]{"copper", "rose_gold", "silver"},
                new long[]{11, 5, 4});
        assertGt6AlloyRecipe("black_bronze", 20, new String[]{"annealed_copper", "rose_gold", "silver"},
                new long[]{11, 5, 4});
        assertGt6AlloyRecipe("black_bronze", 20, new String[]{"copper", "sterling_silver", "gold"},
                new long[]{11, 5, 4});
        assertGt6AlloyRecipe("black_bronze", 20, new String[]{"annealed_copper", "sterling_silver", "gold"},
                new long[]{11, 5, 4});
    }

    @Test
    void auditedCompositionOnlyMaterialsCannotBecomeInventedCrucibleRecipes() {
        String[] complete = {"electrum", "sterling_silver", "rose_gold", "angmallen", "inductive_alloy",
                "cd_in_ag_alloy", "brass", "cobalt_brass", "bronze", "black_bronze", "bismuth_bronze",
                "hepatizon", "arsenic_copper", "arsenic_bronze", "gilded_iron", "aluminium_alloy"};
        for (String name : complete) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(name));
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("gtqtcore:" + name));
        }
        for (GT6AlloyRecipes recipe : GT6AlloyRecipes.ALL) {
            assertFalse("gilded_iron".equals(recipe.output));
            assertFalse("aluminium_alloy".equals(recipe.output));
        }
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("Tumbaga"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("GoldInductive"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("AluminumAlloy"));
        assertEquals("rosegold", GT6MaterialIdentity.canonicalAlloyName("tumbaga"));
        assertEquals("inductivealloy", GT6MaterialIdentity.canonicalAlloyName("goldinductive"));
        assertEquals("unverifiedalloy", GT6MaterialIdentity.canonicalAlloyName("unverifiedalloy"));
    }

    @Test
    void industrialAlloyRecipesUseGt6InputsAndAutomaticDividers() {
        assertGt6AlloyRecipe("invar", 3, new String[]{"wrought_iron", "nickel"}, new long[]{2, 1});
        assertGt6AlloyRecipe("constantan", 2, new String[]{"copper", "nickel"}, new long[]{1, 1});
        assertGt6AlloyRecipe("nichrome", 5, new String[]{"nickel", "chrome"}, new long[]{4, 1});
        assertGt6AlloyRecipe("kanthal", 3, new String[]{"wrought_iron", "aluminium", "chrome"},
                new long[]{1, 1, 1});
        assertGt6AlloyRecipe("magnalium", 3, new String[]{"magnesium", "aluminium"}, new long[]{1, 2});
        assertGt6AlloyRecipe("stainless_steel", 9,
                new String[]{"wrought_iron", "invar", "chrome", "manganese"}, new long[]{4, 3, 1, 1});
        assertGt6AlloyRecipe("ultimet", 9, new String[]{"cobalt", "nickel", "chrome", "molybdenum"},
                new long[]{5, 1, 2, 1});
        assertGt6AlloyRecipe("tin_alloy", 2, new String[]{"tin", "wrought_iron"}, new long[]{1, 1});
        assertGt6AlloyRecipe("battery_alloy", 5, new String[]{"lead", "antimony"}, new long[]{4, 1});
        assertGt6AlloyRecipe("soldering_alloy", 10, new String[]{"tin", "antimony"}, new long[]{9, 1});
        assertGt6AlloyRecipe("osmiridium", 2, new String[]{"osmium", "iridium"}, new long[]{1, 1});
        assertGt6AlloyRecipe("vanadium_gallium", 4, new String[]{"vanadium", "gallium"}, new long[]{3, 1});
        assertGt6AlloyRecipe("niobium_titanium", 2, new String[]{"niobium", "titanium"}, new long[]{1, 1});
        assertGt6AlloyRecipe("aluminium_brass", 4, new String[]{"aluminium", "copper"}, new long[]{3, 1});
        // The four audited additional recipes must remain alongside the originals.
        assertGt6AlloyRecipe("ultimet", 36, new String[]{"cobalt", "nichrome", "chrome", "molybdenum"},
                new long[]{20, 5, 7, 4});
        assertGt6AlloyRecipe("stainless_steel", 36,
                new String[]{"wrought_iron", "nichrome", "chrome", "manganese"}, new long[]{24, 5, 3, 4});
        assertGt6AlloyRecipe("constantan", 2, new String[]{"annealed_copper", "nickel"}, new long[]{1, 1});
        assertGt6AlloyRecipe("aluminium_brass", 4, new String[]{"annealed_copper", "aluminium"},
                new long[]{1, 3});
    }

    @Test
    void completeIndustrialRecipesSuppressHostFallbackEvenWithoutMatchingInputs() {
        String[] outputs = {"invar", "constantan", "nichrome", "kanthal", "magnalium", "stainless_steel",
                "ultimet", "tin_alloy", "battery_alloy", "soldering_alloy", "osmiridium",
                "vanadium_gallium", "niobium_titanium", "aluminium_brass"};
        for (String output : outputs) {
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet(output));
            assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("gtqtcore:" + output));
        }
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("gtceu:cupronickel"));
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("AluminumBrass"));
        assertEquals("constantan", GT6MaterialIdentity.canonicalAlloyName("cupronickel"));
        assertEquals("aluminiumbrass", GT6MaterialIdentity.canonicalAlloyName("aluminumbrass"));
        // End Steel was subsequently ported from MT.EndSteel's explicit setAloy.
        assertTrue(GT6AlloyRecipes.hasCompleteRecipeSet("end_steel"));
        assertFalse(GT6AlloyRecipes.hasCompleteRecipeSet("unknown_alloy"));
        assertFalse(GT6AlloyRecipes.hasCompleteRecipeSet(null));
    }

    private static void assertGt6AlloyRecipe(String output, long amount, String[] inputs, long[] units) {
        GT6AlloyRecipes found = null;
        for (GT6AlloyRecipes recipe : GT6AlloyRecipes.ALL) {
            if (output.equals(recipe.output) && java.util.Arrays.equals(inputs, recipe.inputs)) {
                assertNull(found); // No duplicate candidate may override the verified yield.
                found = recipe;
            }
        }
        assertNotNull(found);
        assertEquals(amount, found.outputUnits);
        assertArrayEquals(units, found.inputUnits);
    }

    @Test
    void fictionalAlloyComponentsResolveVerifiedInheritedTemperatures() {
        String[] names = {"vulcanite", "celenegil", "shadow_steel", "inolashite", "haderoth",
                "desichalkos", "tartarite", "amordrine", "atlarus", "adamantium"};
        int[] melting = {1039, 1699, 1595, 1717, 1944, 2297, 4250, 1997, 3276, 5225};
        long[] boiling = {2048, 3466, 2746, 3434, 3896, 4595, 13026, 4315, 11524, 14528};
        for (int i = 0; i < names.length; i++) {
            assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(names[i]));
            assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(names[i]));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(names[i]));
        }
        assertEquals(1357, GT6InheritedPhaseData.meltingPoint("orichalcum"));
        assertEquals(2011, GT6InheritedPhaseData.meltingPoint("shadow_iron"));
        assertEquals(2041, GT6InheritedPhaseData.meltingPoint("mithril"));
        assertEquals(5225, GT6InheritedPhaseData.meltingPoint("adamantine"));
        assertEquals(1315, GT6InheritedPhaseData.meltingPoint("prometheum"));
        assertEquals(14528L, GT6InheritedPhaseData.boilingPoint("adamantine"));
        assertEquals(-1, CrucibleMaterialPhaseData.knownMeltingPoint("unverified_fictional_alloy"));
        assertEquals(Long.MAX_VALUE, CrucibleMaterialPhaseData.boilingPoint("unverified_fictional_alloy"));
    }

    @Test
    void specialAlloysAcceptVerifiedZeroHeatAndKeepSoulSandDefaults() {
        assertEquals(0, CrucibleMaterialPhaseData.knownMeltingPoint("magic"));
        assertEquals(0L, CrucibleMaterialPhaseData.boilingPoint("magic"));
        assertEquals(1023L, GT6DeclaredPhaseData.compositionPoint(new long[]{2046, 0}, new long[]{1, 1}, 1));
        assertEquals(1L, GT6DeclaredPhaseData.compositionPoint(new long[]{0}, new long[]{1}, 0));
        assertEquals(-1L, GT6DeclaredPhaseData.compositionPoint(new long[]{100, -1}, new long[]{1, 1}, 1));
        assertEquals(-1L, GT6DeclaredPhaseData.compositionPoint(new long[]{100, Long.MAX_VALUE}, new long[]{1, 1}, 1));
        String[] names = {"ironwood", "steeleaf", "alumite", "soularium", "clay_compound", "end_steel"};
        int[] melting = {1580, 1023, 1565, 1033, 1400, 1297};
        long[] boiling = {2816, 1567, 3780, 3012, 2800, 3846};
        for (int i = 0; i < names.length; i++) {
            assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(names[i]));
            assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(names[i]));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(names[i]));
        }
        assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint("soul_sand"));
        assertEquals(3000L, CrucibleMaterialPhaseData.boilingPoint("soul_sand"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("soul_sand"));
        assertEquals(1400, CrucibleMaterialPhaseData.knownMeltingPoint("crude_steel"));
        assertEquals(2800L, CrucibleMaterialPhaseData.boilingPoint("crude_steel"));
        assertEquals("steel", CrucibleSmeltingRule.find("steeleaf").target);
        assertEquals(GTValues.M / 4, CrucibleSmeltingRule.find("steeleaf").convert(GTValues.M));
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("aluminium") > melting[2]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("endstone") > melting[5]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("lava") > melting[5]);
    }

    @Test
    void redstoneAndGlowstoneAlloysPreserveDefinitionAndRecipeOrder() {
        String[] names = {"red_alloy", "blue_alloy", "purple_alloy", "mingrade", "electrotine_alloy",
                "redstone_alloy", "nikoline_alloy", "electrum_flux", "conductive_iron", "energetic_silver",
                "signalum", "lumium", "enderium_base", "enderium", "obsidian_steel", "pulsating_iron",
                "energetic_alloy", "vibrant_alloy"};
        int[] melting = {1400, 1400, 1400, 1400, 1400, 1093, 1593, 761, 1255, 744, 1353, 593, 1071, 1897, 1374, 2367, 580, 1750};
        long[] boiling = {2835, 2435, 2435, 2835, 3134, 2519, 3269, 1927, 2317, 1511, 2735, 1682, 3070, 3427, 3913, 3459, 1742, 2763};
        for (int i = 0; i < names.length; i++) {
            assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(names[i]));
            assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(names[i]));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(names[i]));
        }
        assertEquals(778L, GT6DeclaredPhaseData.compositionPoint(new long[]{918, 500}, new long[]{2, 1}, 1));
        // Later changes to a component's temperature do not retroactively
        // recompute a previously configured material's averaged temperature.
        assertEquals(1750L, GT6DeclaredPhaseData.compositionPoint(new long[]{778, 2723}, new long[]{1, 1}, 1));
        assertEquals(580, CrucibleMaterialPhaseData.knownMeltingPoint("energetic_alloy"));
        assertEquals(593, CrucibleMaterialPhaseData.knownMeltingPoint("lumium"));
        assertEquals(600L, CrucibleMaterialPhaseData.boilingPoint("glowstone_ceres"));
        assertEquals(1593, CrucibleMaterialPhaseData.knownMeltingPoint("teslatine_alloy"));
        assertEquals(1374, CrucibleMaterialPhaseData.knownMeltingPoint("dark_steel"));
        assertEquals(2367, CrucibleMaterialPhaseData.knownMeltingPoint("phased_iron"));
        for (String name : new String[]{"phased_gold", "vibrant"}) {
            assertEquals(1750, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(2763L, CrucibleMaterialPhaseData.boilingPoint(name));
        }
    }

    @Test
    void industrialAlloysUseGt6DefinitionsRatherThanHostComposition() {
        String[] names = {"invar", "constantan", "nichrome", "kanthal", "magnalium", "stainless_steel",
                "ultimet", "tin_alloy", "battery_alloy", "soldering_alloy", "osmiridium",
                "vanadium_gallium", "niobium_titanium", "aluminium_brass", "electrical_steel", "flamascus_steel"};
        int[] melting = {1916, 1542, 1818, 1708, 929, 1943, 1980, 1258, 660, 544, 3012, 1712, 2345, 1039, 1866, 2996};
        long[] boiling = {3151, 3010, 3137, 2956, 2315, 3029, 3331, 3004, 1989, 2773, 4993, 3379, 4288, 2802, 3336, 3696};
        for (int i = 0; i < names.length; i++) {
            assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(names[i]));
            assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(names[i]));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(names[i]));
        }
        assertEquals(1542, CrucibleMaterialPhaseData.knownMeltingPoint("cupronickel"));
        assertEquals(3010L, CrucibleMaterialPhaseData.boilingPoint("cupronickel"));
        assertEquals(1039, CrucibleMaterialPhaseData.knownMeltingPoint("aluminum_brass"));
        assertEquals(2802L, CrucibleMaterialPhaseData.boilingPoint("aluminum_brass"));
        // The extra recipe's Nichrome is an input, not a new thermal composition.
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("nichrome") > melting[5]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("manganese") > melting[5]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("chromium") > melting[6]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("molybdenum") > melting[6]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("meteoric_steel") > melting[14]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("annealed_copper") > melting[13]);
        assertEquals(2996L, CrucibleMaterialPhaseData.boilingPoint("damascus_steel") - 200);
        assertEquals(3696L, CrucibleMaterialPhaseData.boilingPoint("damascus_steel") + 500);
    }

    @Test
    void specialSteelsKeepNestedGt6HeatAndMeteoricVariants() {
        String[] names = {"black_steel", "blue_steel", "red_steel", "damascus_steel", "vanadium_steel",
                "tungsten_steel", "steel_galvanized", "titanium_gold", "meteoric_black_steel",
                "meteoric_blue_steel", "meteoric_red_steel", "meteoflame_black_steel",
                "meteoflame_blue_steel", "meteoflame_red_steel"};
        int[] melting = {1838, 1717, 1743, 2080, 2073, 2870, 1910, 1790, 1958, 1827, 1853, 3000, 2877, 2971};
        long[] boiling = {3080, 2967, 3061, 3196, 3243, 4481, 2938, 3452, 3200, 3077, 3171, 3700, 3577, 3671};
        for (int i = 0; i < names.length; i++) {
            assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(names[i]));
            assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(names[i]));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(names[i]));
        }
        assertEquals(2870, CrucibleMaterialPhaseData.knownMeltingPoint("WolframSteel"));
        assertEquals(4481L, CrucibleMaterialPhaseData.boilingPoint("WolframSteel"));
        assertEquals(3000, CrucibleMaterialPhaseData.boilingPoint("meteoric_black_steel") - 200);
        assertEquals(3577L, CrucibleMaterialPhaseData.boilingPoint("meteoric_blue_steel") + 500);
        assertEquals("steel", CrucibleSmeltingRule.find("steel_galvanized").target);
        assertEquals(GTValues.M, CrucibleSmeltingRule.find("steel_galvanized").convert(GTValues.M));
        // Later recipes cannot clamp heat: DeepIron/Infuscolium, MeteoricSteel,
        // Sintered Tungsten and Vanadium all boil above the output melting point.
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("deep_iron") > melting[0]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("infuscolium") > melting[0]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("meteoric_steel") > melting[5]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("tungsten_sintered") > melting[5]);
        assertTrue(CrucibleMaterialPhaseData.boilingPoint("vanadium") > melting[4]);
        // Do not overwrite these final steal/heat chains with their composition.
        assertEquals(1873, CrucibleMaterialPhaseData.knownMeltingPoint("spring_steel"));
        assertEquals(1873, CrucibleMaterialPhaseData.knownMeltingPoint("tungsten_alloy"));
    }

    @Test
    void commonAlloysKeepGt6CompositionHeatAndExplicitOverrides() {
        String[] names = {"electrum", "sterling_silver", "rose_gold", "angmallen", "gold_inductive",
                "Cd-In-Ag-Alloy", "gilded_iron", "cobalt_brass", "aluminium_alloy", "black_bronze",
                "bismuth_bronze", "hepatizon"};
        int[] melting = {1285, 1258, 1341, 1674, 918, 752, 1763, 1202, 949, 1328, 1036, 1347};
        long[] boiling = {2782, 2515, 3070, 3131, 2314, 1940, 3133, 2870, 2808, 2813, 2635, 2982};
        for (int i = 0; i < names.length; i++) {
            assertEquals(melting[i], GT6DeclaredPhaseData.meltingPoint(names[i]));
            assertEquals(boiling[i], GT6DeclaredPhaseData.boilingPoint(names[i]));
            assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(names[i]));
            assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(names[i]));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(names[i]));
        }
        assertEquals(1341, CrucibleMaterialPhaseData.knownMeltingPoint("tumbaga"));
        assertEquals(918, CrucibleMaterialPhaseData.knownMeltingPoint("inductive_alloy"));
        assertEquals(2314L, CrucibleMaterialPhaseData.boilingPoint("inductive_alloy"));
        // Explicit heat follows alloy composition: never replace it by a mean.
        assertEquals(1160, CrucibleMaterialPhaseData.knownMeltingPoint("brass"));
        assertEquals(1357, CrucibleMaterialPhaseData.knownMeltingPoint("bronze"));
        assertEquals(1070, CrucibleMaterialPhaseData.knownMeltingPoint("arsenic_copper"));
        // Coated iron has a composition but no new alloy recipe or changed target.
        assertEquals("iron", CrucibleSmeltingRule.find("gilded_iron").target);
        assertEquals(GTValues.M, CrucibleSmeltingRule.find("gilded_iron").convert(GTValues.M));
    }

    @Test
    void specialFluidNamesShareVerifiedGt6ThermalAndQuantityData() {
        String[] gases = {"hydrogen_sulfide", "hydrofluoric_acid", "nitric_oxide"};
        String[] gt6Names = {"hydrosulfuric_acid", "hydrogen_fluoride", "nitrogen_monoxide"};
        int[] melting = {191, 189, 100};
        long[] boiling = {213, 292, 200};
        for (int i = 0; i < gases.length; i++) {
            for (String name : new String[]{gases[i], gt6Names[i]}) {
                assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(name));
                assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(name));
                assertEquals(1000, CrucibleFluidUnits.fluidUnit(name));
                assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name));
            }
        }
        for (String name : new String[]{"water_distilled", "distilled_water"}) {
            assertEquals(273, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(373L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(name));
            assertEquals(1000, CrucibleFluidUnits.fluidUnit(name));
            assertEquals(1000.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 0));
        }
        assertEquals(1000.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("water_dirty", 0));
        assertEquals(275, CrucibleMaterialPhaseData.knownMeltingPoint("semiheavy_water"));
        assertEquals(374L, CrucibleMaterialPhaseData.boilingPoint("semiheavy_water"));
        assertEquals(277, CrucibleMaterialPhaseData.knownMeltingPoint("heavy_water"));
        assertEquals(375L, CrucibleMaterialPhaseData.boilingPoint("heavy_water"));
        assertEquals(280, CrucibleMaterialPhaseData.knownMeltingPoint("tritiated_water"));
        assertEquals(377L, CrucibleMaterialPhaseData.boilingPoint("tritiated_water"));
        for (String name : new String[]{"steam", "fresh_water", "holy_water", "sea_water", "water_dirty"}) {
            assertEquals(273, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(373L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name));
        }
        assertEquals(273, CrucibleMaterialPhaseData.knownMeltingPoint("hydrogen_peroxide"));
        assertEquals(423L, CrucibleMaterialPhaseData.boilingPoint("hydrogen_peroxide"));
    }

    @Test
    void lapisUsesGt6IntermediateMaterialTemperaturesRatherThanFlatteningAtoms() {
        String[] names = {"pyrite", "lazurite", "sodalite", "lapis"};
        int[] melting = {862, 1352, 1331, 1335};
        long[] boiling = {1522, 2218, 2206, 2221};
        for (int i = 0; i < names.length; i++) {
            assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(names[i]));
            assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(names[i]));
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(names[i]));
            // MT.java defines no positive smelting target or disabled target here.
            assertNull(CrucibleSmeltingRule.find(names[i]));
        }
        assertEquals(1335L, GT6DeclaredPhaseData.compositionPoint(
                new long[]{1352, 1331, 862, 1612}, new long[]{12, 2, 1, 1}, 0));
        assertEquals(2221L, GT6DeclaredPhaseData.compositionPoint(
                new long[]{2218, 2206, 1522, 3000}, new long[]{12, 2, 1, 1}, 0));
        // LapisLazuli is only a commented-out placeholder in MT.java:1453.
        assertEquals(-1, GT6DeclaredPhaseData.meltingPoint("lapis_lazuli"));
    }

    @Test
    void quartzMixturesAndDecorativeGemFactoriesKeepDistinctTargetsAndHeat() {
        for (String name : new String[]{"milky_quartz", "nether_quartz", "void_quartz", "sunny_quartz",
                "lavender_quartz", "red_quartz", "blaze_quartz", "smokey_quartz", "smoky_quartz",
                "quartz_smoky", "mana_quartz", "elven_quartz", "certus_quartz", "charged_certus_quartz"}) {
            assertEquals(1986, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(3220L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertEquals("silicon_dioxide", CrucibleSmeltingRule.find(name).target);
            assertEquals(GTValues.M, CrucibleSmeltingRule.find(name).convert(GTValues.M));
        }
        for (String name : new String[]{"black_quartz", "quartz_black"}) {
            assertEquals(2893, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(3760L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertEquals("silicon_dioxide", CrucibleSmeltingRule.find(name).target);
        }
        assertEquals(1490, CrucibleMaterialPhaseData.knownMeltingPoint("fluix"));
        assertEquals(2646L, CrucibleMaterialPhaseData.boilingPoint("fluix"));
        for (String name : new String[]{"red_jasper", "jasper", "ocean_jasper", "rainforest_jasper",
                "blue_jasper", "green_jasper", "yellow_jasper"}) {
            assertEquals(1927, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(3191L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertEquals("", CrucibleSmeltingRule.find(name).target);
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name));
        }
        for (String name : new String[]{"tiger_eye", "yellow_tiger_eye", "cats_eye", "green_tiger_eye",
                "dragon_eye", "red_tiger_eye", "hawks_eye", "blue_tiger_eye", "black_eye", "black_tiger_eye",
                "tiger_iron", "green_aventurine", "aventurine", "brown_aventurine", "yellow_aventurine",
                "black_aventurine", "blue_aventurine", "red_aventurine"}) {
            assertEquals(1986, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(3220L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertEquals("", CrucibleSmeltingRule.find(name).target);
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name));
        }
    }

    @Test
    void silicateGemsKeepExplicitGt6TemperaturesAndDisabledSmeltingAliases() {
        String[] gems = {"topaz", "blue_topaz", "tanzanite", "zanite", "amazonite", "alexandrite",
                "opal", "onyx_red", "onyx_black", "onyx", "sugilite", "peridot", "olivine", "amethyst", "dioptase"};
        int[] melting = {1431, 1431, 1736, 1736, 1853, 1319, 1986, 1986, 1986, 1986, 1644, 1525, 1525, 1951, 1023};
        long[] boiling = {2092, 2092, 2618, 2618, 2937, 2027, 3220, 3220, 3220, 3220, 2799, 2460, 2460, 3202, 1713};
        for (int i = 0; i < gems.length; i++) {
            assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(gems[i]));
            assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(gems[i]));
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(gems[i]);
            assertNotNull(rule);
            assertEquals("", rule.target);
            assertEquals(0, rule.convert(GTValues.M));
            assertFalse(CrucibleSmeltingRule.hasMeltingFlag(gems[i]));
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(gems[i]));
        }
    }

    @Test
    void sapphireImpuritiesAndBerylUseGt6HeatWhileKeepingExplicitYields() {
        String[] sapphires = {"ruby", "blue_sapphire", "green_sapphire", "purple_sapphire", "yellow_sapphire", "orange_sapphire"};
        int[] melting = {2317, 2256, 2108, 2318, 2294, 2180};
        long[] boiling = {3199, 3230, 2935, 3321, 3301, 3180};
        for (int i = 0; i < sapphires.length; i++) {
            assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(sapphires[i]));
            assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(sapphires[i]));
            assertEquals(3L * GTValues.M / 4, CrucibleSmeltingRule.find(sapphires[i]).convert(GTValues.M));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(sapphires[i]));
        }
        assertEquals(2041, CrucibleMaterialPhaseData.knownMeltingPoint("rutile"));
        assertEquals(3560L, CrucibleMaterialPhaseData.boilingPoint("rutile"));
        for (String name : new String[]{"emerald", "aquamarine", "morganite", "heliodor", "goshenite", "bixbite", "maxixe", "scarlet_emerald"}) {
            assertEquals(1803, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(2851L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertEquals(GTValues.M / 36, CrucibleSmeltingRule.find(name).convert(GTValues.M));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(name));
        }
        assertEquals(1814, CrucibleMaterialPhaseData.knownMeltingPoint("spinel"));
        assertEquals(2529L, CrucibleMaterialPhaseData.boilingPoint("spinel"));
        for (String name : new String[]{"balas_ruby", "fools_ruby"}) {
            // Its final composition replaces the earlier steal(Ruby) heat.
            assertEquals(785, CrucibleMaterialPhaseData.knownMeltingPoint(name));
            assertEquals(1087L, CrucibleMaterialPhaseData.boilingPoint(name));
            assertEquals("", CrucibleSmeltingRule.find(name).target);
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name));
        }
        assertEquals(2345, CrucibleMaterialPhaseData.knownMeltingPoint("saphire"));
        assertEquals("alumina", CrucibleSmeltingRule.find("saphire").target);
    }

    @Test
    void garnetTemperaturesDoNotOverrideInheritedDisabledSmelting() {
        String[] names = {"almandine", "grossular", "pyrope", "spessartine", "andradite", "uvarovite"};
        String[] aliases = {"garnet_red", "garnet_orange", "garnet_purple", "garnet", "garnet_yellow", "garnet_green"};
        int[] melting = {1759, 1655, 1626, 1715, 1258, 1295};
        long[] boiling = {2745, 2538, 2479, 2625, 2052, 2033};
        for (int i = 0; i < names.length; i++) {
            for (String name : new String[]{names[i], aliases[i]}) {
                assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(name));
                assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(name));
                CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
                assertNotNull(rule);
                assertEquals("", rule.target);
                assertEquals(0, rule.convert(GTValues.M));
                assertFalse(CrucibleSmeltingRule.hasMeltingFlag(name));
                assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name));
            }
        }
        assertEquals(1986, CrucibleMaterialPhaseData.knownMeltingPoint("flint"));
        assertEquals(2345, CrucibleMaterialPhaseData.knownMeltingPoint("sapphire"));
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("flint"));
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("sapphire"));
        assertEquals(1944, CrucibleMaterialPhaseData.knownMeltingPoint("diatomite"));
        assertEquals(3142L, CrucibleMaterialPhaseData.boilingPoint("diatomite"));
        assertEquals(1551, CrucibleMaterialPhaseData.knownMeltingPoint("garnet_sand"));
        assertEquals(2412L, CrucibleMaterialPhaseData.boilingPoint("garnet_sand"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("diatomite"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("garnet_sand"));
    }

    @Test
    void hydratedMineralsKeepGt6ComponentTemperaturesWithoutInventingMeltingFlags() {
        assertEquals(786, CrucibleMaterialPhaseData.knownMeltingPoint("perlite"));
        assertEquals(2186L, CrucibleMaterialPhaseData.boilingPoint("perlite"));
        assertEquals(698, CrucibleMaterialPhaseData.knownMeltingPoint("trona"));
        assertEquals(1310L, CrucibleMaterialPhaseData.boilingPoint("trona"));
        assertEquals(440, CrucibleMaterialPhaseData.knownMeltingPoint("mirabilite"));
        assertEquals(624L, CrucibleMaterialPhaseData.boilingPoint("mirabilite"));
        assertEquals(511, CrucibleMaterialPhaseData.knownMeltingPoint("bischofite"));
        assertEquals(810L, CrucibleMaterialPhaseData.boilingPoint("bischofite"));
        assertEquals(2210, CrucibleMaterialPhaseData.knownMeltingPoint("kyanite"));
        assertEquals(3238L, CrucibleMaterialPhaseData.boilingPoint("kyanite"));
        for (String mineral : new String[]{"perlite", "trona", "mirabilite", "bischofite", "borax",
                "spodumene", "lepidolite", "glauconite", "glauconite_sand", "vermiculite", "mica",
                "kyanite", "alunite"}) {
            assertTrue(CrucibleMaterialPhaseData.hasKnownMeltingPoint(mineral));
            assertTrue(CrucibleMaterialPhaseData.boilingPoint(mineral) < Long.MAX_VALUE);
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(mineral));
        }
        assertEquals(GT6DeclaredPhaseData.meltingPoint("glauconite"),
                GT6DeclaredPhaseData.meltingPoint("glauconite_sand"));
        assertEquals(GT6DeclaredPhaseData.boilingPoint("glauconite"),
                GT6DeclaredPhaseData.boilingPoint("glauconite_sand"));
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("water"));
        assertEquals(273, GT6DeclaredPhaseData.meltingPoint("water"));
        assertEquals(373L, GT6DeclaredPhaseData.boilingPoint("water"));
    }

    @Test
    void splitLegacyHorseTypesKeepGt6CrucibleProductsWithoutIncludingLlamas() {
        java.util.List<Class<? extends net.minecraft.entity.EntityLivingBase>> horses = java.util.Arrays.asList(
                net.minecraft.entity.passive.EntityHorse.class,
                net.minecraft.entity.passive.EntityDonkey.class,
                net.minecraft.entity.passive.EntityMule.class,
                net.minecraft.entity.passive.EntityZombieHorse.class,
                net.minecraft.entity.passive.EntitySkeletonHorse.class);
        for (Class<? extends net.minecraft.entity.EntityLivingBase> horse : horses) {
            CrucibleEntityRecycling.Rule rule = CrucibleEntityRecycling.rule(horse, null);
            assertNotNull(rule);
            assertArrayEquals(new String[]{"meatraw"}, rule.materials);
            assertArrayEquals(new int[]{3}, rule.units);
            assertEquals(310, rule.temperature);
            assertEquals(1, rule.repetitions);
        }
        assertNull(CrucibleEntityRecycling.rule(net.minecraft.entity.passive.EntityLlama.class, null));
    }

    @Test
    void entityProductMaterialsUseVerifiedGt6PhaseRulesAndDensity() {
        CrucibleSmeltingRule snow = CrucibleSmeltingRule.find("snow");
        assertNotNull(snow);
        assertEquals("water", snow.target);
        assertEquals(GTValues.M, snow.convert(GTValues.M));
        assertEquals(273, snow.meltingPoint);
        assertEquals(373, snow.boilingPoint);
        assertEquals(273, CrucibleMaterialPhaseData.knownMeltingPoint("snow"));
        assertEquals(373L, CrucibleMaterialPhaseData.boilingPoint("snow"));
        CrucibleSmeltingRule raw = CrucibleSmeltingRule.find("meat_raw");
        assertNotNull(raw);
        assertEquals("meat_cooked", raw.target);
        assertEquals(GTValues.M, raw.convert(GTValues.M));
        for (String material : new String[]{"meat_raw", "meat_cooked", "meat_rotten"}) {
            assertEquals(477, CrucibleMaterialPhaseData.knownMeltingPoint(material));
            assertEquals(550L, CrucibleMaterialPhaseData.boilingPoint(material));
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(material));
            assertEquals(1000.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(material, 0));
        }
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("fish_cooked"));
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("fish_rotten"));
        assertEquals(422, CrucibleMaterialPhaseData.knownMeltingPoint("soylent_green"));
        assertEquals(500L, CrucibleMaterialPhaseData.boilingPoint("soylent_green"));
        assertEquals(1000.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("soylent_green", 0));
        assertEquals(1000.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("snow", 0));
    }

    @Test
    void gt6ContactDeathProductsRetainExplicitMaterialQuantitiesAndTemperatures() {
        CrucibleEntityRecycling.Rule golem = CrucibleEntityRecycling.rule(
                net.minecraft.entity.monster.EntityIronGolem.class, null);
        assertNotNull(golem);
        assertArrayEquals(new String[]{"iron"}, golem.materials);
        assertArrayEquals(new int[]{4}, golem.units);
        assertEquals(-1, golem.temperature);
        CrucibleEntityRecycling.Rule wither = CrucibleEntityRecycling.rule(
                net.minecraft.entity.monster.EntityWitherSkeleton.class, null);
        assertNotNull(wither);
        assertArrayEquals(new String[]{"bone", "coal"}, wither.materials);
        assertArrayEquals(new int[]{1, 1}, wither.units);
        CrucibleEntityRecycling.Rule cow = CrucibleEntityRecycling.rule(
                net.minecraft.entity.passive.EntityMooshroom.class, null);
        assertNotNull(cow);
        assertArrayEquals(new String[]{"meatraw"}, cow.materials);
        assertEquals(3, cow.units[0]);
        assertEquals(310, cow.temperature);
        CrucibleEntityRecycling.Rule creeper = CrucibleEntityRecycling.rule(
                net.minecraft.entity.monster.EntityCreeper.class, null);
        assertNotNull(creeper);
        assertEquals("gunpowder", creeper.materials[0]);
        assertEquals(293, creeper.temperature);
        assertNull(CrucibleEntityRecycling.rule(net.minecraft.entity.passive.EntityBat.class, null));
        assertNull(CrucibleEntityRecycling.rule(net.minecraft.entity.player.EntityPlayer.class, "ordinary_player"));
        CrucibleEntityRecycling.Rule greg = CrucibleEntityRecycling.rule(
                net.minecraft.entity.player.EntityPlayer.class, "gregoriust");
        assertNotNull(greg);
        assertEquals("technetium", greg.materials[0]);
        assertEquals(1, greg.units[0]);
        assertEquals(16, greg.repetitions);
    }

    @Test
    void onlyADeathCausedByHotContactCanProduceGt6EntityMaterials() {
        assertTrue(CrucibleEntityRecycling.shouldRecycleDeath(true, false, 321));
        assertFalse(CrucibleEntityRecycling.shouldRecycleDeath(false, false, 1000));
        assertFalse(CrucibleEntityRecycling.shouldRecycleDeath(true, true, 1000));
        assertFalse(CrucibleEntityRecycling.shouldRecycleDeath(true, false, 320));
        assertFalse(CrucibleEntityRecycling.shouldRecycleDeath(true, false, 259));
    }

    @Test
    void contactAndRetrievalTemperatureDamageIncludesGt6FrostBoundaries() {
        assertEquals(0.0F, CrucibleTransferLogic.contactTemperatureDamage(260, 10));
        assertEquals(0.0F, CrucibleTransferLogic.contactTemperatureDamage(320, 10));
        assertEquals(1.0F, CrucibleTransferLogic.contactTemperatureDamage(259, 10));
        assertEquals(1.0F, CrucibleTransferLogic.contactTemperatureDamage(321, 10));
        assertEquals(2.0F, CrucibleTransferLogic.contactTemperatureDamage(220, 10));
        assertEquals(2.0F, CrucibleTransferLogic.contactTemperatureDamage(400, 10));
        assertEquals(10.0F, CrucibleTransferLogic.contactTemperatureDamage(0, 10));
        assertEquals(5.0F, CrucibleTransferLogic.contactTemperatureDamage(0, 5));
        assertEquals(10.0F, CrucibleTransferLogic.contactTemperatureDamage(1000, 10));
        assertEquals(5.0F, CrucibleTransferLogic.contactTemperatureDamage(1000, 5));
        assertEquals(5.0F, CrucibleTransferLogic.contactTemperatureDamage(Long.MIN_VALUE, 5));
        assertEquals(5.0F, CrucibleTransferLogic.contactTemperatureDamage(Long.MAX_VALUE, 5));
        assertEquals(0.0F, CrucibleTransferLogic.contactTemperatureDamage(0, 0));
        assertEquals(0.0F, CrucibleTransferLogic.contactTemperatureDamage(0, Float.NaN));
    }

    @Test
    void liquidInputTemperatureUsesGt6PhaseBoundsWithoutGuessingMissingData() {
        assertEquals(298, CrucibleTransferLogic.fluidInputTemperature(250, 273, 373));
        assertEquals(300, CrucibleTransferLogic.fluidInputTemperature(300, 273, 373));
        assertEquals(372, CrucibleTransferLogic.fluidInputTemperature(1000, 273, 373));
        assertEquals(1325, CrucibleTransferLogic.fluidInputTemperature(1300, 1300, 4000));
        assertEquals(3999, CrucibleTransferLogic.fluidInputTemperature(10000, 1300, 4000));
        // Narrow phase windows are swapped by GT6 UT.Code.bind, not collapsed.
        assertEquals(19, CrucibleTransferLogic.fluidInputTemperature(0, 14, 20));
        assertEquals(25, CrucibleTransferLogic.fluidInputTemperature(25, 14, 20));
        assertEquals(39, CrucibleTransferLogic.fluidInputTemperature(100, 14, 20));
        assertEquals(5000, CrucibleTransferLogic.fluidInputTemperature(5000, -1, 6000));
        assertEquals(5000, CrucibleTransferLogic.fluidInputTemperature(5000, 1000, Long.MAX_VALUE));
        assertEquals(5000, CrucibleTransferLogic.fluidInputTemperature(5000, 1000, 0));
        assertEquals((long) Integer.MAX_VALUE + 25L,
                CrucibleTransferLogic.fluidInputTemperature(Long.MAX_VALUE, Integer.MAX_VALUE,
                        (long) Integer.MAX_VALUE + 5L));
    }

    @Test
    void declaredElementAndCompoundGasesUseTheirGt6GasUnit() {
        for (String material : new String[]{"hydrogen", "deuterium", "tritium", "helium", "helium_3",
                "nitrogen", "oxygen", "fluorine", "neon", "chlorine", "argon", "krypton", "xenon", "radon",
                "helium-neon", "air", "nitrogen_monoxide", "nitrogen_dioxide", "ammonia", "carbon_monoxide",
                "carbon_dioxide", "carbon_trioxide", "methane", "sulfur_dioxide", "sulfur_trioxide",
                "hydrosulfuric_acid", "hydrochloric_acid", "hydrogen_fluoride",
                "hydrogen_sulfide", "hydrofluoric_acid", "nitric_oxide"}) {
            assertEquals(1000, CrucibleFluidUnits.fluidUnit(material));
            assertEquals(GTValues.M, CrucibleFluidUnits.materialAmount(1000,
                    CrucibleFluidUnits.fluidUnit(material)));
        }
        // KU is already accounted in material units (KU*M/1000), not metal mB.
        assertEquals(1000, CrucibleFluidUnits.fluidAmount(GTValues.M, 0,
                CrucibleFluidUnits.fluidUnit("air")));
        assertEquals(GTValues.L, CrucibleFluidUnits.fluidUnit("iron"));
        assertEquals(GTValues.L, CrucibleFluidUnits.fluidUnit("plasma.iron"));
    }

    @Test
    void explicitGt6FluidBindingsDoNotUseMetalBucketUnits() {
        for (String material : new String[]{"propane", "butane", "propylene", "ethylene", "waterdistilled", "distilled_water",
                "nitrofuel", "oil", "fuel", "kerosine", "diesel", "petrol", "biomass", "ethanol",
                "frying_oil_hot", "plastic"}) {
            assertEquals(1000, CrucibleFluidUnits.fluidUnit(material));
            assertEquals(16000, CrucibleFluidUnits.fluidAmount(16L * GTValues.M, 0,
                    CrucibleFluidUnits.fluidUnit(material)));
        }
        for (String material : new String[]{"pyrotheum", "cryotheum", "petrotheum", "aerotheum"}) {
            assertEquals(250, CrucibleFluidUnits.fluidUnit(material));
            assertEquals(4000, CrucibleFluidUnits.fluidAmount(16L * GTValues.M, 0, 250));
        }
        assertEquals(160000, CrucibleFluidUnits.fluidUnit("steam"));
        assertEquals(2560000, CrucibleFluidUnits.fluidAmount(16L * GTValues.M, 0, 160000));
        // Plasma is not a material name: its unit must eventually be selected by
        // the actual fluid representation, not applied to all forms of iron.
        assertEquals(GTValues.L, CrucibleFluidUnits.fluidUnit("iron"));
        assertEquals(GTValues.L, CrucibleFluidUnits.fluidUnit("unknown_material"));
    }

    @Test
    void specialFluidUnitsRetainExactQuantityAcrossSmallFillsAndDrains() {
        for (int unit : new int[]{250, 504, 1000, 160000}) {
            CrucibleFluidUnits.Quantity accumulated = new CrucibleFluidUnits.Quantity(0, 0);
            CrucibleFluidUnits.Quantity one = CrucibleFluidUnits.exactFluidAmount(1, unit);
            for (int i = 0; i < 1001; i++) {
                accumulated = CrucibleFluidUnits.merge(accumulated.amount, accumulated.remainder,
                        one.amount, one.remainder, unit);
                assertNotNull(accumulated);
            }
            CrucibleFluidUnits.Quantity bulk = CrucibleFluidUnits.exactFluidAmount(1001, unit);
            assertEquals(bulk.amount, accumulated.amount);
            assertEquals(bulk.remainder, accumulated.remainder);
            assertEquals(1001, CrucibleFluidUnits.fluidAmount(accumulated.amount, accumulated.remainder, unit));
            for (int i = 0; i < 1001; i++) {
                accumulated = CrucibleFluidUnits.drain(accumulated.amount, accumulated.remainder, 1, unit);
                assertNotNull(accumulated);
            }
            assertEquals(0, accumulated.amount);
            assertEquals(0, accumulated.remainder);
        }
    }

    @Test
    void shapelessCraftingToolsAreExcludedOnlyWhenTheRecipeReturnsThem() {
        assertTrue(CrucibleToolRecycling.isReturnedCraftingTool("toolHammer", false));
        assertTrue(CrucibleToolRecycling.isReturnedCraftingTool("craftingToolFile", false));
        assertFalse(CrucibleToolRecycling.isReturnedCraftingTool("toolHammer", true));
        assertFalse(CrucibleToolRecycling.isReturnedCraftingTool("craftingToolFile", true));
        assertFalse(CrucibleToolRecycling.isReturnedCraftingTool("plateIron", false));
        assertFalse(CrucibleToolRecycling.isReturnedCraftingTool(null, false));
    }

    @Test
    void basinLavaCastingConsumesAFullBucketAndKeepsTheRemainder() {
        long basinCapacity = 9L * GTValues.M;
        assertEquals(1296, CrucibleFluidUnits.fluidAmount(basinCapacity, 0, GTValues.L));
        long bucketCost = CrucibleFluidUnits.materialAmount(1000, GTValues.L);
        assertEquals(1, basinCapacity / bucketCost);
        assertEquals(0, GTValues.M / bucketCost); // 144 mB is not one obsidian.
        CrucibleFluidUnits.Quantity remainder = CrucibleFluidUnits.drain(basinCapacity, 0, 1000, GTValues.L);
        assertNotNull(remainder);
        assertEquals(basinCapacity - bucketCost, remainder.amount);
        assertEquals(296, CrucibleFluidUnits.fluidAmount(remainder.amount, remainder.remainder, GTValues.L));
        assertEquals(0, remainder.amount / bucketCost);
        assertNull(CrucibleFluidUnits.drain(remainder.amount, remainder.remainder, 1000, GTValues.L));
        assertEquals(4536, CrucibleFluidUnits.fluidAmount(basinCapacity, 0, 504));
    }

    @Test
    void crucibleAndCastingReceiversShareAuthoritativeMeltingTemperatureSelection() {
        assertEquals(2345, CrucibleMaterialPhaseData.knownMeltingPoint("alumina"));
        assertEquals(662, CrucibleMaterialPhaseData.knownMeltingPoint("vanadium_pentoxide"));
        assertEquals(1811, CrucibleMaterialPhaseData.knownMeltingPoint("iron"));
        assertEquals(1873, CrucibleMaterialPhaseData.knownMeltingPoint("spring_steel"));
        assertEquals(273, CrucibleMaterialPhaseData.knownMeltingPoint("ice"));
        assertEquals(1070, CrucibleMaterialPhaseData.knownMeltingPoint("arsenic_copper"));
        assertEquals(-1, CrucibleMaterialPhaseData.knownMeltingPoint("unknown_material"));
        assertEquals(-1, CrucibleMaterialPhaseData.knownMeltingPoint(null));
    }

    @Test
    void verifiedMeltingTemperatureDoesNotRequireAForgeFluidRegistration() {
        assertTrue(CrucibleMaterialPhaseData.hasKnownMeltingPoint("alumina"));
        assertTrue(CrucibleMaterialPhaseData.hasKnownMeltingPoint("tungsten_trioxide"));
        assertTrue(CrucibleMaterialPhaseData.hasKnownMeltingPoint("vanadium_pentoxide"));
        assertTrue(CrucibleMaterialPhaseData.hasKnownMeltingPoint("spring_steel"));
        assertTrue(CrucibleMaterialPhaseData.hasKnownMeltingPoint("iron"));
        assertTrue(CrucibleMaterialPhaseData.hasKnownMeltingPoint("ice"));
        assertFalse(CrucibleMaterialPhaseData.hasKnownMeltingPoint("unknown_material"));
        assertFalse(CrucibleMaterialPhaseData.hasKnownMeltingPoint(null));
        // Disabled smelting remains a separate target gate even if heat is known.
        assertEquals("", CrucibleSmeltingRule.find("spinel").target);
    }

    @Test
    void tungstenMolybdenumAndUraniumMineralTemperaturesKeepGt6IdentityAndFlags() {
        assertEquals(504, GT6DeclaredPhaseData.meltingPoint("uraninite"));
        assertEquals(1528, GT6DeclaredPhaseData.boilingPoint("uraninite"));
        assertEquals(845, GT6DeclaredPhaseData.meltingPoint("pitchblende"));
        assertEquals(2333, GT6DeclaredPhaseData.boilingPoint("pitchblende"));
        assertEquals(1273, GT6DeclaredPhaseData.meltingPoint("stolzite"));
        assertEquals(1665, GT6DeclaredPhaseData.boilingPoint("stolzite"));
        assertEquals(704, GT6DeclaredPhaseData.meltingPoint("powellite"));
        assertEquals(1171, GT6DeclaredPhaseData.boilingPoint("powellite"));
        assertEquals(978, GT6DeclaredPhaseData.meltingPoint("tantalum_pentoxide"));
        assertEquals(1701, GT6DeclaredPhaseData.boilingPoint("tantalum_pentoxide"));
        assertEquals(956, GT6DeclaredPhaseData.meltingPoint("tantalite"));
        assertEquals(1780, GT6DeclaredPhaseData.boilingPoint("tantalite"));
        assertEquals(822, GT6DeclaredPhaseData.meltingPoint("columbite"));
        assertEquals(1601, GT6DeclaredPhaseData.boilingPoint("columbite"));
        assertEquals(889, GT6DeclaredPhaseData.meltingPoint("coltan"));
        assertEquals(1690, GT6DeclaredPhaseData.boilingPoint("coltan"));
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("stolzite"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("scheelite"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("tantalum_pentoxide"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("tantalite"));
        assertEquals(2800, GT6DeclaredPhaseData.meltingPoint("bauxite"));
    }

    @Test
    void gt6MineralTemperaturesUseVerifiedComponentsAndTruncationOrder() {
        assertEquals(662, GT6DeclaredPhaseData.meltingPoint("vanadium_pentoxide"));
        assertEquals(1115, GT6DeclaredPhaseData.boilingPoint("vanadium_pentoxide"));
        assertEquals(1236, GT6DeclaredPhaseData.meltingPoint("ferrovanadium"));
        assertEquals(2368, GT6DeclaredPhaseData.boilingPoint("ferrovanadium"));
        assertEquals(891, GT6DeclaredPhaseData.meltingPoint("garnierite"));
        assertEquals(1638, GT6DeclaredPhaseData.boilingPoint("garnierite"));
        assertEquals(986, GT6DeclaredPhaseData.meltingPoint("chalcopyrite"));
        assertEquals(1850, GT6DeclaredPhaseData.boilingPoint("chalcopyrite"));
        assertEquals(784, GT6DeclaredPhaseData.meltingPoint("galena"));
        assertEquals(1850, GT6DeclaredPhaseData.boilingPoint("galena"));
        assertEquals(1097, GT6DeclaredPhaseData.meltingPoint("pentlandite"));
        assertEquals(2024, GT6DeclaredPhaseData.boilingPoint("pentlandite"));
        // Stibnite's explicit heat(823) must not be replaced by a composition mean.
        assertEquals(823, GT6DeclaredPhaseData.meltingPoint("stibnite"));
        assertEquals(1646, GT6DeclaredPhaseData.boilingPoint("stibnite"));
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("vanadium_pentoxide"));
        assertEquals(-1, GT6DeclaredPhaseData.compositionPoint(new long[]{100}, new long[]{0}, 0));
        assertEquals(-1, GT6DeclaredPhaseData.compositionPoint(new long[]{Long.MAX_VALUE}, new long[]{1}, 0));
        assertEquals(-1, GT6DeclaredPhaseData.compositionPoint(new long[]{100}, new long[]{Long.MAX_VALUE}, 1));
        assertEquals(-1, GT6DeclaredPhaseData.compositionPoint(new long[]{100}, new long[]{1, 2}, 0));
    }

    @Test
    void aluminaUsesItsExplicitGt6MoltenUnitWithoutChangingSolidMaterialAmount() {
        assertEquals(504, CrucibleFluidUnits.fluidUnit("Alumina"));
        assertEquals(GTValues.M, CrucibleFluidUnits.materialAmount(504, 504));
        CrucibleFluidUnits.Quantity filled = CrucibleFluidUnits.exactFluidAmount(504, 504);
        assertEquals(GTValues.M, filled.amount);
        assertEquals(0, filled.remainder);
        assertEquals(504, CrucibleFluidUnits.fluidAmount(GTValues.M, 0, 504));
        CrucibleFluidUnits.Quantity single = CrucibleFluidUnits.exactFluidAmount(1, 504);
        assertEquals(7200, single.amount);
        assertEquals(0, single.remainder);
        assertEquals(8064, CrucibleFluidUnits.fluidAmount(16 * GTValues.M, 0, 504));
        CrucibleFluidUnits.Quantity emptied = CrucibleFluidUnits.drain(filled.amount, filled.remainder, 504, 504);
        assertNotNull(emptied);
        assertEquals(0, emptied.amount);
        assertEquals(0, emptied.remainder);
        assertEquals(144, CrucibleFluidUnits.fluidUnit("unknown_liquid"));
        assertEquals(144, CrucibleFluidUnits.fluidUnit(null));
    }

    @Test
    void waterFluidUnitsAndLegacyMigrationPreserveWholeBuckets() {
        assertEquals(1000, CrucibleFluidUnits.fluidUnit("water"));
        assertEquals(1000, CrucibleFluidUnits.fluidUnit("ice"));
        assertEquals(144, CrucibleFluidUnits.fluidUnit("iron"));
        assertEquals(144, CrucibleFluidUnits.fluidUnit("lava"));
        assertEquals(GTValues.M, CrucibleFluidUnits.materialAmount(1000, 1000));
        assertEquals(16 * GTValues.M, CrucibleFluidUnits.materialAmount(16000, 1000));
        assertEquals(1000, CrucibleTransferLogic.materialFluidAmount(GTValues.M, GTValues.M, 1000));
        assertEquals(1, CrucibleTransferLogic.materialFluidAmount(
                CrucibleFluidUnits.materialAmount(1, 1000), GTValues.M, 1000));
        long oldBucket = CrucibleFluidUnits.materialAmount(1000, 144);
        assertEquals(GTValues.M, CrucibleFluidUnits.exactLegacyWaterAmount(oldBucket).amount);
        CrucibleFluidUnits.Quantity migratedUnit = CrucibleFluidUnits.exactLegacyWaterAmount(GTValues.M);
        assertEquals(144, CrucibleFluidUnits.fluidAmount(migratedUnit.amount, migratedUnit.remainder, 1000));
        assertTrue(CrucibleFluidUnits.exactLegacyWaterAmount(Long.MAX_VALUE).amount > 0);
        assertEquals(0, CrucibleFluidUnits.materialAmount(0, 1000));
    }

    @Test
    void repeatedSingleMillibucketTransfersPreserveExactQuantity() {
        CrucibleFluidUnits.Quantity single = CrucibleFluidUnits.exactFluidAmount(1, 1000);
        CrucibleFluidUnits.Quantity stored = CrucibleFluidUnits.exactFluidAmount(0, 1000);
        for (int i = 0; i < 20000; i++) {
            stored = CrucibleFluidUnits.merge(stored.amount, stored.remainder,
                    single.amount, single.remainder, 1000);
            assertNotNull(stored);
        }
        CrucibleFluidUnits.Quantity bulk = CrucibleFluidUnits.exactFluidAmount(20000, 1000);
        assertEquals(bulk.amount, stored.amount);
        assertEquals(bulk.remainder, stored.remainder);
        assertEquals(20000, CrucibleFluidUnits.fluidAmount(stored.amount, stored.remainder, 1000));
        for (int i = 0; i < 20000; i++) {
            stored = CrucibleFluidUnits.drain(stored.amount, stored.remainder, 1, 1000);
            assertNotNull(stored);
        }
        assertEquals(0, stored.amount);
        assertEquals(0, stored.remainder);
    }

    @Test
    void fractionalLegacyWaterAndInvalidTransfersAreNotRoundedOrConsumed() {
        CrucibleFluidUnits.Quantity legacy = CrucibleFluidUnits.exactLegacyWaterAmount(1);
        assertEquals(0, legacy.amount);
        assertEquals(144, legacy.remainder);
        assertNull(CrucibleFluidUnits.drain(legacy.amount, legacy.remainder, 1, 1000));
        assertNull(CrucibleFluidUnits.merge(Long.MAX_VALUE, 999, 0, 1, 1000));
        assertNull(CrucibleFluidUnits.merge(-1, 0, 2, 0, 1000));
        assertNull(CrucibleFluidUnits.merge(1, 1000, 0, 0, 1000));
        assertNull(CrucibleFluidUnits.drain(1, 0, -1, 1000));
        assertEquals(0, CrucibleFluidUnits.exactFluidAmount(144, 144).remainder);
    }

    @Test
    void waterAndIcePhaseBoundariesKeepMaterialQuantity() {
        assertTrue(CrucibleTransferLogic.shouldFreezeWater(272));
        assertFalse(CrucibleTransferLogic.shouldFreezeWater(273));
        assertFalse(CrucibleTransferLogic.shouldFreezeWater(372));
        assertFalse(CrucibleTransferLogic.shouldBoilWater(372));
        assertTrue(CrucibleTransferLogic.shouldBoilWater(373));
        assertEquals("water", CrucibleSmeltingRule.find("ice").target);
        assertEquals(273, CrucibleSmeltingRule.find("ice").meltingPoint);
        assertEquals(373L, CrucibleMaterialPhaseData.boilingPoint("ice"));
        assertEquals(GTValues.M, CrucibleSmeltingRule.find("ice").convert(GTValues.M));
        assertEquals(1L, CrucibleSmeltingRule.find("ice").convert(1L));
        assertEquals(Long.MAX_VALUE, CrucibleSmeltingRule.find("ice").convert(Long.MAX_VALUE));
        assertEquals(273, CrucibleSmeltingRule.find("water").meltingPoint);
    }

    @Test
    void contentMergeChecksPreserveSeparateEntriesInsteadOfOverflowOrClipping() {
        assertTrue(CrucibleTransferLogic.canMergeMaterialAmounts(0, 1));
        assertTrue(CrucibleTransferLogic.canMergeMaterialAmounts(Long.MAX_VALUE - 1, 1));
        assertFalse(CrucibleTransferLogic.canMergeMaterialAmounts(Long.MAX_VALUE, 1));
        assertFalse(CrucibleTransferLogic.canMergeMaterialAmounts(Long.MAX_VALUE - 1, 2));
        assertFalse(CrucibleTransferLogic.canMergeMaterialAmounts(-1, 1));
        assertFalse(CrucibleTransferLogic.canMergeMaterialAmounts(1, -1));
        assertFalse(CrucibleTransferLogic.canMergeMaterialAmounts(1, 0));
        assertEquals(Long.MAX_VALUE, CrucibleTransferLogic.saturatingMaterialSum(Long.MAX_VALUE - 1, 2));
    }

    @Test
    void fireEffectCountKeepsExistingBoundWithoutOverflowingLargeContents() {
        assertEquals(0, CrucibleTransferLogic.boundedMaterialEffectCount(0, GTValues.M, 64));
        assertEquals(0, CrucibleTransferLogic.boundedMaterialEffectCount(-1, GTValues.M, 64));
        assertEquals(1, CrucibleTransferLogic.boundedMaterialEffectCount(1, GTValues.M, 64));
        assertEquals(9, CrucibleTransferLogic.boundedMaterialEffectCount(GTValues.M, GTValues.M, 64));
        assertEquals(64, CrucibleTransferLogic.boundedMaterialEffectCount(Long.MAX_VALUE, GTValues.M, 64));
        assertEquals(0, CrucibleTransferLogic.boundedMaterialEffectCount(1, 0, 64));
    }

    @Test
    void materialHazardFireCountMatchesGt6ThroughTheFullSixteenUnitCapacity() {
        long capacity = 16L * GTValues.M;
        assertEquals(0, CrucibleTransferLogic.materialHazardFireAttempts(0, GTValues.M, capacity));
        assertEquals(1, CrucibleTransferLogic.materialHazardFireAttempts(1, GTValues.M, capacity));
        assertEquals(9, CrucibleTransferLogic.materialHazardFireAttempts(GTValues.M, GTValues.M, capacity));
        assertEquals(72, CrucibleTransferLogic.materialHazardFireAttempts(8L * GTValues.M, GTValues.M, capacity));
        assertEquals(144, CrucibleTransferLogic.materialHazardFireAttempts(capacity, GTValues.M, capacity));
        assertEquals(144, CrucibleTransferLogic.materialHazardFireAttempts(Long.MAX_VALUE, GTValues.M, capacity));
        assertEquals(0, CrucibleTransferLogic.materialHazardFireAttempts(1, 0, capacity));
    }

    @Test
    void numericMaterialOverridesUseSnapshotIdsAndNumericIdentityTakesPrecedence() {
        assertEquals("phosphorus", GT6MaterialIdentity.canonicalElementName("phosphor"));
        assertEquals("lanthanum", GT6MaterialIdentity.canonicalElementName("lanthanium"));
        assertEquals("cesium", GT6MaterialIdentity.canonicalElementName("caesium"));
        assertEquals("osmium", GT6MaterialIdentity.canonicalElementName("osmiumelemental"));
        assertEquals("phosphorite", GT6MaterialIdentity.canonicalElementName("phosphorite"));
        assertEquals("phosphorus", GT6MaterialIdentity.canonicalElementName("phosphorus"));
        assertEquals("Iron", GT6MaterialIdentity.name(260));
        assertEquals("Copper", GT6MaterialIdentity.name(290));
        assertEquals("Water", GT6MaterialIdentity.name(9800));
        assertNull(GT6MaterialIdentity.name(0));
        assertNull(GT6MaterialIdentity.name(-1));
        assertNull(GT6MaterialIdentity.name(32000));
        NBTTagCompound entry = new NBTTagCompound();
        entry.setShort("i", (short) 260);
        entry.setString("m", "Copper");
        entry.setLong("a", 648648000L);
        NBTTagCompound list = new NBTTagCompound();
        list.setInteger("size", 1);
        list.setTag("0", entry);
        NBTTagCompound root = new NBTTagCompound();
        root.setTag(CrucibleRecyclingOverride.TAG, list);
        NBTTagCompound before = root.copy();
        int[] lookups = {0};
        assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
            assertEquals("Iron", name);
            lookups[0]++;
            return null; // Missing host material must retain the source item.
        }).isEmpty());
        assertEquals(1, lookups[0]);
        assertEquals(before, root);
    }

    @Test
    void gt6OverrideAmountsUseGt6UnitsAndMalformedListsCannotFallBack() {
        assertEquals(GTValues.M, CrucibleRecyclingOverride.convertAmount(648648000L));
        assertEquals(GTValues.M / 9, CrucibleRecyclingOverride.convertAmount(648648000L / 9));
        assertEquals(0, CrucibleRecyclingOverride.convertAmount(1));
        assertEquals(0, CrucibleRecyclingOverride.convertAmount(-1));
        assertTrue(CrucibleRecyclingOverride.convertAmount(Long.MAX_VALUE) > 0);
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound list = new NBTTagCompound();
        list.setInteger("size", Integer.MAX_VALUE);
        root.setTag(CrucibleRecyclingOverride.TAG, list);
        NBTTagCompound before = root.copy();
        assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
            throw new AssertionError("Malformed list must not resolve materials");
        }).isEmpty());
        assertEquals(before, root);
        NBTTagCompound entry = new NBTTagCompound();
        entry.setShort("i", (short) 26);
        entry.setString("m", "iron");
        entry.setLong("a", 648648000L);
        list.setInteger("size", 1);
        list.setTag("0", entry);
        assertTrue(CrucibleRecyclingOverride.parse(root, name -> {
            throw new AssertionError("GT6 numeric IDs must not be treated as CEu IDs");
        }).isEmpty());
    }

    @Test
    void unresolvedToolNbtIsRetainedAndResolutionDoesNotCreateOrModifyTags() {
        ItemStack stack = new ItemStack(new Item());
        assertFalse(CrucibleToolRecycling.isTool(stack));
        assertTrue(CrucibleToolRecycling.resolve(stack).isEmpty());
        assertFalse(stack.hasTagCompound());
        NBTTagCompound tool = new NBTTagCompound();
        tool.setString("Material", "gtceu:iron");
        tool.setInteger("MaxDurability", 100);
        tool.setInteger("Durability", 25);
        NBTTagCompound root = new NBTTagCompound();
        root.setTag("GT.Tool", tool);
        stack.setTagCompound(root);
        NBTTagCompound before = root.copy();
        assertTrue(CrucibleToolRecycling.isTool(stack));
        assertTrue(CrucibleToolRecycling.resolve(stack).isEmpty());
        assertEquals(before, stack.getTagCompound());
        // The known override tag is allowed, but it is parsed separately and
        // cannot make an unknown recipe yield guessed base tool materials.
        root.setTag(CrucibleRecyclingOverride.TAG, new NBTTagCompound());
        assertTrue(CrucibleToolRecycling.isSafeTool(stack));
        before = root.copy();
        assertTrue(CrucibleToolRecycling.resolve(stack).isEmpty());
        assertEquals(before, stack.getTagCompound());
        tool.setTag("Inventory", new NBTTagCompound());
        assertFalse(CrucibleToolRecycling.isSafeTool(stack));
        before = root.copy();
        assertTrue(CrucibleToolRecycling.resolve(stack).isEmpty());
        assertEquals(before, stack.getTagCompound());
    }

    @Test
    void alloyHeatIncludesVerifiedLateRecipeMeltingPointAdjustment() {
        assertEquals(1357, GT6DeclaredPhaseData.meltingPoint("bronze"));
        assertEquals(2835L, CrucibleMaterialPhaseData.boilingPoint("bronze"));
        assertEquals(1160, GT6DeclaredPhaseData.meltingPoint("brass"));
        assertEquals(2800, GT6DeclaredPhaseData.meltingPoint("annealed_copper"));
        assertEquals(1070, GT6DeclaredPhaseData.meltingPoint("arsenic_copper"));
        assertEquals(1357, GT6DeclaredPhaseData.meltingPoint("arsenic_bronze"));
        assertEquals(2835L, CrucibleMaterialPhaseData.boilingPoint("arsenic_copper"));
        assertEquals(2146, GT6DeclaredPhaseData.meltingPoint("knightmetal"));
        assertEquals(3234L, CrucibleMaterialPhaseData.boilingPoint("knightmetal"));
        assertEquals(3945, GT6DeclaredPhaseData.meltingPoint("gaia_spirit"));
        assertEquals(6328L, CrucibleMaterialPhaseData.boilingPoint("gaia_spirit"));
        assertEquals(GT6DeclaredPhaseData.meltingPoint("elvenelementium"),
                GT6DeclaredPhaseData.meltingPoint("elementium"));
    }

    @Test
    void diamondDeclarationsOverrideCarbonHeatForEveryVerifiedVariant() {
        for (String name : new String[]{"diamond", "diamond_blue", "diamond_green", "diamond_purple",
                "diamond_red", "diamond_yellow", "diamond_pink", "diamond_industrial", "Blue Diamond",
                "Green Diamond", "Purple Diamond", "Red Diamond", "Yellow Diamond", "Pink Diamond",
                "mana_diamond", "elven_dragonstone", "gravitite", "diamantine"}) {
            assertEquals(4200, CrucibleMaterialPhaseData.knownMeltingPoint(name), name);
            assertEquals(4300L, CrucibleMaterialPhaseData.boilingPoint(name), name);
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            assertNotNull(rule, name);
            assertEquals("carbon", rule.target, name);
            assertEquals(2 * GTValues.M, rule.convert(GTValues.M), name);
        }
        assertEquals(3800, CrucibleMaterialPhaseData.knownMeltingPoint("carbon"));
        assertEquals(4300L, CrucibleMaterialPhaseData.boilingPoint("carbon"));
    }

    @Test
    void enderAmethystUsesActualGt6NameAndOwnHeatWithoutEnablingSmelting() {
        assertEquals("AmethystEnder", GT6MaterialIdentity.name(8329));
        assertTrue(CrucibleSolidifyingRule.isSnapshotMaterial("AmethystEnder"));
        assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget("AmethystEnder"));
        for (String name : new String[]{"AmethystEnder", "amethyst_ender", "Amethyst Ender",
                "EnderAmethyst", "ender_amethyst"}) {
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            assertNotNull(rule, name);
            assertEquals("", rule.target, name);
            assertEquals(0, rule.convert(GTValues.M), name);
            assertFalse(CrucibleSmeltingRule.hasDefaultSelfTarget(name), name);
            assertFalse(CrucibleSmeltingRule.hasMeltingFlag(name), name);
            assertFalse(GT6DeclaredPhaseData.hasBurningExemption(name), name);
            assertEquals(1625, CrucibleMaterialPhaseData.knownMeltingPoint(name), name);
            assertEquals(2669L, CrucibleMaterialPhaseData.boilingPoint(name), name);
        }
        // setGenerifying does not replace the ender variant's six-part heat
        // average with ordinary amethyst's five-part average.
        assertEquals(1951, CrucibleMaterialPhaseData.knownMeltingPoint("amethyst"));
        assertEquals(3202L, CrucibleMaterialPhaseData.boilingPoint("amethyst"));
    }

    @Test
    void amethystDensitiesUseFivePartDividerAndZeroDensityMagic() {
        double silica = (2329.6 + 2 * 1.429) / 3;
        double density = (4 * silica + 7874) / 5;
        for (String name : new String[]{"amethyst", "AmethystEnder", "ender_amethyst"}) {
            assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(name), name);
            assertEquals(density, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(
                    name, 999999), 1.0e-9, name);
            assertEquals(density / 9, CrucibleTransferLogic.materialWeightKg(
                    GTValues.M, density, GTValues.M), 1.0e-9, name);
        }
        assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity("Magic"));
        assertEquals(0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("Magic", 999999));
        assertTrue(CrucibleTransferLogic.isAirDensity(
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("Magic")));
        assertEquals(1200D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(
                "parity_unknown_gem", 0));
    }

    @Test
    void verifiedDisabledSmeltingGemsDoNotInheritHostCombustionFlags() {
        for (String name : new String[]{"spinel", "balas_ruby", "fools_ruby", "almandine", "grossular",
                "pyrope", "spessartine", "andradite", "uvarovite", "garnet_red", "garnet_orange",
                "garnet_purple", "garnet", "garnet_yellow", "garnet_green", "red_jasper", "jasper",
                "ocean_jasper", "rainforest_jasper", "blue_jasper", "green_jasper", "yellow_jasper",
                "tiger_eye", "yellow_tiger_eye", "cats_eye", "green_tiger_eye", "dragon_eye",
                "red_tiger_eye", "hawks_eye", "blue_tiger_eye", "black_eye", "black_tiger_eye",
                "tiger_iron", "green_aventurine", "aventurine", "brown_aventurine", "yellow_aventurine",
                "black_aventurine", "blue_aventurine", "red_aventurine", "topaz", "blue_topaz",
                "tanzanite", "zanite", "amazonite", "alexandrite", "opal", "onyx_red", "onyx_black",
                "onyx", "sugilite", "peridot", "olivine", "amethyst", "dioptase", "amethyst_ender",
                "ender_amethyst", "dilithium", "hexorium_black", "hexorium_red", "hexorium_green",
                "hexorium_blue", "hexorium_white"}) {
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, true), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 5000, true), name);
            assertFalse(GT6MaterialHazardData.isExplosive(name), name);
        }
        // wood() never grants MELTING; Silverwood's only setSmelting call
        // has zero output, so its explicit FLAMMABLE tag is not exempt.
        assertEquals(0, CrucibleSmeltingRule.find("silverwood").convert(GTValues.M));
        assertFalse(GT6InheritedBurningExemptions.contains("silverwood"));
        assertTrue(GT6MaterialHazardData.shouldBurn("silverwood", 314, false));
        assertFalse(GT6MaterialHazardData.shouldBurn("parity_unknown_gem", 314, false));
        assertTrue(GT6MaterialHazardData.shouldBurn("parity_unknown_gem", 314, true));
    }

    @Test
    void refinedMaterialOutputCopiesDoNotReplaceTheirOwnThermalProperties() {
        String[] names = {"refined_glowstone", "GlowstoneRefined", "refined_obsidian", "ObsidianRefined"};
        int[] melting = {855, 855, 2750, 2750};
        long[] boiling = {1853, 1853, 4150, 4150};
        String[] hotTargets = {"glowstone", "glowstone", "lava", "lava"};
        String[] coldTargets = {"glowstone", "glowstone", "obsidian", "obsidian"};
        for (int i = 0; i < names.length; i++) {
            assertEquals(melting[i], CrucibleMaterialPhaseData.knownMeltingPoint(names[i]), names[i]);
            assertEquals(boiling[i], CrucibleMaterialPhaseData.boilingPoint(names[i]), names[i]);
            assertEquals(hotTargets[i], CrucibleSmeltingRule.find(names[i]).target);
            assertEquals(coldTargets[i], CrucibleSolidifyingRule.target(names[i]));
            // The 2-component configuration is not a 2U smelting yield.
            assertEquals(GTValues.M, CrucibleSmeltingRule.find(names[i]).convert(GTValues.M));
            assertTrue(CrucibleSmeltingRule.hasMeltingFlag(names[i]));
            assertFalse(GT6MaterialHazardData.shouldBurn(names[i], 5000, true));
        }
        assertEquals(500, CrucibleMaterialPhaseData.knownMeltingPoint("glowstone"));
        assertEquals(1300, CrucibleMaterialPhaseData.knownMeltingPoint("obsidian"));
    }

    @Test
    void refinedMaterialBoundariesUseSourceHeatRatherThanOutputHeat() {
        int glowstone = CrucibleMaterialPhaseData.knownMeltingPoint("refined_glowstone");
        int obsidian = CrucibleMaterialPhaseData.knownMeltingPoint("refined_obsidian");
        assertEquals(CrucibleTransferLogic.IntakePhase.NONE,
                CrucibleTransferLogic.intakePhase(286, 854, glowstone));
        assertEquals(CrucibleTransferLogic.IntakePhase.MELT,
                CrucibleTransferLogic.intakePhase(286, 855, glowstone));
        assertEquals(CrucibleTransferLogic.IntakePhase.NONE,
                CrucibleTransferLogic.intakePhase(286, 1300, obsidian));
        assertEquals(CrucibleTransferLogic.IntakePhase.MELT,
                CrucibleTransferLogic.intakePhase(286, 2750, obsidian));
        assertEquals(CrucibleTransferLogic.IntakePhase.SOLIDIFY,
                CrucibleTransferLogic.intakePhase(2750, 2749, obsidian));
        assertEquals(2775, CrucibleTransferLogic.fluidInputTemperature(1600, obsidian, 4150));
    }

    @Test
    void refinedMaterialDensitiesKeepGt6NestedConfigurationsAndExplicitDividers() {
        double silica = (2329.6 + 2 * 1.429) / 3;
        double phosphate = (1820 + 4 * 1.429) / 5;
        double phosphorite = (5 * 1540 + 3 * phosphate + 1.696) / 9;
        double glowstone = (5 * phosphorite + 3 * 19282 + silica + 0.1785) / 10;
        double obsidian = (1738 + 7874 + 6 * silica + 4 * 1.429) / 64;
        String[] names = {"phosphorite", "glowstone", "obsidian", "lava", "refined_glowstone", "GlowstoneRefined",
                "refined_obsidian", "ObsidianRefined"};
        double[] densities = {phosphorite, glowstone, obsidian, obsidian, glowstone + 5323, glowstone + 5323,
                obsidian + 3530, obsidian + 3530};
        for (int i = 0; i < names.length; i++) {
            assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(names[i]), names[i]);
            assertEquals(densities[i], CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(
                    names[i], 999999), 1.0e-9, names[i]);
            assertEquals(densities[i] / 9, CrucibleTransferLogic.materialWeightKg(
                    GTValues.M, densities[i], GTValues.M), 1.0e-9, names[i]);
        }
        for (String name : new String[]{"Blue Diamond", "Green Diamond", "Purple Diamond",
                "Red Diamond", "Yellow Diamond", "Pink Diamond"}) {
            assertEquals(3530, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name), 1.0e-9);
        }
    }

    @Test
    void copiedOutputRulesUseTheTargetsSmeltingProductAndRatio() {
        assertEquals("ceramic", CrucibleSmeltingRule.find("brick").target);
        assertEquals("tungsten", CrucibleSmeltingRule.find("tungsten_sintered").target);
        assertEquals("iron", CrucibleSmeltingRule.find("refined_iron").target);
        assertEquals("glowstone", CrucibleSmeltingRule.find("GlowstoneRefined").target);
        assertEquals("lava", CrucibleSmeltingRule.find("ObsidianRefined").target);
        assertEquals(4L, CrucibleSmeltingRule.find("ObsidianRefined").convert(4));
        assertEquals("ash", CrucibleSmeltingRule.find("wood_treated").target);
        assertEquals(1L, CrucibleSmeltingRule.find("wood_treated").convert(4));
        assertEquals(500, CrucibleSmeltingRule.find("wood_treated").meltingPoint);
        assertEquals(600L, CrucibleSmeltingRule.find("wood_polished").boilingPoint);
    }

    @Test
    void solidificationUsesExplicitOutputCopiesRatherThanReversingSmelting() {
        assertEquals("ceramic", CrucibleSolidifyingRule.target("brick"));
        assertEquals("wood", CrucibleSolidifyingRule.target("wood_treated"));
        assertEquals("wood", CrucibleSolidifyingRule.target("wood_polished"));
        assertEquals("tungsten", CrucibleSolidifyingRule.target("tungsten_sintered"));
        assertEquals("glowstone", CrucibleSolidifyingRule.target("GlowstoneRefined"));
        assertEquals("obsidian", CrucibleSolidifyingRule.target("refined_obsidian"));
        assertEquals("iron", CrucibleSolidifyingRule.target("refined_iron"));
        assertNull(CrucibleSolidifyingRule.target("iron"));
        assertNull(CrucibleSolidifyingRule.target("tin"));
        assertNull(CrucibleSolidifyingRule.target(null));
    }

    @Test
    void inheritedHeatUsesVerifiedSourcesAndResolvesChainedInheritance() {
        assertEquals(1811, GT6InheritedPhaseData.meltingPoint("deep_iron"));
        assertEquals(3134L, CrucibleMaterialPhaseData.boilingPoint("deep_iron"));
        assertEquals(1873, GT6InheritedPhaseData.meltingPoint("tungsten_alloy"));
        assertEquals(3134L, GT6InheritedPhaseData.boilingPoint("tungsten_alloy"));
        assertEquals(2011, GT6InheritedPhaseData.meltingPoint("ancient_debris"));
        assertEquals(3334L, CrucibleMaterialPhaseData.boilingPoint("ancient_debris"));
        assertEquals(2246, GT6InheritedPhaseData.meltingPoint("netherite"));
        assertEquals(3334L, GT6InheritedPhaseData.boilingPoint("netherite"));
        assertEquals(2311, GT6InheritedPhaseData.meltingPoint("dark_thaumium"));
        assertEquals(4134L, CrucibleMaterialPhaseData.boilingPoint("dark_thaumium"));
        assertEquals(-1, GT6InheritedPhaseData.meltingPoint("unmapped_material"));
        assertEquals(Long.MAX_VALUE, GT6InheritedPhaseData.boilingPoint("unmapped_material"));
        assertEquals(-1, GT6InheritedPhaseData.meltingPoint(null));
    }

    @Test
    void derivedHeatExpressionsKeepSeparateMeltingAndBoilingReferences() {
        assertEquals(2246, GT6DeclaredPhaseData.meltingPoint("dark_iron"));
        assertEquals(3134L, CrucibleMaterialPhaseData.boilingPoint("dark_iron"));
        assertEquals(3134, GT6DeclaredPhaseData.meltingPoint("meteoflame_steel"));
        assertEquals(3834L, CrucibleMaterialPhaseData.boilingPoint("meteoflame_steel"));
        assertEquals(2446, GT6DeclaredPhaseData.meltingPoint("kreknorite"));
        assertEquals(3534L, CrucibleMaterialPhaseData.boilingPoint("kreknorite"));
    }

    @Test
    void burningExemptionsFollowVerifiedGt6FactoriesAndDeclarations() {
        assertTrue(GT6InheritedBurningExemptions.contains("Stainless Steel"));
        assertTrue(GT6InheritedBurningExemptions.contains("ancient_debris"));
        assertTrue(GT6InheritedBurningExemptions.contains("WaxRefractory"));
        assertTrue(GT6InheritedBurningExemptions.contains("Basalt"));
        // Wood's setSmelting(Ash, U4) adds MELTING even though it is FLAMMABLE.
        assertTrue(GT6InheritedBurningExemptions.contains("wood"));
        assertFalse(GT6InheritedBurningExemptions.contains("unregistered_custom_alloy"));
    }
    @Test
    void extremeSavedContentsCannotWrapFluidDisplayOrFreeCapacity() {
        assertEquals(144, CrucibleTransferLogic.materialFluidAmount(3_628_800, 3_628_800, 144));
        assertEquals(0, CrucibleTransferLogic.materialFluidAmount(1, 3_628_800, 144));
        assertEquals(0, CrucibleTransferLogic.materialFluidAmount(-1, 3_628_800, 144));
        assertEquals(Integer.MAX_VALUE,
                CrucibleTransferLogic.materialFluidAmount(Long.MAX_VALUE, 3_628_800, 144));
        assertEquals(Long.MAX_VALUE, CrucibleTransferLogic.saturatingMaterialSum(Long.MAX_VALUE - 1, 2));
        assertEquals(7, CrucibleTransferLogic.saturatingMaterialSum(7, -1));
        assertEquals(8, CrucibleTransferLogic.saturatingMaterialSum(7, 1));
    }
    @Test
    void damagedItemFallbackPreservesRemainingMaterialWithFloorRounding() {
        assertEquals(75L, CrucibleTransferLogic.remainingDurabilityMaterial(100, 25, 100));
        assertEquals(0L, CrucibleTransferLogic.remainingDurabilityMaterial(1, 1, 2));
        assertEquals(0L, CrucibleTransferLogic.remainingDurabilityMaterial(100, 100, 100));
        assertEquals(0L, CrucibleTransferLogic.remainingDurabilityMaterial(100, 101, 100));
        assertEquals(0L, CrucibleTransferLogic.remainingDurabilityMaterial(100, 0, 0));
        assertEquals(100L, CrucibleTransferLogic.remainingDurabilityMaterial(100, -1, 100));
        assertEquals(4_611_686_018_427_387_903L,
                CrucibleTransferLogic.remainingDurabilityMaterial(Long.MAX_VALUE, 1, 2));
    }

    @Test
    void explosionStrengthMatchesGt6IntegerScaleBoundaries() {
        assertEquals(0, CrucibleTransferLogic.contentExplosionStrength(0, 100));
        assertEquals(1, CrucibleTransferLogic.contentExplosionStrength(1, 100));
        assertEquals(1, CrucibleTransferLogic.contentExplosionStrength(19, 100));
        assertEquals(2, CrucibleTransferLogic.contentExplosionStrength(20, 100));
        assertEquals(5, CrucibleTransferLogic.contentExplosionStrength(99, 100));
        assertEquals(6, CrucibleTransferLogic.contentExplosionStrength(100, 100));
        assertEquals(6, CrucibleTransferLogic.contentExplosionStrength(101, 100));
        assertEquals(5, CrucibleTransferLogic.contentExplosionStrength(Long.MAX_VALUE - 1, Long.MAX_VALUE));
    }

    @Test
    void vaporDamageUsesStrict320KelvinBoundaryAndGt6Multiplier() {
        assertEquals(0.0F, CrucibleTransferLogic.vaporHeatDamage(320));
        assertEquals(0.84F, CrucibleTransferLogic.vaporHeatDamage(321), 0.00001F);
        assertEquals(2.92F, CrucibleTransferLogic.vaporHeatDamage(373), 0.00001F);
        assertEquals(40.0F, CrucibleTransferLogic.vaporHeatDamage(1300));
    }

    @Test
    void sourceDefinedOreShapesDoNotAffectOrdinaryStorageBlocks() {
        assertOreForm("blockRawIron", "Iron", 9);
        assertOreForm("blockOreIron", "Iron", 9);
        assertOreForm("crateGtRawCopper", "Copper", 16);
        assertOreForm("crateGtOreCopper", "Copper", 16);
        assertOreForm("crateGt64RawTin", "Tin", 64);
        assertOreForm("crateGt64OreTin", "Tin", 64);
        assertOreForm("oreDenseIron", "Iron", 2);
        assertOreForm("denseoreIron", "Iron", 2);
        assertNull(CrucibleOreInputForm.find("blockIron"));
        assertNull(CrucibleOreInputForm.find("oreNetherrackIron"));
        assertNull(CrucibleOreInputForm.find("blockRaw"));
    }

    private static void assertOreForm(String name, String material, int multiplier) {
        CrucibleOreInputForm form = CrucibleOreInputForm.find(name);
        assertNotNull(form);
        assertEquals(material, form.materialName);
        assertEquals(multiplier, form.multiplier);
    }

    @Test
    void dimensionOreMultiplierUsesExactResolvedPrefixNotMaterialName() {
        assertEquals(2, CrucibleOreInputForm.registeredOreMultiplier("oreNether"));
        assertEquals(2, CrucibleOreInputForm.registeredOreMultiplier("oreEnd"));
        assertEquals(1, CrucibleOreInputForm.registeredOreMultiplier("ore"));
        assertEquals(1, CrucibleOreInputForm.registeredOreMultiplier("oreNetherrack"));
        assertEquals(1, CrucibleOreInputForm.registeredOreMultiplier("oreEndstone"));
        assertEquals(1, CrucibleOreInputForm.registeredOreMultiplier("oreNetherLean"));
        assertEquals(1, CrucibleOreInputForm.registeredOreMultiplier("oreEndLean"));
        assertEquals(1, CrucibleOreInputForm.registeredOreMultiplier("oreNetherQuartz"));
        assertEquals(1, CrucibleOreInputForm.registeredOreMultiplier(null));
        assertEquals(6L * GTValues.M, GTValues.M *
                CrucibleOreInputForm.registeredOreMultiplier("oreNether") *
                CrucibleOreInputRule.find("iron").multiplier);
    }

    @Test
    void smeltingRatiosAndTemperatureFixturesMatchGt6Source() {
        assertEquals(3L, CrucibleSmeltingRule.find("cassiterite").convert(4));
        assertEquals(757, CrucibleSmeltingRule.find("cassiterite").meltingPoint);
        assertEquals(1514L, CrucibleSmeltingRule.find("cassiterite").boilingPoint);
        assertEquals(2L, CrucibleSmeltingRule.find("chalcopyrite").convert(9));
        assertEquals(1L, CrucibleSmeltingRule.find("Brown Limonite").convert(2));
        assertEquals(1L, CrucibleSmeltingRule.find("emerald").convert(36));
        assertEquals(10L, CrucibleSmeltingRule.find("diamond").convert(5));
        assertEquals(-1L, CrucibleSmeltingRule.find("diamond").convert(Long.MAX_VALUE));
        assertEquals(4L, CrucibleSmeltingRule.find("rubber").convert(6));
        assertNull(CrucibleSmeltingRule.find("rubber").target);
        assertTrue(CrucibleSmeltingRule.hasMeltingFlag("rubber"));
        assertEquals(0L, CrucibleSmeltingRule.find("topaz").convert(1));
        assertFalse(CrucibleSmeltingRule.hasMeltingFlag("topaz"));
        assertEquals(2046, GT6DeclaredPhaseData.meltingPoint("steel"));
        assertEquals(3134L, CrucibleMaterialPhaseData.boilingPoint("steel"));
    }
    @Test
    void unifiedFluidInputIsLimitedOnlyBySharedFreeCapacity() {
        assertEquals(1_000, CrucibleTransferLogic.acceptedSharedFluidAmount(1_000, 1_500));
        assertEquals(1_500, CrucibleTransferLogic.acceptedSharedFluidAmount(2_000, 1_500));
        assertEquals(0, CrucibleTransferLogic.acceptedSharedFluidAmount(1_000, 0));
        assertEquals(0, CrucibleTransferLogic.acceptedSharedFluidAmount(-1, 1_000));
    }

    @Test
    void queueIsBoundedTo64ItemCounts() {
        assertEquals(64, CrucibleTransferLogic.acceptedQueueItems(0, 128, 64));
        assertEquals(4, CrucibleTransferLogic.acceptedQueueItems(60, 20, 64));
        assertEquals(0, CrucibleTransferLogic.acceptedQueueItems(64, 1, 64));
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
    void signedThermalEnergySupportsBothHeatingAndCooling() {
        assertEquals(80L, CrucibleTransferLogic.accumulateSignedEnergy(100L, -20L));
        assertEquals(-20L, CrucibleTransferLogic.accumulateSignedEnergy(0L, -20L));
        assertEquals(Long.MAX_VALUE, CrucibleTransferLogic.accumulateSignedEnergy(Long.MAX_VALUE - 1L, 2L));
        assertEquals(Long.MIN_VALUE, CrucibleTransferLogic.accumulateSignedEnergy(Long.MIN_VALUE + 1L, -2L));
        assertEquals(-8L, CrucibleTransferLogic.temperatureDeltaForEnergy(-17L, 100.0D));
        assertEquals(8L, CrucibleTransferLogic.temperatureDeltaForEnergy(17L, 100.0D));
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
    void registeredDensityFallbackDoesNotPropagateNonPhysicalValues() {
        for (double density : new double[]{0.0D, -100.0D, Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(1200.0D,
                    CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("thirdparty:unmapped_material", density));
        }
        // Known zero-density data must not be masked by the fallback.
        assertEquals(0.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("fermium", Double.NaN));
    }

    @Test
    void extremeThermalMassCannotTurnHeatIntoCoolingByOverflowingTheCost() {
        for (double mass : new double[]{Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN}) {
            assertEquals(Long.MAX_VALUE, CrucibleTransferLogic.requiredEnergyPerKelvin(mass));
            assertEquals(0, CrucibleTransferLogic.temperatureDeltaForEnergy(128, mass));
            assertEquals(0, CrucibleTransferLogic.temperatureDeltaForEnergy(-128, mass));
        }
        assertEquals(1, CrucibleTransferLogic.requiredEnergyPerKelvin(0));
        assertEquals(316, CrucibleTransferLogic.requiredEnergyPerKelvin(31_583.333333D));
        assertEquals(1, CrucibleTransferLogic.temperatureDeltaForEnergy(Long.MAX_VALUE, Double.MAX_VALUE));
        assertEquals(-1, CrucibleTransferLogic.temperatureDeltaForEnergy(-Long.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    void unknownMaterialUsesSlightlyHeavierDefaultDensityWithoutOverridingRegisteredFluidDensity() {
        assertEquals(1_200.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("gtqtcore:unknown", 0.0D));
        assertEquals(1_500.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("gtqtcore:unknown", 1_500.0D));
    }

    @Test
    void realWorldAlloyOverridesPreserveGradeNumbersAndUseBulkDensities() {
        assertEquals(8_890.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:hastelloy_c_276"));
        assertEquals(8_860.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:hastelloy_n"));
        assertEquals(9_000.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:hastelloy_w"));
        assertEquals(8_220.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:hastelloy_x"));
        assertEquals(8_193.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:inconel_718"));
        assertEquals(7_250.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:incoloy_ma_956"));
        assertEquals(8_000.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:maraging_steel_250"));
        assertEquals(8_000.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:maraging_steel_300"));
        assertEquals(8_083.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:maraging_steel_350"));
        assertEquals(7_840.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:zeron_100"));
        assertEquals(6_560.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:zircaloy_4"));
        assertEquals(8_193.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("gtceu:inconel_718", 1_200.0D));
    }

    @Test
    void trailingDigitsFallBackToElementOnlyAfterExactMaterialLookup() {
        assertEquals(18_950.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:uranium_235"));
        assertEquals(0.0D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("gtceu:unmapped_grade_718"));
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
    void oreInputRulesCoverAllExplicitGt6TargetCrushingOverrides() {
        assertOreInputRule("Adamantium", "adamantine", 2);
        assertOreInputRule("Iron", "hematite", 3);
        assertOreInputRule("Aluminium", "alumina", 2);
        assertOreInputRule("Titanium", "rutile", 2);
        assertOreInputRule("Tungsten", "scheelite", 2);
        assertOreInputRule("Uranium 238", "uraninite", 2);
        assertOreInputRule("Fluorine", "fluorite", 2);
        assertOreInputRule("Tantalum", "tantalite", 2);
        assertOreInputRule("Niobium", "columbite", 2);
        assertOreInputRule("Naquadah-Enriched", "naquadah", 2);
        assertOreInputRule("Naquadria", "naquadah", 4);
        assertOreInputRule("Dilithium", "dolamide", 2);
        assertNull(CrucibleOreInputRule.find("unmapped_material"));
    }

    private static void assertOreInputRule(String name, String target, int multiplier) {
        CrucibleOreInputRule rule = CrucibleOreInputRule.find(name);
        assertNotNull(rule);
        assertEquals(target, rule.target);
        assertEquals(multiplier, rule.multiplier);
    }

    @Test
    void lowDensityThresholdUsesGt6AirBoundary() {
        assertTrue(CrucibleTransferLogic.isAirDensity(0.08988D));
        assertTrue(CrucibleTransferLogic.isAirDensity(1.2D));
        assertFalse(CrucibleTransferLogic.isAirDensity(1.20001D));
        assertFalse(CrucibleTransferLogic.isAirDensity(200.0D));
        assertTrue(CrucibleTransferLogic.isAirDensity(0.0D));
        assertTrue(CrucibleTransferLogic.isAirDensity(Double.MIN_VALUE));
        assertFalse(CrucibleTransferLogic.isAirDensity(-1.0D));
        assertFalse(CrucibleTransferLogic.isAirDensity(Double.POSITIVE_INFINITY));
        assertFalse(CrucibleTransferLogic.isAirDensity(Double.NEGATIVE_INFINITY));
        assertFalse(CrucibleTransferLogic.isAirDensity(Double.NaN));
    }

    @Test
    void fluidHeatTransferIsWeightedAndSimulationCalculationIsPure() {
        long simulated = CrucibleTransferLogic.mixTemperature(1_000, 10.0D, 300, 10.0D);
        assertEquals(650, simulated);
        assertEquals(simulated, CrucibleTransferLogic.mixTemperature(1_000, 10.0D, 300, 10.0D));
    }
}
