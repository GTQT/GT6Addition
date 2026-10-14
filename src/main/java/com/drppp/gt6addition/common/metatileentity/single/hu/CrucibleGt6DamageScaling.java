package com.drppp.gt6addition.common.metatileentity.single.hu;

import com.drppp.gt6addition.common.config.GT6AdditionConfig;
import net.minecraftforge.fml.common.Loader;

import java.util.Map;

/** Configurable mirror of GT6's optional TerraFirmaCraft damage scaling. */
final class CrucibleGt6DamageScaling {
    private static final float TFC_DAMAGE_MULTIPLIER = 80.0F;
    private static final String TFC_MOD_ID = "terrafirmacraft";
    private static final String TFC_PLUS_MOD_ID = "terrafirmacraftplus";

    private CrucibleGt6DamageScaling() {}

    static float scale(float damage) {
        return scale(damage, hasTerraFirmaCraft(), GT6AdditionConfig.useTfcAttackMultiplierForCrucible);
    }

    static float scale(float damage, boolean terraFirmaCraftLoaded, boolean multiplierEnabled) {
        return terraFirmaCraftLoaded && multiplierEnabled ? damage * TFC_DAMAGE_MULTIPLIER : damage;
    }

    static boolean hasTerraFirmaCraft() {
        Map<String, ?> indexedMods = Loader.instance().getIndexedModList();
        if (indexedMods == null) return false;
        return (indexedMods.containsKey(TFC_MOD_ID) && Loader.isModLoaded(TFC_MOD_ID)) ||
                (indexedMods.containsKey(TFC_PLUS_MOD_ID) && Loader.isModLoaded(TFC_PLUS_MOD_ID));
    }
}
