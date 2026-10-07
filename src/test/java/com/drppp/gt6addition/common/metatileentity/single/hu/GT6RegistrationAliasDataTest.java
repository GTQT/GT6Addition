package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Frozen source-call inventory, independent of the production lookup table. */
class GT6RegistrationAliasDataTest {
    private static List<String[]> sourceCalls() throws Exception {
        InputStream stream = GT6RegistrationAliasDataTest.class.getResourceAsStream("/gt6-registration-aliases.txt");
        assertNotNull(stream);
        List<String[]> calls = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) calls.add(line.split("\\|"));
        }
        return calls;
    }

    private static String normalized(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    @Test
    void everyLiteralSourceContractUsesItsNativeGt6IdNotACeuId() throws Exception {
        List<String[]> calls = sourceCalls();
        assertEquals(209, calls.size());
        Set<String> unique = new HashSet<>();
        for (String[] call : calls) {
            assertEquals(5, call.length);
            String alias = normalized(call[0]), nativeName = normalized(call[2]);
            int id = Integer.parseInt(call[1]);
            unique.add(alias);
            assertTrue(id > 0 && id < 10000);
            assertTrue(Integer.parseInt(call[3]) > 0);
            assertEquals(nativeName, normalized(GT6MaterialIdentity.name(id)), call[0]);
            assertEquals(Integer.valueOf(id), GT6RegistrationAliasData.identities().get(alias), call[0]);
            assertEquals(nativeName, GT6RegistrationAliasData.canonicalName(alias), call[0]);
        }
        assertEquals(202, unique.size());
        assertEquals(unique, GT6RegistrationAliasData.identities().keySet());
        assertThrows(UnsupportedOperationException.class, () -> GT6RegistrationAliasData.identities().put("invented", 1));
    }

    @Test
    void aliasesUseTheSamePhaseTargetsFlagsDensityAndOreMultipliers() throws Exception {
        for (String[] call : sourceCalls()) {
            String alias = normalized(call[0]);
            // The raw GT6 name Osmium is Germanium; CEu's material path is
            // elemental osmium. Raw recycling names have a separate adapter.
            if ("osmium".equals(alias)) continue;
            String nativeName = normalized(call[2]);
            if ("phosphorus".equals(nativeName)) nativeName = "tricalciumphosphate";
            assertTrue(CrucibleSolidifyingRule.isSnapshotMaterial(alias), alias);
            assertEquals(CrucibleSolidifyingRule.target(nativeName), CrucibleSolidifyingRule.target(alias), alias);
            assertEquals(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(nativeName),
                    CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(alias), alias);
            CrucibleSmeltingRule nativeRule = CrucibleSmeltingRule.find(nativeName);
            CrucibleSmeltingRule aliasRule = CrucibleSmeltingRule.find(alias);
            if (nativeRule == null) assertNull(aliasRule, alias);
            else {
                assertNotNull(aliasRule, alias);
                assertEquals(nativeRule.target, aliasRule.target, alias);
                assertEquals(nativeRule.convert(90720000L), aliasRule.convert(90720000L), alias);
            }
            assertEquals(CrucibleMaterialPhaseData.knownMeltingPoint(nativeName),
                    CrucibleMaterialPhaseData.knownMeltingPoint(alias), alias);
            assertEquals(CrucibleMaterialPhaseData.boilingPoint(nativeName),
                    CrucibleMaterialPhaseData.boilingPoint(alias), alias);
            assertEquals(GT6MaterialHazardData.knownCombustionFlags(nativeName),
                    GT6MaterialHazardData.knownCombustionFlags(alias), alias);
            assertEquals(GT6MaterialHazardData.knownAcidFlag(nativeName), GT6MaterialHazardData.knownAcidFlag(alias), alias);
            assertEquals(GT6MaterialHazardData.shouldBurn(nativeName, 314, true),
                    GT6MaterialHazardData.shouldBurn(alias, 314, true), alias);
            assertEquals(GT6OreMultiplierData.multiplier(nativeName, 99), GT6OreMultiplierData.multiplier(alias, 99), alias);
            // Consistency of the common resolver, not proof that every native
            // density/thermal datum has already been ported from the source.
            assertEquals(CrucibleTransferLogic.hasKnownGt6MaterialDensity(nativeName),
                    CrucibleTransferLogic.hasKnownGt6MaterialDensity(alias), alias);
            assertEquals(CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(nativeName),
                    CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(alias), 0.000001, alias);
        }
    }

    @Test
    void symbolsLabelsMembershipsAndNamespacesAreNotRegistrationAliases() {
        for (String name : new String[]{"cr", "ge", "ti", "anysapphire", "anywood", "silverwood",
                "gregtech:chrome", "thirdparty:ptfe", "chrome_like", "gyubnera_mixture"}) {
            assertEquals(name, GT6RegistrationAliasData.canonicalName(name));
            assertFalse(GT6RegistrationAliasData.identities().containsKey(name));
        }
        for (String name : new String[]{"gregtech:chrome", "thirdparty:ptfe", "chrome_like", "gyubnera_mixture"}) {
            assertFalse(CrucibleSolidifyingRule.isSnapshotMaterial(name), name);
            assertEquals(name, GT6MaterialIdentity.hostRecyclingName(name));
            assertEquals(99, GT6OreMultiplierData.multiplier(name, 99));
        }
        assertEquals("catseye", GT6MaterialIdentity.canonicalOreName("cat'seye"));
    }

    @Test
    void rawSourceAndHostElementNamesNeverConflateOsmiumOrPhosphorus() {
        assertEquals("germanium", GT6RegistrationAliasData.canonicalName("osmium"));
        assertEquals("germanium", GT6MaterialIdentity.hostRecyclingName("Osmium"));
        assertEquals("osmium", GT6MaterialIdentity.canonicalOreName("osmium"));
        assertEquals(3306, CrucibleMaterialPhaseData.knownMeltingPoint("osmium"));
        assertEquals(1211, CrucibleMaterialPhaseData.knownMeltingPoint("germanium"));
        assertEquals("gregtech:osmium", GT6MaterialIdentity.hostRecyclingName("gregtech:osmium"));
        assertEquals("gregtech:tricalcium_phosphate", GT6MaterialIdentity.hostRecyclingName("Phosphorous"));
        assertEquals("gregtech:phosphorus", GT6MaterialIdentity.hostRecyclingName("Phosphor"));
        assertEquals(317, CrucibleMaterialPhaseData.knownMeltingPoint("phosphorus"));
        assertEquals(829, CrucibleMaterialPhaseData.knownMeltingPoint("phosphorous"));
        assertEquals(1820D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("phosphorus"));
        assertEquals(1070.05728D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("phosphorous"), 0.000001);
    }

    @Test
    void polymersUseTheGt6ApproximateCh2ConfigurationAndSelfYield() {
        double expectedDensity = (2267D + 2 * 0.08988D) / 3;
        for (String name : new String[]{"plastic", "teflon", "polymer", "ptfe", "polytetrafluoroethylene",
                "pvc", "polyvinyl_chloride", "bakelite", "hardplastic", "polycarbonate"}) {
            assertEquals(expectedDensity, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name), 0.000001, name);
            assertEquals(423, CrucibleMaterialPhaseData.knownMeltingPoint(name), name);
            assertEquals(846, CrucibleMaterialPhaseData.boilingPoint(name), name);
            assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name), name);
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            assertNotNull(rule, name);
            assertNull(rule.target, name);
            assertEquals(2 * 90720000L, rule.convert(3 * 90720000L), name);
            assertEquals(Integer.valueOf(1), GT6MaterialHazardData.knownCombustionFlags(name), name);
            assertFalse(GT6MaterialHazardData.isExplosive(name), name);
            assertFalse(GT6MaterialHazardData.shouldBurn(name, 424, true), name);
            assertEquals(1, GT6OreMultiplierData.multiplier(name, 99), name);
        }
        assertEquals("gregtech:polytetrafluoroethylene", GT6MaterialIdentity.hostRecyclingName("PTFE"));
        assertEquals("gregtech:polyvinyl_chloride", GT6MaterialIdentity.hostRecyclingName("PVC"));
    }

    @Test
    void chromeUsesChromiumSourceStatisticsRatherThanHostFallbacks() {
        assertEquals(7150D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("chrome"));
        assertEquals(2180, CrucibleMaterialPhaseData.knownMeltingPoint("chrome"));
        assertEquals(2944, CrucibleMaterialPhaseData.boilingPoint("chrome"));
        assertTrue(GT6ElementPhaseData.hasMeltingFlag("chrome"));
        assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget("chrome"));
        assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget("chrome"));
        assertEquals(Integer.valueOf(0), GT6MaterialHazardData.knownCombustionFlags("chrome"));
    }
}
