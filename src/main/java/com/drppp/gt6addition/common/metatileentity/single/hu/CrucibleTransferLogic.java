package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Pure boundary calculations shared by the crucible transfer code and tests. */
public final class CrucibleTransferLogic {
    public static final int OBSIDIAN_MELTING_TEMPERATURE = 1_300;
    public static final double GT6_IRON_DENSITY_KG_PER_CUBIC_METER = 7_874.0D;
    public static final double GT6_OSMIUM_DENSITY_KG_PER_CUBIC_METER = 22_610.0D;
    public static final double GT6_TUNGSTEN_CARBIDE_DENSITY_KG_PER_CUBIC_METER = 15_600.0D;
    private static final Map<String, Double> GT6_MATERIAL_DENSITIES = createGt6MaterialDensities();

    private CrucibleTransferLogic() {}

    public static int acceptedFluidAmount(String currentFluid, int currentAmount,
                                          String offeredFluid, int offeredAmount, int capacity) {
        if (offeredFluid == null || offeredAmount <= 0 || capacity <= 0 || currentAmount < 0 ||
                (currentAmount > 0 && !offeredFluid.equals(currentFluid))) {
            return 0;
        }
        return Math.min(offeredAmount, Math.max(0, capacity - currentAmount));
    }

    public static int acceptedQueueItems(int queuedItems, int requestedItems, int capacity) {
        return Math.min(Math.max(0, requestedItems), Math.max(0, capacity - queuedItems));
    }

    public static boolean canMeltWithinLimit(int maxTemperature, int meltingTemperature,
                                             boolean hasFluidOutput) {
        return hasFluidOutput && meltingTemperature > 0 && meltingTemperature <= maxTemperature;
    }

    public static long requiredEnergyPerKelvin(double thermalMassKg) {
        return 1L + (long) (Math.max(0.0D, thermalMassKg) / 100.0D);
    }

    public static long temperatureGainForHeat(long availableHeat, double thermalMassKg) {
        return Math.max(0L, availableHeat) / requiredEnergyPerKelvin(thermalMassKg);
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

    public static double materialWeightKg(long materialAmount, double densityKgPerCubicMeter,
                                          long materialUnitsPerIngot) {
        if (materialAmount <= 0L || densityKgPerCubicMeter <= 0.0D || materialUnitsPerIngot <= 0L) {
            return 0.0D;
        }
        // GT6 uses 9 material units per cubic metre. Material amounts in this
        // mod are normalized to one ingot (GTValues.M), so this keeps the same
        // density-to-mass conversion without relying on the material having a
        // registered fluid.
        return materialAmount * densityKgPerCubicMeter / (materialUnitsPerIngot * 9.0D);
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
        if ("tungstencarbide".equals(name)) {
            return GT6_TUNGSTEN_CARBIDE_DENSITY_KG_PER_CUBIC_METER;
        }
        Double density = GT6_MATERIAL_DENSITIES.get(name);
        return density == null ? 0.0D : density;
    }

    public static boolean hasKnownGt6MaterialDensity(String materialName) {
        String name = normalizeMaterialName(materialName);
        return "tungstencarbide".equals(name) || GT6_MATERIAL_DENSITIES.containsKey(name);
    }

    public static double gt6MaterialDensityKgPerCubicMeter(String materialName,
                                                           double registeredFluidDensity) {
        if (hasKnownGt6MaterialDensity(materialName)) {
            return knownGt6MaterialDensityKgPerCubicMeter(materialName);
        }
        return registeredFluidDensity > 0.0D ? registeredFluidDensity : 1_000.0D;
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
        while (!normalized.isEmpty() && Character.isDigit(normalized.charAt(normalized.length() - 1))) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if ("aluminum".equals(normalized)) return "aluminium";
        if ("cesium".equals(normalized)) return "caesium";
        if ("lanthanum".equals(normalized)) return "lanthanium";
        if ("phosphorus".equals(normalized)) return "phosphor";
        if ("deuterium".equals(normalized) || "tritium".equals(normalized)) return "hydrogen";
        return normalized;
    }

    private static Map<String, Double> createGt6MaterialDensities() {
        Map<String, Double> densities = new HashMap<>();
        // Values are GT6 mGramPerCubicCentimeter (and setDensity values) converted to kg/m^3.
        String[] entries = {
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
        densities.put("h2o", densities.get("water"));
        densities.put("hdo", densities.get("semiheavywater"));
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

    public static long mixTemperature(long currentTemperature, double currentMass,
                                      long incomingTemperature, double incomingMass) {
        double totalMass = currentMass + incomingMass;
        if (totalMass <= 0.0D) return Math.max(0L, currentTemperature);
        return Math.max(0L, Math.round((currentTemperature * currentMass +
                incomingTemperature * incomingMass) / totalMass));
    }
}
