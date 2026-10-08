package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Locale;
import java.util.Set;

/**
 * Explicit physical identities declared outside GT6's positive material ID array.
 * This is a read-only source-data table, never a Material registry.
 */
final class GT6NonpositiveMaterialData {
    private static final Set<String> PHYSICAL_IDENTITIES =
            java.util.Collections.singleton("refinediron");

    private GT6NonpositiveMaterialData() {}

    static boolean contains(String name) {
        if (name == null || name.indexOf(':') >= 0) return false;
        String normalized = name.toLowerCase(Locale.ROOT).replace("_", "")
                .replace("-", "").replace(" ", "");
        return PHYSICAL_IDENTITIES.contains(GT6MaterialIdentity.canonicalOreName(normalized));
    }
}
