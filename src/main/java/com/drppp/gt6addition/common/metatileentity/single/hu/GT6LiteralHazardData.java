package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Read-only GT6 MT hazard/processing flags; this data never registers materials. */
final class GT6LiteralHazardData {
    static final int FLAMMABLE = 1;
    static final int EXPLOSIVE = 2;
    static final int UNBURNABLE = 4;
    static final int MELTING = 8;
    static final int ACID = 16;
    private static final int EXPECTED_ROWS = 1084;
    private static final Map<Integer, Profile> BY_ID;
    private static final Map<String, Profile> BY_NAME;
    private static final Set<String> AMBIGUOUS_NAMES;

    static {
        Map<Integer, Profile> byId = new HashMap<>();
        Map<String, Profile> byName = new HashMap<>();
        Set<String> ambiguous = new HashSet<>();
        InputStream stream = GT6LiteralHazardData.class.getResourceAsStream("/gt6-material-hazard-data.txt");
        if (stream == null) throw new ExceptionInInitializerError("Missing read-only GT6 hazard data");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\\|", -1);
                if (fields.length != 4) throw new IllegalStateException("Malformed GT6 hazard row: " + line);
                int id = Integer.parseInt(fields[0]);
                String sourceName = fields[1];
                int sourceLine = Integer.parseInt(fields[2]);
                int flags = Integer.parseInt(fields[3]);
                if (id <= 0 || sourceName.isEmpty() || sourceLine <= 0 || flags < 0 || (flags & ~31) != 0) {
                    throw new IllegalStateException("Invalid GT6 hazard row: " + line);
                }
                String mechanicsName = GT6MaterialIdentity.mechanicsName(id);
                if (mechanicsName == null) throw new IllegalStateException("Unknown GT6 hazard identity: " + line);
                Profile profile = new Profile(id, sourceName, mechanicsName, sourceLine, flags);
                if (byId.put(id, profile) != null) throw new IllegalStateException("Duplicate GT6 hazard ID: " + line);
                String key = normalize(mechanicsName);
                Profile previous = byName.putIfAbsent(key, profile);
                if (previous != null && previous.flags != flags) {
                    byName.remove(key);
                    ambiguous.add(key);
                }
            }
        } catch (IOException | NumberFormatException exception) {
            throw new ExceptionInInitializerError(exception);
        }
        if (byId.size() != EXPECTED_ROWS) {
            throw new ExceptionInInitializerError("Expected " + EXPECTED_ROWS +
                    " literal GT6 hazard identities, got " + byId.size());
        }
        BY_ID = Collections.unmodifiableMap(byId);
        BY_NAME = Collections.unmodifiableMap(byName);
        AMBIGUOUS_NAMES = Collections.unmodifiableSet(ambiguous);
    }

    private GT6LiteralHazardData() {}

    static Profile find(String registeredMaterialName) {
        if (registeredMaterialName == null) return null;
        String key = normalize(registeredMaterialName);
        if (key.isEmpty() || AMBIGUOUS_NAMES.contains(key)) return null;
        return BY_NAME.get(key);
    }

    static Profile findById(int materialId) {
        return BY_ID.get(materialId);
    }

    static int size() { return BY_ID.size(); }

    private static String normalize(String name) {
        if (name == null || name.indexOf(':') >= 0) return "";
        String key = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        key = GT6MaterialIdentity.canonicalElementName(key);
        key = GT6MaterialIdentity.canonicalOxideName(key);
        key = GT6MaterialIdentity.canonicalCompoundName(key);
        key = GT6MaterialIdentity.canonicalOreName(key);
        return GT6MaterialIdentity.canonicalAlloyName(key);
    }

    static final class Profile {
        final int id;
        final String sourceName;
        final String mechanicsName;
        final int sourceLine;
        final int flags;

        private Profile(int id, String sourceName, String mechanicsName, int sourceLine, int flags) {
            this.id = id;
            this.sourceName = sourceName;
            this.mechanicsName = mechanicsName;
            this.sourceLine = sourceLine;
            this.flags = flags;
        }
    }
}
