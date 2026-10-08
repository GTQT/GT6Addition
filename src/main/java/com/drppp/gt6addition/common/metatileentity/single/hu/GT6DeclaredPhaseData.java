package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Verified heat(...) declarations and explicit derived values from GT6 MT.java.
 * Single-argument heat(melt) sets boil=melt*2 in OreDictMaterial.
 * Exempt marks a directly declared UNBURNABLE/MELTING or positive setSmelting;
 * absence does not prove the absence of an inherited flag.
 */
final class GT6DeclaredPhaseData {
    private static final Map<String, int[]> DATA = new HashMap<>();

    static {
        // MT.java:531-535 explicitly uses heat(0,0,0), not constructor
        // defaults. Particle tags do not grant MELTING/UNBURNABLE.
        put("photon", 0, 0, false);
        put("neutrino", 0, 0, false);
        put("neutron", 0, 0, false);
        put("proton", 0, 0, false);
        put("electron", 0, 0, false);
        // MT.java:1055: explicit alloyElectrolyzer thermal override.
        put("carborundum", 3000, 3100, true);
        DATA.put("siliconcarbide", DATA.get("carborundum"));
        // MT.java:1007; also the explicit H2O component of hydrated minerals.
        put("water", 273, 373, true);
        // Explicit component heat, not inferred from a processing target.
        // Keep these here before configurations: the public phase resolver
        // also reads this table and cannot be called during initialization.
        put("ice", 273, 373, true); // MT.java:1013
        put("coal", 1700, 4300, true); // MT.java:1522, C boiling point.
        put("semiheavywater", 275, 374, true); // MT.java:1008
        put("heavywater", 277, 375, true); // MT.java:1009
        put("tritiatedwater", 280, 377, true); // MT.java:1010
        put("steam", 273, 373, false); // MT.java:1011; distinct material, no MELTING.
        put("freshwater", 273, 373, false); // MT.java:1014
        put("holywater", 273, 373, false); // MT.java:1015
        put("seawater", 273, 373, false); // MT.java:1016
        put("waterdirty", 273, 373, false); // MT.java:1017 internal name.
        put("waterdistilled", 273, 373, true); // MT.java:1018
        put("hydrogenperoxide", 273, 423, false); // MT.java:1019
        DATA.put("distilledwater", DATA.get("waterdistilled")); // CEu FirstDegreeMaterials:1326.
        put("rutile", 2041, 3560, true); // MT.java:1086: Ti melting +100, Ti boiling.
        // MT.java inherited/expressive heat data: Fe = 1811/3134 K.
        put("wroughtiron", 2011, 3134, true);
        put("steel", 2046, 3134, true);
        put("hsla", 1873, 3134, true);
        put("meteoriciron", 2011, 3334, true);
        put("meteoricsteel", 2246, 3334, true); // Steel heat +200 K, MT.java:1738.
        put("darkiron", 2246, 3134, true); // Steel melting +200, Fe boiling; MT.java:1730.
        put("thaumium", 2311, 4134, true); // Fe heat +500/+1000; MT.java:1774.
        put("meteoflamesteel", 3134, 3834, true); // MeteoricSteel boiling -200/+500; MT.java:1769.
        put("kreknorite", 2446, 3534, true); // MeteoricSteel heat +200/+200; MT.java:1848.
        put("annealedcopper", 2800, 2835, true); // MT.java:1656
        put("brass", 1160, 2835, true); // MT.java:1700
        put("bronze", 1357, 2835, true); // Cu heat; MT.java:1705
        // Initially Cu heat (1357/2835). addAlloyingRecipe at MT.java:3374
        // lowers melting to As boiling (1090) minus 20; OreDictMaterial:459.
        put("arseniccopper", 1070, 2835, true);
        put("arsenicbronze", 1357, 2835, true); // MT.java:1710; later recipe does not lower it.
        put("knightmetal", 2146, 3234, true); // Steel heat +100/+100; MT.java:1766
        put("fierysteel", 2934, 3634, true); // Steel boiling -200/+500; MT.java:1767
        put("manasteel", 2311, 4134, true); // Fe heat +500/+1000; MT.java:1820
        put("terrasteel", 2561, 4634, true); // Fe heat +750/+1500; MT.java:1821
        put("elvenelementium", 2811, 5134, true); // Fe heat +1000/+2000; MT.java:1822
        put("elementium", 2811, 5134, true); // Explicit ore name alias on MT.java:1822
        put("gaiaspirit", 3945, 6328, true); // W heat +250/+500; MT.java:1823
        put("mauftrium", 1811, 3134, true); // Fe heat; MT.java:1825
        put("elvorium", 2811, 5134, true); // Fe heat +1000/+2000; MT.java:1826
        putComposition("niflheimpower", 1, new String[]{"elvorium"}, new long[]{1}); // MT.java:1827
        putComposition("muspelheimpower", 1, new String[]{"elvorium"}, new long[]{1}); // MT.java:1828
        put("octine", 2934, 3634, true); // Steel boiling -200/+500; MT.java:1850
        // Explicit expressions in MT.java:1094,3703-3705; Fe melting point is 1811 K.
        put("hematite", 2 * 1811 / 3, 2 * (2 * 1811 / 3), true);
        put("magnetite", 1811, 3622, true);
        put("basalticmineralsand", 1811, 3622, true);
        put("graniticmineralsand", 1811, 3622, true);
        put("hydrochloricacid", 100, 200, false); // MT.java:1020
        put("hydrogenfluoride", 189, 292, false); // MT.java:1021
        DATA.put("hydrofluoricacid", DATA.get("hydrogenfluoride")); // CEu:1234, H1 F1.
        put("air", 100, 200, false); // MT.java:1027
        put("nitrogenmonoxide", 100, 200, false); // MT.java:1028
        DATA.put("nitricoxide", DATA.get("nitrogenmonoxide")); // CEu:1240, N1 O1.
        put("nitrogendioxide", 100, 200, false); // MT.java:1029
        put("ammonia", 195, 239, false); // MT.java:1030
        put("nitricacid", 231, 356, false); // MT.java:1031
        put("carbonmonoxide", 100, 200, false); // MT.java:1034
        put("carbondioxide", 100, 200, false); // MT.java:1035
        put("carbontrioxide", 100, 200, false); // MT.java:1036
        put("methane", 100, 200, false); // MT.java:1037
        put("sugar", 459, 918, true); // MT.java:1038
        put("glycerol", 291, 563, false); // MT.java:1040
        put("glyceryl", 287, 323, false); // MT.java:1041
        put("sulfurdioxide", 100, 200, false); // MT.java:1044
        put("sulfurtrioxide", 100, 200, false); // MT.java:1045
        put("hydrosulfuricacid", 191, 213, false); // MT.java:1046
        DATA.put("hydrogensulfide", DATA.get("hydrosulfuricacid")); // CEu:1167, H2 S1.
        put("sulfuricacid", 200, 400, false); // MT.java:1047
        put("disulfuricacid", 200, 400, false); // MT.java:1048
        put("silveriodide", 831, 1779, false); // MT.java:1051
        put("hexafluorosilicicacid", 250, 381, false); // MT.java:1054
        put("silicondioxide", 1986, 3220, false); // MT.java:1056
        put("glass", 1200, 2400, true); // MT.java:1057
        put("phosphate", 400, 800, false); // MT.java:1074
        // MT.java:1294-1297: Ca3 + (PO4)2, not the elemental P.
        // Preserve the nested PO4 heat override instead of flattening atoms.
        putComposition("tricalciumphosphate", 0,
                new String[]{"calcium", "phosphate"}, new long[]{3, 2}, false);
        DATA.put("bluephosphorus", DATA.get("tricalciumphosphate"));
        DATA.put("redphosphorus", DATA.get("tricalciumphosphate"));
        DATA.put("whitephosphorus", DATA.get("tricalciumphosphate"));
        put("tungstentrioxide", 1746, 1970, true); // MT.java:1077
        put("tungsticacid", 373, 1746, true); // MT.java:1078
        put("alumina", 2345, 3250, true); // MT.java:1081
        put("aluminiumfluoride", 1560, 3120, true); // MT.java:1082
        put("aluminiumhydroxide", 573, 1146, true); // MT.java:1083
        put("titaniumtetrachloride", 249, 409, false); // MT.java:1087
        put("pyrolusite", 808, 2334, true); // MT.java:1090
        put("manganesechloride", 927, 1498, true); // MT.java:1091
        put("ferrouschloride", 950, 1296, true); // MT.java:1095
        put("ferricchloride", 580, 589, true); // MT.java:1096
        put("magnesiumchloride", 987, 1685, true); // MT.java:1100
        put("magnesiumcarbonate", 623, 3000, true); // MT.java:1101
        put("calciumchloride", 1048, 2208, true); // MT.java:1104
        put("calciumsulfate", 1730, 3000, false); // MT.java:1105
        put("gypsum", 1730, 3000, false); // MT.java:1106
        put("quicklime", 2886, 3120, false); // MT.java:1107
        put("calcite", 1612, 3000, true); // MT.java:1108
        put("lithiumchloride", 880, 1655, true); // MT.java:1121
        put("lithiumchlorate", 400, 800, true); // MT.java:1122
        put("lithiumperchlorate", 509, 703, true); // MT.java:1123
        put("lithiumhydroxide", 735, 1197, false); // MT.java:1126
        put("salt", 1074, 1686, false); // MT.java:1129
        put("sodiumnitrate", 607, 1214, false); // MT.java:1130
        put("sodiumhydroxide", 596, 1661, false); // MT.java:1131
        put("sodiumsulfite", 306, 612, false); // MT.java:1136
        put("sodiumsulfate", 1157, 1702, false); // MT.java:1137
        put("sodiumpyrosulfate", 674, 1348, false); // MT.java:1138
        put("sodiumcarbonate", 1124, 2248, true); // MT.java:1139
        put("sodiumaluminate", 1920, 3840, false); // MT.java:1140
        put("sodiumfluoride", 1266, 2532, false); // MT.java:1141
        put("cryolite", 1285, 2570, true); // MT.java:1142
        put("iodinesalt", 300, 370, false); // MT.java:1146
        put("sylvite", 1040, 1690, false); // MT.java:1147
        put("potassiumnitrate", 607, 1214, false); // MT.java:1148
        put("potassiumhydroxide", 633, 1600, false); // MT.java:1149
        put("potassiumsulfite", 306, 612, false); // MT.java:1153
        put("potassiumsulfate", 1342, 1962, false); // MT.java:1154
        put("potassiumpyrosulfate", 598, 1196, false); // MT.java:1155
        put("potassiumcarbonate", 1164, 2328, true); // MT.java:1156
        put("potassiumaluminate", 1920, 3840, false); // MT.java:1157
        put("potassiumfluoride", 1131, 2262, false); // MT.java:1158
        put("chloroauricacid", 200, 400, false); // MT.java:1163
        put("chloroplatinicacid", 200, 400, false); // MT.java:1164
        put("stannicchloride", 200, 400, false); // MT.java:1165
        put("blackvitriol", 200, 400, false); // MT.java:1168
        put("bluevitriol", 200, 400, false); // MT.java:1169
        put("greenvitriol", 200, 400, false); // MT.java:1170
        put("redvitriol", 200, 400, false); // MT.java:1171
        put("pinkvitriol", 200, 400, false); // MT.java:1172
        put("cyanvitriol", 200, 400, false); // MT.java:1173
        put("whitevitriol", 200, 400, false); // MT.java:1174
        put("grayvitriol", 200, 400, false); // MT.java:1175
        put("martianvitriol", 200, 400, false); // MT.java:1176
        put("vitriolofclay", 200, 400, false); // MT.java:1177
        put("uraniumtetrafluoride", 1309, 1690, true); // MT.java:1180
        put("uraniumhexafluoride", 100, 329, false); // MT.java:1181
        put("uranium238tetrafluoride", 1309, 1690, true); // MT.java:1182
        put("uranium238hexafluoride", 100, 329, false); // MT.java:1183
        put("uranium235tetrafluoride", 1309, 1690, true); // MT.java:1184
        put("uranium235hexafluoride", 100, 329, false); // MT.java:1185
        put("aquaregia", 200, 400, false); // MT.java:1188
        put("lava", 1300, 4000, true); // MT.java:1194
        put("biomass", 200, 400, false); // MT.java:1195
        put("biofuel", 100, 400, false); // MT.java:1196
        put("ethanol", 100, 400, false); // MT.java:1197
        put("oil", 100, 400, false); // MT.java:1198
        put("oilsand", 100, 400, false); // MT.java:1199
        put("crudeoil", 100, 400, false); // MT.java:1200
        put("fuel", 100, 400, false); // MT.java:1201
        put("nitrofuel", 100, 400, false); // MT.java:1202
        put("kerosine", 100, 400, false); // MT.java:1203
        put("diesel", 100, 400, false); // MT.java:1204
        put("petrol", 100, 400, false); // MT.java:1205
        put("propane", 100, 200, false); // MT.java:1206
        put("butane", 100, 200, false); // MT.java:1207
        put("propylene", 100, 200, false); // MT.java:1208
        put("ethylene", 100, 200, false); // MT.java:1209
        put("creosote", 100, 400, false); // MT.java:1210
        put("fishoil", 100, 400, false); // MT.java:1211
        put("whaleoil", 100, 400, false); // MT.java:1212
        put("seedoil", 100, 400, false); // MT.java:1213
        put("hempoil", 100, 400, false); // MT.java:1214
        put("linoil", 100, 400, false); // MT.java:1215
        put("sunfloweroil", 100, 400, false); // MT.java:1216
        put("nutoil", 100, 400, false); // MT.java:1217
        put("oliveoil", 100, 400, false); // MT.java:1218
        put("fryingoilhot", 100, 400, false); // MT.java:1219
        put("glue", 150, 500, false); // MT.java:1220
        put("lubricant", 150, 500, false); // MT.java:1221
        put("constructionfoam", 500, 2000, true); // MT.java:1222
        put("uuamplifier", 50, 500, false); // MT.java:1223
        put("uumatter", 50, 500, false); // MT.java:1224
        put("latex", 150, 500, false); // MT.java:1225
        put("bark", 400, 500, true); // MT.java:1243
        put("wood", 400, 500, true); // MT.java:1244
        put("woodtreated", 500, 600, false); // MT.java:1245
        put("woodpolished", 500, 600, false); // MT.java:1246
        put("woodrubber", 350, 450, true); // MT.java:1247
        put("bamboo", 350, 450, true); // MT.java:1248
        put("skyroot", 350, 450, true); // MT.java:1249
        put("weedwood", 350, 450, true); // MT.java:1250
        put("livingwood", 350, 500, true); // MT.java:1251
        put("dreamwood", 350, 550, true); // MT.java:1252
        put("shimmerwood", 350, 550, true); // MT.java:1253
        put("greatwood", 400, 600, true); // MT.java:1254
        put("silverwood", 450, 650, false); // MT.java:1255
        put("peanutwood", 350, 450, false); // MT.java:1256
        put("marshmallow", 1000, 3000, false); // MT.java:1257 wood() retains defaults, no MELTING/FLAMMABLE.
        put("liveroot", 1178, 2465, false); // MT.java:1258
        put("petrifiedwood", 350, 450, false); // MT.java:1259
        put("wax", 350, 700, false); // MT.java:1262
        put("waxbee", 350, 700, false); // MT.java:1263
        put("waxrefractory", 2600, 5200, true); // MT.java:1264
        put("waxparaffin", 400, 800, false); // MT.java:1265
        put("waxplant", 350, 700, false); // MT.java:1266
        put("waxmagic", 350, 700, false); // MT.java:1267
        put("waxamnesic", 350, 700, false); // MT.java:1268
        put("waxsoulful", 350, 700, false); // MT.java:1269
        put("blaze", 4000, 8000, true); // MT.java:1275
        put("prismarine", 1000, 3000, false); // MT.java:1639 default values; stone factory.
        put("prismarinedark", 1000, 3000, false); // MT.java:1640
        put("ceramic", 2000, 4000, false); // MT.java:1279
        put("claybrick", 2000, 4000, false); // MT.java:1280
        put("clay", 2000, 4000, true); // MT.java:1281
        // clay() ends with the same explicit heat and ceramic target.
        for (String name : new String[]{"claybrown", "clayred", "bentonite", "palygorskite", "fullersearth", "kaolinite"}) {
            put(name, 2000, 4000, true);
        }
        put("porcelain", 1800, 3600, false); // MT.java:1289
        put("niter", 607, 1214, false); // MT.java:1293
        put("rubber", 410, 820, true); // MT.java:1301
        put("plastic", 423, 846, true); // MT.java:1302
        put("teflon", 423, 846, true); // MT.java:1303
        put("pvc", 423, 846, true); // MT.java:1304
        put("bakelite", 423, 846, true); // MT.java:1305
        put("hardplastic", 423, 846, true); // MT.java:1306
        putComposition("bone", 8, new String[]{"calcium"}, new long[]{1}, false); // MT.java:1307
        put("tallow", 350, 700, true); // MT.java:1312
        put("meatcooked", 477, 550, true); // MT.java:1317; meat factory inherits MELTING at :168.
        put("meatraw", 477, 550, true); // MT.java:1318
        put("meatrotten", 477, 550, true); // MT.java:1319; meat factory inherits MELTING at :168.
        put("fishcooked", 477, 550, true); // MT.java:1320; same meat factory.
        put("fishraw", 477, 550, true); // MT.java:1321
        put("fishrotten", 477, 550, true); // MT.java:1322; same meat factory.
        put("tofu", 422, 500, true); // MT.java:1331
        put("wheat", 1000, 3000, false); // MT.java:1323; grain/dustfood retain defaults.
        for (String name : new String[]{"barley", "rye", "rice", "oat", "abyssaloat", "corn", "potato"}) {
            put(name, 1000, 3000, false); // MT.java:1324-1330; no later thermal overrides.
        }
        // MT.java:1228,1585,1611 retain the OreDictMaterial constructor heat.
        // InfusedDull's factory adds UNBURNABLE; HexoriumWhite disables
        // smelting. Neither exemption/target is copied by ANY's steal().
        put("ash", 1000, 3000, false);
        put("ashes", 1000, 3000, false);
        put("infuseddull", 1000, 3000, true);
        put("hexoriumwhite", 1000, 3000, false);
        put("soylentgreen", 422, 500, true); // MT.java:1332
        put("cheese", 320, 500, true); // MT.java:1333
        put("amber", 473, 946, false); // MT.java:1439
        put("goldenamber", 473, 946, false); // MT.java:1440
        put("dominicanamber", 473, 946, false); // MT.java:1441
        // MT.java:1358-1368: final heat() overrides both diamond()'s
        // steal(Carbon) and the subsequent component configuration.
        for (String name : new String[]{"diamond", "diamondblue", "diamondgreen", "diamondpurple",
                "diamondred", "diamondyellow", "diamondpink", "diamondindustrial",
                "bluediamond", "greendiamond", "purplediamond", "reddiamond", "yellowdiamond",
                "pinkdiamond", "manadiamond", "elvendragonstone", "gravitite"}) {
            put(name, 4200, 4300, true);
        }
        put("enderpearl", 2723, 3785, true); // MT.java:1499
        put("endereye", 3447, 4978, true); // MT.java:1500
        put("netherstar", 3896, 5127, true); // MT.java:1501
        put("graphene", 4300, 4400, true); // MT.java:1535
        put("ectoplasm", 400, 3000, false); // MT.java:1536
        put("firestone", 3000, 3700, true); // MT.java:1537
        put("redstone", 500, 1500, false); // MT.java:1540
        put("nikolite", 1500, 3000, false); // MT.java:1541
        put("glowstone", 500, 600, false); // MT.java:1544
        put("glowstoneceres", 500, 600, false); // MT.java:1545
        put("glowstoneio", 500, 600, false); // MT.java:1546
        put("glowstoneenceladus", 500, 600, false); // MT.java:1547
        put("glowstoneproteus", 500, 600, false); // MT.java:1548
        put("glowstonepluto", 500, 600, false); // MT.java:1549
        put("gloomstone", 500, 600, false); // MT.java:1550
        put("stone", 1100, 2200, true); // MT.java:1631
        put("concrete", 500, 1000, true); // MT.java:1632
        put("netherrack", 1500, 3000, true); // MT.java:1633
        put("netherbrick", 1800, 3000, true); // MT.java:1634
        put("endstone", 1200, 2400, false); // MT.java:1635
        put("obsidian", 1300, 4000, true); // MT.java:1636
        put("bedrock", 4000, 8000, true); // MT.java:1637
        put("oilshale", 500, 1000, false); // MT.java:1646
        put("petrotheum", 400, 2000, true); // MT.java:1649
        put("aerotheum", 299, 300, true); // MT.java:1650
        put("pyrotheum", 3800, 6400, true); // MT.java:1651
        put("cryotheum", 40, 1000, true); // MT.java:1652
        put("alduorite", 1567, 3134, false); // MT.java:1659
        put("infuscolium", 1828, 3656, false); // MT.java:1660
        put("rubracium", 1847, 3694, false); // MT.java:1661
        put("meutoite", 1837, 3674, false); // MT.java:1662
        put("lemurite", 1179, 2358, false); // MT.java:1663
        put("aredrite", 2240, 4480, false); // MT.java:1664
        put("ceruclase", 1867, 3734, false); // MT.java:1665
        put("oureclase", 2789, 5578, false); // MT.java:1666
        put("kalendrite", 2679, 5358, false); // MT.java:1667
        put("carmot", 1178, 2356, false); // MT.java:1668
        put("sanguinite", 3104, 6208, false); // MT.java:1669
        put("vyroxeres", 2348, 4696, false); // MT.java:1670
        put("eximite", 2758, 5516, false); // MT.java:1671
        put("ignatius", 1978, 3956, false); // MT.java:1672
        put("tungstencarbide", 3070, 6270, true); // MT.java:1720
        put("tantalumhafniumcarbide", 4263, 8526, true); // MT.java:1734
        put("voidmetal", 3000, 5000, false); // MT.java:1776
        put("chromiumdioxide", 650, 1300, false); // MT.java:1779
        put("yttriumbariumcuprate", 1200, 2400, false); // MT.java:1781
        put("niobiumnitride", 2573, 5146, false); // MT.java:1782
        put("bedrockhslaalloy", 4000, 8000, false); // MT.java:1803
        put("bedrockium", 2000, 4000, true); // MT.java:1858
        put("draconium", 4500, 9000, true); // MT.java:1861
        put("draconiumawakened", 5500, 11000, true); // MT.java:1862
        put("crystalmatrix", 3896, 5127, true); // MT.java:1865
        put("cosmicneutronium", 100000, 200000, true); // MT.java:1866
        put("infinity", 100000, 200000, true); // MT.java:1867
        put("stibnite", 823, 1646, true); // MT.java:3711
        put("barite", 1853, 3706, true); // MT.java:3721
        put("brownlimonite", 1523, 3046, true); // MT.java:3739
        put("yellowlimonite", 1523, 3046, true); // MT.java:3740
        put("bauxite", 2800, 5600, false); // MT.java:3748
        put("skystone", 2200, 4400, false); // MT.java:3802
        put("holystone", 2000, 4000, false); // MT.java:3803
        put("livingrock", 1800, 3600, false); // MT.java:3804
        put("deadrock", 1800, 3600, true); // MT.java:3805
        put("betweenstone", 1000, 2000, false); // MT.java:3806
        put("pitstone", 1200, 2400, false); // MT.java:3807
        put("cragrock", 1400, 2800, false); // MT.java:3808
        put("templerock", 1600, 3200, false); // MT.java:3809
        put("mazestone", 2000, 4000, false); // MT.java:3810
        put("castlerock", 2000, 4000, false); // MT.java:3811
        put("umber", 987, 1974, false); // MT.java:3812
        put("komatiite", 1673, 3346, true); // MT.java:3815
        put("pumice", 1673, 3346, true); // MT.java:3816
        put("gabbro", 1673, 3346, true); // MT.java:3817
        put("basalt", 1673, 3346, true); // MT.java:3818
        put("granitered", 1500, 3000, false); // MT.java:3828
        put("graniteblack", 1500, 3000, false); // MT.java:3829
        put("granite", 1500, 3000, false); // MT.java:3830

        // MT.java:1691-1708. setAloy/uumAloy compute heat then alloySimple
        // records the recipe without addAlloyingRecipe's boiling-point clamp.
        // Brass/Bronze retain their explicit later heat above. The additional
        // recipes at 3366,3369-3370,3376-3380 do not lower this batch's heat.
        putComposition("electrum", 0, new String[]{"silver", "gold"}, new long[]{1, 1});
        putComposition("sterlingsilver", 0, new String[]{"copper", "silver"}, new long[]{1, 4});
        putComposition("rosegold", 0, new String[]{"copper", "gold"}, new long[]{1, 4});
        DATA.put("tumbaga", DATA.get("rosegold")); // Explicit ore name, MT.java:1693.
        putComposition("angmallen", 0, new String[]{"gold", "wroughtiron"}, new long[]{1, 1});
        putComposition("goldinductive", 0, new String[]{"gold", "redstone"}, new long[]{1, 1});
        DATA.put("inductivealloy", DATA.get("goldinductive")); // Explicit local name, MT.java:1695.
        putComposition("cdinagalloy", 0, new String[]{"cadmium", "indium", "silver"}, new long[]{1, 1, 1});
        putComposition("gildediron", 9, new String[]{"iron", "gold"}, new long[]{9, 1});
        putComposition("cobaltbrass", 0, new String[]{"brass", "aluminium", "cobalt"}, new long[]{7, 1, 1});
        putComposition("aluminiumalloy", 45, new String[]{"aluminium", "silicon"}, new long[]{45, 1});
        putComposition("blackbronze", 0, new String[]{"copper", "electrum"}, new long[]{3, 2});
        putComposition("bismuthbronze", 0, new String[]{"bismuth", "brass"}, new long[]{1, 4});
        putComposition("hepatizon", 0, new String[]{"gold", "bronze"}, new long[]{1, 1});

        // MT.java:1714-1733. Keep nested alloy truncation and the actual
        // component sum even when commonDivider differs (Damascus/coating).
        putComposition("blacksteel", 0, new String[]{"nickel", "blackbronze", "steel"}, new long[]{1, 1, 3});
        putComposition("bluesteel", 0, new String[]{"sterlingsilver", "bismuthbronze", "steel", "blacksteel"}, new long[]{1, 1, 2, 4});
        putComposition("redsteel", 0, new String[]{"rosegold", "brass", "steel", "blacksteel"}, new long[]{1, 1, 2, 4});
        putComposition("damascussteel", 50, new String[]{"steel", "vanadium", "tungsten"}, new long[]{50, 1, 1});
        putComposition("vanadiumsteel", 0, new String[]{"steel", "vanadium"}, new long[]{4, 1});
        putComposition("tungstensteel", 0, new String[]{"steel", "tungsten"}, new long[]{1, 1});
        DATA.put("wolframsteel", DATA.get("tungstensteel")); // Explicit ore name, MT.java:1719.
        putComposition("steelgalvanized", 9, new String[]{"steel", "zinc"}, new long[]{9, 1});
        putComposition("titaniumgold", 0, new String[]{"titanium", "gold"}, new long[]{3, 1});
        // MT.java:1739-1741; setGenerifying does not copy the ordinary steel's heat.
        putComposition("meteoricblacksteel", 0, new String[]{"nickel", "blackbronze", "meteoricsteel"}, new long[]{1, 1, 3});
        putComposition("meteoricbluesteel", 0, new String[]{"sterlingsilver", "bismuthbronze", "meteoricsteel", "meteoricblacksteel"}, new long[]{1, 1, 2, 4});
        putComposition("meteoricredsteel", 0, new String[]{"rosegold", "brass", "meteoricsteel", "meteoricblacksteel"}, new long[]{1, 1, 2, 4});
        // MT.java:1770-1772: final heat overrides the preceding Ma composition.
        put("meteoflameblacksteel", 3000, 3700, true); // MeteoricBlackSteel boiling -200/+500.
        put("meteoflamebluesteel", 2877, 3577, true); // MeteoricBlueSteel boiling -200/+500.
        put("meteoflameredsteel", 2971, 3671, true); // MeteoricRedSteel boiling -200/+500.
        // The additional recipes at 3346,3357-3360 have component boiling
        // points above these output melting points; they leave this heat intact.

        // MT.java:1754-1763,1777-1784,1809. StainlessSteel uses Invar's
        // already-truncated heat, not a flattened host element configuration.
        putComposition("invar", 0, new String[]{"wroughtiron", "nickel"}, new long[]{2, 1});
        putComposition("constantan", 0, new String[]{"copper", "nickel"}, new long[]{1, 1});
        DATA.put("cupronickel", DATA.get("constantan")); // Explicit MT alias, 1755.
        putComposition("nichrome", 0, new String[]{"nickel", "chromium"}, new long[]{4, 1});
        putComposition("kanthal", 0, new String[]{"wroughtiron", "aluminium", "chromium"}, new long[]{1, 1, 1});
        putComposition("magnalium", 0, new String[]{"magnesium", "aluminium"}, new long[]{1, 2});
        putComposition("stainlesssteel", 0, new String[]{"wroughtiron", "invar", "chromium", "manganese"}, new long[]{4, 3, 1, 1});
        putComposition("ultimet", 0, new String[]{"cobalt", "nickel", "chromium", "molybdenum"}, new long[]{5, 1, 2, 1});
        putComposition("tinalloy", 0, new String[]{"tin", "wroughtiron"}, new long[]{1, 1});
        putComposition("batteryalloy", 0, new String[]{"lead", "antimony"}, new long[]{4, 1});
        putComposition("solderingalloy", 0, new String[]{"tin", "antimony"}, new long[]{9, 1});
        putComposition("osmiridium", 0, new String[]{"osmium", "iridium"}, new long[]{1, 1});
        putComposition("vanadiumgallium", 0, new String[]{"vanadium", "gallium"}, new long[]{3, 1});
        putComposition("niobiumtitanium", 0, new String[]{"niobium", "titanium"}, new long[]{1, 1});
        putComposition("aluminiumbrass", 0, new String[]{"aluminium", "copper"}, new long[]{3, 1});
        DATA.put("aluminumbrass", DATA.get("aluminiumbrass")); // Explicit ore name, 1784.
        putComposition("electricalsteel", 1, new String[]{"steel", "silicon"}, new long[]{1, 1});
        put("flamascussteel", 2996, 3696, true); // MT.java:1773, DamascusSteel boiling -200/+500.
        // Extra recipes at 3355-3356,3361,3371,3384 do not lower these
        // melting points: every listed input boils above the output melt.

        // MT.java:1744-1753,1791-1794,1805-1808. stealStatsElement
        // copies mass/density/atomic stats only, not melting or boiling heat.
        put("redalloy", 1400, 2835, true);
        put("bluealloy", 1400, 2435, true);
        put("purplealloy", 1400, 2435, true);
        put("mingrade", 1400, 2835, true);
        put("electrotinealloy", 1400, 3134, true);
        putComposition("redstonealloy", 1, new String[]{"silicon", "redstone"}, new long[]{1, 1});
        putComposition("nikolinealloy", 1, new String[]{"silicon", "nikolite"}, new long[]{1, 1});
        DATA.put("teslatinealloy", DATA.get("nikolinealloy")); // Explicit MT.java:1749 alias.
        putComposition("electrumflux", 1, new String[]{"electrum", "redstone"}, new long[]{1, 2});
        putComposition("conductiveiron", 1, new String[]{"wroughtiron", "redstone"}, new long[]{1, 1});
        putComposition("energeticsilver", 1, new String[]{"silver", "redstone", "glowstone"}, new long[]{1, 1, 1});
        putComposition("vividalloy", 1, new String[]{"energeticsilver", "enderpearl"}, new long[]{1, 1}); // MT.java:1815
        putComposition("signalum", 8, new String[]{"copper", "silver", "redalloy"}, new long[]{1, 2, 5});
        putComposition("lumium", 4, new String[]{"tin", "silver", "glowstone"}, new long[]{3, 1, 4});
        putComposition("enderiumbase", 4, new String[]{"tin", "silver", "platinum"}, new long[]{2, 1, 1});
        putComposition("enderium", 1, new String[]{"enderiumbase", "enderpearl"}, new long[]{1, 1});
        // MT.java:1796-1797. setAllToTheOutputOf copies processing
        // targets only; the preceding configurations retain their own heat.
        putComposition("refinedglowstone", 1, new String[]{"glowstone", "germanium"}, new long[]{1, 1});
        DATA.put("glowstonerefined", DATA.get("refinedglowstone"));
        putComposition("refinedobsidian", 1, new String[]{"obsidian", "diamond"}, new long[]{1, 1});
        DATA.put("obsidianrefined", DATA.get("refinedobsidian"));
        putComposition("yellorite", 1, new String[]{"yellorium", "oxygen"}, new long[]{1, 2}); // MT.java:1802
        putComposition("obsidiansteel", 1, new String[]{"steel", "obsidian"}, new long[]{1, 9});
        DATA.put("darksteel", DATA.get("obsidiansteel")); // Explicit MT.java:1805 alias.
        putComposition("pulsatingiron", 1, new String[]{"wroughtiron", "enderpearl"}, new long[]{1, 1});
        DATA.put("phasediron", DATA.get("pulsatingiron")); // Explicit MT.java:1806 alias.
        putComposition("energeticalloy", 1, new String[]{"goldinductive", "glowstone"}, new long[]{2, 1});
        // VibrantAlloy is defined BEFORE the post-init recipe clamp. Its
        // configuration captures EnergeticAlloy's initial 778/1742 K heat.
        putComposition("vibrantalloy", 1, new String[]{"energeticalloy", "enderpearl"}, new long[]{1, 1});
        DATA.put("phasedgold", DATA.get("vibrantalloy"));
        DATA.put("vibrant", DATA.get("vibrantalloy")); // Explicit MT.java:1808 ore names.
        // MT.java:3351-3353; all six nonordinary glowstone family members
        // boil at 600 K. addAlloyingRecipe lowers 778 to max(293,600-20).
        // Lumium's 593 K melt is below 600, so its heat remains unchanged.
        put("energeticalloy", 580, 1742, true);

        // MT.java:539 explicitly sets zero heat. Zero is a known component
        // value, not the -1 / Long.MAX_VALUE sentinel for missing data.
        put("magic", 0, 0, true);
        // MT.java:1622 only steals atomic stats/density, retaining the
        // OreDictMaterial defaults (1000/3000 K), unlike an SiO2 heat copy.
        put("soulsand", 1000, 3000, false);
        putComposition("ironwood", 18, new String[]{"wroughtiron", "liveroot", "angmallen"}, new long[]{8, 9, 2}); // MT.java:1764
        putComposition("steeleaf", 1, new String[]{"steel", "magic"}, new long[]{1, 1}); // MT.java:1765
        putComposition("alumite", 9, new String[]{"alumina", "wroughtiron", "obsidian"}, new long[]{5, 2, 18}); // MT.java:1786
        putComposition("soularium", 1, new String[]{"soulsand", "gold"}, new long[]{9, 1}); // MT.java:1810
        putComposition("claycompound", 1, new String[]{"stone", "ceramic"}, new long[]{2, 1}); // MT.java:1811
        DATA.put("crudesteel", DATA.get("claycompound")); // Explicit ore name.
        putComposition("endsteel", 1, new String[]{"endstone", "obsidiansteel", "obsidian"}, new long[]{1, 1, 9}); // MT.java:1812
        putComposition("melodicalloy", 1, new String[]{"endsteel", "endereye"}, new long[]{1, 1}); // MT.java:1813
        putComposition("stellaralloy", 2, new String[]{"melodicalloy", "netherstar", "clay"}, new long[]{1, 1, 4}); // MT.java:1814
        // Extra recipes at 3364-3365 use Endstone/Lava or Al/WroughtIron/Lava;
        // their boiling points exceed the product melt, so no later clamp.

        // MT.java:793-794,1677,1682-1688. These are GT6 fictional element
        // values, not inferred real-world temperatures. Adamantine heat(Ad)
        // is resolved through the existing verified inheritance chain.
        put("atlarus", 3276, 11524, true);
        put("adamantium", 5225, 14528, true);
        put("dolamide", 1000, 3000, false); // MT.java:1842, OreDictMaterial.java:248 defaults.
        // MT.java:1787-1790. No later alloy temperature overrides.
        // Ardite's metal factory does not override OreDictMaterial's defaults.
        put("ardite", 1000, 3000, true); // MT.java:1785
        put("vibranium", 4852, 9415, true); // MT.java:722, explicit UNBURNABLE
        putComposition("manyullyn", 0, new String[]{"cobalt", "ardite"}, new long[]{1, 1});
        putComposition("vibraniumsteel", 0, new String[]{"vibranium", "steel"}, new long[]{1, 3});
        putComposition("vibraniumsilver", 0, new String[]{"vibranium", "silver"}, new long[]{1, 3});
        putComposition("vibramantium", 0, new String[]{"vibranium", "adamantium"}, new long[]{1, 3});
        // MT.java:1838,1853-1855,1875-1876. Preserve nested alloys
        // and the actual component weights, not the output divider.
        putComposition("desh", 0,
                new String[]{"boron", "lanthanum", "neodymium", "niobium", "cobalt", "cerium", "lithium"},
                new long[]{2, 2, 1, 1, 1, 1, 1});
        putComposition("workersalloy", 4, new String[]{"desh", "mercury"}, new long[]{4, 1}); // MT.java:1839
        putComposition("hssg", 0, new String[]{"tungstensteel", "chromium", "molybdenum", "vanadium"},
                new long[]{5, 1, 2, 1});
        putComposition("hsse", 0, new String[]{"hssg", "cobalt", "manganese", "silicon"}, new long[]{6, 1, 1, 1});
        putComposition("hsss", 0, new String[]{"hssg", "osmiridium", "iridium"}, new long[]{6, 2, 1});
        putComposition("titaniumiridium", 0, new String[]{"iridium", "titanium"}, new long[]{1, 1});
        DATA.put("iritanium", DATA.get("titaniumiridium")); // Explicit MT.java:1875 ore name.
        putComposition("titaniumaluminide", 3, new String[]{"titanium", "aluminium"}, new long[]{3, 7});
        // MT.java:696,744,1871-1872: fictional GT6 element values,
        // not CEu blast-furnace temperatures or host alloy composition.
        put("trinium", 2645, 4523, true);
        // MT.java:695,715. Host bare names identify the elements, while
        // the separate *_alloy registrations carry the GT6 alloy identity.
        put("duranium", 1200, 2491, true);
        put("tritanium", 2000, 3138, true);
        DATA.put("duraniumelemental", DATA.get("duranium"));
        DATA.put("tritaniumelemental", DATA.get("tritanium"));
        putComposition("duraniumalloy", 0, new String[]{"duranium", "magnesium"}, new long[]{7, 1});
        putComposition("tritaniumalloy", 0, new String[]{"tritanium", "duranium"}, new long[]{3, 1});
        put("naquadah", 1500, 3000, true);
        // MT.java:745-746,757-760,811,944: explicit fictional elements.
        put("naquadahenriched", 1500, 3000, true);
        DATA.put("enrichednaquadah", DATA.get("naquadahenriched"));
        put("naquadria", 1500, 3000, true);
        put("abyssalnite", 1500, 3000, true);
        put("coralium", 2000, 4000, true);
        put("dreadium", 2500, 5000, true);
        put("ethaxium", 3000, 6000, true);
        put("macguffium", 200, 1000, false);
        put("gravitonium", 112, 1275, false);
        putComposition("trinaquadalloy", 0, new String[]{"trinium", "naquadah", "carbon"}, new long[]{6, 2, 1});
        DATA.put("naquadahalloy", DATA.get("trinaquadalloy")); // Explicit MT.java:1871 ore alias.
        putComposition("trinitanium", 0, new String[]{"trinium", "titanium"}, new long[]{2, 1});
        putComposition("vulcanite", 0, new String[]{"copper", "tellurium"}, new long[]{1, 1});
        putComposition("celenegil", 0, new String[]{"platinum", "orichalcum"}, new long[]{1, 1});
        putComposition("shadowsteel", 0, new String[]{"shadowiron", "lemurite"}, new long[]{1, 1});
        putComposition("inolashite", 0, new String[]{"alduorite", "ceruclase"}, new long[]{1, 1});
        putComposition("haderoth", 0, new String[]{"mithril", "rubracium"}, new long[]{1, 1});
        putComposition("desichalkos", 0, new String[]{"eximite", "meutoite"}, new long[]{1, 1});
        putComposition("tartarite", 0, new String[]{"adamantine", "atlarus"}, new long[]{1, 1});
        putComposition("amordrine", 0, new String[]{"prometheum", "kalendrite"}, new long[]{1, 1});

        // MT.java:1058,1380,1393-1398. uumMcfg calls setMcfg; garnet's
        // valgemelec factory disables smelting, unlike sapphire and flint.
        putComposition("flint", 0, new String[]{"silicondioxide"}, new long[]{1}, true);
        putComposition("sapphire", 6, new String[]{"alumina"}, new long[]{5}, true);
        // MT.java:1381-1386; the impurity changes heat, not the 3/4 Alumina target.
        putComposition("ruby", 6, new String[]{"alumina", "chromium"}, new long[]{5, 1}, true);
        putComposition("bluesapphire", 6, new String[]{"alumina", "iron"}, new long[]{5, 1}, true);
        putComposition("greensapphire", 6, new String[]{"alumina", "magnesium"}, new long[]{5, 1}, true);
        putComposition("purplesapphire", 6, new String[]{"alumina", "vanadium"}, new long[]{5, 1}, true);
        putComposition("yellowsapphire", 6, new String[]{"alumina", "rutile"}, new long[]{5, 1}, true);
        putComposition("orangesapphire", 6, new String[]{"alumina", "copper"}, new long[]{5, 1}, true);
        putComposition("spinel", 0, new String[]{"alumina", "magnesium", "oxygen"}, new long[]{5, 1, 1}, false); // MT.java:1389
        putComposition("balasruby", 0, new String[]{"chromium", "magnesium", "oxygen"}, new long[]{2, 1, 4}, false); // MT.java:1390; after steal(Ruby).
        DATA.put("foolsruby", DATA.get("balasruby"));
        DATA.put("saphire", DATA.get("sapphire")); // Explicit GT6 ore-name spelling.
        // MT.java:1371-1377; emerald factory explicitly grants MELTING.
        for (String name : new String[]{"emerald", "aquamarine", "morganite", "heliodor", "goshenite", "bixbite", "maxixe"}) {
            putComposition(name, 0, new String[]{"alumina", "beryllium", "silicondioxide", "oxygen"}, new long[]{5, 3, 18, 3}, true);
        }
        DATA.put("scarletemerald", DATA.get("bixbite"));
        // MT.java:1425-1437. valgemelec retains disabled smelting.
        for (String name : new String[]{"topaz", "bluetopaz"}) {
            putComposition(name, 0, new String[]{"alumina", "silicondioxide", "fluorine", "water"}, new long[]{5, 3, 2, 3}, false);
        }
        for (String name : new String[]{"tanzanite", "zanite"}) {
            putComposition(name, 0, new String[]{"alumina", "silicondioxide", "calcium", "water", "oxygen"}, new long[]{15, 18, 4, 3, 4}, false);
        }
        putComposition("amazonite", 0, new String[]{"alumina", "silicondioxide", "potassium", "oxygen"}, new long[]{5, 18, 2, 1}, false);
        putComposition("alexandrite", 0, new String[]{"alumina", "beryllium", "oxygen"}, new long[]{1, 1, 1}, false);
        for (String name : new String[]{"opal", "onyxred", "onyxblack"}) {
            putComposition(name, 0, new String[]{"silicondioxide"}, new long[]{1}, false);
        }
        putComposition("sugilite", 0, new String[]{"potassium", "sodium", "pyrolusite", "lithium", "silicondioxide", "oxygen"}, new long[]{1, 2, 2, 3, 36, 2}, false);
        putComposition("peridot", 0, new String[]{"silicondioxide", "iron", "magnesium"}, new long[]{2, 1, 2}, false);
        putComposition("amethyst", 0, new String[]{"silicondioxide", "iron"}, new long[]{4, 1}, false);
        // MT.java:1498: setGenerifying(Amethyst) changes neither heat nor processing
        // targets. The zero-temperature Magic component still counts in the mean.
        putComposition("amethystender", 5, new String[]{"silicondioxide", "iron", "magic"},
                new long[]{4, 1, 1}, false);
        DATA.put("enderamethyst", DATA.get("amethystender"));
        putComposition("dioptase", 0, new String[]{"silicondioxide", "copper", "oxygen", "water"}, new long[]{3, 1, 1, 3}, false);
        DATA.put("onyx", DATA.get("onyxblack"));
        DATA.put("olivine", DATA.get("peridot"));
        // MT.java:1553-1566 quartz definitions; black quartz and Fluix are mixtures.
        for (String name : new String[]{"milkyquartz", "netherquartz", "voidquartz", "sunnyquartz",
                "lavenderquartz", "redquartz", "blazequartz", "smokeyquartz", "smokyquartz",
                "quartzsmoky", "manaquartz", "elvenquartz", "certusquartz"}) {
            putComposition(name, 0, new String[]{"silicondioxide"}, new long[]{1}, true);
        }
        putComposition("quartzblack", 1, new String[]{"silicondioxide", "carbon"}, new long[]{1, 1}, true);
        DATA.put("blackquartz", DATA.get("quartzblack"));
        putComposition("fluix", 2, new String[]{"silicondioxide", "redstone"}, new long[]{2, 1}, true);
        // MT.java:212-214 factory configurations, applied to :1401-1422.
        for (String name : new String[]{"redjasper", "jasper", "oceanjasper", "rainforestjasper",
                "bluejasper", "greenjasper", "yellowjasper"}) {
            putComposition(name, 0, new String[]{"silicondioxide", "iron"}, new long[]{2, 1}, false);
        }
        for (String name : new String[]{"tigereye", "yellowtigereye", "catseye", "greentigereye",
                "dragoneye", "redtigereye", "hawkseye", "bluetigereye", "blackeye", "blacktigereye",
                "tigeriron", "greenaventurine", "aventurine", "brownaventurine", "yellowaventurine",
                "blackaventurine", "blueaventurine", "redaventurine"}) {
            putComposition(name, 0, new String[]{"silicondioxide"}, new long[]{1}, false);
        }
        putComposition("almandine", 0, new String[]{"alumina", "iron", "silicondioxide", "oxygen"}, new long[]{5, 3, 9, 3}, false);
        putComposition("grossular", 0, new String[]{"alumina", "calcium", "silicondioxide", "oxygen"}, new long[]{5, 3, 9, 3}, false);
        putComposition("pyrope", 0, new String[]{"alumina", "magnesium", "silicondioxide", "oxygen"}, new long[]{5, 3, 9, 3}, false);
        putComposition("spessartine", 0, new String[]{"alumina", "manganese", "silicondioxide", "oxygen"}, new long[]{5, 3, 9, 3}, false);
        putComposition("andradite", 0, new String[]{"calcium", "iron", "silicondioxide", "oxygen"}, new long[]{3, 2, 9, 6}, false);
        putComposition("uvarovite", 0, new String[]{"calcium", "chromium", "silicondioxide", "oxygen"}, new long[]{3, 2, 9, 6}, false);
        // Explicit ore names in those declarations, not guessed colour matches.
        DATA.put("garnetred", DATA.get("almandine"));
        DATA.put("garnetorange", DATA.get("grossular"));
        DATA.put("garnetpurple", DATA.get("pyrope"));
        DATA.put("garnet", DATA.get("spessartine"));
        DATA.put("garnetyellow", DATA.get("andradite"));
        DATA.put("garnetgreen", DATA.get("uvarovite"));

        // MT.java:1237,1516-1518; dependencies must be evaluated before Lapis.
        // ore-dust/elec/cent factories do not imply a MELTING flag.
        putComposition("pyrite", 0, new String[]{"iron", "sulfur"}, new long[]{1, 2}, false);
        putComposition("lazurite", 0, new String[]{"alumina", "silicondioxide", "calcium", "sodium"}, new long[]{6, 6, 8, 8}, false);
        putComposition("sodalite", 0, new String[]{"alumina", "silicondioxide", "sodium", "chlorine"}, new long[]{3, 3, 4, 1}, false);
        putComposition("lapis", 0, new String[]{"lazurite", "sodalite", "pyrite", "calcite"}, new long[]{12, 2, 1, 1}, false);

        // No subsequent heat override for these declarations. Preserve GT6's
        // own component configuration, not the host's potentially different one.
        putComposition("vanadiumpentoxide", 0, new String[]{"vanadium", "oxygen"}, new long[]{2, 5}); // MT.java:1065
        putComposition("garnierite", 1, new String[]{"nickel", "oxygen"}, new long[]{1, 1}); // MT.java:3701
        putComposition("realgar", 0, new String[]{"arsenic", "sulfur"}, new long[]{1, 1}); // MT.java:3707
        putComposition("cinnabar", 0, new String[]{"mercury", "sulfur"}, new long[]{1, 1}); // MT.java:3708
        putComposition("molybdenite", 0, new String[]{"molybdenum", "sulfur"}, new long[]{1, 2}); // MT.java:3709
        putComposition("sphalerite", 0, new String[]{"zinc", "sulfur"}, new long[]{1, 1}); // MT.java:3710
        putComposition("pentlandite", 0, new String[]{"nickel", "sulfur"}, new long[]{9, 8}); // MT.java:3712
        putComposition("chalcopyrite", 0, new String[]{"copper", "iron", "sulfur"}, new long[]{1, 1, 2}); // MT.java:3713
        putComposition("arsenopyrite", 0, new String[]{"iron", "arsenic", "sulfur"}, new long[]{1, 1, 1}); // MT.java:3714
        putComposition("cobaltite", 0, new String[]{"cobalt", "arsenic", "sulfur"}, new long[]{1, 1, 1}); // MT.java:3715
        putComposition("galena", 0, new String[]{"lead", "silver", "sulfur"}, new long[]{3, 3, 2}); // MT.java:3716
        putComposition("cooperite", 0, new String[]{"platinum", "nickel", "palladium", "sulfur"}, new long[]{3, 1, 1, 1}); // MT.java:3717
        putComposition("tetrahedrite", 0, new String[]{"copper", "antimony", "iron", "sulfur"}, new long[]{3, 1, 1, 3}); // MT.java:3718
        putComposition("kesterite", 0, new String[]{"copper", "zinc", "tin", "sulfur"}, new long[]{2, 1, 1, 4}); // MT.java:3719
        putComposition("stannite", 0, new String[]{"copper", "iron", "tin", "sulfur"}, new long[]{2, 1, 1, 4}); // MT.java:3720
        putComposition("ferrovanadium", 0, new String[]{"magnetite", "vanadiumpentoxide"}, new long[]{1, 1}); // MT.java:3742
        putComposition("uraninite", 1, new String[]{"uranium", "oxygen"}, new long[]{1, 2}); // MT.java:3702, U_238 has internal name Uranium.
        putComposition("celestine", 0, new String[]{"strontium", "sulfur", "oxygen"}, new long[]{1, 1, 4}); // MT.java:3722
        putComposition("scheelite", 0, new String[]{"calcium", "tungstentrioxide", "oxygen"}, new long[]{1, 4, 1}, false); // MT.java:3724
        putComposition("wolframite", 0, new String[]{"magnesium", "tungstentrioxide", "oxygen"}, new long[]{1, 4, 1}, false); // MT.java:3725
        putComposition("ferberite", 0, new String[]{"iron", "tungstentrioxide", "oxygen"}, new long[]{1, 4, 1}, false); // MT.java:3726
        putComposition("huebnerite", 0, new String[]{"manganese", "tungstentrioxide", "oxygen"}, new long[]{1, 4, 1}, false); // MT.java:3727
        putComposition("tungstate", 0, new String[]{"lithium", "tungstentrioxide", "oxygen"}, new long[]{2, 4, 1}, false); // MT.java:3728
        putComposition("stolzite", 0, new String[]{"lead", "tungstentrioxide", "oxygen"}, new long[]{1, 4, 1}); // MT.java:3730
        putComposition("russellite", 0, new String[]{"bismuth", "tungstentrioxide", "oxygen"}, new long[]{2, 4, 3}); // MT.java:3731
        putComposition("pinalite", 0, new String[]{"lead", "tungstentrioxide", "chlorine", "oxygen"}, new long[]{3, 4, 2, 2}); // MT.java:3732
        // MT.java:3734-3737: no later heat override or MELTING tag.
        putComposition("wollastonite", 0, new String[]{"calcium", "silicondioxide", "oxygen"}, new long[]{1, 3, 1}, false);
        putComposition("zeolite", 0, new String[]{"alumina", "sodium", "silicondioxide", "water", "oxygen"}, new long[]{5, 2, 12, 6, 1}, false);
        putComposition("pollucite", 0, new String[]{"alumina", "caesium", "silicondioxide", "water", "oxygen"}, new long[]{5, 2, 12, 6, 1}, false);
        // MT.java:3742: the literal saved name, distinct from the field alias.
        DATA.put("vanadiummagnetite", DATA.get("ferrovanadium"));
        putComposition("niobiumpentoxide", 0, new String[]{"niobium", "oxygen"}, new long[]{2, 5}, false); // MT.java:1068
        putComposition("tantalumpentoxide", 0, new String[]{"tantalum", "oxygen"}, new long[]{2, 5}, false); // MT.java:1071
        putComposition("tantalite", 0, new String[]{"tantalumpentoxide", "pyrolusite"}, new long[]{7, 1}, false); // MT.java:3743, MnO2 internal name Pyrolusite.
        putComposition("columbite", 0, new String[]{"niobiumpentoxide", "pyrolusite"}, new long[]{7, 1}, false); // MT.java:3744
        putComposition("coltan", 0, new String[]{"tantalite", "columbite"}, new long[]{1, 1}, false); // MT.java:3745
        putComposition("ilmenite", 0, new String[]{"iron", "titanium", "oxygen"}, new long[]{1, 1, 3}); // MT.java:3747, explicit MELTING.
        putComposition("chromite", 0, new String[]{"iron", "chromium", "oxygen"}, new long[]{1, 2, 4}); // MT.java:3749
        putComposition("powellite", 0, new String[]{"calcium", "molybdenum", "oxygen"}, new long[]{1, 1, 4}); // MT.java:3750
        putComposition("wulfenite", 0, new String[]{"lead", "molybdenum", "oxygen"}, new long[]{1, 1, 4}); // MT.java:3751
        putComposition("bastnasite", 0, new String[]{"cerium", "carbon", "fluorine", "oxygen"}, new long[]{1, 1, 1, 3}); // MT.java:3752
        putComposition("pitchblende", 0, new String[]{"uraninite", "thorium", "lead"}, new long[]{3, 1, 1}); // MT.java:3753
        putComposition("malachite", 0, new String[]{"copper", "carbontrioxide", "hydrogen", "oxygen"}, new long[]{2, 4, 2, 2}); // MT.java:3754
        putComposition("bromargyrite", 0, new String[]{"silver", "bromine"}, new long[]{1, 1}); // MT.java:3755
        putComposition("smithsonite", 0, new String[]{"zinc", "carbon", "oxygen"}, new long[]{1, 1, 3}); // MT.java:3756
        putComposition("sperrylite", 0, new String[]{"platinum", "arsenic"}, new long[]{1, 2}); // MT.java:3757
        // MT.java:3759-3775. These ore-dust factories do not grant MELTING.
        putComposition("perlite", 1, new String[]{"obsidian", "water"}, new long[]{1, 1}, false);
        putComposition("trona", 6, new String[]{"sodiumcarbonate", "water"}, new long[]{6, 6}, false);
        putComposition("mirabilite", 7, new String[]{"sodiumsulfate", "water"}, new long[]{7, 30}, false);
        putComposition("bischofite", 3, new String[]{"magnesiumchloride", "water"}, new long[]{3, 6}, false);
        putComposition("borax", 0, new String[]{"sodium", "boron", "water", "oxygen"}, new long[]{2, 4, 30, 7}, false);
        putComposition("spodumene", 0, new String[]{"alumina", "lithium", "silicondioxide", "oxygen"}, new long[]{5, 2, 12, 1}, false);
        putComposition("lepidolite", 0, new String[]{"alumina", "potassium", "lithium", "fluorine", "oxygen"}, new long[]{10, 1, 3, 2, 6}, false);
        putComposition("glauconite", 0, new String[]{"alumina", "potassium", "magnesium", "water", "oxygen"}, new long[]{10, 1, 2, 3, 7}, false);
        // GlauconiteSand is the same GT6 material and an explicit ore-name alias.
        putComposition("glauconitesand", 0, new String[]{"alumina", "potassium", "magnesium", "water", "oxygen"}, new long[]{10, 1, 2, 3, 7}, false);
        putComposition("vermiculite", 0, new String[]{"alumina", "iron", "silicondioxide", "water", "hydrogen"}, new long[]{10, 3, 12, 12, 2}, false);
        putComposition("mica", 0, new String[]{"alumina", "potassium", "silicondioxide", "fluorine"}, new long[]{15, 2, 18, 4}, false);
        putComposition("kyanite", 0, new String[]{"alumina", "silicondioxide"}, new long[]{5, 3}, false);
        putComposition("alunite", 0, new String[]{"alumina", "potassiumhydroxide", "sulfurtrioxide", "water", "oxygen"}, new long[]{15, 6, 16, 15, 9}, false);
        putComposition("diatomite", 0, new String[]{"flint", "hematite", "sapphire"}, new long[]{8, 1, 1}, false); // MT.java:3765
        putComposition("garnetsand", 0, new String[]{"almandine", "andradite", "grossular", "pyrope", "spessartine", "uvarovite"}, new long[]{1, 1, 1, 1, 1, 1}, false); // MT.java:3776
        // MT.java:3779-3790: elemental Dn/Tn, not their similarly named alloys.
        putComposition("diduraniumtrioxide", 0, new String[]{"duraniumelemental", "oxygen"}, new long[]{2, 3}, false);
        putComposition("tritaniumdioxide", 0, new String[]{"tritaniumelemental", "oxygen"}, new long[]{1, 2}, false);
        for (String halogen : new String[]{"fluorine", "chlorine", "bromine", "iodine", "astatine"}) {
            String suffix = halogen.equals("fluorine") ? "fluoride" : halogen.equals("chlorine") ? "chloride" :
                    halogen.equals("bromine") ? "bromide" : halogen.equals("iodine") ? "iodide" : "astatide";
            putComposition("duraniumhexa" + suffix, 0, new String[]{"duraniumelemental", halogen}, new long[]{1, 6}, false);
            putComposition("tritaniumhexa" + suffix, 0, new String[]{"tritaniumelemental", halogen}, new long[]{1, 6}, false);
        }
        // MT.java:3813-3814. Generifying Stone does not copy its temperatures.
        putComposition("shale", 0, new String[]{"calcite", "milkyquartz", "clay"}, new long[]{2, 1, 1}, false);
        putComposition("redrock", 0, new String[]{"calcite", "flint", "clayred"}, new long[]{2, 1, 1}, false);
        for (String name : GT6WoodMaterialData.names()) put(name, 400, 500, true);

        // MT.java:1024,1039,1061-1062,1124-1125,1132-1159,1189-1191.
        // Explicit configuration thermal averages, NOT host molecular weights.
        putComposition("heliumneon", 0, new String[]{"helium", "neon"}, new long[]{1, 1}, false);
        putComposition("vanilla", 0, new String[]{"carbon", "hydrogen", "oxygen"}, new long[]{8, 8, 3}, false);
        putComposition("hydrogenborate", 0, new String[]{"hydrogen", "boron", "oxygen"}, new long[]{3, 1, 3}, false);
        putComposition("datolite", 0, new String[]{"hydrogen", "calcium", "boron", "silicon", "oxygen"}, new long[]{2, 2, 2, 2, 10}, false);
        putComposition("lithiumoxide", 0, new String[]{"lithium", "oxygen"}, new long[]{2, 1}, false);
        putComposition("ferrite", 0, new String[]{"lithium", "iron", "oxygen"}, new long[]{2, 2, 4}, false);
        putComposition("sodiumhydrogencarbonate", 0, new String[]{"sodium", "hydrogen", "carbon", "oxygen"}, new long[]{1, 1, 1, 3}, false);
        putComposition("sodiumbisulfate", 0, new String[]{"sodium", "hydrogen", "sulfur", "oxygen"}, new long[]{1, 1, 1, 4}, false);
        putComposition("sodiumpersulfate", 0, new String[]{"sodium", "sulfur", "oxygen"}, new long[]{1, 1, 4}, false);
        putComposition("sodiumsulfide", 0, new String[]{"sodium", "sulfur"}, new long[]{2, 1}, false);
        putComposition("potassiumbisulfate", 0, new String[]{"potassium", "hydrogen", "sulfur", "oxygen"}, new long[]{1, 1, 1, 4}, false);
        putComposition("potassiumpersulfate", 0, new String[]{"potassium", "sulfur", "oxygen"}, new long[]{1, 1, 4}, false);
        putComposition("potassiumsulfide", 0, new String[]{"potassium", "sulfur"}, new long[]{2, 1}, false);
        putComposition("potassiumheptafluorotantalate", 0, new String[]{"potassium", "tantalum", "fluorine"}, new long[]{2, 1, 7}, false);
        put("saltwater", 273, 363, false); // :1143 heat(C,C+90), AFTER configuration.
        put("saltedwater", 273, 363, false); // :1160, KCl rather than NaCl.
        putComposition("cobalthexahydrate", 0, new String[]{"cobalt", "water"}, new long[]{1, 6}, false);
        putComposition("methaneice", 2, new String[]{"methane", "ice"}, new long[]{1, 2}, false);
        putComposition("nitrocarbon", 0, new String[]{"nitrogen", "carbon"}, new long[]{1, 1}, false);

        // :1230,1233-1239,1298-1299,1308-1310,1350-1351,1478-1479.
        putComposition("volcanicashes", 0, new String[]{"flint", "hematite", "magnesium"}, new long[]{6, 1, 1}, false);
        putComposition("chalk", 0, new String[]{"calcite"}, new long[]{1}, false);
        putComposition("dolomite", 0, new String[]{"calcite", "magnesiumcarbonate"}, new long[]{1, 1}, false);
        putComposition("asbestos", 0, new String[]{"magnesium", "silicondioxide", "water", "oxygen"}, new long[]{3, 6, 6, 3}, false);
        putComposition("talc", 0, new String[]{"magnesium", "silicondioxide", "water", "oxygen"}, new long[]{3, 12, 3, 3}, false);
        putComposition("potassiumfeldspar", 0, new String[]{"potassium", "alumina", "silicondioxide", "oxygen"}, new long[]{2, 5, 18, 1}, false);
        putComposition("biotite", 0, new String[]{"potassium", "magnesium", "alumina", "fluorine", "silicondioxide"}, new long[]{2, 6, 15, 4, 18}, false);
        putComposition("apatite", 0, new String[]{"calcium", "phosphate", "chlorine"}, new long[]{5, 3, 1}, false);
        putComposition("phosphorite", 0, new String[]{"calcium", "phosphate", "fluorine"}, new long[]{5, 3, 1}, false);
        putComposition("slimybone", 8, new String[]{"calcium"}, new long[]{1}, false);
        putComposition("gunpowder", 4, new String[]{"carbon", "sulfur", "sodiumnitrate"}, new long[]{2, 1, 1}, false);
        putComposition("dynamite", 0, new String[]{"glyceryl", "wood"}, new long[]{1, 1}, false);
        put("chocolate", 313, 400, false); // :1336 heat(C+40,400), AFTER configuration.
        put("butter", 313, 500, false); // :1350
        put("saltedbutter", 313, 500, false); // :1351
        // A zero-temperature component still produces the source's clamped 1/2 K.
        putComposition("vinteum", 0, new String[]{"magic"}, new long[]{1}, false);
        putComposition("vinteumpurified", 0, new String[]{"magic"}, new long[]{1}, false);

        // :1511-1513,1525-1528,1534,1581-1582,1626,1845,3777,3819-3820.
        putComposition("zircon", 0, new String[]{"zirconium", "silicondioxide", "oxygen"}, new long[]{1, 3, 2}, false);
        putComposition("azurite", 0, new String[]{"copper", "carbontrioxide", "oxygen", "water"}, new long[]{3, 8, 1, 3}, false);
        putComposition("eudialyte", 0, new String[]{"zircon", "pyrolusite", "sodium", "calcium", "chlorine", "silicondioxide", "oxygen"}, new long[]{18, 3, 15, 6, 2, 75, 12}, false);
        putComposition("prismane", 1, new String[]{"carbon"}, new long[]{4}, false);
        putComposition("lonsdaleite", 1, new String[]{"carbon"}, new long[]{8}, false);
        putComposition("lignite", 7, new String[]{"carbon", "water", "darkashes"}, new long[]{2, 4, 1}, false);
        putComposition("lignitecoke", 7, new String[]{"carbon", "darkashes"}, new long[]{2, 1}, false);
        putComposition("hydratedcoal", 8, new String[]{"coal", "water"}, new long[]{8, 1}, false);
        putComposition("energiumred", 0, new String[]{"sapphire", "redstone"}, new long[]{4, 5}, false);
        putComposition("energiumcyan", 0, new String[]{"sapphire", "nikolite"}, new long[]{4, 5}, false);
        putComposition("monazite", 0, new String[]{"rareearth", "phosphate"}, new long[]{1, 1}, false);
        putComposition("duralumin", 0, new String[]{"aluminium", "copper"}, new long[]{1, 1}, false);
        putComposition("quartzsand", 0, new String[]{"certusquartz", "milkyquartz"}, new long[]{1, 1}, false);
        putComposition("marble", 0, new String[]{"magnesium", "calcite"}, new long[]{1, 7}, false);
        putComposition("limestone", 0, new String[]{"calcite"}, new long[]{1}, false);
    }

