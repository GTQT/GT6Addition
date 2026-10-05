package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongUnaryOperator;

/** Pure boundary calculations shared by the crucible transfer code and tests. */
public final class CrucibleTransferLogic {
    public static final int OBSIDIAN_MELTING_TEMPERATURE = 1_300;
    public static final int WATER_MELTING_TEMPERATURE = 273;
    public static final double GT6_AIR_DENSITY_LIMIT_KG_PER_CUBIC_METER = 1.2D;
    public static final double DEFAULT_UNKNOWN_MATERIAL_DENSITY_KG_PER_CUBIC_METER = 1_200.0D;
    public static final double GT6_IRON_DENSITY_KG_PER_CUBIC_METER = 7_874.0D;
    public static final double GT6_OSMIUM_DENSITY_KG_PER_CUBIC_METER = 22_610.0D;
    public static final double GT6_TUNGSTEN_CARBIDE_DENSITY_KG_PER_CUBIC_METER = 15_600.0D;
    private static final Map<String, Double> GT6_MATERIAL_DENSITIES = createGt6MaterialDensities();
    private static final Map<String, Double> REAL_WORLD_MATERIAL_DENSITIES = createRealWorldMaterialDensities();

    private CrucibleTransferLogic() {}

    /** Smeltery.fillMoldAtSide uses temperature and the hot target, not a cached phase flag. */
    static boolean canPourIntoMold(long amount, long temperature, int meltingPoint, boolean selfSmeltingTarget) {
        return amount > 0L && meltingPoint >= 0 && temperature >= meltingPoint && selfSmeltingTarget;
    }

    /** Offer all available units; the receiver determines its actual demand during simulation. */
    static long transferToMold(long available, LongUnaryOperator simulate, LongUnaryOperator commit) {
        if (available <= 0L) return 0L;
        long accepted = Math.min(available, simulate.applyAsLong(available));
        if (accepted <= 0L) return 0L;
        // Only the committed quantity may be debited. A failed/partial commit
        // must not consume the full simulated acceptance from the source.
        return Math.max(0L, Math.min(accepted, commit.applyAsLong(accepted)));
    }

