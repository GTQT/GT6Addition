package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Read-only targetSmelting/targetSolidifying data evaluated from GT6 MT.java.
 * Target IDs are translated to existing host materials at use time; this table
 * never registers or mutates a Material.
 */
final class GT6LiteralTargetData {
    static final long GT6_U = 648648000L;
    private static final int EXPECTED_ROWS = 1084;
    private static final Map<Integer, Profile> BY_ID;
    private static final Map<String, Profile> BY_NAME;
    private static final Set<String> AMBIGUOUS_NAMES;

    static {
        Map<Integer, Profile> byId = new HashMap<>();
        Map<String, Profile> byName = new HashMap<>();
        Set<String> ambiguous = new HashSet<>();
        for (GT6LiteralTargetRows.Row row : GT6LiteralTargetRows.ROWS) {
            int id = row.id;
            String sourceName = row.sourceName;
            int sourceLine = row.sourceLine;
            int hotTargetId = row.hotTargetId;
            long hotAmount = row.hotAmount;
            int coldTargetId = row.coldTargetId;
            long coldAmount = row.coldAmount;
            if (id <= 0 || sourceName.isEmpty() || sourceLine <= 0 || hotTargetId <= 0 ||
                    coldTargetId <= 0 || hotAmount < 0 || coldAmount < 0) {
                throw new IllegalStateException("Invalid GT6 target row: " + sourceName);
            }
            String mechanicsName = GT6MaterialIdentity.mechanicsName(id);
            String hotTargetName = GT6MaterialIdentity.mechanicsName(hotTargetId);
            String coldTargetName = GT6MaterialIdentity.mechanicsName(coldTargetId);
            GT6LiteralHazardData.Profile hazard = GT6LiteralHazardData.findById(id);
            if (mechanicsName == null || hotTargetName == null || coldTargetName == null || hazard == null ||
                    !sourceName.equals(hazard.sourceName) || sourceLine != hazard.sourceLine) {
                throw new IllegalStateException("Unknown GT6 target identity/flags: " + sourceName);
            }
            Profile profile = new Profile(id, sourceName, mechanicsName, sourceLine,
                    hotTargetId, hotTargetName, hotAmount, coldTargetId, coldTargetName, coldAmount);
            if (byId.put(id, profile) != null) throw new IllegalStateException("Duplicate GT6 target ID: " + id);
            String key = normalize(mechanicsName);
            Profile previous = byName.putIfAbsent(key, profile);
            if (previous != null && !previous.sameContract(profile)) {
                byName.remove(key);
                ambiguous.add(key);
            }
        }
        if (byId.size() != EXPECTED_ROWS) {
            throw new ExceptionInInitializerError("Expected " + EXPECTED_ROWS +
                    " literal GT6 target identities, got " + byId.size());
        }
        BY_ID = Collections.unmodifiableMap(byId);
        BY_NAME = Collections.unmodifiableMap(byName);
        AMBIGUOUS_NAMES = Collections.unmodifiableSet(ambiguous);
    }

    private GT6LiteralTargetData() {}

    static Profile find(String registeredMaterialName) {
        if (registeredMaterialName == null) return null;
        String key = normalize(registeredMaterialName);
        if (key.isEmpty() || AMBIGUOUS_NAMES.contains(key)) return null;
        return BY_NAME.get(key);
    }

    static boolean isAmbiguous(String registeredMaterialName) {
        if (registeredMaterialName == null) return false;
        String key = normalize(registeredMaterialName);
        return !key.isEmpty() && AMBIGUOUS_NAMES.contains(key);
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
        final int hotTargetId;
        final String hotTargetName;
        final long hotAmount;
        final int coldTargetId;
        final String coldTargetName;
        final long coldAmount;

        private Profile(int id, String sourceName, String mechanicsName, int sourceLine,
                        int hotTargetId, String hotTargetName, long hotAmount,
                        int coldTargetId, String coldTargetName, long coldAmount) {
            this.id = id;
            this.sourceName = sourceName;
            this.mechanicsName = mechanicsName;
            this.sourceLine = sourceLine;
            this.hotTargetId = hotTargetId;
            this.hotTargetName = hotTargetName;
            this.hotAmount = hotAmount;
            this.coldTargetId = coldTargetId;
            this.coldTargetName = coldTargetName;
            this.coldAmount = coldAmount;
        }

        private boolean sameContract(Profile other) {
            return normalize(hotTargetName).equals(normalize(other.hotTargetName)) &&
                    hotAmount == other.hotAmount &&
                    normalize(coldTargetName).equals(normalize(other.coldTargetName)) &&
                    coldAmount == other.coldAmount;
        }
    }
}
