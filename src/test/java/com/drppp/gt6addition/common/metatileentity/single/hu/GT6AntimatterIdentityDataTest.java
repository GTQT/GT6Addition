package com.drppp.gt6addition.common.metatileentity.single.hu;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GT6AntimatterIdentityDataTest {
    @Test
    void all418AmDeclarationsKeepTheirIdentityThermalDensityAndDefaultTarget() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/gt6-antimatter-source-values.txt");
        assertNotNull(stream);
        Set<Integer> ids = new HashSet<>();
        Set<Integer> mtIds = literalMtIds();
        int meltingFlags = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\\|", -1);
                assertEquals(6, fields.length, line);
                int id = Integer.parseInt(fields[0]);
                assertTrue(ids.add(id), "duplicate AM id " + line);
                assertFalse(mtIds.contains(id), "AM and MT identities must not collide: " + line);
                String name = fields[1];
                int melting = Integer.parseInt(fields[2]);
                int boiling = Integer.parseInt(fields[3]);
                double densityGramsPerCubicCentimeter = Double.parseDouble(fields[4]);
                boolean meltingFlag = "1".equals(fields[5]);

                GT6AntimatterIdentityData.Profile profile = GT6AntimatterIdentityData.find(id);
                assertNotNull(profile, line);
                assertEquals(melting, profile.melting, line);
                assertEquals(boiling, profile.boiling, line);
                assertEquals(densityGramsPerCubicCentimeter, profile.densityGramsPerCubicCentimeter, 1e-12, line);
                if (GT6AntimatterIdentityData.isAmbiguousId(id)) {
                    assertNull(GT6MaterialIdentity.name(id), "ambiguous GT6 numeric ID " + line);
                    assertNull(GT6MaterialIdentity.mechanicsName(id), "ambiguous GT6 numeric ID " + line);
                } else {
                    assertEquals(normalize(name), normalize(GT6MaterialIdentity.mechanicsName(id)), line);
                    assertEquals(normalize(name), normalize(GT6MaterialIdentity.recyclingName(id)), line);
                    assertNull(GT6MaterialIdentity.name(id), "AM IDs remain separate from the MT inventory: " + line);
                }
                assertEquals(melting, CrucibleMaterialPhaseData.knownMeltingPoint(name), line);
                assertEquals(boiling, CrucibleMaterialPhaseData.boilingPoint(name), line);
                assertEquals(densityGramsPerCubicCentimeter * 1000D,
                        CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name), 1e-6, line);
                assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name), line);
                assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget(name), line);
                assertFalse(GT6MaterialHazardData.shouldBurn(name, 10_000, true), line);
                assertEquals(meltingFlag, GT6DeclaredPhaseData.hasBurningExemption(name), line);
                if (meltingFlag) meltingFlags++;
            }
        }
        assertEquals(418, ids.size());
        assertTrue(GT6AntimatterIdentityData.isAmbiguousId(1580));
        assertNull(GT6MaterialIdentity.name(1580));
        assertEquals("Anti-Unpentoctium", GT6AntimatterIdentityData.find(1580).name);
        assertEquals(1, meltingFlags, "Only Anti-Adamantium explicitly carries MELTING in AM.java");
    }

    @Test
    void unknownAndNegativeIdsDoNotBecomeAntimatterOrHostMaterialIdentities() {
        for (int id : new int[]{-1, 0, 4006, 7991, 30012, 1580}) {
            assertNull(GT6MaterialIdentity.name(id), "id " + id);
            assertNull(GT6MaterialIdentity.mechanicsName(id), "id " + id);
        }
    }

    @Test
    void allLiteralAmAliasesResolveToTheirExactCanonicalIdentity() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/gt6-antimatter-source-aliases.txt");
        assertNotNull(stream);
        Map<String, String> runtimeAliases = GT6AntimatterIdentityData.aliases();
        Set<String> seen = new HashSet<>();
        int sourceRows = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\\|", -1);
                assertEquals(2, fields.length, line);
                String alias = normalize(fields[0]);
                String target = normalize(fields[1]);
                assertTrue(seen.add(alias) || alias.equals("antiquicksilver"), "duplicate source alias " + line);
                assertEquals(target, GT6AntimatterIdentityData.canonicalName(fields[0]), line);
                assertEquals(target, GT6MaterialIdentity.canonicalOreName(alias), line);
                assertEquals(target, normalize(GT6MaterialIdentity.hostRecyclingName(fields[0])), line);
                assertEquals(target, runtimeAliases.get(alias), line);
                assertTrue(GT6AntimatterIdentityData.containsName(fields[1]), line);
                assertEquals(CrucibleMaterialPhaseData.knownMeltingPoint(fields[1]),
                        CrucibleMaterialPhaseData.knownMeltingPoint(fields[0]), line);
                assertEquals(CrucibleMaterialPhaseData.boilingPoint(fields[1]),
                        CrucibleMaterialPhaseData.boilingPoint(fields[0]), line);
                assertEquals(CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(fields[1]),
                        CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(fields[0]), 1e-6, line);
                assertEquals(GT6MaterialHazardData.knownCombustionFlags(fields[1]),
                        GT6MaterialHazardData.knownCombustionFlags(fields[0]), line);
                assertEquals(GT6MaterialHazardData.knownAcidFlag(fields[1]),
                        GT6MaterialHazardData.knownAcidFlag(fields[0]), line);
                assertEquals(GT6DeclaredPhaseData.hasBurningExemption(fields[1]),
                        GT6DeclaredPhaseData.hasBurningExemption(fields[0]), line);
                assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget(fields[0]), line);
                assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(fields[0]), line);
                sourceRows++;
            }
        }
        assertEquals(22, sourceRows);
        assertEquals(21, runtimeAliases.size());
        assertEquals("positron", GT6MaterialIdentity.canonicalOreName("antielectron"));
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("Anti-Adamantine"));
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("Anti-Bibibium"));
    }

    @Test
    void numericAmRecyclingIdsResolveByNameButAmbiguousIdsAreRejected() {
        NBTTagCompound unambiguous = recyclingEntry(4004);
        String[] resolved = {null};
        assertTrue(CrucibleRecyclingOverride.parse(unambiguous, name -> {
            resolved[0] = name;
            return null;
        }).isEmpty());
        assertEquals(GT6MaterialIdentity.hostRecyclingName("Anti-Proton"), resolved[0]);

        NBTTagCompound ambiguous = recyclingEntry(1580);
        int[] lookups = {0};
        assertTrue(CrucibleRecyclingOverride.parse(ambiguous, name -> {
            lookups[0]++;
            return null;
        }).isEmpty());
        assertEquals(0, lookups[0], "Do not resolve the colliding MT.Upo/AM identity by guess");
    }

    private static NBTTagCompound recyclingEntry(int materialId) {
        NBTTagCompound entry = new NBTTagCompound();
        entry.setShort("i", (short) materialId);
        entry.setLong("a", 648648000L);
        NBTTagCompound list = new NBTTagCompound();
        list.setInteger("size", 1);
        list.setTag("0", entry);
        NBTTagCompound root = new NBTTagCompound();
        root.setTag(CrucibleRecyclingOverride.TAG, list);
        return root;
    }

    private static Set<Integer> literalMtIds() throws Exception {
        InputStream stream = GT6AntimatterIdentityDataTest.class.getResourceAsStream("/gt6-density-source-values.txt");
        assertNotNull(stream);
        Set<Integer> ids = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                ids.add(Integer.parseInt(line.substring(0, line.indexOf('|'))));
            }
        }
        assertEquals(1084, ids.size());
        return ids;
    }

    private static String normalize(String name) {
        assertNotNull(name);
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
