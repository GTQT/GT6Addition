package com.drppp.gt6addition.common.material;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;

/** GT6-style material item prefixes used by the crucible. */
public final class GT6AdditionOrePrefixes {

    private static final MaterialIconType SCRAP_GT_ICON = new MaterialIconType("scrapGt");

    /** One scrap is exactly one ninth of a material unit (one ingot). */
    public static final OrePrefix SCRAP_GT = new OrePrefix(
            "scrapGt",
            GTValues.M / 9L,
            null,
            SCRAP_GT_ICON,
            1L,
            GT6AdditionOrePrefixes::canHaveScrap);

    private static boolean registered;

    private GT6AdditionOrePrefixes() {
    }

    /** Register before MetaItems creates its per-prefix material items. */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        SCRAP_GT.maxStackSize = 18;
        MetaItems.addOrePrefix(SCRAP_GT);
    }

    private static boolean canHaveScrap(Material material) {
        return material != null && (material.hasProperty(PropertyKey.DUST) ||
                material.hasProperty(PropertyKey.INGOT) || material.hasProperty(PropertyKey.GEM));
    }
}