    static {
        // MT.java:1720-1723/1731: literal internal names and ore-name aliases.
        DATA.put("wolframcarbide", DATA.get("tungstencarbide"));
        DATA.put("carbide", DATA.get("tungstencarbide"));
        DATA.put("hslasteel", DATA.get("hsla"));
        DATA.put("galvanizedsteel", DATA.get("steelgalvanized"));
    }

    static {
        // AM.java's direct element(...) heat values. Only the explicit
        // Anti-Adamantium MELTING tag grants a burning exemption; ordinary
        // temperature declarations and default self-targets do not.
        for (GT6AntimatterIdentityData.Profile profile : GT6AntimatterIdentityData.profiles()) {
            put(profile.name, profile.melting, profile.boiling, profile.meltingFlag);
        }
    }

    private GT6DeclaredPhaseData() {}

    private static void put(String name, int melt, int boil, boolean exempt) {
        DATA.put(name, new int[]{melt, boil, exempt ? 1 : 0});
    }

    private static void putComposition(String name, long divider, String[] components, long[] weights) {
        putComposition(name, divider, components, weights, true);
    }

    private static void putComposition(String name, long divider, String[] components, long[] weights, boolean exempt) {
        long[] melting = new long[components.length];
        long[] boiling = new long[components.length];
        for (int i = 0; i < components.length; i++) {
            int[] declared = DATA.get(components[i]);
            melting[i] = declared == null ? GT6ElementPhaseData.meltingPoint(components[i]) : declared[0];
            boiling[i] = declared == null ? GT6ElementPhaseData.boilingPoint(components[i]) : declared[1];
            // Existing heat(material)/steal(material) chains are valid
            // components too. Unknown chains retain their missing sentinels.
            if (declared == null && melting[i] < 0) {
                melting[i] = GT6InheritedPhaseData.meltingPoint(components[i]);
            }
            if (declared == null && boiling[i] == Long.MAX_VALUE) {
                boiling[i] = GT6InheritedPhaseData.boilingPoint(components[i]);
            }
            GT6NativeScalarData.Profile scalar = GT6NativeScalarData.find(components[i]);
            if (scalar != null) {
                if (melting[i] < 0) melting[i] = scalar.melting;
                if (boiling[i] == Long.MAX_VALUE) boiling[i] = scalar.boiling;
            }
        }
        long melt = compositionPoint(melting, weights, divider);
        long boil = compositionPoint(boiling, weights, divider);
        if (melt <= 0 || boil <= 0 || melt >= Integer.MAX_VALUE || boil > Integer.MAX_VALUE) return;
        // Thermal data alone must not grant MELTING or a burning exemption.
        put(name, (int) melt, (int) Math.max(melt + 1, boil), exempt);
    }

