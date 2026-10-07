package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GT6TechnicalMaterialDataTest {
    private static final long U = 648648000L;
    // Independent inventory from ANY.java initialization, not generated from
    // the production profiles. Columns: family, stats, hot, numerator,
    // denominator, cold. '-' is constructor self/U; '!' is disabled smelting.
    private static final String[] CONTRACTS = {
            "anyglowstone|glowstone|gregtech:glowstone|1|1|gregtech:glowstone",
            "anydiamond|diamond|carbon|2|1|gregtech:diamond",
            "anysapphire|sapphire|alumina|3|4|gregtech:sapphire",
            "anyemerald|emerald|beryllium|1|36|gregtech:emerald",
            "anyamethyst|amethyst|!|0|1|gregtech:amethyst",
            "anygarnet|spessartine|-|1|1|-",
            "anyjasper|jasper|-|1|1|-",
            "anytigereye|tigereye|-|1|1|-",
            "anyaventurine|greenaventurine|-|1|1|-",
            "anyamber|amber|-|1|1|-",
            "anyfluorite|fluorite|gtqtcore:fluorite|1|1|gtqtcore:fluorite",
            "anyphosphorus|tricalciumphosphate|gregtech:tricalcium_phosphate|1|1|gregtech:tricalcium_phosphate",
            "anyblaze|blaze|-|1|1|-",
            "anyprismarine|prismarine|gt6addition:prismarine|1|1|gt6addition:prismarine",
            "anygrains|wheat|gregtech:wheat|1|1|gregtech:wheat",
            "anyflour|wheat|gregtech:wheat|1|1|gregtech:wheat",
            "anyflourorgrains|wheat|gregtech:wheat|1|1|gregtech:wheat",
            "anywax|wax|gt6addition:wax|1|1|gt6addition:wax",
            "anystone|stone|gregtech:stone|1|1|gregtech:stone",
            "anycalcite|calcite|gregtech:calcite|1|1|gregtech:calcite",
            "anyclay|clay|ceramic|1|1|gregtech:clay",
            "anysalt|salt|gregtech:salt|1|1|gregtech:salt",
            "anyiron|iron|gregtech:iron|1|1|gregtech:iron",
            "anyironorsteel|iron|gregtech:iron|1|1|gregtech:iron",
            "anyironsteel|steel|gregtech:steel|1|1|gregtech:steel",
            "anyblacksteel|blacksteel|gregtech:black_steel|1|1|gregtech:black_steel",
            "anybluesteel|bluesteel|gregtech:blue_steel|1|1|gregtech:blue_steel",
            "anyredsteel|redsteel|gregtech:red_steel|1|1|gregtech:red_steel",
            "anymagiciron|manasteel|gregtech:iron|1|1|gregtech:iron",
            "anycopper|copper|gregtech:copper|1|1|gregtech:copper",
            "anyashes|ash|gregtech:ash|1|1|gregtech:ash",
            "anycarbon|carbon|gregtech:carbon|1|1|gregtech:carbon",
            "anycoalcarbon|carbon|gregtech:carbon|1|1|gregtech:carbon",
            "anysilicon|silicon|gregtech:silicon|1|1|gregtech:silicon",
            "anysilicondioxide|silicondioxide|gregtech:silicon_dioxide|1|1|gregtech:silicon_dioxide",
            "quartz|milkyquartz|gregtech:silicon_dioxide|1|1|gregtech:silicon_dioxide",
            "anysand|sand|glass|1|1|gt6addition:sand",
            "anytungsten|tungsten|gregtech:tungsten|1|1|gregtech:tungsten",
            "anythaumiccrystal|infuseddull|-|1|1|-",
            "hexorium|hexoriumwhite|-|1|1|-",
            "anywood|wood|ash|1|4|gregtech:wood",
            "anydefaultwood|wood|ash|1|4|gregtech:wood",
            "anynormalwood|wood|ash|1|4|gregtech:wood",
            "anymagicalwood|wood|ash|1|4|gregtech:wood",
            "anytreatedwood|wood|ash|1|4|gregtech:wood",
            "anyuntreatedwood|wood|ash|1|4|gregtech:wood",
            "anywoodorplastic|wood|-|1|1|-",
            "anyrubber|rubber|gregtech:rubber|2|3|gregtech:rubber",
            "anyplastic|plastic|gregtech:plastic|2|3|gregtech:plastic",
            "anyhardplastic|hardplastic|gregtech:plastic|2|3|gregtech:plastic",
            "anysteel|-|-|1|1|-",
            "anybronze|-|-|1|1|-",
            "anymetal|-|-|1|1|-"
    };

    @Test void all53FamiliesCopyHotAndColdTargetsIndependentlyIncludingQuantities() {
        Set<String> names = new HashSet<>();
        int copied = 0, defaults = 0, disabled = 0;
        for (String contract : CONTRACTS) {
            String[] parts = contract.split("\\|", -1);
            String name = parts[0];
            assertTrue(names.add(name));
            GT6TechnicalMaterialData.Profile profile = GT6TechnicalMaterialData.find(name);
            assertNotNull(profile, name);
            assertEquals("-".equals(parts[1]) ? null : parts[1], profile.statsSource, name);
            CrucibleSmeltingRule hot = CrucibleSmeltingRule.find(name);
            boolean defaultSelf = "-".equals(parts[2]);
            int numerator = Integer.parseInt(parts[3]), denominator = Integer.parseInt(parts[4]);
            if (defaultSelf) {
                defaults++;
                assertNull(hot, name);
                assertNull(profile.targetsSource, name);
            } else {
                copied++;
                if (numerator == 0) disabled++;
                String targetSource = "anymagiciron".equals(name) ? "iron" : "quartz".equals(name) ?
                        "silicondioxide" : "anyhardplastic".equals(name) ? "plastic" : parts[1];
                assertEquals(targetSource, profile.targetsSource, name);
                assertNotNull(profile.targetsSource, name);
                assertNotNull(hot, name);
                assertEquals("!".equals(parts[2]) ? "" : parts[2], hot.target, name);
                assertEquals(U * numerator / denominator, hot.convert(U), name);
            }
            assertEquals(defaultSelf, CrucibleSmeltingRule.hasDefaultSelfTarget(name), name);
            assertEquals(!defaultSelf && numerator > 0, CrucibleSmeltingRule.hasMeltingFlag(name), name);
            String cold = "-".equals(parts[5]) ? null : parts[5];
            assertEquals(cold, CrucibleSolidifyingRule.target(name), name);
            assertEquals(cold == null, CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(name), name);
            assertTrue(CrucibleSolidifyingRule.isSnapshotMaterial(name), name);
        }
        assertEquals(53, names.size());
        assertEquals(names, GT6TechnicalMaterialData.names());
        assertEquals(41, copied);
        assertEquals(12, defaults);
        assertEquals(1, disabled);
    }

    @Test void allFamiliesUseSourceHeatNotTheHotOutputOrTheDecorativeTexture() {
        Map<String, long[]> heat = nativeHeat();
        for (String contract : CONTRACTS) {
            String[] parts = contract.split("\\|");
            long[] expected = "-".equals(parts[1]) ? new long[]{1000, 3000} : heat.get(parts[1]);
            assertNotNull(expected, parts[0]);
            assertEquals(expected[0], CrucibleMaterialPhaseData.knownMeltingPoint(parts[0]), parts[0]);
            assertEquals(expected[1], CrucibleMaterialPhaseData.boilingPoint(parts[0]), parts[0]);
            assertEquals(expected[0], GT6DeclaredPhaseData.meltingPoint(parts[0]), parts[0]);
            assertEquals(expected[1], GT6DeclaredPhaseData.boilingPoint(parts[0]), parts[0]);
        }
        assertEquals(2311, CrucibleMaterialPhaseData.knownMeltingPoint("any_magic_iron"));
        assertEquals(1811, CrucibleMaterialPhaseData.knownMeltingPoint("iron"));
        assertEquals(3800, CrucibleMaterialPhaseData.knownMeltingPoint("any_coal_carbon"));
        assertEquals(1700, CrucibleMaterialPhaseData.knownMeltingPoint("coal"));
    }

    @Test void allFamiliesUseVerifiedNestedDensityOrConstructorDefaults() {
        Map<String, Double> density = nativeDensities();
        for (String contract : CONTRACTS) {
            String[] parts = contract.split("\\|");
            Double expected = "-".equals(parts[1]) ? 1000D : density.get(parts[1]);
            assertNotNull(expected, parts[0]);
            assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(parts[0]), parts[0]);
            assertEquals(expected, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(parts[0]), 1E-7, parts[0]);
            // The newly registered native density sources must agree too.
            if (!"-".equals(parts[1])) assertEquals(expected,
                    CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(parts[1]), 1E-7, parts[1]);
        }
        assertNotEquals(density.get("steel"), CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("any_steel"));
        assertEquals(density.get("hardplastic"),
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("polycarbonate"), 1E-7);
    }

    @Test void copyingOnlyLooksOrStatsDoesNotCopyMembersMeltingOrUnburnableTags() {
        for (String contract : CONTRACTS) {
            String[] parts = contract.split("\\|");
            boolean expected = !"-".equals(parts[2]) && Integer.parseInt(parts[3]) > 0;
            assertEquals(expected, GT6DeclaredPhaseData.hasBurningExemption(parts[0]), parts[0]);
        }
        assertTrue(GT6DeclaredPhaseData.hasBurningExemption("infused_dull"));
        assertFalse(GT6DeclaredPhaseData.hasBurningExemption("any_thaumic_crystal"));
        assertEquals(0, CrucibleSmeltingRule.find("hexorium_white").convert(U));
        assertTrue(CrucibleSmeltingRule.hasDefaultSelfTarget("hexorium"));
        assertFalse(CrucibleSmeltingRule.hasMeltingFlag("any_amethyst"));
    }

    @Test void aliasesAreExplicitAndUnknownNamesStayOutsideSnapshotAuthority() {
        for (String alias : new String[]{"Any Coal/Carbon", "AnyCoalCarbon", "ANY_COAL_CARBON"}) {
            assertSame(GT6TechnicalMaterialData.find("anycoalcarbon"), GT6TechnicalMaterialData.find(alias));
            assertEquals("gregtech:carbon", CrucibleSmeltingRule.find(alias).target);
            assertEquals(2267D, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(alias));
        }
        assertSame(GT6TechnicalMaterialData.find("Hexorium"), GT6TechnicalMaterialData.find("any_hexorium"));
        for (String name : new String[]{"AnyQuartz", "AnyFlourAndGrains", "any_ironish", "foreign:AnySteel", ""}) {
            assertFalse(GT6TechnicalMaterialData.contains(name), name);
            assertNull(CrucibleSmeltingRule.find(name), name);
            assertFalse(CrucibleSolidifyingRule.isSnapshotMaterial(name), name);
        }
        assertNull(GT6TechnicalMaterialData.find(null));
        assertThrows(UnsupportedOperationException.class, () -> GT6TechnicalMaterialData.names().clear());
    }

    @Test void copiedRatiosPreserveFractionalMaterialAndOverflowProtection() {
        for (String name : new String[]{"anyrubber", "anyplastic", "anyhardplastic", "anysapphire", "anyemerald", "anywood"}) {
            String[] row = null;
            for (String contract : CONTRACTS) if (contract.startsWith(name + "|")) row = contract.split("\\|");
            assertNotNull(row);
            int numerator = Integer.parseInt(row[3]), denominator = Integer.parseInt(row[4]);
            CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(name);
            for (long amount : new long[]{1, 17, U, Long.MAX_VALUE}) {
                for (int remainder : new int[]{0, 1, CrucibleFluidUnits.STORAGE_UNIT - 1}) {
                    CrucibleFluidUnits.Quantity actual = rule.convertStored(amount, remainder);
                    assertNotNull(actual, name);
                    BigInteger expected = BigInteger.valueOf(amount).multiply(BigInteger.valueOf(CrucibleFluidUnits.STORAGE_UNIT))
                            .add(BigInteger.valueOf(remainder)).multiply(BigInteger.valueOf(numerator))
                            .divide(BigInteger.valueOf(denominator));
                    assertEquals(expected, BigInteger.valueOf(actual.amount).multiply(BigInteger.valueOf(CrucibleFluidUnits.STORAGE_UNIT))
                            .add(BigInteger.valueOf(actual.remainder)), name);
                }
            }
        }
        assertEquals(-1, CrucibleSmeltingRule.find("anydiamond").convert(Long.MAX_VALUE));
    }

    private static Map<String, long[]> nativeHeat() {
        Map<String, long[]> values = new HashMap<>();
        values.put("glowstone", new long[]{500, 600});
        values.put("diamond", new long[]{4200, 4300});
        values.put("sapphire", new long[]{2345, 3250});
        values.put("emerald", average(29, new long[]{5, 3, 18, 3},
                new long[]{2345, 3250}, new long[]{1560, 2742}, new long[]{1986, 3220}, new long[]{54, 90}));
        values.put("amethyst", average(5, new long[]{4, 1}, new long[]{1986, 3220}, new long[]{1811, 3134}));
        values.put("spessartine", average(20, new long[]{5, 3, 9, 3}, new long[]{2345, 3250},
                new long[]{1519, 2334}, new long[]{1986, 3220}, new long[]{54, 90}));
        values.put("jasper", average(3, new long[]{2, 1}, new long[]{1986, 3220}, new long[]{1811, 3134}));
        values.put("tigereye", new long[]{1986, 3220});
        values.put("greenaventurine", new long[]{1986, 3220});
        values.put("amber", new long[]{473, 946});
        values.put("fluorite", new long[]{1633, 3266});
        values.put("tricalciumphosphate", average(5, new long[]{3, 2}, new long[]{1115, 1757}, new long[]{400, 800}));
        values.put("blaze", new long[]{4000, 8000});
        for (String name : new String[]{"prismarine", "wheat", "ash", "sand", "infuseddull", "hexoriumwhite"})
            values.put(name, new long[]{1000, 3000});
        values.put("wax", new long[]{350, 700});
        values.put("stone", new long[]{1100, 2200});
        values.put("calcite", new long[]{1612, 3000});
        values.put("clay", new long[]{2000, 4000});
        values.put("salt", new long[]{1074, 1686});
        values.put("iron", new long[]{1811, 3134});
        values.put("steel", new long[]{2046, 3134});
        long[] electrum = average(2, new long[]{1, 1}, new long[]{1234, 2435}, new long[]{1337, 3129});
        long[] blackBronze = average(5, new long[]{3, 2}, new long[]{1357, 2835}, electrum);
        long[] brass = {1160, 2835};
        long[] bismuthBronze = average(5, new long[]{1, 4}, new long[]{544, 1837}, brass);
        long[] sterling = average(5, new long[]{1, 4}, new long[]{1357, 2835}, new long[]{1234, 2435});
        long[] roseGold = average(5, new long[]{1, 4}, new long[]{1357, 2835}, new long[]{1337, 3129});
        long[] blackSteel = average(5, new long[]{1, 1, 3}, new long[]{1728, 3186}, blackBronze, values.get("steel"));
        values.put("blacksteel", blackSteel);
        values.put("bluesteel", average(8, new long[]{1, 1, 2, 4}, sterling, bismuthBronze, values.get("steel"), blackSteel));
        values.put("redsteel", average(8, new long[]{1, 1, 2, 4}, roseGold, brass, values.get("steel"), blackSteel));
        values.put("manasteel", new long[]{2311, 4134});
        values.put("copper", new long[]{1357, 2835});
        values.put("carbon", new long[]{3800, 4300});
        values.put("silicon", new long[]{1687, 3538});
        values.put("silicondioxide", new long[]{1986, 3220});
        values.put("milkyquartz", new long[]{1986, 3220});
        values.put("tungsten", new long[]{3695, 5828});
        values.put("wood", new long[]{400, 500});
        values.put("rubber", new long[]{410, 820});
        values.put("plastic", new long[]{423, 846});
        values.put("hardplastic", new long[]{423, 846});
        return values;
    }

    /** Source setMoleculeConfiguration weights divided by U, then normalized for heat. */
    private static long[] average(long divider, long[] weights, long[]... points) {
        double total = 0;
        for (long weight : weights) total += weight * U / divider;
        double melting = 0, boiling = 0;
        for (int i = 0; i < weights.length; i++) {
            long amount = weights[i] * U / divider;
            melting += points[i][0] * amount / total;
            boiling += points[i][1] * amount / total;
        }
        return new long[]{(long) melting, (long) boiling};
    }

    private static Map<String, Double> nativeDensities() {
        Map<String, Double> values = new HashMap<>();
        double silica = (2329.6 + 2 * 1.429) / 3, alumina = (2 * 2698 + 3 * 1.429) / 5;
        double phosphate = (1820 + 4 * 1.429) / 5;
        double phosphorite = (5 * 1540 + 3 * phosphate + 1.696) / 9;
        values.put("glowstone", (5 * phosphorite + 3 * 19282 + silica + 0.1785) / 10);
        values.put("diamond", 3530D);
        values.put("sapphire", 5 * alumina / 6);
        double emerald = alumina * (5 * U / 29) / U + 1850D * (3 * U / 29) / U +
                silica * (18 * U / 29) / U + 1.429 * (3 * U / 29) / U;
        values.put("emerald", emerald);
        values.put("amethyst", (4 * silica + 7874) / 5);
        values.put("spessartine", (5 * alumina + 3 * 7440 + 9 * silica + 3 * 1.429) / 20);
        values.put("jasper", (2 * silica + 7874) / 3);
        for (String name : new String[]{"tigereye", "greenaventurine", "stone", "sand", "silicondioxide", "milkyquartz"})
            values.put(name, silica);
        for (String name : new String[]{"amber", "blaze", "prismarine", "wheat", "wax", "ash", "infuseddull", "hexoriumwhite"})
            values.put(name, 1000D);
        values.put("fluorite", (1540 + 2 * 1.696) / 3);
        values.put("tricalciumphosphate", (3 * 1540 + 2 * phosphate) / 5);
        values.put("calcite", (1540 + 2267 + 3 * 1.429) / 5);
        values.put("clay", (2 * (5 * alumina + 12 * silica) / 18 + 1000) / 2);
        values.put("salt", (971 + 3.214) / 2);
        for (String name : new String[]{"iron", "steel", "manasteel"}) values.put(name, 7874D);
        double blackBronze = (3 * 8960 + 10501 + 19282) / 5D, brass = (3 * 8960 + 7134) / 4D;
        double blackSteel = (8912 + blackBronze + 3 * 7874) / 5;
        values.put("blacksteel", blackSteel);
        values.put("bluesteel", ((8960 + 4 * 10501) / 5D + (9807 + 4 * brass) / 5 + 2 * 7874 + 4 * blackSteel) / 8);
        values.put("redsteel", ((8960 + 4 * 19282) / 5D + brass + 2 * 7874 + 4 * blackSteel) / 8);
        values.put("copper", 8960D);
        values.put("carbon", 2267D);
        values.put("silicon", 2329.6);
        values.put("tungsten", 19250D);
        values.put("wood", (6 * 2267 + 15 * 1000) / 21D);
        values.put("rubber", (5 * 2267 + 8 * 0.08988) / 13D);
        values.put("plastic", (2267 + 2 * 0.08988) / 3);
        values.put("hardplastic", values.get("plastic"));
        return values;
    }
}
