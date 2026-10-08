package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        InputStream stream = GT6AntimatterIdentityData.class.getResourceAsStream("/gt6-antimatter-materials.txt");
        if (stream == null) throw new ExceptionInInitializerError("Missing read-only GT6 AM identity data");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\\|", -1);
                if (fields.length != 6) throw new IllegalStateException("Malformed GT6 AM identity row: " + line);
                int id = Integer.parseInt(fields[0]);
                String name = fields[1];
                int melting = Integer.parseInt(fields[2]);
                int boiling = Integer.parseInt(fields[3]);
                double density = Double.parseDouble(fields[4]);
                boolean meltingFlag = parseFlag(fields[5], line);
                if (id <= 0 || id >= 32767 || name.isEmpty() || melting < 0 || boiling < 0 ||
                        !Double.isFinite(density) || density < 0) {
                    throw new IllegalStateException("Invalid GT6 AM identity row: " + line);
                }
                Profile profile = new Profile(id, name, melting, boiling, density, meltingFlag);
                String key = normalize(name);
                if (byId.put(id, profile) != null || byName.put(key, profile) != null) {
                    throw new IllegalStateException("Duplicate GT6 AM identity row: " + line);
                }
                profiles.add(profile);
            }
        } catch (IOException | NumberFormatException exception) {
            throw new ExceptionInInitializerError(exception);
        }
        if (profiles.size() != EXPECTED_DECLARATIONS) {
            throw new ExceptionInInitializerError("Expected " + EXPECTED_DECLARATIONS +
                    " literal GT6 AM identities, got " + profiles.size());
        }
        Map<String, String> aliases = new HashMap<>();
        InputStream aliasStream = GT6AntimatterIdentityData.class.getResourceAsStream("/gt6-antimatter-aliases.txt");
        if (aliasStream == null) throw new ExceptionInInitializerError("Missing read-only GT6 AM alias data");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(aliasStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\\|", -1);
                if (fields.length != 2 || fields[0].isEmpty() || fields[1].isEmpty()) {
                    throw new IllegalStateException("Malformed GT6 AM alias row: " + line);
                }
                String alias = normalize(fields[0]);
                String target = normalize(fields[1]);
                if (!byName.containsKey(target) || byName.containsKey(alias)) {
                    throw new IllegalStateException("Invalid GT6 AM alias target: " + line);
                }
                String previous = aliases.put(alias, target);
                if (previous != null && !previous.equals(target)) {
                    throw new IllegalStateException("Conflicting GT6 AM alias: " + line);
                }
            }
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
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

    private static boolean parseFlag(String value, String row) {
        if ("0".equals(value)) return false;
        if ("1".equals(value)) return true;
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
