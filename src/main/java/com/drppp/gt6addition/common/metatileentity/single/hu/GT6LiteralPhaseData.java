package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Read-only Kelvin values evaluated from the pinned GT6 MT initialization
 * path. This maps data onto an already-registered material identity; it never
 * creates, registers, or changes a CEu Material.
 */
final class GT6LiteralPhaseData {
    private static final int EXPECTED_ROWS = 1084;
    private static final Map<Integer, Profile> BY_ID;
    private static final Map<String, Profile> BY_NAME;
    private static final Set<String> AMBIGUOUS_NAMES;

    static {
        Map<Integer, Profile> byId = new HashMap<>();
        Map<String, Profile> byName = new HashMap<>();
        Set<String> ambiguous = new HashSet<>();
        for (GT6LiteralPhaseRows.Row row : GT6LiteralPhaseRows.ROWS) {
            int id = row.id;
            String sourceName = row.sourceName;
            int sourceLine = row.sourceLine;
            int melting = row.melting;
            long boiling = row.boiling;
            if (id <= 0 || sourceName.isEmpty() || sourceLine <= 0 || melting < 0 || boiling < melting) {
                throw new IllegalStateException("Invalid GT6 phase row: " + sourceName);
            }
            String mechanicsName = GT6MaterialIdentity.mechanicsName(id);
            if (mechanicsName == null) throw new IllegalStateException("Unknown GT6 phase identity: " + sourceName);
            Profile profile = new Profile(id, sourceName, mechanicsName, sourceLine, melting, boiling);
            if (byId.put(id, profile) != null) throw new IllegalStateException("Duplicate GT6 phase ID: " + id);
            String key = normalize(mechanicsName);
            Profile previous = byName.putIfAbsent(key, profile);
            if (previous != null && (previous.melting != melting || previous.boiling != boiling)) {
                byName.remove(key);
                ambiguous.add(key);
            }
        }
        if (byId.size() != EXPECTED_ROWS) {
            throw new ExceptionInInitializerError("Expected " + EXPECTED_ROWS +
                    " literal GT6 phase identities, got " + byId.size());
        }
        BY_ID = java.util.Collections.unmodifiableMap(byId);
        BY_NAME = java.util.Collections.unmodifiableMap(byName);
        AMBIGUOUS_NAMES = java.util.Collections.unmodifiableSet(ambiguous);
    }

    private GT6LiteralPhaseData() {}

    static int meltingPoint(String registeredMaterialName) {
        Profile profile = find(registeredMaterialName);
        return profile == null ? -1 : profile.melting;
    }

    static long boilingPoint(String registeredMaterialName) {
        Profile profile = find(registeredMaterialName);
        return profile == null ? Long.MAX_VALUE : profile.boiling;
    }

    static Profile find(String registeredMaterialName) {
        if (registeredMaterialName == null) return null;
        String key = normalize(registeredMaterialName);
        if (key.isEmpty() || AMBIGUOUS_NAMES.contains(key)) return null;
        return BY_NAME.get(key);
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
        final int melting;
        final long boiling;

        private Profile(int id, String sourceName, String mechanicsName, int sourceLine,
                        int melting, long boiling) {
            this.id = id;
            this.sourceName = sourceName;
            this.mechanicsName = mechanicsName;
            this.sourceLine = sourceLine;
            this.melting = melting;
            this.boiling = boiling;
        }
    }
}
