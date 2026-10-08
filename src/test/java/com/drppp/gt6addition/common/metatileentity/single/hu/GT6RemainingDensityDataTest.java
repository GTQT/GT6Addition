package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Expected values use independent elemental constants and BigInteger
 * component division, never host properties or the production density table.
 */
class GT6RemainingDensityDataTest {
    private static final long U = 648648000L;
    private static final double FE = 7874, CU = 8960, SN = 7287, NI = 8912, AG = 10501, AU = 19282;
    private static final double O = 1.429, AL = 2698, SI = 2329.6, W = 19250, CR = 7150, MG = 1738;
    private static final double SILICA = (SI + 2 * O) / 3, ALUMINA = (2 * AL + 3 * O) / 5;
    private static final double RUBY = (5 * ALUMINA + CR) / 6;
    private static final double PYRITE = (FE + 2 * 2067D) / 3;
    private static final double REDSTONE = (5 * PYRITE + 3 * 13533.6 + SILICA + RUBY) / 10;
    private static final double ELECTRUM = (AG + AU) / 2, BRASS = (3 * CU + 7134) / 4;
    private static final double BRONZE = (3 * CU + SN) / 4;
    private static final double BLACK_BRONZE = (3 * CU + 2 * ELECTRUM) / 5;
    private static final double BISMUTH_BRONZE = (9807 + 4 * BRASS) / 5;
    private static final double BLACK_STEEL = (NI + BLACK_BRONZE + 3 * FE) / 5;
    private static final double STERLING = (CU + 4 * AG) / 5, ROSE_GOLD = (CU + 4 * AU) / 5;
    private static final double BLUE_STEEL = (STERLING + BISMUTH_BRONZE + 2 * FE + 4 * BLACK_STEEL) / 8;
    private static final double RED_STEEL = (ROSE_GOLD + BRASS + 2 * FE + 4 * BLACK_STEEL) / 8;
    private static final double DAMASCUS = mixture(50, new long[]{50, 1, 1}, FE, 6110, W);
    private static final double CERAMIC = (5 * ALUMINA + 12 * SILICA) / 18;
    private static final double OBSIDIAN = (MG + FE + 6 * SILICA + 4 * O) / 64;
    private static final double ENDER_PEARL = (1850 + 4 * 862 + 5 * 1.2506) / 10;
    private static final double GLOWSTONE = (5 * ((5 * 1540 + 3 * ((1820 + 4 * O) / 5) + 1.696) / 9)
            + 3 * AU + SILICA + .1785) / 10;
    private static final double PERIDOT = (2 * SILICA + FE + 2 * MG) / 5;

    private static double mixture(long divider, long[] weights, double... densities) {
        if (divider == 0) for (long weight : weights) divider += weight;
        double result = 0;
        for (int i = 0; i < weights.length; i++) {
            long amount = BigInteger.valueOf(U).multiply(BigInteger.valueOf(weights[i]))
                    .divide(BigInteger.valueOf(divider)).longValueExact();
            result += densities[i] * amount / U;
        }
        return result;
    }

