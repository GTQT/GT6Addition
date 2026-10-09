package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** GT6 Kelvin values take priority; unknown host identities are not assigned guessed boiling points. */
public final class CrucibleMaterialPhaseData {
    private static final Map<String, Long> BOILING_POINTS;

    static {
        Map<String, Long> points = new HashMap<>();
        points.put("aluminium", 2792L);
        points.put("aluminum", 2792L);
        points.put("iron", 3134L);
        points.put("copper", 2835L);
        points.put("zinc", 1180L);
        points.put("silver", 2435L);
        points.put("tin", 2875L);
        points.put("gold", 3129L);
        points.put("lead", 2022L);
        points.put("water", 373L);
        points.put("lava", 4000L);
        points.put("obsidian", 4000L);
        BOILING_POINTS = Collections.unmodifiableMap(points);
    }

    private CrucibleMaterialPhaseData() {}

    /** Shared by the crucible and casting receivers; fluid temperature is only a fallback. */
    public static int meltingPoint(Material material) {
        int point = knownMeltingPoint(material.getName());
        if (point >= 0) return point;
        if (material == Materials.Obsidian) return CrucibleTransferLogic.OBSIDIAN_MELTING_TEMPERATURE;
        if (material == Materials.Water) return CrucibleTransferLogic.WATER_MELTING_TEMPERATURE;
        if (material == Materials.Lava) return 1300;
        if (material.hasFluid()) return material.getFluid().getTemperature();
        int blastTemperature = material.getBlastTemperature();
        return blastTemperature > 0 ? blastTemperature : 1811;
    }

    static int knownMeltingPoint(String materialName) {
        if (materialName == null) return -1;
        GT6TechnicalMaterialData.Profile family = GT6TechnicalMaterialData.find(materialName);
        if (family != null) return GT6TechnicalMaterialData.meltingPoint(family);
        int literal = GT6LiteralPhaseData.meltingPoint(materialName);
        if (literal >= 0) return literal;
        CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(materialName);
        if (rule != null && rule.meltingPoint >= 0) return rule.meltingPoint;
        int point = GT6ElementPhaseData.meltingPoint(materialName);
        if (point >= 0) return point;
        point = GT6InheritedPhaseData.meltingPoint(materialName);
        if (point >= 0) return point;
        return GT6DeclaredPhaseData.meltingPoint(materialName);
    }

    /** Known thermal data does not imply a Forge fluid or a MELTING flag. */
    static boolean hasKnownMeltingPoint(String materialName) {
        return knownMeltingPoint(materialName) >= 0;
    }

    static long boilingPoint(String materialName) {
        if (materialName == null) return Long.MAX_VALUE;
        GT6TechnicalMaterialData.Profile family = GT6TechnicalMaterialData.find(materialName);
        if (family != null) return GT6TechnicalMaterialData.boilingPoint(family);
        long literal = GT6LiteralPhaseData.boilingPoint(materialName);
        if (literal != Long.MAX_VALUE) return literal;
        CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(materialName);
        if (rule != null && rule.boilingPoint != Long.MAX_VALUE) return rule.boilingPoint;
        long inherited = GT6InheritedPhaseData.boilingPoint(materialName);
        if (inherited != Long.MAX_VALUE) return inherited;
        long declared = GT6DeclaredPhaseData.boilingPoint(materialName);
        if (declared != Long.MAX_VALUE) return declared;
        return BOILING_POINTS.getOrDefault(materialName.toLowerCase(Locale.ROOT),
                GT6ElementPhaseData.boilingPoint(materialName));
    }
}
