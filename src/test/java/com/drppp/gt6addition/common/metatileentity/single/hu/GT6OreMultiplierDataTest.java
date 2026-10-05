package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GT6OreMultiplierDataTest {
    @Test void explicitSelfTargetMultipliersDoNotUseHostRecipeYields() {
        check(2, "meteorite", "meteoric_iron", "meteoric_steel", "amber", "golden_amber", "dominican_amber",
                "zircon", "draconium", "borax", "cassiterite", "monazite", "scabyst", "moonstone");
        check(3, "bastnasite", "blue_phosphorus", "red_phosphorus", "white_phosphorus",
                "tricalcium_phosphate", "sodium_nitrate", "potassium_nitrate", "chimerite");
        check(4, "apatite", "bone", "sunstone");
        check(5, "lapis", "sodalite", "lazurite", "malachite", "azurite", "eudialyte");
        check(8, "perlite");
    }

    @Test void knownGt6DefaultOverridesHostMultiplier() {
        check(1, "coal", "lignite", "anthracite", "copper", "gold", "diamond", "emerald", "quartzite",
                "hematite", "rutile", "phosphorus");
    }

    @Test void mineralPhosphorusAndElementalPhosphorusRemainDistinct() {
        assertEquals(1, GT6OreMultiplierData.multiplier("phosphorus", 99));
        assertEquals(3, GT6OreMultiplierData.multiplier("tricalcium_phosphate", 99));
    }

    @Test void factoryMultipliersAreNotLimitedToRedstoneAndNetherQuartz() {
        check(4, "redstone", "nikolite", "glowstone", "glowstone_ceres", "glowstone_io", "glowstone_enceladus",
                "glowstone_proteus", "glowstone_pluto", "gloomstone");
        check(2, "milky_quartz", "nether_quartz", "void_quartz", "sunny_quartz", "lavender_quartz",
                "red_quartz", "blaze_quartz", "smokey_quartz", "mana_quartz", "elven_quartz",
                "quartz_black", "certus_quartz", "charged_certus_quartz", "fluix");
        check(2, "infused_dull", "infused_vis", "infused_air", "infused_fire", "infused_earth",
                "infused_water", "infused_entropy", "infused_order", "infused_balance");
        check(3, "hexorium_black", "hexorium_white");
        check(4, "hexorium_red", "hexorium_green", "hexorium_blue");
    }

    @Test void crushingOverridesKeepTheirOwnMultiplier() {
        check(3, "iron");
        check(2, "adamantium", "aluminium", "titanium", "tungsten", "uranium_238", "fluorine",
                "tantalum", "niobium", "enriched_naquadah", "dilithium");
        check(4, "naquadria");
    }

    @Test void verifiedSpellingAliasesHaveTheSameOreAmount() {
        check(2, "amber_golden", "amber_dominican", "black_quartz", "smoky_quartz", "quartz_smoky");
        check(3, "saltpeter", "salpeter", "nitrate");
        check(4, "electrotine", "teslatite");
    }

    @Test void unknownHostMaterialsRetainTheirOwnPositiveMultiplier() {
        assertEquals(7, GT6OreMultiplierData.multiplier("other_mod_mineral", 7));
        assertEquals(1, GT6OreMultiplierData.multiplier("other_mod_mineral", -1));
        assertEquals(1, GT6OreMultiplierData.multiplier(null, 0));
        // These declarations are commented out in this GT6 snapshot.
        assertEquals(7, GT6OreMultiplierData.multiplier("hexorium_brown", 7));
    }

    private static void check(long expected, String... names) {
        for (String name : names) assertEquals(expected, GT6OreMultiplierData.multiplier(name, 99), name);
    }
}
