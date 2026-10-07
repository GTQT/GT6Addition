package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Literal MT.java factory vararg strings, forwarded through put/addIdenticalNames.
 * This is source identity data, not CEu registry IDs, localized labels, symbols,
 * generification targets or ANY memberships. See scripts/auditGt6Aliases.ps1.
 * Source SHA-256: CF5BD26C6D6E0D4F4078950C7E74DB3182DFF613A46F720DDE72BA535515DE1A.
 */
final class GT6RegistrationAliasData {
    private static final Map<String, Integer> IDS = create();

    private GT6RegistrationAliasData() {}

    private static Map<String, Integer> create() {
        Map<String, Integer> ids = new LinkedHashMap<>();
        put(ids, "Natrium", 110); // MT.java:399, alkali -> Sodium
        put(ids, "Aluminum", 130); // MT.java:401, posttrans -> Aluminium
        put(ids, "Sulphur", 160); // MT.java:404, polyatomic -> Sulfur
        put(ids, "Kalium", 190); // MT.java:407, alkali -> Potassium
        put(ids, "Titan", 220); // MT.java:410, refractmetal -> Titanium
        put(ids, "Chrome", 240); // MT.java:412, refractmetal -> Chromium
        put(ids, "Co60", 278); // MT.java:416, transmetal -> Cobalt-60
        put(ids, "Osmium", 320); // MT.java:421, metalloid -> Germanium
        put(ids, "Columbium", 410); // MT.java:430, refractmetal -> Niobium
        put(ids, "Gregorium", 430); // MT.java:432, transmetal -> Technetium
        put(ids, "Lantanium", 570); // MT.java:446, lanthanide -> Lanthanium
        put(ids, "Lantanum", 570); // MT.java:446, lanthanide -> Lanthanium
        put(ids, "Lanthanum", 570); // MT.java:446, lanthanide -> Lanthanium
        put(ids, "Tantalium", 730); // MT.java:462, refractmetal -> Tantalum
        put(ids, "Wolframium", 740); // MT.java:463, refractmetal -> Tungsten
        put(ids, "Wolfram", 740); // MT.java:463, refractmetal -> Tungsten
        put(ids, "ElnTungsten", 740); // MT.java:463, refractmetal -> Tungsten
        put(ids, "Gol198", 791); // MT.java:469, noblemetal -> Gold-198
        put(ids, "Au198", 791); // MT.java:469, noblemetal -> Gold-198
        put(ids, "Quicksilver", 800); // MT.java:470, precmetal -> Mercury
        put(ids, "QuickSilver", 800); // MT.java:470, precmetal -> Mercury
        put(ids, "Astatine209", 850); // MT.java:475, metalloid -> Astatine
        put(ids, "At209", 850); // MT.java:475, metalloid -> Astatine
        put(ids, "Radium226", 880); // MT.java:478, alkaline -> Radium
        put(ids, "Thorium232", 900); // MT.java:480, actinide -> Thorium
        put(ids, "Th232", 900); // MT.java:480, actinide -> Thorium
        put(ids, "Uranium238", 920); // MT.java:482, actinide -> Uranium
        put(ids, "Uran", 920); // MT.java:482, actinide -> Uranium
        put(ids, "U238", 920); // MT.java:482, actinide -> Uranium
        put(ids, "UraniumEnriched", 921); // MT.java:483, actinide -> Uranium-235
        put(ids, "U235", 921); // MT.java:483, actinide -> Uranium-235
        put(ids, "U233", 922); // MT.java:484, actinide -> Uranium-233
        put(ids, "Neptunium237", 930); // MT.java:485, actinide -> Neptunium
        put(ids, "Np237", 930); // MT.java:485, actinide -> Neptunium
        put(ids, "Plutonium244", 940); // MT.java:486, actinide -> Plutonium
        put(ids, "Pu240", 942); // MT.java:487, actinide -> Plutonium-240
        put(ids, "Pu241", 943); // MT.java:488, actinide -> Plutonium-241
        put(ids, "Pu238", 946); // MT.java:490, actinide -> Plutonium-238
        put(ids, "Pu239", 947); // MT.java:491, actinide -> Plutonium-239
        put(ids, "Am241", 951); // MT.java:493, actinide -> Americium-241
        put(ids, "Am242", 952); // MT.java:494, actinide -> Americium-242
        put(ids, "Tennessine", 1170); // MT.java:517, element -> Farnsium
        put(ids, "Unbipentium", 1250); // MT.java:695, element -> TritaniumElemental
        put(ids, "Unbihexium", 1260); // MT.java:696, element -> Trinium
        put(ids, "Unquadpentium", 1450); // MT.java:715, element -> DuraniumElemental
        put(ids, "Unpentbium", 1520); // MT.java:722, element -> Vibranium
        put(ids, "Unseptquadium", 1740); // MT.java:744, element -> Naquadah
        put(ids, "Unoctpentium", 1850); // MT.java:757, element -> Abyssalnite
        put(ids, "Unocthexium", 1860); // MT.java:758, element -> Coralium
        put(ids, "LiquifiedCoralium", 1860); // MT.java:758, element -> Coralium
        put(ids, "Unoctseptium", 1870); // MT.java:759, element -> Dreadium
        put(ids, "Unoctoctium", 1880); // MT.java:760, element -> Ethaxium
        put(ids, "Bibiunium", 2210); // MT.java:793, element -> Atlarus
        put(ids, "Adamant", 2220); // MT.java:794, element -> Adamantium
        put(ids, "Bibibium", 2220); // MT.java:794, element -> Adamantium
        put(ids, "Bitriennium", 2390); // MT.java:811, element -> Mac-Guffium
        put(ids, "Triseptbium", 3720); // MT.java:944, element -> Gravitonium
        put(ids, "SulphurDioxide", 9834); // MT.java:1044, gaschemdcmp -> Sulfur Dioxide
        put(ids, "SulphurTrioxide", 9835); // MT.java:1045, gaschemdcmp -> Sulfur Trioxide
        put(ids, "SulphuricAcid", 9824); // MT.java:1047, lqudaciddcmp -> Sulfuric Acid
        put(ids, "BoricAcid", 8007); // MT.java:1061, dustelec -> Hydrogen Borate
        put(ids, "TungstenOxide", 8026); // MT.java:1077, oredustdcmp -> Tungsten Trioxide
        put(ids, "NaturalAluminum", 8008); // MT.java:1081, oredustdcmp -> Alumina
        put(ids, "Gibbsite", 8014); // MT.java:1083, oredustdcmp -> Aluminium Hydroxide
        put(ids, "BandedIron", 9104); // MT.java:1094, oredustdcmp -> Hematite
        put(ids, "IronOxide", 9104); // MT.java:1094, oredustdcmp -> Hematite
        put(ids, "Magnesite", 8016); // MT.java:1101, oredustdcmp -> Magnesium Carbonate
        put(ids, "CalciumSulphate", 8274); // MT.java:1105, dustdcmp -> Calcium Sulfate
        put(ids, "Valerite", 9107); // MT.java:1108, oredustdcmp -> Calcite
        put(ids, "Aragonite", 9107); // MT.java:1108, oredustdcmp -> Calcite
        put(ids, "Flux", 9107); // MT.java:1108, oredustdcmp -> Calcite
        put(ids, "Soda", 8039); // MT.java:1132, oredustdcmp -> Sodium Hydrogencarbonate
        put(ids, "SodiumBisulphate", 8230); // MT.java:1133, dustdcmp -> Sodium Bisulfate
        put(ids, "SodiumHydrogenSulfate", 8230); // MT.java:1133, dustdcmp -> Sodium Bisulfate
        put(ids, "SodiumHydrogenSulphate", 8230); // MT.java:1133, dustdcmp -> Sodium Bisulfate
        put(ids, "SodiumPersulphate", 9822); // MT.java:1134, dustdcmp -> Sodium Persulfate
        put(ids, "SodiumSulphide", 9823); // MT.java:1135, dustdcmp -> Sodium Sulfide
        put(ids, "SodiumSulphite", 8269); // MT.java:1136, dustdcmp -> Sodium Sulfite
        put(ids, "SodiumSulphate", 8270); // MT.java:1137, dustdcmp -> Sodium Sulfate
        put(ids, "SodiumPyrosulphate", 8231); // MT.java:1138, dustdcmp -> Sodium Pyrosulfate
        put(ids, "SaltWater", 9804); // MT.java:1143, lqudelec -> Saltwater
        put(ids, "Brine", 9804); // MT.java:1143, lqudelec -> Saltwater
        put(ids, "RockSalt", 8203); // MT.java:1147, oredustdcmp -> Sylvite
        put(ids, "Sylvine", 8203); // MT.java:1147, oredustdcmp -> Sylvite
        put(ids, "Saltpeter", 8205); // MT.java:1148, oredustelec -> Potassium Nitrate
        put(ids, "Nitrate", 8205); // MT.java:1148, oredustelec -> Potassium Nitrate
        put(ids, "Salpeter", 8205); // MT.java:1148, oredustelec -> Potassium Nitrate
        put(ids, "PotassiumBisulphate", 8232); // MT.java:1150, dustdcmp -> Potassium Bisulfate
        put(ids, "PotassiumPersulphate", 8022); // MT.java:1151, dustdcmp -> Potassium Persulfate
        put(ids, "PotassiumSulphide", 8272); // MT.java:1152, dustdcmp -> Potassium Sulfide
        put(ids, "PotassiumSulphite", 8021); // MT.java:1153, dustdcmp -> Potassium Sulfite
        put(ids, "PotassiumSulphate", 8271); // MT.java:1154, dustdcmp -> Potassium Sulfate
        put(ids, "PotassiumPyrosulphate", 8233); // MT.java:1155, dustdcmp -> Potassium Pyrosulfate
        put(ids, "Saltedwater", 9815); // MT.java:1160, lqudelec -> Salted Water
        put(ids, "RomanVitriol", 8404); // MT.java:1169, lqudaciddcmp -> Blue Vitriol
        put(ids, "CyprusVitriol", 8404); // MT.java:1169, lqudaciddcmp -> Blue Vitriol
        put(ids, "SolutionBlueVitriol", 8404); // MT.java:1169, lqudaciddcmp -> Blue Vitriol
        put(ids, "SolutionNickelSulfate", 8408); // MT.java:1173, lqudaciddcmp -> Cyan Vitriol
        put(ids, "SolutionNickelSulphate", 8408); // MT.java:1173, lqudaciddcmp -> Cyan Vitriol
        put(ids, "BioMass", 9840); // MT.java:1195, lqudflam -> Biomass
        put(ids, "Oilsands", 9851); // MT.java:1199, oredust -> Oil Sand
        put(ids, "FuelOil", 9860); // MT.java:1201, lqudexpl -> Fuel
        put(ids, "Gasoline", 9864); // MT.java:1205, lqudexpl -> Petrol
        put(ids, "Creosote Oil", 9870); // MT.java:1210, lqudflam -> Creosote
        put(ids, "Ash", 8200); // MT.java:1228, dust -> Ashes
        put(ids, "DarkAsh", 8201); // MT.java:1229, dust -> Dark Ashes
        put(ids, "AshDark", 8201); // MT.java:1229, dust -> Dark Ashes
        put(ids, "AshesDark", 8201); // MT.java:1229, dust -> Dark Ashes
        put(ids, "VolcanicAsh", 8202); // MT.java:1230, dustcent -> Volcanic Ashes
        put(ids, "AshVolcanic", 8202); // MT.java:1230, dustcent -> Volcanic Ashes
        put(ids, "AshesVolcanic", 8202); // MT.java:1230, dustcent -> Volcanic Ashes
        put(ids, "Chrysotile", 9103); // MT.java:1235, oredustelec -> Asbestos
        put(ids, "Soapstone", 9169); // MT.java:1236, oredustelec -> Talc
        put(ids, "WoodSealed", 8222); // MT.java:1245, wood -> WoodTreated
        put(ids, "Peanutwood", 8227); // MT.java:1256, wood -> Peanut Wood
        put(ids, "BeesWax", 8236); // MT.java:1263, wax -> WaxBee
        put(ids, "Beeswax", 8236); // MT.java:1263, wax -> WaxBee
        put(ids, "BeeWax", 8236); // MT.java:1263, wax -> WaxBee
        put(ids, "Beewax", 8236); // MT.java:1263, wax -> WaxBee
        put(ids, "RefractoryWax", 8237); // MT.java:1264, wax -> WaxRefractory
        put(ids, "Refractorywax", 8237); // MT.java:1264, wax -> WaxRefractory
        put(ids, "ParaffinWax", 8238); // MT.java:1265, wax -> WaxParaffin
        put(ids, "Paraffinwax", 8238); // MT.java:1265, wax -> WaxParaffin
        put(ids, "Brick", 9243); // MT.java:1280, create -> Clay Brick
        put(ids, "FullersEarth", 9154); // MT.java:1285, clay -> Palygorskite
        put(ids, "Nitre", 8206); // MT.java:1293, oredustcent -> Niter
        put(ids, "Phosphorous", 8208); // MT.java:1294, cent -> Phosphorus
        put(ids, "Polymer", 8196); // MT.java:1303, create -> Teflon
        put(ids, "PTFE", 8196); // MT.java:1303, create -> Teflon
        put(ids, "Polycarbonate", 8199); // MT.java:1306, create -> Hard Plastic
        put(ids, "Fossil", 8219); // MT.java:1307, oredustelec -> Bone
        put(ids, "Meat", 9701); // MT.java:1317, meat -> MeatCooked
        put(ids, "Flour", 9702); // MT.java:1323, grain -> Wheat
        put(ids, "Oats", 9707); // MT.java:1327, grain -> Oat
        put(ids, "AbyssalOats", 9719); // MT.java:1328, grain -> Abyssal Oat
        put(ids, "CoffeeDust", 9784); // MT.java:1337, dustfood -> Coffee
        put(ids, "Pepper", 9788); // MT.java:1347, dustfood -> PepperBlack
        put(ids, "ScarletEmerald", 8386); // MT.java:1376, emerald -> Bixbite
        put(ids, "Saphire", 8304); // MT.java:1380, sapphire -> Sapphire
        put(ids, "FoolsRuby", 8303); // MT.java:1390, valgemelec -> Balas Ruby
        put(ids, "GarnetRed", 9101); // MT.java:1393, garnet -> Almandine
        put(ids, "GarnetOrange", 9119); // MT.java:1394, garnet -> Grossular
        put(ids, "GarnetPurple", 9127); // MT.java:1395, garnet -> Pyrope
        put(ids, "Garnet", 9129); // MT.java:1396, garnet -> Spessartine
        put(ids, "GarnetYellow", 9102); // MT.java:1397, garnet -> Andradite
        put(ids, "GarnetGreen", 9135); // MT.java:1398, garnet -> Uvarovite
        put(ids, "Jasper", 8309); // MT.java:1401, jasper -> Red Jasper
        put(ids, "YellowTigerEye", 8430); // MT.java:1409, tigereye -> Tiger Eye
        put(ids, "GreenTigerEye", 8431); // MT.java:1410, tigereye -> Cat's Eye
        put(ids, "RedTigerEye", 8432); // MT.java:1411, tigereye -> Dragon Eye
        put(ids, "BlueTigerEye", 8433); // MT.java:1412, tigereye -> Hawk's Eye
        put(ids, "BlackTigerEye", 8434); // MT.java:1413, tigereye -> Black Eye
        put(ids, "Aventurine", 8448); // MT.java:1417, aventurine -> Green Aventurine
        put(ids, "Onyx", 8416); // MT.java:1433, valgemelec -> OnyxBlack
        put(ids, "Olivine", 8311); // MT.java:1435, valgemelec -> Peridot
        put(ids, "ArcaneAsh", 8367); // MT.java:1480, dust -> Arcane Ashes
        put(ids, "Ender", 8318); // MT.java:1499, elec -> EnderPearl
        put(ids, "Coke", 8349); // MT.java:1523, coal -> Coal Coke
        put(ids, "PetCoke", 8390); // MT.java:1529, coal -> Petroleum Coke
        put(ids, "Electrotine", 8340); // MT.java:1541, redstone -> Nikolite
        put(ids, "Teslatite", 8340); // MT.java:1541, redstone -> Nikolite
        put(ids, "QuartzSmoky", 8397); // MT.java:1560, quartz -> Smokey Quartz
        put(ids, "SmokyQuartz", 8397); // MT.java:1560, quartz -> Smokey Quartz
        put(ids, "BrickNether", 8503); // MT.java:1634, stone -> Nether Brick
        put(ids, "Oilshale", 9853); // MT.java:1646, oredustcent -> Oil Shale
        put(ids, "WrougtIron", 8643); // MT.java:1655, metalmachnd -> Wrought Iron
        put(ids, "Adluorite", 8760); // MT.java:1659, setalore -> Alduorite
        put(ids, "Ardaite", 8701); // MT.java:1664, metalore -> Aredrite
        put(ids, "Fettelite", 8771); // MT.java:1669, metalore -> Sanguinite
        put(ids, "Mythril", 8678); // MT.java:1681, slloymachore -> Mithril
        put(ids, "Tumbaga", 8602); // MT.java:1693, slloymachine -> Rose Gold
        put(ids, "TungstenSteel", 8635); // MT.java:1719, alloymachine -> Tungstensteel
        put(ids, "Wolframsteel", 8635); // MT.java:1719, alloymachine -> Tungstensteel
        put(ids, "WolframSteel", 8635); // MT.java:1719, alloymachine -> Tungstensteel
        put(ids, "Carbide", 8638); // MT.java:1720, alloymachine -> Tungsten Carbide
        put(ids, "WolframCarbide", 8638); // MT.java:1720, alloymachine -> Tungsten Carbide
        put(ids, "HSLA", 8637); // MT.java:1721, alloymachine -> HSLA-Steel
        put(ids, "FzDarkIron", 8648); // MT.java:1730, metalmachore -> Dark Iron
        put(ids, "FZDarkIron", 8648); // MT.java:1730, metalmachore -> Dark Iron
        put(ids, "TeslatineAlloy", 8737); // MT.java:1749, clloy -> Nikoline Alloy
        put(ids, "Cupronickel", 8662); // MT.java:1755, clloymachine -> Constantan
        put(ids, "IronWood", 8672); // MT.java:1764, alloymachine -> Ironwood
        put(ids, "KnightMetal", 8674); // MT.java:1766, alloymachine -> Knightmetal
        put(ids, "Fiery", 8675); // MT.java:1767, alloymachine -> Fiery Steel
        put(ids, "Void", 8681); // MT.java:1776, alloymachine -> Void Metal
        put(ids, "AluminumBrass", 8700); // MT.java:1784, clloymachine -> Aluminium Brass
        put(ids, "DarkSteel", 8731); // MT.java:1805, alloy -> Obsidian Steel
        put(ids, "PhasedIron", 8725); // MT.java:1806, alloy -> Pulsating Iron
        put(ids, "PhasedGold", 8726); // MT.java:1808, slloy -> Vibrant Alloy
        put(ids, "Vibrant", 8726); // MT.java:1808, slloy -> Vibrant Alloy
        put(ids, "CrudeSteel", 8806); // MT.java:1811, alloy -> Clay Compound
        put(ids, "Elementium", 8722); // MT.java:1822, slloymachore -> Elven Elementium
        put(ids, "HeeEndium", 8736); // MT.java:1824, metalore -> Endium
        put(ids, "Ancient", 8744); // MT.java:1832, metalore -> Ancient Debris
        put(ids, "Unstableingot", 8805); // MT.java:1868, setal -> Unstable
        put(ids, "NaquadahAlloy", 8684); // MT.java:1871, alloymachine -> Trinaquadalloy
        put(ids, "Iritanium", 8793); // MT.java:1875, alloymachine -> Titanium Iridium
        put(ids, "CassiteriteSand", 9108); // MT.java:3700, oredustelec -> Cassiterite
        put(ids, "Sheldonite", 9116); // MT.java:3717, oredustdcmp -> Cooperite
        put(ids, "CalciumTungstate", 9128); // MT.java:3724, oredustdcmp -> Scheelite
        put(ids, "Gyubnera", 9195); // MT.java:3727, oredustdcmp -> Huebnerite
        put(ids, "Raspite", 9193); // MT.java:3730, oredustdcmp -> Stolzite
        put(ids, "BogIron", 9137); // MT.java:3740, oredustdcmp -> Yellow Limonite
        put(ids, "Ferrovanadium", 9143); // MT.java:3742, oredustcent -> Vanadium Magnetite
        put(ids, "Illmenite", 9120); // MT.java:3747, oredustdcmp -> Ilmenite
        put(ids, "TitaniumIron", 9120); // MT.java:3747, oredustdcmp -> Ilmenite
        put(ids, "GlauconiteSand", 9150); // MT.java:3769, oredustelec -> Glauconite
        put(ids, "RedRock", 8509); // MT.java:3814, stonecent -> Redrock
        put(ids, "Paduak", 9357); // MT.java:3939, woodnormal -> Padauk
        return Collections.unmodifiableMap(ids);
    }

    private static void put(Map<String, Integer> ids, String alias, int nativeId) {
        String key = alias.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]", "");
        Integer previous = ids.put(key, nativeId);
        if (previous != null && previous != nativeId)
            throw new IllegalStateException("Conflicting GT6 registration alias: " + alias);
        if (GT6MaterialIdentity.name(nativeId) == null)
            throw new IllegalStateException("Missing GT6 alias target: " + nativeId);
    }

    /** Callers normalize spelling; explicit namespaces are never stripped. */
    static String canonicalName(String normalizedName) {
        Integer id = IDS.get(normalizedName);
        if (id == null) return normalizedName;
        return GT6MaterialIdentity.name(id).toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    static Map<String, Integer> identities() {
        return IDS;
    }
}

