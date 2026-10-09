package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import java.math.BigInteger;
import java.util.Locale;

/** Explicit special units from GT6; unverified fluids retain current project units. */
public final class CrucibleFluidUnits {
    // LCM of the prior fluid denominators (90,720,000) and every exact
    // denominator in GT6's literal targetSmelting ratios (through 13,860).
    // This preserves both legacy fluid fractions and non-terminating GT6
    // material conversions such as 1/11 without rounding their remainders.
    static final int LEGACY_STORAGE_UNIT = 90720000;
    static final int STORAGE_UNIT = 997920000;
    private CrucibleFluidUnits() {}

    /** Unit of the actual default fluid used by output and JEI; zero means
     * an unresolved/ambiguous registered binding, not a metal-unit fallback.
     */
    public static int defaultFluidUnit(Material material) {
        if (material == null) return GTValues.L;
        if (!material.hasFluid()) return fluidUnit(material.getName());
        CrucibleFluidInput phase = CrucibleFluidInput.forMaterial(material, material.getFluid());
        return phase == null ? 0 : phase.unit;
    }

    static int gasUnit(String name) {
        String normalized = normalize(name);
        if ("steam".equals(normalized) || "water".equals(normalized) || "ice".equals(normalized)) return 160000;
        if ("aerotheum".equals(normalized) || "glowstone".equals(normalized)) return 250;
        return 1000;
    }

    private static String normalize(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
    }

    /** Denominator used by saves before the explicit-liquid unit expansion. */
    static int legacyFluidUnit(String name) {
        // These liquids formerly fell through to the metal default. Decode
        // old fractional amounts with that denominator, never the new one.
        if (isGt6GeneratedLiquid(normalize(name))) return GTValues.L;
        switch (normalize(name)) {
            case "blaze": case "kerosene": case "fueloil": case "gasoline":
            case "fishoil": case "whaleoil": case "seedoil": case "hempoil":
            case "linoil": case "sunfloweroil": case "nutoil": case "oliveoil":
            case "honey": case "honeydew": case "holywater": case "milk":
            case "glue": case "lubricant": return GTValues.L;
            default: return fluidUnit(name);
        }
    }

    static int plasmaUnit(String materialName) {
        String name = materialName == null ? "" : materialName.toLowerCase(Locale.ROOT)
                .replace("_", "").replace("-", "").replace(" ", "");
        return "helium".equals(name) || "nitrogen".equals(name) ? 1000 : 20736;
    }

    static Quantity storedFluidAmount(int fluidAmount, int fluidUnit) {
        if (fluidAmount < 0 || fluidUnit <= 0 || STORAGE_UNIT % fluidUnit != 0) return null;
        return quantity(BigInteger.valueOf(fluidAmount).multiply(BigInteger.valueOf(GTValues.M))
                .multiply(BigInteger.valueOf(STORAGE_UNIT / fluidUnit)), STORAGE_UNIT);
    }

    static Quantity migrateStoredQuantity(long amount, int remainder, int oldUnit) {
        if (!valid(amount, remainder, oldUnit) || STORAGE_UNIT % oldUnit != 0) return null;
        return quantity(numerator(amount, remainder, oldUnit)
                .multiply(BigInteger.valueOf(STORAGE_UNIT / oldUnit)), STORAGE_UNIT);
    }

    static int storedFluidVolume(long amount, int remainder, int fluidUnit) {
        if (!valid(amount, remainder, STORAGE_UNIT) || fluidUnit <= 0) return 0;
        return numerator(amount, remainder, STORAGE_UNIT).multiply(BigInteger.valueOf(fluidUnit))
                .divide(BigInteger.valueOf(STORAGE_UNIT).multiply(BigInteger.valueOf(GTValues.M)))
                .min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
    }

    static Quantity drainStored(long amount, int remainder, int fluidAmount, int fluidUnit) {
        if (!valid(amount, remainder, STORAGE_UNIT)) return null;
        Quantity consumed = storedFluidAmount(fluidAmount, fluidUnit);
        if (consumed == null) return null;
        return quantity(numerator(amount, remainder, STORAGE_UNIT)
                .subtract(numerator(consumed.amount, consumed.remainder, STORAGE_UNIT)), STORAGE_UNIT);
    }

    /** Project rule: whole 1000 mB lava buckets become obsidian; retain every smaller remainder. */
    static LavaCondensation condenseLava(long amount, int remainder, int fluidUnit) {
        if (!valid(amount, remainder, STORAGE_UNIT) || fluidUnit <= 0) return null;
        int buckets = storedFluidVolume(amount, remainder, fluidUnit) / 1000;
        if (buckets <= 0) return null;
        Quantity remaining = drainStored(amount, remainder, buckets * 1000, fluidUnit);
        return remaining == null ? null : new LavaCondensation(buckets, remaining);
    }

    static final class LavaCondensation {
        final int obsidianCount;
        final Quantity remaining;

        LavaCondensation(int obsidianCount, Quantity remaining) {
            this.obsidianCount = obsidianCount;
            this.remaining = remaining;
        }
    }

