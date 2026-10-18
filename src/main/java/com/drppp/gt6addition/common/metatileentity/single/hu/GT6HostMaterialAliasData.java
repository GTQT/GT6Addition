package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Read-only adapters from literal GT6 material names to existing CEu registry
 * paths. These aliases preserve the host Material identity; they never create
 * or mutate a material. Namespaced identities are deliberately not accepted.
 */
final class GT6HostMaterialAliasData {
    private static final Map<String, String> HOST_PATHS;

    static {
        Map<String, String> paths = new HashMap<>();
        put(paths, "glyceryl", "gregtech:glyceryl_trinitrate");
        // GT6 MT.Phosphor (ID 150) is the elemental P material. Its exact
        // CEu identity is gregtech:phosphorus; GT6 MT.Phosphorus (ID 8208)
        // is a distinct calcium-phosphate compound and is mapped elsewhere.
        put(paths, "phosphor", "gregtech:phosphorus");
        put(paths, "teflon", "gregtech:polytetrafluoroethylene");
        put(paths, "pvc", "gregtech:polyvinyl_chloride");
        put(paths, "hardplastic", "gregtech:polycarbonate");
        put(paths, "chromium", "gregtech:chrome");
        // GT6 MT.Constantan (ID 8662) explicitly registers "Cupronickel" as
        // an alias and assigns Cupronickel = Constantan. CEu's existing
        // gregtech:cupronickel has the same Cu:Ni 1:1 composition; reuse that
        // identity rather than registering or approximating a new material.
        put(paths, "constantan", "gregtech:cupronickel");
        put(paths, "woodtreated", "gregtech:wood_sealed");
        put(paths, "padauk", "gregtech:paduak");
        put(paths, "cooperite", "gregtech:sheldonite");
        put(paths, "stolzite", "gregtech:raspite");
        put(paths, "yellowlimonite", "gregtech:bog_iron");
        put(paths, "ilmenite", "gregtech:illmenite");
        put(paths, "illmenite", "gregtech:illmenite");
        put(paths, "scheelite", "gregtech:calcium_tungstate");
        // MT.Ash (ID 8200) is the registered GT6 name "Ashes"; CEu exposes
        // this existing material at gregtech:ash. Used by GT6 wood smelting.
        put(paths, "ashes", "gregtech:ash");
        HOST_PATHS = Collections.unmodifiableMap(paths);
    }

    private GT6HostMaterialAliasData() {}

    /** Returns a host path only for an unqualified, source-verified GT6 name. */
    static String hostPath(String gt6Name) {
        if (gt6Name == null || gt6Name.isEmpty() || gt6Name.indexOf(':') >= 0) return null;
        String normalized = normalize(gt6Name);
        String canonical = GT6RegistrationAliasData.canonicalName(normalized);
        String path = HOST_PATHS.get(canonical);
        return path == null ? HOST_PATHS.get(normalized) : path;
    }

    private static void put(Map<String, String> paths, String gt6Name, String hostPath) {
        paths.put(normalize(gt6Name), hostPath);
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
