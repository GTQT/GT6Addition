package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Element definitions extracted from GT6 MT.java's numeric element factory calls.
 * Includes isotopes; values are GT6 gameplay data, not claimed real-world measurements.
 * MELTING is inherited from the verified metal/metalloid factory chain.
 */
final class GT6ElementPhaseData {
    private static final Map<String, int[]> DATA = new HashMap<>();

    static {
        put("hydrogen", 14, 20, false);
        put("deuterium", 14, 20, false);
        put("tritium", 14, 20, false);
        put("helium", 1, 4, false);
        put("helium3", 1, 4, false);
        put("lithium", 453, 1560, true);
        put("lithium6", 453, 1560, true);
        put("beryllium", 1560, 2742, true);
        put("beryllium7", 1560, 2742, true);
        put("beryllium8", 1560, 2742, true);
        put("boron", 2349, 4200, true);
        put("boron11", 2349, 4200, true);
        put("carbon", 3800, 4300, true); // MT.java:392-394 explicit MELTING, despite polyatomic factory.
        put("carbon13", 3800, 4300, true);
        put("carbon14", 3800, 4300, true);
        put("nitrogen", 63, 77, false);
        put("oxygen", 54, 90, false);
        put("fluorine", 53, 85, false);
        put("neon", 24, 27, false);
        put("sodium", 370, 1156, true);
        put("magnesium", 923, 1363, true);
        put("aluminium", 933, 2792, true);
        put("silicon", 1687, 3538, true);
        put("phosphor", 317, 550, false);
        put("sulfur", 388, 717, true); // MT.java:404 explicit MELTING.
        put("chlorine", 171, 239, false);
        put("argon", 83, 87, false);
        put("potassium", 336, 1032, true);
        put("calcium", 1115, 1757, true);
        put("scandium", 1814, 3109, true);
        put("titanium", 1941, 3560, true);
        put("vanadium", 2183, 3680, true);
        put("chromium", 2180, 2944, true);
        put("manganese", 1519, 2334, true);
        put("iron", 1811, 3134, true);
        put("cobalt", 1768, 3200, true);
        put("cobalt60", 1768, 3200, true);
        put("nickel", 1728, 3186, true);
        put("copper", 1357, 2835, true);
        put("zinc", 692, 1180, true);
        put("gallium", 302, 2477, true);
        put("germanium", 1211, 3106, true);
        put("arsenic", 887, 1090, true);
        put("selenium", 453, 958, false);
        put("bromine", 265, 332, false);
        put("krypton", 115, 119, false);
        put("rubidium", 312, 961, true);
        put("strontium", 1050, 1655, true);
        put("yttrium", 1799, 3609, true);
        put("zirconium", 2128, 4682, true);
        put("niobium", 2750, 5017, true);
        put("molybdenum", 2896, 4912, true);
        put("technetium", 2430, 4538, true);
        put("ruthenium", 2607, 4423, true);
        put("rhodium", 2237, 3968, true);
        put("palladium", 1828, 3236, true);
        put("silver", 1234, 2435, true);
        put("cadmium", 594, 1040, true);
        put("indium", 429, 2345, true);
        put("tin", 505, 2875, true);
        put("antimony", 903, 1860, true);
        put("tellurium", 722, 1261, true);
        put("iodine", 386, 457, false);
        put("xenon", 161, 165, false);
        put("caesium", 301, 944, true);
        put("barium", 1000, 2170, true);
        put("lanthanium", 1193, 3737, true);
        put("cerium", 1068, 3716, true);
        put("praseodymium", 1208, 3793, true);
        put("neodymium", 1297, 3347, true);
        put("promethium", 1315, 3273, true);
        put("samarium", 1345, 2067, true);
        put("europium", 1099, 1802, true);
        put("gadolinium", 1585, 3546, true);
        put("terbium", 1629, 3503, true);
        put("dysprosium", 1680, 2840, true);
        put("holmium", 1734, 2993, true);
        put("erbium", 1802, 3141, true);
        put("thulium", 1818, 2223, true);
        put("ytterbium", 1097, 1469, true);
        put("lutetium", 1925, 3675, true);
        put("hafnium", 2506, 4876, true);
        put("tantalum", 3290, 5731, true);
        put("tungsten", 3695, 5828, true);
        put("rhenium", 3459, 5869, true);
        put("osmiumelemental", 3306, 5285, true);
        put("iridium", 2719, 4701, true);
        put("platinum", 2041, 4098, true);
        put("gold", 1337, 3129, true);
        put("gold198", 1337, 3129, true);
        put("mercury", 234, 629, true);
        put("thallium", 577, 1746, true);
        put("lead", 600, 2022, true);
        put("bismuth", 544, 1837, true);
        put("polonium", 527, 1235, true);
        put("astatine", 575, 610, true);
        put("radon", 202, 211, false);
        put("francium", 300, 950, true);
        put("radium", 973, 2010, true);
        put("actinium", 1323, 3471, true);
        put("thorium", 2115, 5061, true);
        put("protactinium", 1841, 4300, true);
        put("uranium", 1405, 4404, true);
        put("uranium235", 1405, 4404, true);
        put("uranium233", 1405, 4404, true);
        put("neptunium", 917, 4273, true);
        put("plutonium", 912, 3501, true);
        put("plutonium240", 912, 3501, true);
        put("plutonium241", 912, 3501, true);
        put("plutonium243", 912, 3501, true);
        put("plutonium238", 912, 3501, true);
        put("plutonium239", 912, 3501, true);
        put("americium", 1449, 2880, true);
        put("americium241", 1449, 2880, true);
        put("americium242", 1449, 2880, true);
        put("curium", 1613, 3383, true);
        put("berkelium", 1259, 2900, true);
        put("californium", 1173, 1743, true);
        put("einsteinium", 1133, 1269, true);
        put("fermium", 1125, 3000, true);
        put("mendelevium", 1100, 3000, true);
        put("nobelium", 1100, 3000, true);
        put("lawrencium", 1900, 3000, true);
        put("rutherfordium", 2400, 5800, true);
        put("dubnium", 1000, 3000, true);
        put("seaborgium", 1000, 3000, true);
        put("bohrium", 1000, 3000, true);
        put("hassium", 1000, 3000, true);
        put("meitnerium", 1000, 3000, false);
        put("darmstadtium", 1000, 3000, false);
        put("roentgenium", 1000, 3000, false);
        put("copernicium", 150, 357, true);
        put("nihonium", 700, 1400, false);
        put("flerovium", 340, 420, true);
        put("flerovium298", 340, 420, true);
        put("moscovium", 700, 1400, false);
        put("livermorium", 708, 1085, false);
        put("farnsium", 673, 823, false);
        put("oganesson", 258, 263, false);
        put("ununennium", 290, 903, false);
        put("unbinilium", 953, 1973, false);
        DATA.put("osmium", DATA.get("osmiumelemental"));
        DATA.put("aluminum", DATA.get("aluminium"));
        DATA.put("phosphorus", DATA.get("phosphor"));
        DATA.put("lanthanum", DATA.get("lanthanium"));
        DATA.put("sulphur", DATA.get("sulfur")); // MT.java:404 explicit ore alias.
        DATA.put("cesium", DATA.get("caesium"));
    }

    private GT6ElementPhaseData() {}

    private static void put(String name, int melt, int boil, boolean melting) {
        DATA.put(name, new int[]{melt, boil, melting ? 1 : 0});
    }

    private static int[] find(String name) {
        return name == null ? null : DATA.get(GT6MaterialIdentity.canonicalOreName(
                name.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "")));
    }

    static int meltingPoint(String name) {
        int[] data = find(name);
        return data == null ? -1 : data[0];
    }

    static long boilingPoint(String name) {
        int[] data = find(name);
        return data == null ? Long.MAX_VALUE : data[1];
    }

    static boolean hasMeltingFlag(String name) {
        int[] data = find(name);
        return data != null && data[2] != 0;
    }
}

