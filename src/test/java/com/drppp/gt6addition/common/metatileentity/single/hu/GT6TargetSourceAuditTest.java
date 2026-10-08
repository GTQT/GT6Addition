package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Independent source targets/units. This is not a claim that every external
 * binding or all full-pack input forms have been exercised in a game world.
 */
class GT6TargetSourceAuditTest {
    private static final long SOURCE_U = 648648000L;

    @Test
    void all1084LiteralHotAndColdTargetsAndAmountsMatchSource() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/gt6-target-source-values.txt");
        assertNotNull(stream);
        Set<Integer> ids = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] f = line.split("\\|", -1);
                assertEquals(9, f.length, line);
                int id = Integer.parseInt(f[0]);
                assertTrue(ids.add(id), line);
                assertEquals(normalize(f[1]), normalize(GT6MaterialIdentity.name(id)), line);
                String name = GT6MaterialIdentity.mechanicsName(id);
                int hotId = Integer.parseInt(f[3]), coldId = Integer.parseInt(f[6]);
                assertEquals(normalize(f[4]), normalize(GT6MaterialIdentity.name(hotId)), line);
                assertEquals(normalize(f[7]), normalize(GT6MaterialIdentity.name(coldId)), line);
                long hotAmount = Long.parseLong(f[5]), coldAmount = Long.parseLong(f[8]);
                CrucibleSmeltingRule hot = CrucibleSmeltingRule.find(name);
                if (hotAmount > 0) {
                    String actual = hot == null || hot.target == null ? name : hot.target;
                    assertEquals(identity(GT6MaterialIdentity.mechanicsName(hotId)), identity(actual), "Hot " + line);
                    if (hot == null) assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget(name), line);
                } else {
                    assertNotNull(hot, "An explicitly disabled target is not a default: " + line);
                    // A zero-output rule never resolves/consumes its named target.
                }
                for (long amount : new long[]{1, GTValues.M, GTValues.M + 1L, 17 * GTValues.M + 13L,
                        Long.MAX_VALUE / 4L}) {
                    BigInteger expected = BigInteger.valueOf(amount).multiply(BigInteger.valueOf(hotAmount))
                            .divide(BigInteger.valueOf(SOURCE_U));
                    assertEquals(expected.longValueExact(), hot == null ? amount : hot.convert(amount), line);
                    for (int remainder : new int[]{0, 1, CrucibleFluidUnits.STORAGE_UNIT - 1}) {
                        BigInteger stored = BigInteger.valueOf(amount)
                                .multiply(BigInteger.valueOf(CrucibleFluidUnits.STORAGE_UNIT))
                                .add(BigInteger.valueOf(remainder)).multiply(BigInteger.valueOf(hotAmount))
                                .divide(BigInteger.valueOf(SOURCE_U));
                        CrucibleFluidUnits.Quantity converted = hot == null ?
                                CrucibleFluidUnits.scaleStored(amount, remainder, 1, 1) : hot.convertStored(amount, remainder);
                        assertNotNull(converted, line);
                        BigInteger[] parts = stored.divideAndRemainder(BigInteger.valueOf(CrucibleFluidUnits.STORAGE_UNIT));
                        assertEquals(parts[0].longValueExact(), converted.amount, line);
                        assertEquals(parts[1].intValueExact(), converted.remainder, line);
                    }
                }
                // Every reviewed positive-ID solidifying relation is one U;
                // source proves that fact, not an inferred reverse-smelting rule.
                assertEquals(SOURCE_U, coldAmount, line);
                String cold = CrucibleSolidifyingRule.target(name);
                assertEquals(identity(GT6MaterialIdentity.mechanicsName(coldId)),
                        identity(cold == null ? name : cold), "Cold " + line);
                if (cold == null) assertTrue(CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name), line);
            }
        }
        assertEquals(1084, ids.size(), "Do not reduce literal source scope or certify unresolved records");
    }

    private static String identity(String name) {
        return GT6MaterialIdentity.canonicalOreName(GT6MaterialIdentity.canonicalOxideName(normalize(name)));
    }

    private static String normalize(String name) {
        return name == null ? null : name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
