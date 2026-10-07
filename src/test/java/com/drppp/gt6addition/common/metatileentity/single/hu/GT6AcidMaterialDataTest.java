package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GT6AcidMaterialDataTest {
    @Test void everyLiteralMaterialHasTheSourceAcidFlagNotAnUnknownFallback() {
        // Independent inventory of every literal ACID declaration / acid
        // factory call in MT.java:1020-1188, including fluorite():215.
        Set<Integer> acidIds = new HashSet<>(Arrays.asList(
                9826, 9829, 8025, 9825, 9843, 8024, 9824, 9844, 8011,
                8027, 8010, 8413, 8009, 8400, 8401, 8402, 8403, 8404,
                8405, 8406, 8407, 8408, 8409, 8410, 8411, 8412, 9827,
                9215, 8436, 8437, 8438, 8439, 8440, 8441, 8442, 8443, 8444));
        int known = 0, acids = 0;
        for (int id = 1; id <= Short.MAX_VALUE; id++) {
            String name = GT6MaterialIdentity.name(id);
            if (name == null) continue;
            known++;
            boolean expected = acidIds.contains(id);
            assertEquals(Boolean.valueOf(expected), GT6MaterialHazardData.knownAcidFlag(name), id + ": " + name);
            assertEquals(expected, GT6MaterialHazardData.isAcid(name), name);
            if (expected) acids++;
        }
        assertEquals(1084, known);
        assertEquals(37, acids);
        // CaCO3 contains acid-tagged CO3; configuration does not copy ACID.
        assertTrue(GT6MaterialHazardData.isAcid("carbon_trioxide"));
        assertEquals(Boolean.FALSE, GT6MaterialHazardData.knownAcidFlag("calcite"));
    }

    @Test void technicalFamiliesDoNotInheritAcidFromComponentsOrCopiedTargets() {
        // ANY.java's actual saved names; Fluorite is the only positive tag.
        for (String name : new String[]{"Any Glowstone", "Any Diamond", "Any Sapphire", "Any Emerald",
                "Any Amethyst", "Any Garnet", "Any Jasper", "Any Tiger Eye", "Any Aventurine", "Any Amber",
                "Any Phosphorus", "Any Blaze", "Any Prismarine", "Any Grains", "Any Flour", "Any Flour Or Grains",
                "Any Wax", "Any Stone", "Any Calcite", "Any Clay", "Any Salt", "Any Iron", "Any Iron Or Steel",
                "Any Iron-Steel", "Any Black Steel", "Any Blue Steel", "Any Red Steel", "Any Magic Iron",
                "Any Copper", "Any Ashes", "Any Carbon", "Any CoalCarbon", "Any Silicon", "Any Silicon Dioxide",
                "Quartz", "Any Sand", "Any Tungsten", "Any Thaumic Crystal", "Hexorium", "Any Wood",
                "Any Default Wood", "Any Normal Wood", "Any Magical Wood", "Any Treated Wood", "Any Untreated Wood",
                "Any Wood Or Plastic", "Any Rubber", "Any Plastic", "Any Hard Plastic", "Any Steel", "Any Bronze", "Any Metal"}) {
            assertEquals(Boolean.FALSE, GT6MaterialHazardData.knownAcidFlag(name), name);
            assertFalse(GT6MaterialHazardData.isAcid(name), name);
        }
        assertEquals(Boolean.TRUE, GT6MaterialHazardData.knownAcidFlag("Any Fluorite"));
        assertEquals(Boolean.TRUE, GT6MaterialHazardData.knownAcidFlag("any_fluorite"));
    }

    @Test void literalAliasesAndUnknownNamesAreDistinct() {
        for (String name : new String[]{"SulphuricAcid", "RomanVitriol", "CyprusVitriol", "SolutionBlueVitriol",
                "SolutionNickelSulfate", "SolutionNickelSulphate", "hydrofluoric_acid", "hydrogen_sulfide",
                "aluminum_fluoride", "Cryolite"}) {
            assertEquals(Boolean.TRUE, GT6MaterialHazardData.knownAcidFlag(name), name);
        }
        for (String name : new String[]{"unknown_acid", "acidproof_alloy", "unknown_calcite",
                "cryolite_solution", "thirdparty:cryolite", "thirdparty:iron", "Any Unknown Family", ""}) {
            assertNull(GT6MaterialHazardData.knownAcidFlag(name), name);
            assertFalse(GT6MaterialHazardData.isAcid(name), name);
        }
        assertNull(GT6MaterialHazardData.knownAcidFlag(null));
        assertEquals(Boolean.FALSE, GT6MaterialHazardData.knownAcidFlag("iron"));
        assertEquals(Boolean.FALSE, GT6MaterialHazardData.knownAcidFlag("water"));
        assertEquals(Boolean.FALSE, GT6MaterialHazardData.knownAcidFlag("aluminum_brass"));
        assertEquals(Boolean.FALSE, GT6MaterialHazardData.knownAcidFlag("iron_iii_oxide"));
    }
}