    static Quantity scaleStored(long amount, int remainder, long multiplier, long divisor) {
        if (!valid(amount, remainder, STORAGE_UNIT) || multiplier < 0 || divisor <= 0) return null;
        return quantity(numerator(amount, remainder, STORAGE_UNIT)
                .multiply(BigInteger.valueOf(multiplier)).divide(BigInteger.valueOf(divisor)), STORAGE_UNIT);
    }

    static long occupiedUnits(long amount, int remainder) {
        if (!valid(amount, remainder, STORAGE_UNIT)) return -1;
        return remainder == 0 ? amount : amount == Long.MAX_VALUE ? -1 : amount + 1;
    }

    static boolean replacementFits(long total, long capacity, long oldAmount, int oldRemainder,
                                   long newAmount, int newRemainder, boolean replacing) {
        long oldUnits = replacing ? occupiedUnits(oldAmount, oldRemainder) : 0;
        long newUnits = occupiedUnits(newAmount, newRemainder);
        if (total < 0 || capacity < 0 || oldUnits < 0 || newUnits < 0 || oldUnits > total) return false;
        // An overfull legacy save can still shrink; never allow a new expansion.
        if (replacing && newUnits <= oldUnits) return true;
        long otherUnits = total - oldUnits;
        return otherUnits <= capacity && newUnits <= capacity - otherUnits;
    }

    /** Validate the complete prepared intake before any temperature/inventory commit. */
    static boolean incomingBatchFits(long total, long capacity, long[] amounts, int[] remainders) {
        if (total < 0 || capacity < total || amounts == null || remainders == null ||
                amounts.length == 0 || amounts.length != remainders.length) return false;
        long remaining = capacity - total;
        for (int i = 0; i < amounts.length; i++) {
            long occupied = occupiedUnits(amounts[i], remainders[i]);
            if (occupied <= 0 || occupied > remaining) return false;
            remaining -= occupied;
        }
        return true;
    }

    public static int fluidUnit(String name) {
        if (name == null) return GTValues.L;
        String normalized = normalize(name);
        if (isGt6GeneratedLiquid(normalized)) return 1000;
        switch (normalized) {
            case "water":
            case "ice": return 1000; // MT.java:1007; Ice inherits Water's liquid representation.
            case "alumina": return 504; // Loader_Fluids.java:214, FL.createMolten(MT.Al2O3, 504).
            case "blaze": return 1296; // Loader_Fluids.java:195, 9*L per material U.
            // MT.java:66/68 factories add GASES; Loader_Fluids.java:660
            // delegates to FL.createGas (1000 mB/U), not createMolten.
            case "hydrogen": // MT.java:380-384.
            case "deuterium":
            case "tritium":
            case "helium":
            case "helium3":
            case "nitrogen": // MT.java:395-398.
            case "oxygen":
            case "fluorine":
            case "neon":
            case "chlorine": // MT.java:405-406.
            case "argon":
            case "krypton": // MT.java:425.
            case "xenon": // MT.java:443.
            case "radon": // MT.java:476.
            case "heliumneon": // MT.java:1024, explicit GASES.
            case "air": // MT.java:1027-1030, explicit GASES.
            case "nitrogenmonoxide":
            case "nitricoxide": // CEu FirstDegreeMaterials.java:1240; N1 O1, same as MT.NO.
            case "nitrogendioxide":
            case "ammonia":
            case "carbonmonoxide": // MT.java:1034-1037.
            case "carbondioxide":
            case "carbontrioxide":
            case "methane":
            case "sulfurdioxide": // MT.java:1044-1046.
            case "sulfurtrioxide":
            case "hydrosulfuricacid":
            case "hydrogensulfide": // CEu FirstDegreeMaterials.java:1167; H2 S1, same as MT.H2S.
            case "hydrochloricacid": // MT.java:1020-1021.
            case "hydrofluoricacid": // CEu FirstDegreeMaterials.java:1234; H1 F1, same as MT.HF.
            case "hydrogenfluoride": return 1000;
            case "propane": // Loader_Fluids.java:45-48; FL.create's four-argument overload uses 1000.
            case "butane":
            case "propylene":
            case "ethylene":
            case "waterdistilled": // GT6 MT.DistWater's internal name, MT.java:1018.
            case "distilledwater": // CEu FirstDegreeMaterials.java:1326, same distilled water.
            case "nitrofuel":
            case "oil":
            case "fuel":
            case "kerosine":
            case "kerosene":
            case "fueloil":
            case "gasoline":
            case "diesel":
            case "petrol":
            case "biomass":
            case "ethanol":
            // Loader_Fluids.java:572-587,615-624; explicit 1000 or
            // the FL.create four-argument overload's default 1000 mB/U.
            case "fishoil":
            case "whaleoil":
            case "seedoil":
            case "hempoil":
            case "linoil":
            case "sunfloweroil":
            case "nutoil":
            case "oliveoil":
            case "honey":
            case "honeydew":
            case "holywater":
            case "milk":
            case "glue":
            case "lubricant":
            case "fryingoilhot": // Loader_Fluids.java:106.
            case "plastic": return 1000; // Loader_Fluids.java:191.
            case "pyrotheum": // Loader_Fluids.java:130-133.
            case "cryotheum":
            case "petrotheum":
            case "aerotheum": return 250;
            case "steam": return 160000; // Loader_Fluids.java:52-55, MT.Steam.gas(...160000).
            default: return GTValues.L;
        }
    }

