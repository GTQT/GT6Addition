package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Explicit native identities whose verified factories retain constructor
 * density, with their declaration's final heat override (if any). This is
 * source data, NOT a fallback for arbitrary snapshot or host materials.
 * MT.java:51-57,99-101,159-201,216,219-226,319-326; OreDictMaterial:240,248.
 * setGenerifying/stealLooks do not copy these statistics. No processing or
 * hazard flags are granted here. Source statement inventory lives in tests.
 */
final class GT6NativeScalarData {
    static final class Profile {
        final int melting, boiling;
        final double density;
        Profile(int melting, int boiling, double density) {
            this.melting = melting;
            this.boiling = boiling;
            this.density = density;
        }
    }

    private static final Map<String, Profile> DATA;
    static {
        Map<String, Profile> data = new HashMap<>();
        // These individual declarations have no configuration, heat, statistics
        // copying or later thermal mutation. Factories and initialization were
        // checked; do not extend this list merely because a lookup is missing.
        int[] defaults = {
                8250, 8251, 8252, 8253, 8254, 8255, 8256, 8257, 8258, 8259,
                8260, 8261, 8262, 8263, 8264, 8265, 8200, 8201, 9183, 8247,
                8248, 8210, 8471, 8216, 8266, 8241, 8228, 9709, 9781, 9782,
                9784, 9785, 9786, 9795, 9796, 9797, 9714, 9716, 9717, 9718,
                9788, 9789, 9792, 9794, 8470, 8322, 8321, 8367, 8366, 8452,
                8453, 8454, 8282, 8283, 8284, 8285, 8288, 8293, 8295, 8391,
                8392, 8317, 8360, 8361, 8381, 8382, 8350, 8351, 8352, 8353,
                8354, 8355, 8356, 8357, 8358, 8343, 8344, 8345, 9219, 9220,
                9172, 9185, 9182, 8743, 8753, 8512, 8513, 8514, 8515, 8516,
                9171, 9184, 9244, 9245, 9170, 9218, 9180, 9188, 8511, 9223,
                9173, 9178, 9179, 9181, 9186, 9187, 9222, 9248, 9191, 9249,
                9250, 9251, 9252, 9253, 9254, 9255, 9256, 9257, 9258, 9259,
                9260, 9261, 9262, 9263, 9264, 9265, 9266, 9267, 9268, 9269,
                9270
        };
        for (int id : defaults) put(data, id, 1000, 3000, 1000D);
        // Heat-only overrides leave the constructor density unchanged.
        put(data, 9851, 100, 400, 1000D); // Oil Sand, MT.java:1199
        put(data, 9852, 100, 400, 1000D); // Crude Oil, MT.java:1200
        put(data, 8211, 4000, 8000, 1000D); // Blaze, MT.java:1275
        put(data, 8244, 350, 700, 1000D); // Tallow, MT.java:1312
        put(data, 9778, 422, 500, 1000D); // Tofu, MT.java:1331
        put(data, 9779, 422, 500, 1000D); // Soylent Green, MT.java:1332
        put(data, 9780, 320, 500, 1000D); // Cheese, MT.java:1333
        put(data, 9790, 273, 373, 1000D); // Milk, MT.java:1349
        put(data, 9791, 273, 373, 1000D); // Honey, MT.java:1352
        put(data, 9793, 273, 373, 1000D); // Honeydew, MT.java:1353
        put(data, 8310, 473, 946, 1000D); // Amber, MT.java:1439
        put(data, 8469, 473, 946, 1000D); // Golden Amber, MT.java:1440
        put(data, 8422, 473, 946, 1000D); // Dominican Amber, MT.java:1441
        put(data, 8320, 3896, 5127, 1000D); // Nether Star, MT.java:1501
        put(data, 8373, 400, 3000, 1000D); // Ectoplasm, MT.java:1536
        put(data, 8342, 3000, 3700, 1000D); // Firestone, MT.java:1537
        put(data, 8522, 2000, 4000, 1000D); // Holystone, MT.java:3803
        put(data, 8521, 1800, 3600, 1000D); // Livingrock, MT.java:3804
        put(data, 8523, 1800, 3600, 1000D); // Deadrock, MT.java:3805
        put(data, 8519, 1000, 2000, 1000D); // Betweenstone, MT.java:3806
        put(data, 8520, 1200, 2400, 1000D); // Pitstone, MT.java:3807
        put(data, 8524, 1400, 2800, 1000D); // Cragrock, MT.java:3808
        put(data, 8525, 1600, 3200, 1000D); // Templerock, MT.java:3809
        put(data, 8526, 2000, 4000, 1000D); // Mazestone, MT.java:3810
        put(data, 8527, 2000, 4000, 1000D); // Castlerock, MT.java:3811
        put(data, 8517, 987, 1974, 1000D); // Umber, MT.java:3812
        // :1623-1625 stealStatsElement copies density, not temperatures.
        // Their density comes from the separate verified component/copy table.
        for (int id : new int[]{8102, 8103, 9100}) put(data, id, 1000, 3000, Double.NaN);
        DATA = Collections.unmodifiableMap(data);
    }

    private GT6NativeScalarData() {}

    private static void put(Map<String, Profile> data, int id, int melting, int boiling, double density) {
        String nativeName = GT6MaterialIdentity.name(id);
        if (nativeName == null) throw new IllegalStateException("Missing GT6 scalar identity: " + id);
        String key = normalize(nativeName);
        Profile previous = data.put(key, new Profile(melting, boiling, density));
        // Moonstone (gem) and Moon Stone (stone) normalize to the same host
        // spelling, but their scalar data is identical. Never hide conflicting
        // statistics behind map insertion order.
        if (previous != null && (previous.melting != melting || previous.boiling != boiling ||
                Double.compare(previous.density, density) != 0)) {
            throw new IllegalStateException("Conflicting GT6 scalar identity: " + nativeName);
        }
    }

    static Profile find(String name) { return name == null ? null : DATA.get(normalize(name)); }

    static void addDensities(Map<String, Double> densities) {
        DATA.forEach((name, profile) -> {
            if (!Double.isNaN(profile.density)) densities.put(name, profile.density);
        });
    }

    private static String normalize(String name) {
        return GT6MaterialIdentity.canonicalOreName(name.toLowerCase(Locale.ROOT)
                .replace("_", "").replace("-", "").replace(" ", ""));
    }
}

