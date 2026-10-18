package com.drppp.gt6addition.common.material;

import com.drppp.gt6addition.Tags;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.material.registry.MaterialRegistry;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        String normalized = normalize(name);
        for (String namespace : new String[]{"gtqtcore", "gregtech"}) {
            Material preferred = findExternal(namespace + ":" + name);
            if (preferred != null) return preferred;
            MaterialRegistry registry = GregTechAPI.materialManager.getRegistry(namespace);
            if (registry == null || !namespace.equals(registry.getModid())) continue;
            Material normalizedMatch = null;
            for (Material material : registry.getAllMaterials()) {
                if (!normalized.equals(normalize(material.getName()))) continue;
                if (normalizedMatch != null && normalizedMatch != material) return null;
                normalizedMatch = material;
            }
            if (normalizedMatch != null) return normalizedMatch;
        }
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

    /**
     * Creates a short-lived lookup view for code that must resolve many GT6
     * identities in one operation. The live resolver above remains the source
     * of truth; this snapshot only replaces repeated whole-registry scans with
     * one indexed pass and must not be retained across operations/registry changes.
     */
    public static ExternalMaterialLookup createExternalMaterialLookup() {
        return new ExternalMaterialLookup();
    }

    public static final class ExternalMaterialLookup {
        private final Map<String, Material> byRegistryName = new HashMap<>();
        private final Map<String, List<Material>> preferredNames = new HashMap<>();
        private final Map<String, List<Material>> registryPaths = new HashMap<>();
        private final Set<String> registryIds = new HashSet<>();
        private final boolean prePhase;

        private ExternalMaterialLookup() {
            IMaterialRegistryManager manager = GregTechAPI.materialManager;
            prePhase = manager == null || manager.getPhase() == IMaterialRegistryManager.Phase.PRE;
            if (manager == null) return;
            for (MaterialRegistry registry : manager.getRegistries()) {
                String modid = registry.getModid();
                registryIds.add(modid);
                for (Material material : registry.getAllMaterials()) {
                    if (material == null || material.getRegistryName() == null) continue;
                    byRegistryName.put(material.getRegistryName(), material);
                    if ("gtqtcore".equals(modid) || "gregtech".equals(modid)) {
                        add(preferredNames, modid + "\u0000" + normalize(material.getName()), material);
                    }
                    if (!material.getRegistryName().startsWith(Tags.MOD_ID + ":")) {
                        String registered = material.getRegistryName();
                        int separator = registered.indexOf(':');
                        if (separator >= 0 && separator < registered.length() - 1) {
                            add(registryPaths, normalize(registered.substring(separator + 1)), material);
                        }
                    }
                }
            }
        }

        @Nullable
        public Material findExternal(String name) {
            if (name == null || name.isEmpty()) return null;
            int separator = name.indexOf(':');
            if (separator >= 0) {
                if (separator == 0 || separator == name.length() - 1 ||
                        name.indexOf(':', separator + 1) >= 0 || name.startsWith(Tags.MOD_ID + ":")) return null;
                Material exact = byRegistryName.get(name);
                return exact != null && name.equals(exact.getRegistryName()) ? exact : null;
            }

            String normalized = normalize(name);
            for (String namespace : new String[]{"gtqtcore", "gregtech"}) {
                Material exact = byRegistryName.get(namespace + ":" + name);
                if (exact != null) return exact;
                if (!registryIds.contains(namespace)) continue;
                Material normalizedMatch = unique(preferredNames.get(namespace + "\u0000" + normalized));
                List<Material> matches = preferredNames.get(namespace + "\u0000" + normalized);
                if (matches != null && matches.size() > 1) return null;
                if (normalizedMatch != null) return normalizedMatch;
            }
            if (prePhase) return null;
            return unique(registryPaths.get(normalized));
        }

        private static void add(Map<String, List<Material>> index, String key, Material material) {
            if (key == null || key.isEmpty()) return;
            index.computeIfAbsent(key, ignored -> new ArrayList<>()).add(material);
        }

        @Nullable
        private static Material unique(@Nullable List<Material> matches) {
            return matches == null || matches.size() != 1 ? null : matches.get(0);
        }
    }

    private static String normalize(String path) {
        return path.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
