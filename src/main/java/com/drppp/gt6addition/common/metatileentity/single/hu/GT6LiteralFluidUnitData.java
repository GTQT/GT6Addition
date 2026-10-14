package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Read-only fluid amount-per-material-unit data transcribed from GT6.
 *
 * <p>This is mechanics data only: it does not register materials or fluids.
 * Loader_Fluids uses FL.createLiquid for materials tagged LIQUID, whose
 * amount-per-unit is 1000 mB; the explicit rows preserve that registration
 * fact for exact identity resolution and old-save unit migration. Exceptional
 * amounts are kept separately because they override the normal factory unit.
 */
final class GT6LiteralFluidUnitData {
    private static final Map<String, Integer> LIQUID_MATERIAL_UNITS;
    private static final Map<String, Integer> SPECIAL_FLUID_UNITS;
    private static final Map<String, Integer> SPECIAL_GAS_UNITS;
    private static final Map<String, Integer> SPECIAL_PLASMA_UNITS;

    static {
        Map<String, Integer> liquids = new HashMap<>();
        // MT.java LIQUID tags consumed by Loader_Fluids.java:658 and
        // FL.java:1071-1072 (createLiquid defaults to 1000 mB/U).
        liquid(liquids, 1000,
                "bromine",
                "semiheavywater", "heavywater", "tritiatedwater", "seawater", "waterdirty",
                "hydrogenperoxide", "nitricacid", "glycerol", "glyceryl", "sulfuricacid",
                "sulphuricacid", "disulfuricacid", "hexafluorosilicicacid", "titaniumtetrachloride",
                "saltwater", "brine", "saltedwater", "chloroauricacid", "chloroplatinicacid",
                "stannicchloride", "blackvitriol", "bluevitriol", "romanvitriol", "cyprusvitriol",
                "solutionbluevitriol", "greenvitriol", "redvitriol", "pinkvitriol", "cyanvitriol",
                "solutionnickelsulfate", "solutionnickelsulphate", "whitevitriol", "grayvitriol",
                "martianvitriol", "vitriolofclay", "aquaregia", "creosote", "creosoteoil", "ectoplasm");
        LIQUID_MATERIAL_UNITS = Collections.unmodifiableMap(liquids);

        Map<String, Integer> fluids = new HashMap<>();
        // Loader_Fluids.java:130-150 and :191-214: explicit non-default units.
        fluids.put("alumina", 504); // FL.createMolten(MT.Al2O3, 504).
        fluids.put("blaze", 1296); // 9*L per material U.
        fluids.put("steam", 160000); // FL.create(..., 160000, ...) / MT.Steam.gas.
        fluids.put("pyrotheum", 250);
        fluids.put("cryotheum", 250);
        fluids.put("petrotheum", 250);
        fluids.put("aerotheum", 250);
        SPECIAL_FLUID_UNITS = Collections.unmodifiableMap(fluids);

        Map<String, Integer> gases = new HashMap<>();
        // GT6 gases normally use FL.createGas's 1000 mB/U. Steam and the
        // listed Theum fluids are explicit exceptions in the fluid loader.
        gases.put("steam", 160000);
        gases.put("aerotheum", 250);
        gases.put("glowstone", 250);
        SPECIAL_GAS_UNITS = Collections.unmodifiableMap(gases);

        Map<String, Integer> plasmas = new HashMap<>();
        // Loader_Fluids.java:28-29 creates these two explicit 1000 mB/U plasmas.
        plasmas.put("helium", 1000);
        plasmas.put("nitrogen", 1000);
        SPECIAL_PLASMA_UNITS = Collections.unmodifiableMap(plasmas);
    }

    private GT6LiteralFluidUnitData() {}

    /** Returns 0 if GT6 has no explicit automatic-LIQUID binding for this name. */
    static int liquidMaterialUnit(String materialName) {
        return lookup(LIQUID_MATERIAL_UNITS, materialName);
    }

    /** Returns 0 when GT6 has no explicit amount override for this name. */
    static int specialFluidUnit(String materialName) {
        return lookup(SPECIAL_FLUID_UNITS, materialName);
    }

    /** Returns 0 when GT6 has no explicit gas amount override for this name. */
    static int specialGasUnit(String materialName) {
        return lookup(SPECIAL_GAS_UNITS, materialName);
    }

    /** Returns 0 when GT6 has no explicit plasma amount override for this name. */
    static int specialPlasmaUnit(String materialName) {
        return lookup(SPECIAL_PLASMA_UNITS, materialName);
    }

    private static int lookup(Map<String, Integer> values, String materialName) {
        if (materialName == null) return 0;
        String normalized = materialName.toLowerCase(Locale.ROOT)
                .replace("_", "").replace("-", "").replace(" ", "");
        normalized = GT6MaterialIdentity.canonicalCompoundName(normalized);
        Integer value = values.get(normalized);
        return value == null ? 0 : value;
    }

    private static void liquid(Map<String, Integer> target, int unit, String... names) {
        for (String name : names) target.put(name, unit);
    }
}