    /** OreDictConfigurationComponent division, then setMoleculeConfiguration averaging. */
    static long compositionPoint(long[] points, long[] weights, long divider) {
        if (points == null || weights == null || points.length == 0 || points.length != weights.length || divider < 0) return -1;
        long sum = 0;
        for (long weight : weights) {
            if (weight <= 0 || weight > Long.MAX_VALUE - sum) return -1;
            sum += weight;
        }
        if (divider == 0) divider = sum;
        long[] amounts = new long[weights.length];
        double total = 0;
        for (int i = 0; i < weights.length; i++) {
            // GT6 U is deliberately used here to reproduce component rounding.
            if (weights[i] > Long.MAX_VALUE / 648648000L || points[i] < 0 || points[i] == Long.MAX_VALUE) return -1;
            amounts[i] = weights[i] * 648648000L / divider;
            if (amounts[i] <= 0 || points[i] > Long.MAX_VALUE / amounts[i]) return -1;
            total += amounts[i];
        }
        double temperature = 0;
        for (int i = 0; i < points.length; i++) temperature += (points[i] * amounts[i]) / total;
        return Math.max(1L, (long) temperature);
    }

    private static int[] find(String name) {
        return name == null ? null : DATA.get(GT6MaterialIdentity.canonicalOreName(GT6MaterialIdentity.canonicalCompoundName(
                GT6MaterialIdentity.canonicalOxideName(name.toLowerCase(Locale.ROOT)
                .replace("_", "").replace("-", "").replace(" ", "")))));
    }

