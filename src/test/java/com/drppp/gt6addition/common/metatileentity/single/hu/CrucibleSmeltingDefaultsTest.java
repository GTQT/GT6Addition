package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrucibleSmeltingDefaultsTest {
    @Test void knownElementsAndAlloysRetainAuthoritativeSelfDefaults() {
        for (String name : new String[]{"gold", "tin", "lead", "steel", "stainless_steel", "carbon", "osmium"}) {
            assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget(name), name);
        }
    }

    @Test void defaultMembershipIncludesVerifiedOxideAndElementAliases() {
        assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget("Banded Iron"));
        assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget("aluminum"));
        assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget("lanthanum"));
    }

    @Test void explicitRatiosAndDisabledTargetsAlwaysWin() {
        for (String name : new String[]{"diamond", "coal", "rubber", "pyrolusite", "hexorium_blue", "clay"}) {
            assertFalse(CrucibleSmeltingRule.hasDefaultSelfTarget(name), name);
            assertNotNull(CrucibleSmeltingRule.find(name), name);
        }
    }

    @Test void defaultSelfDoesNotGrantMeltingOrBurningExemption() {
        assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget("dolamide"));
        assertFalse(CrucibleSmeltingRule.hasMeltingFlag("dolamide"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("dolamide"));
    }

    @Test void unknownOrQualifiedLookupKeysDoNotExpandTheSnapshot() {
        for (String name : new String[]{"thirdparty_gold", "thirdparty:gold", "gregtech:gold", ""}) {
            assertFalse(CrucibleSmeltingRule.hasDefaultSelfTarget(name), name);
        }
        assertFalse(CrucibleSmeltingRule.hasDefaultSelfTarget(null));
    }
}