    private static void density(String name, double expected) {
        assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(name), name);
        assertEquals(expected, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(name), 1e-6, name);
        assertEquals(expected, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 999999), 1e-6,
                "Registered fluid must not override MT density: " + name);
    }

    @Test void ironFormsCopyFinalStatisticsButHeatOnlyCopiesAreNotStatisticsCopies() {
        for (String name : new String[]{"wrought_iron", "deep_iron", "shadow_iron", "pig_iron", "iron_compressed",
                "iron_magnetic", "dark_iron", "meteoric_iron", "meteorite", "frozen_iron", "steel_magnetic",
                "meteoric_steel", "HSLA-Steel", "HSLA-Spring-Steel", "HSLA-Tungsten-Alloy", "terrasteel"})
            density(name, FE);
        // RefinedIron has a negative GT6 ID, but steal(WroughtIron) copies
        // mGramPerCubicCentimeter; it is not an unconfigured 1000 kg/m^3 case.
        density("refined_iron", FE);
        density("neodymium_magnetic", 7007);
        // AncientDebris.heat(MeteoricIron) does not copy its 7874 density.
        density("ancient_debris", 1000);
        assertNotEquals(FE, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("ancient_debris"));
    }

    @Test void copiedFantasyOreMetalsKeepTheirExactElementStatistics() {
        String[][] names = {{"annealed_copper", "infuscolium", "vyroxeres"}, {"alduorite"},
                {"rubracium", "tungsten_sintered"}, {"meutoite", "oureclase"}, {"lemurite"}, {"aredrite"},
                {"ceruclase"}, {"kalendrite"}, {"carmot"}, {"sanguinite"}, {"eximite"}, {"ignatius"}};
        double[] expected = {CU, AL, W, NI, MG, 11342, 6685, 21460, 7134, 13533.6, 7440, SN};
        for (int i = 0; i < names.length; i++) for (String name : names[i]) density(name, expected[i]);
    }

    @Test void coatedAndTraceMaterialsAddMassRatherThanNormalizeAtomTotals() {
        density("gilded_iron", FE + mixture(9, new long[]{1}, AU));
        density("steel_galvanized", FE + mixture(9, new long[]{1}, 7134));
        density("aluminium_alloy", AL + mixture(45, new long[]{1}, SI));
        density("damascus_steel", DAMASCUS);
        // Explicit steal(Steel/HSLA/SpringSteel) supersedes prior configs.
        assertNotEquals(FE / 2, CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("HSLA-Steel"));
        assertNotEquals(FE + REDSTONE * 2 / 45,
                CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("HSLA-Spring-Steel"));
    }

    @Test void copperAndBronzeAlloysKeepNativeNestedConfigurations() {
        density("bronze", BRONZE);
        density("angmallen", (AU + FE) / 2);
        density("GoldInductive", (AU + REDSTONE) / 2);
        density("Cd-In-Ag-Alloy", (8690 + 7310 + AG) / 3);
        density("cobalt_brass", (7 * BRASS + AL + 8860) / 9);
        density("hepatizon", (AU + BRONZE) / 2);
        density("arsenic_copper", (3 * CU + 5776) / 4);
        density("arsenic_bronze", (5776 + 4 * BRONZE) / 5);
        density("titanium_gold", (3 * 4540 + AU) / 4);
        density("tantalum_hafnium_carbide", (4 * 16654 + 13310 + 5 * 2267D) / 10);
    }

    @Test void structuralAndElectricalAlloysDoNotUseHostAtomicMass() {
        double invar = (2 * FE + NI) / 3;
        density("invar", invar);
        density("constantan", (CU + NI) / 2);
        density("nichrome", (4 * NI + CR) / 5);
        density("kanthal", (FE + AL + CR) / 3);
        density("magnalium", (MG + 2 * AL) / 3);
        density("stainless_steel", (4 * FE + 3 * invar + CR + 7440) / 9);
        density("ultimet", (5 * 8860 + NI + 2 * CR + 10220) / 9);
        density("tin_alloy", (SN + FE) / 2);
        density("battery_alloy", (4 * 11342 + 6685D) / 5);
        density("soldering_alloy", (9 * SN + 6685) / 10);
        density("vanadium_steel", (4 * FE + 6110) / 5);
        density("tungstensteel", (FE + W) / 2);
    }

    @Test void finalElementStatisticsOverrideRedstoneAndNikoliteConfigurations() {
        density("red_alloy", CU);
        density("blue_alloy", AG);
        density("purple_alloy", CU + AG);
        density("redstone_alloy", SI);
        density("nikoline_alloy", SI);
        density("electrotine_alloy", FE);
        density("conductive_iron", FE);
        density("electrum_flux", ELECTRUM);
        density("mingrade", (CU + REDSTONE) / 2);
    }

    @Test void meteoricAndFireMagicAlloysPreserveNativeNonNormalizedDividers() {
        density("meteoric_black_steel", BLACK_STEEL);
        density("meteoric_blue_steel", BLUE_STEEL);
        density("meteoric_red_steel", RED_STEEL);
        String[] flames = {"meteoflame_steel", "meteoflame_black_steel", "meteoflame_blue_steel",
                "meteoflame_red_steel", "flamascus_steel"};
        double[] expected = {FE, BLACK_STEEL, BLUE_STEEL, RED_STEEL, DAMASCUS};
        for (int i = 0; i < flames.length; i++) density(flames[i], expected[i]);
        for (String name : new String[]{"steeleaf", "knightmetal", "fiery_steel", "fireleaf", "thaumium"}) density(name, FE);
        density("ironwood", mixture(18, new long[]{8, 9, 2}, FE, (6 * 2267 + 15000D) / 21, (AU + FE) / 2));
    }

    @Test void magicOreAndPairAlloysUseFinalNestedStatistics() {
        double adamantine = (3 * 13356.24762 + 4 * O) / 7;
        double prometheum = (3 * 7260 + 4 * O) / 7;
        density("prometheum", prometheum);
        density("vulcanite", (CU + 6232) / 2);
        density("orichalcum", BRASS);
        density("astral_silver", AG);
        density("midasium", AU);
        density("mithril", 21460);
        density("celenegil", (21460 + BRASS) / 2);
        density("shadow_steel", (FE + MG) / 2);
        density("inolashite", (AL + 6685) / 2);
        density("haderoth", (21460 + W) / 2);
        density("desichalkos", (7440 + NI) / 2);
        density("tartarite", (adamantine + 21246.25421) / 2);
        density("amordrine", (prometheum + 21460) / 2);
    }

    @Test void superconductorsAndChromiumDioxideKeepExplicitDividers() {
        density("osmiridium", (22610 + 22560D) / 2);
        density("chromium_dioxide", CR + 2 * O);
        density("vanadium_gallium", (3 * 6110 + 5907D) / 4);
        density("yttrium_barium_cuprate", (4469 + 2 * 3594 + 3 * CU + 7 * O) / 6);
        density("niobium_nitride", (8570 + 1.2506) / 2);
        density("niobium_titanium", (8570 + 4540D) / 2);
        density("aluminium_brass", (3 * AL + CU) / 4);
        density("alumite", (5 * ALUMINA + 2 * FE + 18 * OBSIDIAN) / 9);
        density("manyullyn", (8860 + 1000D) / 2);
    }

    @Test void vibraniumFamiliesUseFictionalSourceElementDensity() {
        density("vibranium_steel", (3239.78365 + 3 * FE) / 4);
        density("vibranium_silver", (3239.78365 + 3 * AG) / 4);
        density("vibramantium", (3239.78365 + 3 * 13356.24762) / 4);
        density("trinaquadalloy", (6 * 1068.74 + 2 * 21000 + 2267) / 9);
        density("trinitanium", (2 * 1068.74 + 4540) / 3);
        density("titanium_iridium", (22560 + 4540D) / 2);
        density("titanium_aluminide", mixture(3, new long[]{3, 7}, 4540, AL));
    }

    @Test void enderIoAndThermalAlloysAreDensitySumsNotFlattenedAverages() {
        double inductive = (AU + REDSTONE) / 2;
        double base = (2 * SN + AG + 21460) / 4;
        density("signalum", (CU + 2 * AG + 5 * CU) / 8);
        density("lumium", (3 * SN + AG) / 4 + GLOWSTONE);
        density("enderium_base", base);
        density("enderium", base + ENDER_PEARL);
        density("bedrock_hsla_alloy", 4 * 13356.24762 + FE);
        density("pulsating_iron", FE + ENDER_PEARL);
        density("energetic_alloy", 2 * inductive + GLOWSTONE);
        density("vibrant_alloy", 2 * inductive + GLOWSTONE + ENDER_PEARL);
        density("electrical_steel", FE + SI);
        density("soularium", 9 * SILICA + AU);
        density("clay_compound", 2 * SILICA + CERAMIC);
        density("spectre_iron", FE + 1000);
    }

    @Test void highSpeedSteelFamiliesUseTheirAlreadyDividedPrerequisites() {
        double tungstenSteel = (FE + W) / 2;
        double hssg = (5 * tungstenSteel + CR + 2 * 10220 + 6110) / 9;
        density("HSS-G", hssg);
        density("HSS-E", (6 * hssg + 8860 + 7440 + SI) / 9);
        density("HSS-S", (6 * hssg + 2 * ((22610 + 22560D) / 2) + 22560) / 9);
    }

    @Test void ancientDebrisAndCrystalMatrixKeepExtremeSourceUnitDividers() {
        density("netherite", 4 * AU + 4000);
        density("netherized_diamond", AU + 1000 + 3530);
        density("crystal_matrix", 20 * 3530 + 2000);
        density("draconium_awakened", 1000);
    }

    @Test void nativeAlloysAndHostElementsRetainDifferentDensities() {
        density("duranium", 20000);
        density("tritanium", 25000);
        density("duranium_alloy", (7 * 20000 + MG) / 8);
        density("tritanium_alloy", (3 * 25000 + 20000D) / 4);
        density("phosphor", 1820);
        density("phosphorus", 1820); // Host element, not raw MT.Phosphorus.
        density("tricalcium_phosphate", (3 * 1540 + 2 * ((1820 + 4 * O) / 5)) / 5);
    }

    @Test void constructorDefaultsAreExplicitIdentitiesNotGuessedMetalDensities() {
        for (String name : new String[]{"dark_thaumium", "void_metal", "ardite", "gaia_spirit", "mauftrium",
                "ancient_debris", "efrine", "kreknorite", "syrmorite", "octine", "bedrockium", "draconium",
                "cosmic_neutronium", "infinity"}) density(name, 1000);
    }

    @Test void convertedGemsCopyActualSourceStatisticsButKeepSeparateIdentities() {
        double sodalite = mixture(11, new long[]{3, 3, 4, 1}, ALUMINA, SILICA, 971, 3.214);
        double lazurite = (6 * ALUMINA + 6 * SILICA + 8 * 1540 + 8 * 971D) / 28;
        density("redstonia", REDSTONE);
        density("palis", (12 * lazurite + 2 * sodalite + PYRITE + (1540 + 2267 + 3 * O) / 5) / 16);
        density("diamantine", 3530);
        density("emeradic", mixture(29, new long[]{5, 3, 18, 3}, ALUMINA, 1850, SILICA, O));
        density("enori", FE);
        assertEquals("Redstonia", GT6MaterialIdentity.name(8375));
        assertEquals("Palis", GT6MaterialIdentity.name(8376));
    }

    @Test void rocksUseConfiguredMineralsNotTheStoneGenerificationTarget() {
        double feldspar = (2 * 862 + 5 * ALUMINA + 18 * SILICA + O) / 26;
        double biotite = (2 * 862 + 6 * MG + 15 * ALUMINA + 4 * 1.696 + 18 * SILICA) / 45;
        double calcite = (1540 + 2267 + 3 * O) / 5;
        double mgCarbonate = (MG + 2267 + 3 * O) / 5;
        for (String name : new String[]{"granite_red", "granite_black", "granite"}) density(name, (biotite + feldspar + SILICA) / 3);
        for (String name : new String[]{"basalt", "gabbro"}) density(name, (PERIDOT + 3 * calcite + 8 * SILICA + 4000) / 16);
        density("komatiite", (PERIDOT + 2 * mgCarbonate + 6 * SILICA + 3000) / 12);
        density("pumice", mixture(11, new long[]{3, 2, 4, 2}, PERIDOT, mgCarbonate, SILICA, 1000));
        density("sky_stone", (2 * PERIDOT + 7007 + FE + 5 * OBSIDIAN) / 9);
        density("diatomite", (8 * SILICA + (2 * FE + 3 * O) / 5 + 5 * ALUMINA / 6) / 10);
    }

    @Test void garnetSandUsesSixIndependentMinerals() {
        double almandine = (5 * ALUMINA + 3 * FE + 9 * SILICA + 3 * O) / 20;
        double andradite = (3 * 1540 + 2 * FE + 9 * SILICA + 6 * O) / 20;
        double grossular = (5 * ALUMINA + 3 * 1540 + 9 * SILICA + 3 * O) / 20;
        double pyrope = (5 * ALUMINA + 3 * MG + 9 * SILICA + 3 * O) / 20;
        double spessartine = (5 * ALUMINA + 3 * 7440 + 9 * SILICA + 3 * O) / 20;
        double uvarovite = (3 * 1540 + 2 * CR + 9 * SILICA + 6 * O) / 20;
        density("garnet_sand", (almandine + andradite + grossular + pyrope + spessartine + uvarovite) / 6);
    }

    @Test void unconfiguredFluidsUseConstructorNotPhysicalGasOrForgeFluidDensity() {
        for (String name : new String[]{"biomass", "bio_fuel", "ethanol", "oil", "fuel", "kerosine", "diesel",
                "petrol", "propane", "butane", "propylene", "ethylene", "creosote", "whale_oil", "seed_oil",
                "hemp_oil", "lin_oil", "sunflower_oil", "nut_oil", "olive_oil", "frying_oil_hot", "glue",
                "lubricant", "construction_foam", "UU-Amplifier", "UU-Matter", "latex"}) density(name, 1000);
        density("nitro_fuel", (1500 + 4 * 1000D) / 5);
        assertFalse(CrucibleTransferLogic.isAirDensity(CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter("propane")));
    }

    @Test void aliasesAreExplicitButUnknownLookalikesDoNotAcquireDensity() {
        density("cupronickel", (CU + NI) / 2);
        density("Iritanium", (22560 + 4540D) / 2);
        density("TeslatineAlloy", SI);
        density("AluminumBrass", (3 * AL + CU) / 4);
        for (String name : new String[]{"some_stainless_steel", "nitro_fuel_mix", "infinity_like", "unknown_granite"}) {
            assertFalse(CrucibleTransferLogic.hasKnownGt6MaterialDensity(name), name);
            assertEquals(1200, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(name, 0), name);
        }
    }
}
