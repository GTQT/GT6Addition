package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** MT.java:318-325,3871-3994: the 110 literal woodnormal material names.
 * The common factory sets C6/H2O15, 400/500 K and Ash/U4 smelting.
 * These are saved names, not display labels or an arbitrary '*wood' rule.
 */
final class GT6WoodMaterialData {
    private static final Set<String> NAMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "oak", "birch", "spruce", "junglewood", "acacia", "darkoak", "crimsonwood", "warpedwood",
            "foxfirewood", "woodcompressed", "wooddead", "woodrotten", "woodmossy", "woodfrozen",
            "woodscorched", "woodvarnished", "woodbleached", "woodtainted", "maple", "willow", "bluemahoe",
            "hazel", "cinnamonwood", "coconutwood", "rainbowood", "bluespruce", "towerwood", "witchwood",
            "ogrewood", "wyvernwood", "aspen", "douglasfir", "sycamore", "whitecedar", "whiteelm",
            "thorntree", "silverpine", "alder", "hawthorn", "rowan", "mahogany", "palm", "autumnwood",
            "cypress", "fir", "japanesemaple", "rainboweucalyptus", "redwood", "sakura", "balsa", "baobab",
            "cherrywood", "chestnutwood", "citruswood", "cocobolowood", "ebony", "giganteumwood",
            "greenheart", "ipe", "kapok", "larch", "limewood", "mahoe", "padauk", "papayawood",
            "plumwood", "poplar", "sequoia", "teak", "walnutwood", "wenge", "zebrawood", "pine",
            "darkwood", "etherealwood", "goldwood", "hellbark", "jacaranda", "mangrove", "sacredoak",
            "magicwood", "applewood", "ashwood", "beech", "boxwood", "brazilwood", "butternutwood",
            "cedar", "elderwood", "elm", "eucalyptus", "figwood", "gingko", "hemlock", "hickory",
            "holly", "hornbeam", "iroko", "locust", "logwood", "maclura", "olivewood", "pearwood",
            "pinkivory", "purpleheart", "rosewood", "sweetgum", "syzgium", "whitebeam", "yew"
    )));

    private GT6WoodMaterialData() {}

    static Set<String> names() { return NAMES; }

    static boolean contains(String name) {
        return name != null && NAMES.contains(GT6MaterialIdentity.canonicalOreName(name.toLowerCase(Locale.ROOT)
                .replace("_", "").replace("-", "").replace(" ", "")));
    }
}