    /** Required source amount for a casting, rounded up so a cast cannot create material. */
    static long requiredMoldInput(long outputAmount, long numerator, long denominator) {
        if (outputAmount <= 0L || numerator <= 0L || denominator <= 0L) return 0L;
        java.math.BigInteger required = java.math.BigInteger.valueOf(outputAmount)
                .multiply(java.math.BigInteger.valueOf(numerator))
                .add(java.math.BigInteger.valueOf(denominator - 1L))
                .divide(java.math.BigInteger.valueOf(denominator));
        return required.compareTo(java.math.BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? 0L : required.longValue();
    }

    public static boolean shouldFreezeWater(long temperature) {
        return temperature < WATER_MELTING_TEMPERATURE;
    }

    /** GT6 also applies cold targets to new contents, not just cooling melts. */
    static boolean shouldApplyColdTarget(long temperature, int meltingPoint, boolean molten,
                                         boolean newContent, boolean differentTarget) {
        return meltingPoint >= 0 && temperature < meltingPoint &&
                (molten || (newContent && differentTarget));
    }

    static boolean shouldApplyHotTarget(long temperature, long previousTemperature,
                                        int meltingPoint, boolean molten, boolean newContent) {
        return meltingPoint >= 0 && temperature >= meltingPoint &&
                (!molten || previousTemperature < meltingPoint || newContent);
    }

    enum IntakePhase { NONE, MELT, SOLIDIFY }

    /** Smeltery.addMaterialStacks changes identity only across the source's melting boundary. */
    static IntakePhase intakePhase(long incomingTemperature, long mixedTemperature, int meltingPoint) {
        if (meltingPoint < 0) return IntakePhase.NONE;
        if (incomingTemperature < meltingPoint && mixedTemperature >= meltingPoint) return IntakePhase.MELT;
        if (incomingTemperature >= meltingPoint && mixedTemperature < meltingPoint) return IntakePhase.SOLIDIFY;
        return IntakePhase.NONE;
    }

    /** GT6 Smeltery fluid-container temperature bound, including reversed bounds. */
    public static long fluidInputTemperature(long incoming, int meltingPoint, long boilingPoint) {
        // An undefined boiling point must not be fabricated from fluid temperature.
        if (meltingPoint < 0 || boilingPoint <= 0 || boilingPoint == Long.MAX_VALUE) return incoming;
        long first = (long) meltingPoint + 25L;
        long second = boilingPoint - 1L;
        // UT.Code.bind swaps the endpoints when melt+25 exceeds boil-1.
        return Math.max(Math.min(first, second), Math.min(Math.max(first, second), incoming));
    }

    /** GT6 Smeltery container extraction also respects the registered liquid's temperature. */
    static boolean canExtractFluidAtTemperature(long vesselTemperature, int meltingPoint, long fluidTemperature) {
        return meltingPoint >= 0 && vesselTemperature >= meltingPoint &&
                (fluidTemperature < 320L || vesselTemperature >= fluidTemperature);
    }

    public static int materialFluidAmount(long materialAmount, long materialUnit, int fluidUnit) {
        if (materialAmount <= 0 || materialUnit <= 0 || fluidUnit <= 0) return 0;
        java.math.BigInteger amount = java.math.BigInteger.valueOf(materialAmount)
                .multiply(java.math.BigInteger.valueOf(fluidUnit))
                .divide(java.math.BigInteger.valueOf(materialUnit));
        return amount.min(java.math.BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
    }

    public static long saturatingMaterialSum(long total, long amount) {
        total = Math.max(0, total);
        if (amount <= 0) return total;
        return amount > Long.MAX_VALUE - total ? Long.MAX_VALUE : total + amount;
    }

    /** Smeltery's UT.Code.scale(total, capacity, 255, false), including tiny contents. */
    static int displayHeight(long amount, long capacity) {
        if (amount <= 0 || capacity <= 0) return 0;
        if (amount >= capacity) return 255;
        return 1 + java.math.BigInteger.valueOf(amount).multiply(java.math.BigInteger.valueOf(254L))
                .divide(java.math.BigInteger.valueOf(capacity)).intValue();
    }

    public static boolean canMergeMaterialAmounts(long current, long incoming) {
        return current >= 0 && incoming > 0 && incoming <= Long.MAX_VALUE - current;
    }

    /** Bounded world-effect count; saturate before multiplication can overflow. */
    public static int boundedMaterialEffectCount(long amount, long materialUnit, int limit) {
        if (amount <= 0 || materialUnit <= 0 || limit <= 0) return 0;
        java.math.BigInteger count = java.math.BigInteger.valueOf(amount)
                .multiply(java.math.BigInteger.valueOf(9)).divide(java.math.BigInteger.valueOf(materialUnit));
        return count.max(java.math.BigInteger.ONE).min(java.math.BigInteger.valueOf(limit)).intValue();
    }

    /** Keep GT6's nine fire attempts per unit through a normal full vessel. */
    public static int materialHazardFireAttempts(long amount, long materialUnit, long capacity) {
        if (capacity <= 0 || materialUnit <= 0) return 0;
        int fullVessel = java.math.BigInteger.valueOf(capacity).multiply(java.math.BigInteger.valueOf(9))
                .divide(java.math.BigInteger.valueOf(materialUnit))
                .min(java.math.BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
        return boundedMaterialEffectCount(amount, materialUnit, fullVessel);
    }

    public static int contentExplosionStrength(long amount, long capacity) {
        if (amount <= 0 || capacity <= 0) return 0;
        if (amount >= capacity) return 6;
        // GT6 UT.Code.scale(amount, capacity, 6, false): five interior steps.
        long step = java.math.BigInteger.valueOf(amount).multiply(java.math.BigInteger.valueOf(5))
                .divide(java.math.BigInteger.valueOf(capacity)).longValue();
        return 1 + (int) step;
    }

    public static float vaporHeatDamage(long temperature) {
        return temperature > 320 ? 2.0F * (temperature - 300) / 50.0F : 0.0F;
    }

    /** GT6's capped temperature damage for contact (10) and retrieval (5). */
    public static float contactTemperatureDamage(long temperature, float cap) {
        if (cap <= 0 || Float.isNaN(cap)) return 0;
        double damage;
        if (temperature > 320) damage = (temperature - 300.0D) / 50.0D;
        else if (temperature < 260) damage = (270.0D - temperature) / 25.0D;
        else return 0;
        return (float) Math.max(1.0D, Math.min(cap, damage));
    }

    public static long remainingDurabilityMaterial(long amount, int damage, int maximumDamage) {
        if (amount <= 0 || maximumDamage <= 0 || damage >= maximumDamage) return 0;
        long remaining = maximumDamage - Math.max(0, damage);
        // Quotient/remainder form preserves floor rounding without overflowing amount * remaining.
        return amount / maximumDamage * remaining + amount % maximumDamage * remaining / maximumDamage;
    }

    public static int acceptedSharedFluidAmount(int offeredAmount, int availableCapacity) {
        return Math.min(Math.max(0, offeredAmount), Math.max(0, availableCapacity));
    }

    public static int acceptedQueueItems(int queuedItems, int requestedItems, int capacity) {
        return Math.min(Math.max(0, requestedItems), Math.max(0, capacity - queuedItems));
    }

    public static long requiredEnergyPerKelvin(double thermalMassKg) {
        double scaledMass = Math.max(0.0D, thermalMassKg) / 100.0D;
        // Keep GT6's 1 + floor(weight/100) for all representable masses.
        // Do not wrap the +1 into a negative heat cost for extreme old NBT.
        if (!Double.isFinite(scaledMass) || scaledMass >= Long.MAX_VALUE) return Long.MAX_VALUE;
        return 1L + (long) scaledMass;
    }

    public static long temperatureGainForHeat(long availableHeat, double thermalMassKg) {
        return Math.max(0L, availableHeat) / requiredEnergyPerKelvin(thermalMassKg);
    }

    public static long temperatureDeltaForEnergy(long storedEnergy, double thermalMassKg) {
        return storedEnergy / requiredEnergyPerKelvin(thermalMassKg);
    }

    public static int nextThermalCooldown(int currentCooldown, boolean heatedThisTick,
                                          int heatCooldownTicks, int passiveCooldownTicks) {
        if (heatedThisTick) {
            return heatCooldownTicks;
        }
        int cooldown = Math.max(0, currentCooldown - 1);
        return cooldown == 0 ? passiveCooldownTicks : cooldown;
    }

    public static boolean shouldPassivelyAdjustTemperature(int currentCooldown, boolean heatedThisTick) {
        return !heatedThisTick && currentCooldown <= 1;
    }

    public static long moveTemperatureTowardAmbient(long temperature, long ambientTemperature) {
        if (temperature > ambientTemperature) {
            return temperature - 1L;
        }
        if (temperature < ambientTemperature) {
            return temperature + 1L;
        }
        return temperature;
    }

    public static long accumulateHeat(long storedHeat, long incomingHeat) {
        long nonNegativeStoredHeat = Math.max(0L, storedHeat);
        long acceptedHeat = Math.max(0L, incomingHeat);
        return Long.MAX_VALUE - nonNegativeStoredHeat < acceptedHeat
                ? Long.MAX_VALUE : nonNegativeStoredHeat + acceptedHeat;
    }

    public static long accumulateSignedEnergy(long storedEnergy, long incomingEnergy) {
        if (incomingEnergy > 0L && storedEnergy > Long.MAX_VALUE - incomingEnergy) {
            return Long.MAX_VALUE;
        }
        if (incomingEnergy < 0L && storedEnergy < Long.MIN_VALUE - incomingEnergy) {
            return Long.MIN_VALUE;
        }
        return storedEnergy + incomingEnergy;
    }

    public static double materialWeightKg(long materialAmount, double densityKgPerCubicMeter,
                                          long materialUnitsPerIngot) {
        return materialWeightKg(materialAmount, 0, 1, densityKgPerCubicMeter, materialUnitsPerIngot);
    }

    /** Include the stored fractional material unit, without using a capacity rounding as physical mass. */
    static double materialWeightKg(long materialAmount, int remainder, int quantityUnit,
                                    double densityKgPerCubicMeter, long materialUnitsPerIngot) {
        if (materialAmount < 0L || quantityUnit <= 0 || remainder < 0 || remainder >= quantityUnit ||
                densityKgPerCubicMeter <= 0.0D || !Double.isFinite(densityKgPerCubicMeter) ||
                materialUnitsPerIngot <= 0L) {
            return 0.0D;
        }
        // GT6 uses 9 material units per cubic metre. Material amounts in this
        // mod are normalized to one ingot (GTValues.M), so this keeps the same
        // density-to-mass conversion without relying on the material having a
        // registered fluid.
        return (materialAmount + remainder / (double) quantityUnit) * densityKgPerCubicMeter /
                (materialUnitsPerIngot * 9.0D);
    }

    /**
     * Converts a CEu material amount through its molecular weight and its
     * material-specific GT6 calibration. A single global multiplier is not
     * valid: GT6's density differs independently of CEu's molecular weight.
     */
    public static double materialWeightKgFromMolecularMass(long materialAmount,
                                                           long materialUnitsPerIngot,
                                                           double ceuMolecularMass,
                                                           double gt6DensityKgPerCubicMeter) {
        if (materialAmount <= 0L || materialUnitsPerIngot <= 0L ||
                gt6DensityKgPerCubicMeter <= 0.0D) {
            return 0.0D;
        }
        if (ceuMolecularMass <= 0.0D) {
            return materialWeightKg(materialAmount, gt6DensityKgPerCubicMeter, materialUnitsPerIngot);
        }
        double gt6KgPerMolecularMassUnit = gt6DensityKgPerCubicMeter / (9.0D * ceuMolecularMass);
        return (materialAmount / (double) materialUnitsPerIngot) * ceuMolecularMass *
                gt6KgPerMolecularMassUnit;
    }

    /** Mirrors GT6's setMoleculeConfiguration density average for CEu components. */
    public static double gt6MoleculeDensityKgPerCubicMeter(double[] componentAmounts,
                                                            double[] componentDensities) {
        if (componentAmounts == null || componentDensities == null) {
            return 0.0D;
        }
        int count = Math.min(componentAmounts.length, componentDensities.length);
        double totalAmount = 0.0D;
        double weightedDensity = 0.0D;
        for (int i = 0; i < count; i++) {
            double amount = componentAmounts[i];
            double density = componentDensities[i];
            if (amount <= 0.0D || density < 0.0D ||
                    Double.isNaN(amount) || Double.isInfinite(amount) ||
                    Double.isNaN(density) || Double.isInfinite(density)) {
                continue;
            }
            totalAmount += amount;
            weightedDensity += amount * density;
        }
        return totalAmount > 0.0D ? weightedDensity / totalAmount : 0.0D;
    }

    public static double knownGt6MaterialDensityKgPerCubicMeter(String materialName) {
        String name = normalizeMaterialName(materialName);
        if (name.isEmpty()) {
            return 0.0D;
        }
        Double density = knownMaterialDensity(name);
        return density == null ? 0.0D : density;
    }

    public static boolean hasKnownGt6MaterialDensity(String materialName) {
        String name = normalizeMaterialName(materialName);
        return knownMaterialDensity(name) != null;
    }

    public static double gt6MaterialDensityKgPerCubicMeter(String materialName,
                                                           double registeredFluidDensity) {
        if (hasKnownGt6MaterialDensity(materialName)) {
            return knownGt6MaterialDensityKgPerCubicMeter(materialName);
        }
        return registeredFluidDensity > 0.0D && Double.isFinite(registeredFluidDensity) ? registeredFluidDensity :
                DEFAULT_UNKNOWN_MATERIAL_DENSITY_KG_PER_CUBIC_METER;
    }

    public static boolean isAirDensity(double densityKgPerCubicMeter) {
        // GT6 compares <= WEIGHT_AIR, including explicitly known zero
        // density elements. Unknown density has already resolved to the
        // positive project fallback; zero is not an unknown sentinel here.
        return densityKgPerCubicMeter >= 0.0D &&
                densityKgPerCubicMeter <= GT6_AIR_DENSITY_LIMIT_KG_PER_CUBIC_METER &&
                !Double.isNaN(densityKgPerCubicMeter) && !Double.isInfinite(densityKgPerCubicMeter);
    }

    private static String normalizeMaterialName(String materialName) {
        if (materialName == null) {
            return "";
        }
        String normalized = materialName.toLowerCase(java.util.Locale.ROOT);
        int namespaceSeparator = normalized.lastIndexOf(':');
        if (namespaceSeparator >= 0) {
            normalized = normalized.substring(namespaceSeparator + 1);
        }
        normalized = normalized.replace("_", "").replace("-", "").replace(" ", "");
        if ("aluminum".equals(normalized)) return "aluminium";
        if ("cesium".equals(normalized)) return "caesium";
        if ("lanthanum".equals(normalized)) return "lanthanium";
        if ("phosphorus".equals(normalized)) return "phosphor";
        if ("deuterium".equals(normalized) || "tritium".equals(normalized)) return "hydrogen";
        return GT6MaterialIdentity.canonicalCompoundName(normalized);
    }

    private static Double knownMaterialDensity(String normalizedName) {
        if (normalizedName == null || normalizedName.isEmpty()) {
            return null;
        }
        if ("tungstencarbide".equals(normalizedName)) {
            return GT6_TUNGSTEN_CARBIDE_DENSITY_KG_PER_CUBIC_METER;
        }

        // Look up full grade/material IDs first: inconel_718 and maraging_steel_250
        // are distinct alloys, not element isotope suffixes.
        Double density = REAL_WORLD_MATERIAL_DENSITIES.get(normalizedName);
        if (density == null) {
            density = GT6_MATERIAL_DENSITIES.get(normalizedName);
        }
        if (density != null) {
            return density;
        }

        // GTCEu isotope IDs append the mass number (for example uranium_235),
        // while most existing GT6 reference densities are element-level.
        String elementName = normalizedName;
        while (!elementName.isEmpty() && Character.isDigit(elementName.charAt(elementName.length() - 1))) {
            elementName = elementName.substring(0, elementName.length() - 1);
        }
        if (elementName.equals(normalizedName) || elementName.isEmpty()) {
            return null;
        }
        density = REAL_WORLD_MATERIAL_DENSITIES.get(elementName);
        return density != null ? density : GT6_MATERIAL_DENSITIES.get(elementName);
    }

    private static Map<String, Double> createRealWorldMaterialDensities() {
        Map<String, Double> densities = new HashMap<>();
        // Room-temperature bulk densities in kg/m^3. Values are explicit overrides
        // only for identifiable commercial alloys; unspecified/process mixtures
        // continue through their CEu component estimate or the configured fallback.
        // Haynes technical data: C-276 8.89, N 8.86, W 9.00 and X 8.22 g/cm^3.
        // https://haynesintl.com/en/alloys/alloy-portfolio/corrosion-resistant-alloys/hastelloy-c-276/
        // https://haynesintl.com/en/alloys/alloy-portfolio/corrosion-resistant-alloys/hastelloy-n/
        // https://haynesintl.com/wp-content/uploads/2023/09/w-brochure.pdf
        // https://haynesintl.com/wp-content/uploads/2024/08/x-brochure.pdf
        String[] entries = {
                "hastelloyc276=8890 hastelloyn=8860 hastelloyw=9000 hastelloyx=8220",
                // Special Metals: INCONEL 718 annealed density 0.296 lb/in^3 = 8.193 g/cm^3.
                // https://www.specialmetals.com/documents/technical-bulletins/inconel/inconel-alloy-718.pdf
                "inconel718=8193",
                // Special Metals: INCOLOY MA956, 7.25 g/cm^3.
                "incoloyma956=7250",
                // Carpenter Technology: NiMark 250/300 and Marage 350, converted from lb/in^3.
                // https://www.carpentertechnology.com/blog/toughness-index-for-alloy-comparisons
                "maragingsteel250=8000 maragingsteel300=8000 maragingsteel350=8083",
                // Rolled Alloys datasheet: ZERON 100 7.84 g/cm^3; NASA handbook: Zircaloy-4 6.56 g/cm^3.
                // https://www.rolledalloys.com/wp-content/uploads/2022/07/ZERON-100_data-book-rolled-alloys.pdf
                // https://ntrs.nasa.gov/api/citations/20240004217/downloads/SNP-HDBK-0008_SNP-Material-Handbook.pdf
                "zeron100=7840 zircaloy=6560 zircaloy4=6560"
        };
        for (String entryLine : entries) {
            for (String entry : entryLine.split("\\s+")) {
                int separator = entry.indexOf('=');
                densities.put(entry.substring(0, separator), Double.parseDouble(entry.substring(separator + 1)));
            }
        }
        return Collections.unmodifiableMap(densities);
    }

    private static Map<String, Double> createGt6MaterialDensities() {
        Map<String, Double> densities = new HashMap<>();
        // Values are GT6 mGramPerCubicCentimeter (and setDensity values) converted to kg/m^3.
        String[] entries = {
                // MT.java:695,715: only the host elemental identities.
                "duranium=20000 duraniumelemental=20000 tritanium=25000 tritaniumelemental=25000",
                "hydrogen=0.08988 deuterium=0.08988 tritium=0.08988 helium=0.1785",
                "lithium=534 beryllium=1850 boron=2340 carbon=2267 nitrogen=1.2506 oxygen=1.429 fluorine=1.696 neon=0.8999",
                "sodium=971 magnesium=1738 aluminium=2698 silicon=2329.6 phosphor=1820 sulfur=2067 chlorine=3.214 argon=1.7837",
                "potassium=862 calcium=1540 scandium=2989 titanium=4540 vanadium=6110 chromium=7150 manganese=7440 cobalt=8860 nickel=8912 copper=8960 zinc=7134",
                "gallium=5907 germanium=5323 arsenic=5776 selenium=4809 bromine=3122 krypton=3.733 rubidium=1532 strontium=2640 yttrium=4469 zirconium=6506 niobium=8570 molybdenum=10220 technetium=11500",
                "ruthenium=12370 rhodium=12410 palladium=12020 silver=10501 cadmium=8690 indium=7310 tin=7287 antimony=6685 tellurium=6232 iodine=4930 xenon=5.887 caesium=1873 barium=3594 lanthanium=6145",
                "cerium=6770 praseodymium=6773 neodymium=7007 promethium=7260 samarium=7520 europium=5243 gadolinium=7895 terbium=8229 dysprosium=8550 holmium=8795 erbium=9066 thulium=9321 ytterbium=6965 lutetium=9840",
                "hafnium=13310 tantalum=16654 tungsten=19250 rhenium=21020 iridium=22560 platinum=21460 gold=19282 mercury=13533.6 thallium=11850 lead=11342 bismuth=9807 polonium=9320 astatine=7000 radon=9.73",
                "francium=1870 radium=5500 actinium=10070 thorium=11720 protactinium=15370 uranium=18950 neptunium=20450 plutonium=19840 americium=13690 curium=13510 berkelium=14790 californium=15100 einsteinium=8840",
                "fermium=0 mendelevium=0 nobelium=0 lawrencium=0 rutherfordium=23200 dubnium=29300 seaborgium=35000 bohrium=37100 hassium=40700 meitnerium=37400 darmstadtium=34800 roentgenium=28700 copernicium=23700 nihonium=16000 flerovium=14000 moscovium=13500 livermorium=12900 farnsium=7200 oganesson=5000",
                "water=1000 semiheavywater=1054 heavywater=1105.6 tritiatedwater=1211.2 steam=1 snow=1000 ice=1000 freshwater=1000 holywater=1000 seawater=1000 dirtywater=1000 distilledwater=1000 hydrogenperoxide=1000 air=1.2",
                // MT.java:1317-1322,1332 keep OreDictMaterial's explicit 1.0 g/cm^3 default.
                "meatraw=1000 meatcooked=1000 meatrotten=1000 fishraw=1000 fishcooked=1000 fishrotten=1000 soylentgreen=1000",
                "nitricacid=1500 glycerol=1500 glyceryl=1500 sulfuricacid=1500 disulfuricacid=1500 hexafluorosilicicacid=1500 saltwater=1000 saltedwater=1000",
                "diamond=3530 diamondblue=3530 diamondgreen=3530 diamondpurple=3530 diamondred=3530 diamondyellow=3530 diamondpink=3530 diamondindustrial=3530 manadiamond=3530 elvendragonstone=3530 gravitite=3530",
                "charcoal=929 coal=929 coalcoke=929 lignite=865 lignitecoke=865 petroleumcoke=929 voidcrystal=929"
        };
        for (String entryLine : entries) {
            for (String entry : entryLine.split("\\s+")) {
                int separator = entry.indexOf('=');
                densities.put(entry.substring(0, separator), Double.parseDouble(entry.substring(separator + 1)));
            }
        }
        densities.put("iron", GT6_IRON_DENSITY_KG_PER_CUBIC_METER);
        densities.put("osmium", GT6_OSMIUM_DENSITY_KG_PER_CUBIC_METER);
        densities.put("aluminum", densities.get("aluminium"));
        densities.put("cesium", densities.get("caesium"));
        densities.put("lanthanum", densities.get("lanthanium"));
        densities.put("phosphorus", densities.get("phosphor"));
        // MT.java:1074,1294: two nested configurations, not the host's
        // flattened Ca3/P2/O8 density average. No later density override.
        double phosphateDensity = (densities.get("phosphor") + 4 * densities.get("oxygen")) / 5;
        double phosphorusCompoundDensity = (3 * densities.get("calcium") + 2 * phosphateDensity) / 5;
        densities.put("phosphate", phosphateDensity);
        densities.put("tricalciumphosphate", phosphorusCompoundDensity);
        densities.put("bluephosphorus", phosphorusCompoundDensity);
        densities.put("redphosphorus", phosphorusCompoundDensity);
        densities.put("whitephosphorus", phosphorusCompoundDensity);
        densities.put("anyphosphorus", phosphorusCompoundDensity);
        // Steel's one-U WroughtIron configuration copies Fe density;
        // Manasteel steals Steel, and ANY.MagicIron steals Manasteel.
        densities.put("anymagiciron", densities.get("iron"));
        // ANY.WoodPlastic steals MT.Wood: six C plus fifteen H2O, divider 21.
        densities.put("anywoodorplastic", (6 * densities.get("carbon") + 15 * densities.get("water")) / 21);
        // MT.java:1056/1081/1396, 212-214: nested component densities,
        // not real-world gem densities or the host's flattened atom counts.
        double silicaDensity = (densities.get("silicon") + 2 * densities.get("oxygen")) / 3;
        double aluminaDensity = (2 * densities.get("aluminium") + 3 * densities.get("oxygen")) / 5;
        // MT.java:539,1436,1498. Magic's declared density is zero; both
        // amethysts use density divider 5, but the ender variant's heat averages
        // all six component units. Do not substitute host atom counts or density.
        densities.put("magic", 0D);
        double amethystDensity = (4 * silicaDensity + densities.get("iron")) / 5;
        densities.put("amethyst", amethystDensity);
        densities.put("amethystender", amethystDensity);
        densities.put("enderamethyst", amethystDensity);
        // MT.java:1299,1544,1636,1796-1797. Preserve nested GT6
        // configurations and their explicit density dividers. In particular
        // Obsidian uses 64, while both refined materials use 1 (a sum,
        // not a two-component average). Output copies do not copy density.
        double phosphoriteDensity = (5 * densities.get("calcium") + 3 * phosphateDensity +
                densities.get("fluorine")) / 9;
        double glowstoneDensity = (5 * phosphoriteDensity + 3 * densities.get("gold") +
                silicaDensity + densities.get("helium")) / 10;
        double obsidianDensity = (densities.get("magnesium") + densities.get("iron") +
                6 * silicaDensity + 4 * densities.get("oxygen")) / 64;
        densities.put("phosphorite", phosphoriteDensity);
        densities.put("glowstone", glowstoneDensity);
        densities.put("obsidian", obsidianDensity);
        densities.put("lava", obsidianDensity); // MT.java:1881, after all declarations.
        densities.put("refinedglowstone", glowstoneDensity + densities.get("germanium"));
        densities.put("glowstonerefined", densities.get("refinedglowstone"));
        densities.put("refinedobsidian", obsidianDensity + densities.get("diamond"));
        densities.put("obsidianrefined", densities.get("refinedobsidian"));
        // Literal colour-before-Diamond ore names, MT.java:1359-1365.
        for (String name : new String[]{"bluediamond", "greendiamond", "purplediamond",
                "reddiamond", "yellowdiamond", "pinkdiamond"}) {
            densities.put(name, densities.get("diamond"));
        }
        // MT.java:1094,794,1675,1842. These are GT6 gameplay densities,
        // including the fictional element, not purported real-world values.
        densities.put("hematite", (2 * densities.get("iron") + 3 * densities.get("oxygen")) / 5);
        densities.put("bandediron", densities.get("hematite"));
        densities.put("ironiiioxide", densities.get("hematite"));
        // MT.java:1068,1090,3744 and setMoleculeConfiguration: MnO2 has
        // explicit divider 1, not the sum of its three atom coefficients.
        double niobiumPentoxideDensity = (2 * densities.get("niobium") + 5 * densities.get("oxygen")) / 7;
        double pyrolusiteDensity = densities.get("manganese") + 2 * densities.get("oxygen");
        densities.put("niobiumpentoxide", niobiumPentoxideDensity);
        densities.put("pyrolusite", pyrolusiteDensity);
        densities.put("columbite", (7 * niobiumPentoxideDensity + pyrolusiteDensity) / 8);
        densities.put("adamantium", 13356.24762D);
        densities.put("adamantine", (3 * densities.get("adamantium") + 4 * densities.get("oxygen")) / 7);
        densities.put("dolamide", 1000D);
        // MT.Stone/Sand stealStatsElement(SiO2), not unknown host mass.
        densities.put("stone", silicaDensity);
        densities.put("sand", silicaDensity);
        densities.put("silicondioxide", silicaDensity);
        densities.put("wax", 1000D); // GT6 base Wax retains the default density.
        // CaCO3's nested Ca/CO3 1:4 configuration equals Ca/C/O 1:1:3.
        densities.put("calcite", (densities.get("calcium") + densities.get("carbon") +
                3 * densities.get("oxygen")) / 5);
        densities.put("anygarnet", (5 * aluminaDensity + 3 * densities.get("manganese") +
                9 * silicaDensity + 3 * densities.get("oxygen")) / 20);
        densities.put("anyjasper", (2 * silicaDensity + densities.get("iron")) / 3);
        densities.put("anytigereye", silicaDensity);
        densities.put("anyaventurine", silicaDensity);
        densities.put("anyamber", 1000D); // MT.Amber retains OreDictMaterial's default 1 g/cm^3.
        // None of these GT6 definitions configures components or overrides density.
        densities.put("blaze", 1000D);
        densities.put("anyblaze", 1000D);
        densities.put("prismarine", 1000D);
        densities.put("prismarinedark", 1000D);
        densities.put("anyprismarine", 1000D);
        densities.put("wheat", 1000D);
        for (String name : new String[]{"barley", "rye", "rice", "oat", "abyssaloat", "corn", "potato"}) {
            densities.put(name, 1000D); // GT6 grain/dustfood defaults, no component configuration.
        }
        densities.put("anygrains", 1000D);
        densities.put("anythaumiccrystal", 1000D);
        densities.put("anyhexorium", 1000D);
        densities.put("tofu", 1000D);
        densities.put("fishoil", 1000D); // MT.FishOil has no density/components override.
        // MT.Bone uumMcfg(8, Ca, U): density uses the divided Ca quantity;
        // temperature averages the components and thus still equals Calcium.
        densities.put("bone", densities.get("calcium") / 8);
        densities.put("h2o", densities.get("water"));
        densities.put("hdo", densities.get("semiheavywater"));
        // MT.java:1017-1018 internal names differ from existing host aliases.
        densities.put("waterdirty", densities.get("dirtywater"));
        densities.put("waterdistilled", densities.get("distilledwater"));
        densities.put("d2o", densities.get("heavywater"));
        densities.put("t2o", densities.get("tritiatedwater"));
        densities.put("hno3", densities.get("nitricacid"));
        densities.put("h2so4", densities.get("sulfuricacid"));
        densities.put("h2s2o7", densities.get("disulfuricacid"));
        densities.put("h2sif6", densities.get("hexafluorosilicicacid"));
        densities.put("petcoke", densities.get("petroleumcoke"));
        return Collections.unmodifiableMap(densities);
    }

    public static boolean shouldCondenseLava(long temperature) {
        return temperature < 1300L;
    }

    public static boolean shouldBoilWater(long temperature) {
        return temperature >= 373L;
    }

    public static int obsidianUnitsForLava(int millibuckets) {
        return Math.max(0, millibuckets) / 1000;
    }

    /** Smeltery.addMaterialStacks casts weights, then truncates the retained temperature difference. */
    public static long smelteryIntakeTemperature(long currentTemperature, double currentMass,
                                                long incomingTemperature, double incomingMass) {
        double totalMass = currentMass + incomingMass;
        if (totalMass <= 0.0D || Double.isNaN(totalMass)) return Math.max(0L, currentTemperature);
        long retainedWeight = (long) Math.max(0.0D, currentMass);
        long totalWeight = (long) totalMass;
        if (retainedWeight == 0L) return Math.max(0L, incomingTemperature);
        if (totalWeight <= 0L) return Math.max(0L, currentTemperature);
        // Use the source's integer formula, while preventing its unchecked
        // amount * weight multiplication from overflowing on legacy input.
        java.math.BigInteger incoming = java.math.BigInteger.valueOf(incomingTemperature);
        java.math.BigInteger difference = java.math.BigInteger.valueOf(currentTemperature).subtract(incoming);
        java.math.BigInteger retainedDifference = difference.abs()
                .multiply(java.math.BigInteger.valueOf(retainedWeight))
                .divide(java.math.BigInteger.valueOf(totalWeight));
        java.math.BigInteger mixed = difference.signum() > 0 ?
                incoming.add(retainedDifference) : incoming.subtract(retainedDifference);
        return mixed.max(java.math.BigInteger.ZERO)
                .min(java.math.BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }

    public static long mixTemperature(long currentTemperature, double currentMass,
                                      long incomingTemperature, double incomingMass) {
        double totalMass = currentMass + incomingMass;
        if (totalMass <= 0.0D) return Math.max(0L, currentTemperature);
        return Math.max(0L, Math.round((currentTemperature * currentMass +
                incomingTemperature * incomingMass) / totalMass));
    }
}
