package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GT6CombustionMaterialDataTest {
    @Test void everyLiteralMaterialHasTheSourceCombustionTags() {
        // Independent MT.java declaration inventory: explicit tags and actual
        // liquid flame/explosive, grain, coal, woodnormal factory bodies.
        // gasflam/gasexpl do NOT grant the tags in this snapshot (:99-100).
        Set<Integer> flammableOnly = new HashSet<>(Arrays.asList(
                60, 61, 62, 120, 160, 9832, 9703, 9828, 8024, 8019, 8205, 9833,
                9840, 9841, 9842, 9850, 9870, 9871, 9887, 9872, 9873, 9874, 9875,
                9876, 9877, 9880, 9881, 8275, 8221, 8222, 8267, 8224, 8418, 8291,
                8286, 8289, 8290, 8414, 8296, 8297, 8227, 8277, 8206, 8209, 8226,
                8217, 8218, 8196, 8197, 8198, 8199, 8241, 8228, 9702, 9704, 9705,
                9706, 9707, 9719, 9708, 8336, 8334, 8349, 8362, 8363, 8364, 8337,
                8365, 8390, 8360, 8361, 8502, 9853));
        for (int id = 9300; id <= 9409; id++) flammableOnly.add(id);
        Set<Integer> explosiveOnly = new HashSet<>(Arrays.asList(1741, 1742));
        Set<Integer> both = new HashSet<>(Arrays.asList(150, 720, 9821, 8207, 9820,
                9860, 9861, 9862, 9863, 9864, 8208, 8458, 8459, 8460, 8220, 8249));
        int known = 0, flammable = 0, explosive = 0, noTags = 0;
        for (int id = 1; id <= Short.MAX_VALUE; id++) {
            String name = GT6MaterialIdentity.name(id);
            if (name == null) continue;
            known++;
            int expected = both.contains(id) ? 3 : flammableOnly.contains(id) ? 1 : explosiveOnly.contains(id) ? 2 : 0;
            assertEquals(Integer.valueOf(expected), GT6MaterialHazardData.knownCombustionFlags(name), id + ": " + name);
            assertEquals((expected & 2) != 0, GT6MaterialHazardData.isExplosive(name), name);
            if ((expected & 1) != 0) flammable++;
            if ((expected & 2) != 0) explosive++;
            if (expected == 0) noTags++;
        }
        assertEquals(1084, known);
        assertEquals(199, flammable);
        assertEquals(18, explosive);
        assertEquals(883, noTags);
    }

    @Test void technicalFamiliesOnlyHaveTheirExplicitCombustionTags() {
        String[] names = {"Any Glowstone", "Any Diamond", "Any Sapphire", "Any Emerald", "Any Amethyst",
                "Any Garnet", "Any Jasper", "Any Tiger Eye", "Any Aventurine", "Any Amber", "Any Fluorite",
                "Any Phosphorus", "Any Blaze", "Any Prismarine", "Any Grains", "Any Flour", "Any Flour Or Grains",
                "Any Wax", "Any Stone", "Any Calcite", "Any Clay", "Any Salt", "Any Iron", "Any Iron Or Steel",
                "Any Iron-Steel", "Any Black Steel", "Any Blue Steel", "Any Red Steel", "Any Magic Iron",
                "Any Copper", "Any Ashes", "Any Carbon", "Any CoalCarbon", "Any Silicon", "Any Silicon Dioxide",
                "Quartz", "Any Sand", "Any Tungsten", "Any Thaumic Crystal", "Hexorium", "Any Wood",
                "Any Default Wood", "Any Normal Wood", "Any Magical Wood", "Any Treated Wood", "Any Untreated Wood",
                "Any Wood Or Plastic", "Any Rubber", "Any Plastic", "Any Hard Plastic", "Any Steel", "Any Bronze", "Any Metal"};
        Set<String> flames = new HashSet<>(Arrays.asList("Any Grains", "Any Flour", "Any Flour Or Grains",
                "Any Wood", "Any Default Wood", "Any Normal Wood", "Any Magical Wood", "Any Treated Wood", "Any Untreated Wood"));
        assertEquals(53, names.length);
        for (String name : names) {
            int expected = name.equals("Any Phosphorus") ? 3 : flames.contains(name) ? 1 : 0;
            assertEquals(Integer.valueOf(expected), GT6MaterialHazardData.knownCombustionFlags(name), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, true), name);
        }
    }

    @Test void woodFactoryDoesNotCreateAshTargetsOrBurningExemptions() {
        // MT.java:299-307 wood() only adds wood/ore registration metadata.
        // :1255 zero setSmelting cannot grant MELTING; :1256 steal only copies
        // heat/stats; :1257 Marshmallow retains constructor targets and heat.
        assertEquals(0, CrucibleSmeltingRule.find("Silverwood").convert(648648000L));
        assertFalse(CrucibleSmeltingRule.hasMeltingFlag("Silverwood"));
        assertFalse(GT6InheritedBurningExemptions.contains("Silverwood"));
        for (String name : new String[]{"Peanut Wood", "Marshmallow"}) {
            assertNull(CrucibleSmeltingRule.find(name), name);
            assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget(name), name);
            assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name), name);
            assertFalse(CrucibleSmeltingRule.hasMeltingFlag(name), name);
            assertFalse(GT6InheritedBurningExemptions.contains(name), name);
        }
        assertEquals(350, CrucibleMaterialPhaseData.knownMeltingPoint("Peanutwood"));
        assertEquals(450, CrucibleMaterialPhaseData.boilingPoint("Peanutwood"));
        assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint("Marshmallow"));
        assertEquals(3000, CrucibleMaterialPhaseData.boilingPoint("Marshmallow"));
        for (String name : new String[]{"Silverwood", "Peanutwood"}) {
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 313, true), name);
            assertTrue(GT6MaterialHazardData.shouldBurn(name, 314, false), name);
            assertEquals((6 * 2267D + 15 * 1000D) / 21,
                    CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 99999), 1e-6, name);
        }
        assertEquals(Integer.valueOf(0), GT6MaterialHazardData.knownCombustionFlags("Marshmallow"));
        assertFalse(GT6MaterialHazardData.shouldBurn("Marshmallow", 2999, true));
        assertEquals(1000D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter("Marshmallow", 99999));
    }

    @Test void flourAndGrainFamiliesCopyTargetsAndHeatButNativeGrainsStillBurn() {
        for (String name : new String[]{"Any Grains", "Any Flour", "Any Flour Or Grains"}) {
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            assertNotNull(rule, name);
            assertEquals("gregtech:wheat", rule.target, name);
            assertEquals(648648000L, rule.convert(648648000L), name);
            assertEquals("gregtech:wheat", CrucibleSolidifyingRule.target(name), name);
            assertEquals(1000, CrucibleMaterialPhaseData.knownMeltingPoint(name), name);
            assertEquals(3000, CrucibleMaterialPhaseData.boilingPoint(name), name);
            assertEquals(1000D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 99999), name);
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(name), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, true), name);
        }
        // Wheat only retains default self/U; copying it calls positive
        // setSmelting on ANY, which grants MELTING to ANY but not to Wheat.
        assertTrue(GT6MaterialHazardData.shouldBurn("Wheat", 314, false));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("Wheat"));
    }

    @Test void allWoodFamiliesCopyWoodNotTheirDisplayOrMemberMaterials() {
        double density = (6 * 2267D + 15 * 1000D) / 21;
        for (String name : new String[]{"Any Wood", "Any Default Wood", "Any Normal Wood", "Any Magical Wood",
                "Any Treated Wood", "Any Untreated Wood"}) {
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            assertNotNull(rule, name);
            assertEquals("ash", rule.target, name);
            assertEquals(648648000L / 4, rule.convert(648648000L), name);
            assertEquals("gregtech:wood", CrucibleSolidifyingRule.target(name), name);
            assertEquals(400, CrucibleMaterialPhaseData.knownMeltingPoint(name), name);
            assertEquals(500, CrucibleMaterialPhaseData.boilingPoint(name), name);
            assertEquals(density, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 99999), 1e-6, name);
            assertTrue(GT6DeclaredPhaseData.hasBurningExemption(name), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, true), name);
        }
        assertEquals(500, CrucibleMaterialPhaseData.knownMeltingPoint("WoodTreated"));
        assertEquals(600, CrucibleMaterialPhaseData.boilingPoint("WoodTreated"));
        assertNull(CrucibleSmeltingRule.find("Any Wood Or Plastic"));
        assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget("Any Wood Or Plastic"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("Any Wood Or Plastic"));
    }

    @Test void knownZeroTagsOverrideHostFlagsButUnknownNamesRemainUnknown() {
        for (String name : new String[]{"Marshmallow", "LiveRoot", "Any Carbon", "Any CoalCarbon",
                "Any Plastic", "Any Rubber", "Any Wood Or Plastic", "Calcite", "AluminumBrass"}) {
            assertEquals(Integer.valueOf(0), GT6MaterialHazardData.knownCombustionFlags(name), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, true), name);
            assertFalse(GT6MaterialHazardData.isExplosive(name), name);
        }
        for (String name : new String[]{"thirdparty:marshmallow", "unknown_liveroot", "Any Unknown Family", ""}) {
            assertNull(GT6MaterialHazardData.knownCombustionFlags(name), name);
            assertTrue(GT6MaterialHazardData.shouldBurn(name, 314, true), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 314, false), name);
        }
        assertNull(GT6MaterialHazardData.knownCombustionFlags(null));
    }
}
