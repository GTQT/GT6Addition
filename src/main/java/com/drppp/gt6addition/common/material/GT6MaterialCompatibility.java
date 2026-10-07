package com.drppp.gt6addition.common.material;

import com.drppp.gt6addition.Tags;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.material.registry.MaterialRegistry;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Read-only bindings. Missing materials remain absent; source statistics never create registry entries. */
public final class GT6MaterialCompatibility {
    // Exact retired addon paths only. Arbitrary third-party namespaces must never be stripped.
    private static final Set<String> RETIRED_PATHS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "arsenic_copper", "arsenic_bronze", "ancient_debris", "netherite", "tantalum_hafnium_carbide",
            "duranium_alloy", "tritanium_alloy", "blue_phosphorus", "red_phosphorus", "white_phosphorus",
            "any_phosphorus", "any_magic_iron", "any_wood_or_plastic", "any_garnet", "any_jasper",
            "any_tiger_eye", "any_aventurine", "any_amber", "any_thaumic_crystal", "any_hexorium",
            "wax", "sand", "any_blaze", "prismarine", "prismarine_dark", "any_prismarine",
            "meat_cooked", "meat_raw", "meat_rotten", "fish_cooked", "fish_raw", "fish_rotten",
            "barley", "rye", "rice", "oat", "abyssal_oat", "corn", "potato", "any_grains", "tofu",
            "soylent_green", "snow", "fluorite", "niobium_pentoxide", "columbite", "adamantium",
            "adamantine", "dolamide")));

    private GT6MaterialCompatibility() {}

    @Nullable
    public static String retiredPath(@Nullable String registryName) {
        if (registryName == null || !registryName.startsWith(Tags.MOD_ID + ":")) return null;
        String path = registryName.substring(Tags.MOD_ID.length() + 1);
        return RETIRED_PATHS.contains(path) ? path : null;
    }

    /** Core, then CEu, then an unambiguous same-name external material. Never edit its properties. */
    @Nullable
    public static Material findExternal(String name) {
        if (name == null || name.isEmpty() || GregTechAPI.materialManager == null) return null;
        int separator = name.indexOf(':');
        if (separator >= 0) {
            if (separator == 0 || separator == name.length() - 1 ||
                    name.indexOf(':', separator + 1) >= 0 || name.startsWith(Tags.MOD_ID + ":")) return null;
            Material exact = GregTechAPI.materialManager.getMaterial(name);
            // CEu may fall back to gregtech when the requested registry is absent.
            return exact != null && name.equals(exact.getRegistryName()) ? exact : null;
        }
        for (String namespace : new String[]{"gtqtcore", "gregtech"}) {
            Material preferred = findExternal(namespace + ":" + name);
            if (preferred != null) return preferred;
        }
        String normalized = normalize(name);
        Material candidate = null;
        if (GregTechAPI.materialManager.getPhase() == IMaterialRegistryManager.Phase.PRE) return null;
        // MaterialEvent still has OPEN registries; getRegisteredMaterials is only valid after close.
        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
          for (Material material : registry.getAllMaterials()) {
            if (material == null || material.getRegistryName().startsWith(Tags.MOD_ID + ":")) continue;
            String registered = material.getRegistryName();
            if (!normalized.equals(normalize(registered.substring(registered.indexOf(':') + 1)))) continue;
            if (candidate != null && candidate != material) return null;
            candidate = material;
          }
        }
        return candidate;
    }

    private static String normalize(String path) {
        return path.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