    static int meltingPoint(String name) {
        GT6TechnicalMaterialData.Profile family = GT6TechnicalMaterialData.find(name);
        if (family != null) return GT6TechnicalMaterialData.meltingPoint(family);
        int[] value = find(name);
        if (value != null) return value[0];
        GT6NativeScalarData.Profile scalar = GT6NativeScalarData.find(name);
        return scalar == null ? -1 : scalar.melting;
    }

    static long boilingPoint(String name) {
        GT6TechnicalMaterialData.Profile family = GT6TechnicalMaterialData.find(name);
        if (family != null) return GT6TechnicalMaterialData.boilingPoint(family);
        int[] value = find(name);
        if (value != null) return value[1];
        GT6NativeScalarData.Profile scalar = GT6NativeScalarData.find(name);
        return scalar == null ? Long.MAX_VALUE : scalar.boiling;
    }

    static boolean hasBurningExemption(String name) {
        if (GT6AntimatterIdentityData.hasMeltingFlag(name)) return true;
        if (GT6TechnicalMaterialData.contains(name)) {
            // Positive copied setSmelting adds MELTING to the family. The
            // only independently declared UNBURNABLE family is ANY.W.
            return "anytungsten".equals(name.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "")) ||
                    CrucibleSmeltingRule.hasMeltingFlag(name);
        }
        int[] value = find(name);
        return value != null && value[2] != 0;
    }
}