    /** Explicit LIQUID tags in MT.java, consumed by Loader_Fluids:658 and
     * FL.createLiquid:1072 (1000 mB/U). lqud* factories alone do not imply
     * this tag: Mercury, for example, is explicitly registered with 144.
     */
    private static boolean isGt6GeneratedLiquid(String normalized) {
        switch (GT6MaterialIdentity.canonicalCompoundName(normalized)) {
            case "bromine": // MT.java:424.
            case "semiheavywater": // :1008-1019.
            case "heavywater":
            case "tritiatedwater":
            case "seawater":
            case "waterdirty":
            case "hydrogenperoxide":
            case "nitricacid": // :1031-1054.
            case "glycerol":
            case "glyceryl":
            case "sulfuricacid":
            case "sulphuricacid": // Literal ore-name alias, :1047.
            case "disulfuricacid":
            case "hexafluorosilicicacid":
            case "titaniumtetrachloride": // :1087.
            case "saltwater": // :1143,1160.
            case "brine":
            case "saltedwater":
            case "chloroauricacid": // :1163-1177,1188; literal aliases included.
            case "chloroplatinicacid":
            case "stannicchloride":
            case "blackvitriol":
            case "bluevitriol":
            case "romanvitriol":
            case "cyprusvitriol":
            case "solutionbluevitriol":
            case "greenvitriol":
            case "redvitriol":
            case "pinkvitriol":
            case "cyanvitriol":
            case "solutionnickelsulfate":
            case "solutionnickelsulphate":
            case "whitevitriol":
            case "grayvitriol":
            case "martianvitriol":
            case "vitriolofclay":
            case "aquaregia":
            case "creosote": // :1210.
            case "creosoteoil":
            case "ectoplasm": return true; // :1536.
            default: return false;
        }
    }

    static long materialAmount(int fluidAmount, int fluidUnit) {
        if (fluidAmount <= 0 || fluidUnit <= 0) return 0;
        return ceilDivide(BigInteger.valueOf(fluidAmount).multiply(BigInteger.valueOf(GTValues.M)), fluidUnit);
    }

    static Quantity exactFluidAmount(int fluidAmount, int unit) {
        return quantity(BigInteger.valueOf(Math.max(0, fluidAmount)).multiply(BigInteger.valueOf(GTValues.M)), unit);
    }

    static Quantity exactLegacyWaterAmount(long amount) {
        return quantity(BigInteger.valueOf(Math.max(0L, amount)).multiply(BigInteger.valueOf(GTValues.L)), 1000);
    }

    static Quantity merge(long first, int firstRemainder, long second, int secondRemainder, int unit) {
        if (!valid(first, firstRemainder, unit) || !valid(second, secondRemainder, unit)) return null;
        return quantity(numerator(first, firstRemainder, unit).add(numerator(second, secondRemainder, unit)), unit);
    }

    static Quantity drain(long amount, int remainder, int fluidAmount, int unit) {
        if (!valid(amount, remainder, unit) || fluidAmount < 0) return null;
        BigInteger remaining = numerator(amount, remainder, unit)
                .subtract(BigInteger.valueOf(Math.max(0, fluidAmount)).multiply(BigInteger.valueOf(GTValues.M)));
        return remaining.signum() < 0 ? null : quantity(remaining, unit);
    }

    public static int fluidAmount(long amount, int remainder, int unit) {
        if (unit <= 0 || amount < 0 || remainder < 0 || remainder >= unit) return 0;
        return numerator(amount, remainder, unit).divide(BigInteger.valueOf(GTValues.M))
                .min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
    }

    private static BigInteger numerator(long amount, int remainder, int unit) {
        return BigInteger.valueOf(amount).multiply(BigInteger.valueOf(unit)).add(BigInteger.valueOf(remainder));
    }

    private static boolean valid(long amount, int remainder, int unit) {
        return unit > 0 && amount >= 0 && remainder >= 0 && remainder < unit;
    }

    private static Quantity quantity(BigInteger numerator, int unit) {
        if (unit <= 0 || numerator.signum() < 0) return null;
        BigInteger[] parts = numerator.divideAndRemainder(BigInteger.valueOf(unit));
        if (parts[0].compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) return null;
        return new Quantity(parts[0].longValue(), parts[1].intValue());
    }

    static final class Quantity {
        final long amount;
        final int remainder;
        Quantity(long amount, int remainder) {
            this.amount = amount;
            this.remainder = remainder;
        }
    }

    private static long ceilDivide(BigInteger amount, int divisor) {
        BigInteger result = amount.add(BigInteger.valueOf(divisor - 1L)).divide(BigInteger.valueOf(divisor));
        return result.min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }
}
