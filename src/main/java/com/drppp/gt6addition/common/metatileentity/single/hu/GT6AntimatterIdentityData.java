package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Read-only identity and direct scalar declarations from GT6's AM.java.
 * This data never constructs or registers a CEu Material.
 */
final class GT6AntimatterIdentityData {
    private static final int EXPECTED_DECLARATIONS = 418;

    static final class Profile {
        final int id;
        final String name;
        final int melting;
        final int boiling;
        final double densityGramsPerCubicCentimeter;
        final boolean meltingFlag;

        private Profile(int id, String name, int melting, int boiling,
                        double densityGramsPerCubicCentimeter, boolean meltingFlag) {
            this.id = id;
            this.name = name;
            this.melting = melting;
            this.boiling = boiling;
            this.densityGramsPerCubicCentimeter = densityGramsPerCubicCentimeter;
            this.meltingFlag = meltingFlag;
        }
    }

    private static final Map<Integer, Profile> BY_ID;
    private static final Map<String, Profile> BY_NAME;
    private static final Map<String, String> ALIASES;
    private static final List<Profile> PROFILES;

    static {
        Map<Integer, Profile> byId = new HashMap<>();
        Map<String, Profile> byName = new HashMap<>();
        List<Profile> profiles = new ArrayList<>();
        for (GT6AntimatterRows.Row row : GT6AntimatterRows.ROWS) {
            int id = row.id;
            String name = row.name;
            int melting = row.melting;
            int boiling = row.boiling;
            double density = row.density;
            boolean meltingFlag = parseFlag(row.meltingFlag, name);
            if (id <= 0 || id >= 32767 || name.isEmpty() || melting < 0 || boiling < 0 ||
                    !Double.isFinite(density) || density < 0) {
                throw new IllegalStateException("Invalid GT6 AM identity row: " + name);
            }
            Profile profile = new Profile(id, name, melting, boiling, density, meltingFlag);
            String key = normalize(name);
            if (byId.put(id, profile) != null || byName.put(key, profile) != null) {
                throw new IllegalStateException("Duplicate GT6 AM identity row: " + name);
            }
            profiles.add(profile);
        }
        if (profiles.size() != EXPECTED_DECLARATIONS) {
            throw new ExceptionInInitializerError("Expected " + EXPECTED_DECLARATIONS +
                    " literal GT6 AM identities, got " + profiles.size());
        }
        Map<String, String> aliases = new HashMap<>();
        for (GT6AntimatterAliasRows.Row row : GT6AntimatterAliasRows.ROWS) {
            if (row.alias.isEmpty() || row.target.isEmpty()) {
                throw new IllegalStateException("Malformed GT6 AM alias row: " + row.alias);
            }
            String alias = normalize(row.alias);
            String target = normalize(row.target);
            if (!byName.containsKey(target) || byName.containsKey(alias)) {
                throw new IllegalStateException("Invalid GT6 AM alias target: " + row.alias);
            }
            String previous = aliases.put(alias, target);
            if (previous != null && !previous.equals(target)) {
                throw new IllegalStateException("Conflicting GT6 AM alias: " + row.alias);
            }
        }
        if (aliases.size() != 21) {
            throw new ExceptionInInitializerError("Expected 21 normalized literal GT6 AM aliases, got " + aliases.size());
        }
        BY_ID = Collections.unmodifiableMap(byId);
        BY_NAME = Collections.unmodifiableMap(byName);
        ALIASES = Collections.unmodifiableMap(aliases);
        PROFILES = Collections.unmodifiableList(profiles);
    }

    private GT6AntimatterIdentityData() {}

    private static boolean parseFlag(int value, String row) {
        if (value == 0) return false;
        if (value == 1) return true;
        throw new IllegalStateException("Invalid GT6 AM material flag: " + row);
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    static String name(int id) {
        if (isAmbiguousId(id)) return null;
        Profile profile = BY_ID.get(id);
        return profile == null ? null : profile.name;
    }

    static Profile find(int id) {
        return BY_ID.get(id);
    }

    static boolean containsName(String name) {
        return name != null && BY_NAME.containsKey(canonicalName(name));
    }

    /**
     * AM.java:241 accidentally reuses MT.java:728's ID 1580. Numeric-only
     * recycling data cannot distinguish the two GT6 material objects.
     */
    static boolean isAmbiguousId(int id) {
        return id == 1580;
    }

    static List<Profile> profiles() {
        return PROFILES;
    }

    /** Resolve only addIdenticalNames declarations from AM.java. */
    static String canonicalName(String name) {
        if (name == null) return null;
        String normalized = normalize(name);
        return ALIASES.getOrDefault(normalized, normalized);
    }

    static Map<String, String> aliases() {
        return ALIASES;
    }

    static boolean hasMeltingFlag(String name) {
        Profile profile = name == null ? null : BY_NAME.get(canonicalName(name));
        return profile != null && profile.meltingFlag;
    }
}
