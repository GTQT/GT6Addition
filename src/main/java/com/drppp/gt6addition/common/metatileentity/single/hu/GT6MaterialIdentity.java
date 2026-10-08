package com.drppp.gt6addition.common.metatileentity.single.hu;

/** Explicit numeric IDs from the bundled GT6 MT.java reference snapshot.
 * Factories forward the ID unchanged to OreDictMaterial.createMaterial.
 * Only literal positive IDs with literal names are included; unknown/dynamic
 * IDs and third-party ranges must not be interpreted as CEu registry IDs.
 */
final class GT6MaterialIdentity {
    private GT6MaterialIdentity() {}

    /** Source-verified Fe2O3 names; never infer identity from name similarity. */
    static String canonicalOxideName(String normalizedName) {
        if ("bandediron".equals(normalizedName) || "ironiiioxide".equals(normalizedName)) return "hematite";
        return normalizedName;
    }

    /** MT.java:1041 and CEu OrganicChemistryMaterials:89 both define C3H5N3O9.
     * Glycerol (C3H8O3) is a different compound and must remain separate.
     */
    static String canonicalCompoundName(String normalizedName) {
        return "glyceryltrinitrate".equals(normalizedName) ? "glyceryl" : normalizedName;
    }

    /** Literal registration aliases, with explicit host spelling adapters.
     * Callers normalize spelling first; an explicit namespace is never removed here.
     */
    static String canonicalOreName(String normalizedName) {
        if (normalizedName == null) return null;
        normalizedName = normalizedName.replace("'", ""); // Cat's Eye / Hawk's Eye native spelling.
        // CEu's osmium is element 76, not GT6's legacy "Osmium" alias
        // of Germanium (:421). Raw GT6 records use hostRecyclingName instead.
        if ("osmium".equals(normalizedName)) return normalizedName;
        // CEu's phosphorus is elemental; GT6 Phosphorous is an explicit
        // alias of the distinct Ca3(PO4)2 compound (:1294).
        if ("phosphorous".equals(normalizedName)) return "tricalciumphosphate";
        // Existing host polymer bindings, not new GT6 literal aliases.
        if ("polytetrafluoroethylene".equals(normalizedName)) return "teflon";
        if ("polyvinylchloride".equals(normalizedName)) return "pvc";
        return GT6AntimatterIdentityData.canonicalName(GT6RegistrationAliasData.canonicalName(normalizedName));
    }

    /** One known retired addon identity; do not strip arbitrary namespaces. */
    static String migrateOwnRegistryName(String name) {
        return "gt6addition:wheat".equals(name) ? "gregtech:wheat" : name;
    }

    /** Explicit registry namespaces must survive normalized-name fallback. */
    static boolean allowsRegistryNamespace(String requestedName, String registeredName) {
        if (requestedName == null || requestedName.isEmpty() || registeredName == null || registeredName.isEmpty())
            return false;
        int separator = requestedName.indexOf(':');
        if (separator < 0) return true; // Legacy unqualified names remain supported.
        if (separator == 0 || separator == requestedName.length() - 1 ||
                requestedName.indexOf(':', separator + 1) >= 0) return false;
        int registeredSeparator = registeredName.indexOf(':');
        return registeredSeparator > 0 && requestedName.substring(0, separator)
                .equals(registeredName.substring(0, registeredSeparator));
    }

    /** GT6 recycling names are not the host registry's same-looking names. */
    static String hostRecyclingName(String gt6Name) {
        if (gt6Name == null) return null;
        if (gt6Name.indexOf(':') >= 0) return gt6Name;
        String originalName = gt6Name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]", "");
        String normalized = GT6AntimatterIdentityData.canonicalName(
                GT6RegistrationAliasData.canonicalName(originalName));
        switch (normalized) {
            case "glyceryl": return "gregtech:glyceryl_trinitrate";
            case "teflon": return "gregtech:polytetrafluoroethylene";
            case "pvc": return "gregtech:polyvinyl_chloride";
            // ANY.java:94-108,117-133: negative-ID technical families
            // save their sanitized name, not an index in MT's numeric array.
            // These families steal the same material's heat/density and
            // copy its processing targets. Do not collapse member variants.
            case "anyiron":
            case "anyironorsteel": return "gregtech:iron";
            case "anyironsteel": return "gregtech:steel";
            case "anyblacksteel": return "gregtech:black_steel";
            case "anybluesteel": return "gregtech:blue_steel";
            case "anyredsteel": return "gregtech:red_steel";
            case "anycopper": return "gregtech:copper";
            case "anytungsten": return "gregtech:tungsten";
            case "anycarbon":
            case "anycoalcarbon":
            case "anycoal/carbon": return "gregtech:carbon";
            case "anydiamond": return "gregtech:diamond";
            case "anysapphire": return "gregtech:sapphire";
            case "anyemerald": return "gregtech:emerald";
            // ANY.java:101 copies Amethyst's targets, including disabled
            // smelting. Adjacent Garnet/Jasper/TigerEye families only steal
            // stats and must not inherit a member's processing targets.
            case "anyamethyst": return "gregtech:amethyst";
            case "anyglowstone": return "gregtech:glowstone";
            case "anyfluorite": return "gtqtcore:fluorite";
            // ANY.java:112-133,136-149: copy the base processing targets,
            // not the target suggested by the family's display name.
            case "anywood":
            case "anydefaultwood":
            case "anynormalwood":
            case "anymagicalwood":
            case "anytreatedwood":
            case "anyuntreatedwood": return "gregtech:wood";
            case "anysilicon": return "gregtech:silicon";
            case "anysilicondioxide": return "gregtech:silicon_dioxide";
            case "anyrubber": return "gregtech:rubber";
            case "anyashes": return "gregtech:ash";
            case "anyclay": return "gregtech:clay";
            case "anysalt": return "gregtech:salt";
            // Both families copy MT.Plastic's targets. Hard Plastic steals
            // Polycarbonate's identical heat/CH2 density, not its identity.
            case "anyplastic":
            case "anyhardplastic": return "gregtech:plastic";
            // MT.java:403 versus :1294. The latter is Ca3(PO4)2,
            // not the host's elemental phosphorus, despite its GT6 name.
            case "phosphor": return "gregtech:phosphorus";
            case "phosphorus":
            case "phosphorous": return "gregtech:tricalcium_phosphate";
            case "bluephosphorus": return "gt6addition:blue_phosphorus";
            case "redphosphorus": return "gt6addition:red_phosphorus";
            case "whitephosphorus": return "gt6addition:white_phosphorus";
            // ANY.java:108 adds MELTING independently of MT.Phosphorus.
            case "anyphosphorus": return "gt6addition:any_phosphorus";
            case "anymagiciron": return "gt6addition:any_magic_iron";
            case "anywoodorplastic": return "gt6addition:any_wood_or_plastic";
            case "anygarnet": return "gt6addition:any_garnet";
            case "anyjasper": return "gt6addition:any_jasper";
            case "anytigereye": return "gt6addition:any_tiger_eye";
            case "anyaventurine": return "gt6addition:any_aventurine";
            case "anyamber": return "gt6addition:any_amber";
            case "anythaumiccrystal": return "gt6addition:any_thaumic_crystal";
            case "hexorium": return "gt6addition:any_hexorium"; // Actual ANY.Hexorium saved name.
            case "wax":
            case "anywax": return "gt6addition:wax";
            case "sand":
            case "anysand": return "gt6addition:sand";
            case "anystone": return "gregtech:stone";
            case "anycalcite": return "gregtech:calcite";
            // ANY.Quartz's actual saved name is Quartz, not AnyQuartz.
            // MilkyQuartz steals the same SiO2 stats and heat, but this
            // family explicitly copies SiO2's processing targets.
            case "quartz": return "gregtech:silicon_dioxide";
            case "anyblaze": return "gt6addition:any_blaze";
            case "prismarine": return "gt6addition:prismarine";
            case "prismarinedark": return "gt6addition:prismarine_dark";
            case "anyprismarine": return "gt6addition:any_prismarine";
            case "wheat":
            case "flour": return "gregtech:wheat"; // MT.Wheat's explicit ore alias.
            case "barley": return "gt6addition:barley";
            case "rye": return "gt6addition:rye";
            case "rice": return "gt6addition:rice";
            case "oat":
            case "oats": return "gt6addition:oat";
            case "abyssaloat":
            case "abyssaloats": return "gt6addition:abyssal_oat";
            case "corn": return "gt6addition:corn";
            case "potato": return "gt6addition:potato";
            case "anygrains":
            case "anyflour":
            case "anyflourorgrains": return "gt6addition:any_grains";
            case "fishcooked": return "gt6addition:fish_cooked";
            case "fishraw": return "gt6addition:fish_raw";
            case "fishrotten": return "gt6addition:fish_rotten";
            case "fishoil": return "gregtech:fish_oil";
            case "tofu": return "gt6addition:tofu";
            case "duraniumelemental": return "gregtech:duranium";
            case "tritaniumelemental": return "gregtech:tritanium";
            case "duranium": return "gt6addition:duranium_alloy";
            case "tritanium": return "gt6addition:tritanium_alloy";
            default:
                return normalized.equals(originalName) ? gt6Name : normalized;
        }
    }

    /** Literal MT.java alloy aliases, not similarity-based material matching. */
    static String canonicalAlloyName(String normalizedName) {
        if ("carborundum".equals(normalizedName)) return "siliconcarbide";
        if ("cupronickel".equals(normalizedName)) return "constantan";
        if ("aluminumbrass".equals(normalizedName)) return "aluminiumbrass";
        if ("tumbaga".equals(normalizedName)) return "rosegold";
        if ("goldinductive".equals(normalizedName)) return "inductivealloy";
        if ("wolframsteel".equals(normalizedName)) return "tungstensteel";
        if ("wolframcarbide".equals(normalizedName) || "carbide".equals(normalizedName)) return "tungstencarbide";
        if ("hsla".equals(normalizedName)) return "hslasteel";
        if ("hslaspringsteel".equals(normalizedName)) return "springsteel";
        if ("hslatungstenalloy".equals(normalizedName)) return "tungstenalloy";
        if ("galvanizedsteel".equals(normalizedName)) return "steelgalvanized";
        if ("teslatinealloy".equals(normalizedName)) return "nikolinealloy";
        if ("darksteel".equals(normalizedName)) return "obsidiansteel";
        if ("phasediron".equals(normalizedName)) return "pulsatingiron";
        if ("phasedgold".equals(normalizedName) || "vibrant".equals(normalizedName)) return "vibrantalloy";
        if ("crudesteel".equals(normalizedName)) return "claycompound";
        if ("glowstonerefined".equals(normalizedName)) return "refinedglowstone";
        if ("obsidianrefined".equals(normalizedName)) return "refinedobsidian";
        if ("elementium".equals(normalizedName)) return "elvenelementium";
        if ("iritanium".equals(normalizedName)) return "titaniumiridium";
        if ("wrougtiron".equals(normalizedName)) return "wroughtiron";
        if ("naquadahalloy".equals(normalizedName)) return "trinaquadalloy";
        return normalizedName;
    }

    /** Verified element spelling differences also used by GT6ElementPhaseData. */
    static String canonicalElementName(String normalizedName) {
        if (normalizedName == null) return "";
        switch (normalizedName) {
            case "aluminum": return "aluminium"; // MT.java aluminium() explicit alias.
            case "sulphur": return "sulfur"; // MT.java:404 explicit alias.
            case "phosphor": return "phosphorus";
            case "lanthanium": return "lanthanum";
            case "caesium": return "cesium";
            case "osmiumelemental": return "osmium";
            default: return normalizedName;
        }
    }

    /** Statistics lookup for a native numeric identity, not a host Material
     * name or registration request. Preserve the original names for recycling
     * and disambiguate only the three verified host/native collisions.
     */
    static String mechanicsName(int id) {
        switch (id) {
            case 8208: return "tricalciumphosphate"; // MT.Phosphorus, not elemental P.
            case 8751: return "duraniumalloy"; // MT.DuraniumAlloy, not Dn.
            case 8752: return "tritaniumalloy"; // MT.TritaniumAlloy, not Tn.
            default:
                String materialName = name(id);
                return materialName == null ? GT6AntimatterIdentityData.name(id) : materialName;
        }
    }

    /** Native source spelling used by GT6 recycling NBT before host-name mapping. */
    static String recyclingName(int id) {
        String mtName = name(id);
        return mtName == null ? GT6AntimatterIdentityData.name(id) : mtName;
    }

    static String name(int id) {
        switch (id) {
            case 1: return "Photon"; // MT.java:531
            case 2: return "Neutrino"; // MT.java:532
            case 3: return "Neutron"; // MT.java:533
            case 4: return "Proton"; // MT.java:534
            case 5: return "Electron"; // MT.java:535
            case 10: return "Hydrogen"; // MT.java:380
            case 11: return "Deuterium"; // MT.java:381
            case 12: return "Tritium"; // MT.java:382
            case 20: return "Helium"; // MT.java:383
            case 21: return "Helium3"; // MT.java:384
            case 30: return "Lithium"; // MT.java:385
            case 31: return "Lithium6"; // MT.java:386
            case 40: return "Beryllium"; // MT.java:387
            case 41: return "Beryllium7"; // MT.java:388
            case 42: return "Beryllium8"; // MT.java:389
            case 50: return "Boron"; // MT.java:390
            case 51: return "Boron11"; // MT.java:391
            case 60: return "Carbon"; // MT.java:392
            case 61: return "Carbon13"; // MT.java:393
            case 62: return "Carbon14"; // MT.java:394
            case 70: return "Nitrogen"; // MT.java:395
            case 80: return "Oxygen"; // MT.java:396
            case 90: return "Fluorine"; // MT.java:397
            case 100: return "Neon"; // MT.java:398
            case 110: return "Sodium"; // MT.java:399
            case 120: return "Magnesium"; // MT.java:400
            case 130: return "Aluminium"; // MT.java:401
            case 140: return "Silicon"; // MT.java:402
            case 150: return "Phosphor"; // MT.java:403
            case 160: return "Sulfur"; // MT.java:404
            case 170: return "Chlorine"; // MT.java:405
            case 180: return "Argon"; // MT.java:406
            case 190: return "Potassium"; // MT.java:407
            case 200: return "Calcium"; // MT.java:408
            case 210: return "Scandium"; // MT.java:409
            case 220: return "Titanium"; // MT.java:410
            case 230: return "Vanadium"; // MT.java:411
            case 240: return "Chromium"; // MT.java:412
            case 250: return "Manganese"; // MT.java:413
            case 260: return "Iron"; // MT.java:414
            case 270: return "Cobalt"; // MT.java:415
            case 278: return "Cobalt60"; // MT.java:416
            case 280: return "Nickel"; // MT.java:417
            case 290: return "Copper"; // MT.java:418
            case 300: return "Zinc"; // MT.java:419
            case 310: return "Gallium"; // MT.java:420
            case 320: return "Germanium"; // MT.java:421
            case 330: return "Arsenic"; // MT.java:422
            case 340: return "Selenium"; // MT.java:423
            case 350: return "Bromine"; // MT.java:424
            case 360: return "Krypton"; // MT.java:425
            case 370: return "Rubidium"; // MT.java:426
            case 380: return "Strontium"; // MT.java:427
            case 390: return "Yttrium"; // MT.java:428
            case 400: return "Zirconium"; // MT.java:429
            case 410: return "Niobium"; // MT.java:430
            case 420: return "Molybdenum"; // MT.java:431
            case 430: return "Technetium"; // MT.java:432
            case 440: return "Ruthenium"; // MT.java:433
            case 450: return "Rhodium"; // MT.java:434
            case 460: return "Palladium"; // MT.java:435
            case 470: return "Silver"; // MT.java:436
            case 480: return "Cadmium"; // MT.java:437
            case 490: return "Indium"; // MT.java:438
            case 500: return "Tin"; // MT.java:439
            case 510: return "Antimony"; // MT.java:440
            case 520: return "Tellurium"; // MT.java:441
            case 530: return "Iodine"; // MT.java:442
            case 540: return "Xenon"; // MT.java:443
            case 550: return "Caesium"; // MT.java:444
            case 560: return "Barium"; // MT.java:445
            case 570: return "Lanthanium"; // MT.java:446
            case 580: return "Cerium"; // MT.java:447
            case 590: return "Praseodymium"; // MT.java:448
            case 600: return "Neodymium"; // MT.java:449
            case 610: return "Promethium"; // MT.java:450
            case 620: return "Samarium"; // MT.java:451
            case 630: return "Europium"; // MT.java:452
            case 640: return "Gadolinium"; // MT.java:453
            case 650: return "Terbium"; // MT.java:454
            case 660: return "Dysprosium"; // MT.java:455
            case 670: return "Holmium"; // MT.java:456
            case 680: return "Erbium"; // MT.java:457
            case 690: return "Thulium"; // MT.java:458
            case 700: return "Ytterbium"; // MT.java:459
            case 710: return "Lutetium"; // MT.java:460
            case 720: return "Hafnium"; // MT.java:461
            case 730: return "Tantalum"; // MT.java:462
            case 740: return "Tungsten"; // MT.java:463
            case 750: return "Rhenium"; // MT.java:464
            case 760: return "OsmiumElemental"; // MT.java:465
            case 770: return "Iridium"; // MT.java:466
            case 780: return "Platinum"; // MT.java:467
            case 790: return "Gold"; // MT.java:468
            case 791: return "Gold198"; // MT.java:469
            case 800: return "Mercury"; // MT.java:470
            case 810: return "Thallium"; // MT.java:471
            case 820: return "Lead"; // MT.java:472
            case 830: return "Bismuth"; // MT.java:473
            case 840: return "Polonium"; // MT.java:474
            case 850: return "Astatine"; // MT.java:475
            case 860: return "Radon"; // MT.java:476
            case 870: return "Francium"; // MT.java:477
            case 880: return "Radium"; // MT.java:478
            case 890: return "Actinium"; // MT.java:479
            case 900: return "Thorium"; // MT.java:480
            case 910: return "Protactinium"; // MT.java:481
            case 920: return "Uranium"; // MT.java:482
            case 921: return "Uranium235"; // MT.java:483
            case 922: return "Uranium233"; // MT.java:484
            case 930: return "Neptunium"; // MT.java:485
            case 940: return "Plutonium"; // MT.java:486
            case 942: return "Plutonium240"; // MT.java:487
            case 943: return "Plutonium241"; // MT.java:488
            case 945: return "Plutonium243"; // MT.java:489
            case 946: return "Plutonium238"; // MT.java:490
            case 947: return "Plutonium239"; // MT.java:491
            case 950: return "Americium"; // MT.java:492
            case 951: return "Americium241"; // MT.java:493
            case 952: return "Americium242"; // MT.java:494
            case 960: return "Curium"; // MT.java:495
            case 970: return "Berkelium"; // MT.java:496
            case 980: return "Californium"; // MT.java:497
            case 990: return "Einsteinium"; // MT.java:498
            case 1000: return "Fermium"; // MT.java:499
            case 1010: return "Mendelevium"; // MT.java:500
            case 1020: return "Nobelium"; // MT.java:501
            case 1030: return "Lawrencium"; // MT.java:502
            case 1040: return "Rutherfordium"; // MT.java:503
            case 1050: return "Dubnium"; // MT.java:504
            case 1060: return "Seaborgium"; // MT.java:505
            case 1070: return "Bohrium"; // MT.java:506
            case 1080: return "Hassium"; // MT.java:507
            case 1090: return "Meitnerium"; // MT.java:508
            case 1100: return "Darmstadtium"; // MT.java:509
            case 1110: return "Roentgenium"; // MT.java:510
            case 1120: return "Copernicium"; // MT.java:511
            case 1130: return "Nihonium"; // MT.java:512
            case 1140: return "Flerovium"; // MT.java:513
            case 1148: return "Flerovium298"; // MT.java:514
            case 1150: return "Moscovium"; // MT.java:515
            case 1160: return "Livermorium"; // MT.java:516
            case 1170: return "Farnsium"; // MT.java:517
            case 1180: return "Oganesson"; // MT.java:518
            case 1190: return "Ununennium"; // MT.java:519
            case 1200: return "Unbinilium"; // MT.java:520
            case 1250: return "TritaniumElemental"; // MT.java:695
            case 1260: return "Trinium"; // MT.java:696
            case 1450: return "DuraniumElemental"; // MT.java:715
            case 1520: return "Vibranium"; // MT.java:722
            case 1740: return "Naquadah"; // MT.java:744
            case 1741: return "NaquadahEnriched"; // MT.java:745
            case 1742: return "Naquadria"; // MT.java:746
            case 1850: return "Abyssalnite"; // MT.java:757
            case 1860: return "Coralium"; // MT.java:758
            case 1870: return "Dreadium"; // MT.java:759
            case 1880: return "Ethaxium"; // MT.java:760
            case 2210: return "Atlarus"; // MT.java:793
            case 2220: return "Adamantium"; // MT.java:794
            case 2390: return "MacGuffium"; // MT.java:811
            case 3720: return "Gravitonium"; // MT.java:944
            case 4000: return "Magic"; // MT.java:539
            case 8000: return "SiliconDioxide"; // MT.java:1056
            case 8001: return "Glass"; // MT.java:1057
            case 8002: return "Flint"; // MT.java:1058
            case 8003: return "Carborundum"; // MT.java:1055
            case 8004: return "LithiumOxide"; // MT.java:1124
            case 8005: return "Ferrite"; // MT.java:1125
            case 8006: return "Datolite"; // MT.java:1062
            case 8007: return "HydrogenBorate"; // MT.java:1061
            case 8008: return "Alumina"; // MT.java:1081
            case 8009: return "Cryolite"; // MT.java:1142
            case 8010: return "AluminiumFluoride"; // MT.java:1082
            case 8011: return "HexafluorosilicicAcid"; // MT.java:1054
            case 8012: return "SodiumAluminate"; // MT.java:1140
            case 8013: return "SodiumCarbonate"; // MT.java:1139
            case 8014: return "AluminiumHydroxide"; // MT.java:1083
            case 8015: return "PotassiumHydroxide"; // MT.java:1149
            case 8016: return "MagnesiumCarbonate"; // MT.java:1101
            case 8017: return "FerricChloride"; // MT.java:1096
            case 8018: return "MagnesiumChloride"; // MT.java:1100
            case 8019: return "SodiumNitrate"; // MT.java:1130
            case 8020: return "PotassiumCarbonate"; // MT.java:1156
            case 8021: return "PotassiumSulfite"; // MT.java:1153
            case 8022: return "PotassiumPersulfate"; // MT.java:1151
            case 8023: return "PotassiumAluminate"; // MT.java:1157
            case 8024: return "HydrosulfuricAcid"; // MT.java:1046
            case 8025: return "Ammonia"; // MT.java:1030
            case 8026: return "TungstenTrioxide"; // MT.java:1077
            case 8027: return "TungsticAcid"; // MT.java:1078
            case 8028: return "CalciumChloride"; // MT.java:1104
            case 8029: return "LithiumChloride"; // MT.java:1121
            case 8030: return "FerrousChloride"; // MT.java:1095
            case 8031: return "ManganeseChloride"; // MT.java:1091
            case 8032: return "LithiumHydroxide"; // MT.java:1126
            case 8033: return "LithiumChlorate"; // MT.java:1122
            case 8034: return "LithiumPerchlorate"; // MT.java:1123
            case 8035: return "FerricOxyhydroxide"; // MT.java:1097
            case 8036: return "PotassiumFluoride"; // MT.java:1158
            case 8037: return "SodiumFluoride"; // MT.java:1141
            case 8038: return "PotassiumHeptafluorotantalate"; // MT.java:1159
            case 8039: return "SodiumHydrogencarbonate"; // MT.java:1132
            case 8100: return "Sand"; // MT.java:1618
            case 8101: return "Soulsand"; // MT.java:1622
            case 8102: return "SluiceSand"; // MT.java:1623
            case 8103: return "PlatinumGroupSludge"; // MT.java:1624
            case 8104: return "RedSand"; // MT.java:1619
            case 8105: return "WhiteEndSand"; // MT.java:1620
            case 8106: return "BlackEndSand"; // MT.java:1621
            case 8196: return "Teflon"; // MT.java:1303
            case 8197: return "PVC"; // MT.java:1304
            case 8198: return "Bakelite"; // MT.java:1305
            case 8199: return "HardPlastic"; // MT.java:1306
            case 8200: return "Ashes"; // MT.java:1228
            case 8201: return "DarkAshes"; // MT.java:1229
            case 8202: return "VolcanicAshes"; // MT.java:1230
            case 8203: return "Sylvite"; // MT.java:1147
            case 8204: return "Salt"; // MT.java:1129
            case 8205: return "PotassiumNitrate"; // MT.java:1148
            case 8206: return "Niter"; // MT.java:1293
            case 8207: return "Phosphate"; // MT.java:1074
            case 8208: return "Phosphorus"; // MT.java:1294
            case 8209: return "Apatite"; // MT.java:1298
            case 8210: return "Blizz"; // MT.java:1274
            case 8211: return "Blaze"; // MT.java:1275
            case 8212: return "Pyrotheum"; // MT.java:1651
            case 8213: return "Cryotheum"; // MT.java:1652
            case 8214: return "Obsidian"; // MT.java:1636
            case 8215: return "Clay"; // MT.java:1281
            case 8216: return "Paper"; // MT.java:1300
            case 8217: return "Rubber"; // MT.java:1301
            case 8218: return "Plastic"; // MT.java:1302
            case 8219: return "Bone"; // MT.java:1307
            case 8220: return "Gunpowder"; // MT.java:1309
            case 8221: return "Wood"; // MT.java:1244
            case 8222: return "WoodTreated"; // MT.java:1245
            case 8223: return "LiveRoot"; // MT.java:1258
            case 8224: return "WoodRubber"; // MT.java:1247
            case 8225: return "Ceramic"; // MT.java:1279
            case 8226: return "Phosphorite"; // MT.java:1299
            case 8227: return "PeanutWood"; // MT.java:1256
            case 8228: return "Indigo"; // MT.java:1314
            case 8229: return "CobaltHexahydrate"; // MT.java:1189
            case 8230: return "SodiumBisulfate"; // MT.java:1133
            case 8231: return "SodiumPyrosulfate"; // MT.java:1138
            case 8232: return "PotassiumBisulfate"; // MT.java:1150
            case 8233: return "PotassiumPyrosulfate"; // MT.java:1155
            case 8234: return "VanadiumPentoxide"; // MT.java:1065
            case 8235: return "Wax"; // MT.java:1262
            case 8236: return "WaxBee"; // MT.java:1263
            case 8237: return "WaxRefractory"; // MT.java:1264
            case 8238: return "WaxParaffin"; // MT.java:1265
            case 8239: return "WaxPlant"; // MT.java:1266
            case 8240: return "WaxMagic"; // MT.java:1267
            case 8241: return "Leather"; // MT.java:1313
            case 8242: return "IodineSalt"; // MT.java:1146
            case 8243: return "SilverIodide"; // MT.java:1051
            case 8244: return "Tallow"; // MT.java:1312
            case 8245: return "Petrotheum"; // MT.java:1649
            case 8246: return "Aerotheum"; // MT.java:1650
            case 8247: return "Basalz"; // MT.java:1272
            case 8248: return "Blitz"; // MT.java:1273
            case 8249: return "Dynamite"; // MT.java:1310
            case 8250: return "Black"; // MT.java:989
            case 8251: return "Red"; // MT.java:990
            case 8252: return "Green"; // MT.java:991
            case 8253: return "Brown"; // MT.java:992
            case 8254: return "Blue"; // MT.java:993
            case 8255: return "Purple"; // MT.java:994
            case 8256: return "Cyan"; // MT.java:995
            case 8257: return "LightGray"; // MT.java:996
            case 8258: return "Gray"; // MT.java:997
            case 8259: return "Pink"; // MT.java:998
            case 8260: return "Lime"; // MT.java:999
            case 8261: return "Yellow"; // MT.java:1000
            case 8262: return "LightBlue"; // MT.java:1001
            case 8263: return "Magenta"; // MT.java:1002
            case 8264: return "Orange"; // MT.java:1003
            case 8265: return "White"; // MT.java:1004
            case 8266: return "Asphalt"; // MT.java:1311
            case 8267: return "WoodPolished"; // MT.java:1246
            case 8268: return "SodiumHydroxide"; // MT.java:1131
            case 8269: return "SodiumSulfite"; // MT.java:1136
            case 8270: return "SodiumSulfate"; // MT.java:1137
            case 8271: return "PotassiumSulfate"; // MT.java:1154
            case 8272: return "PotassiumSulfide"; // MT.java:1152
            case 8273: return "Porcelain"; // MT.java:1289
            case 8274: return "CalciumSulfate"; // MT.java:1105
            case 8275: return "Bark"; // MT.java:1243
            case 8276: return "ClayBrown"; // MT.java:1282
            case 8277: return "PetrifiedWood"; // MT.java:1259
            case 8278: return "ManaDiamond"; // MT.java:1366
            case 8279: return "ElvenDragonstone"; // MT.java:1367
            case 8280: return "WaxAmnesic"; // MT.java:1268
            case 8281: return "WaxSoulful"; // MT.java:1269
            case 8282: return "CrimsonMiddle"; // MT.java:1487
            case 8283: return "GreenMiddle"; // MT.java:1488
            case 8284: return "AquaMiddle"; // MT.java:1489
            case 8285: return "Valonite"; // MT.java:1490
            case 8286: return "Weedwood"; // MT.java:1250
            case 8287: return "SlimyBone"; // MT.java:1308
            case 8288: return "Scabyst"; // MT.java:1491
            case 8289: return "Livingwood"; // MT.java:1251
            case 8290: return "Dreamwood"; // MT.java:1252
            case 8291: return "Skyroot"; // MT.java:1249
            case 8292: return "Zanite"; // MT.java:1428
            case 8293: return "Ambrosium"; // MT.java:1494
            case 8294: return "Gravitite"; // MT.java:1368
            case 8295: return "Continuum"; // MT.java:1495
            case 8296: return "Greatwood"; // MT.java:1254
            case 8297: return "Silverwood"; // MT.java:1255
            case 8298: return "EnergiumRed"; // MT.java:1581
            case 8299: return "EnergiumCyan"; // MT.java:1582
            case 8300: return "Diamond"; // MT.java:1358
            case 8301: return "Emerald"; // MT.java:1371
            case 8302: return "Ruby"; // MT.java:1381
            case 8303: return "BalasRuby"; // MT.java:1390
            case 8304: return "Sapphire"; // MT.java:1380
            case 8305: return "GreenSapphire"; // MT.java:1383
            case 8306: return "Topaz"; // MT.java:1425
            case 8307: return "BlueTopaz"; // MT.java:1426
            case 8308: return "Tanzanite"; // MT.java:1427
            case 8309: return "RedJasper"; // MT.java:1401
            case 8310: return "Amber"; // MT.java:1439
            case 8311: return "Peridot"; // MT.java:1435
            case 8312: return "Opal"; // MT.java:1431
            case 8313: return "Amethyst"; // MT.java:1436
            case 8314: return "OrangeSapphire"; // MT.java:1386
            case 8315: return "YellowSapphire"; // MT.java:1385
            case 8316: return "Vinteum"; // MT.java:1478
            case 8317: return "Dilithium"; // MT.java:1508
            case 8318: return "EnderPearl"; // MT.java:1499
            case 8319: return "EnderEye"; // MT.java:1500
            case 8320: return "NetherStar"; // MT.java:1501
            case 8321: return "Jade"; // MT.java:1443
            case 8322: return "Craponite"; // MT.java:1442
            case 8323: return "Aquamarine"; // MT.java:1372
            case 8324: return "Morganite"; // MT.java:1373
            case 8325: return "Dioptase"; // MT.java:1437
            case 8326: return "Spinel"; // MT.java:1389
            case 8327: return "VinteumPurified"; // MT.java:1479
            case 8328: return "BlueSapphire"; // MT.java:1382
            case 8329: return "AmethystEnder"; // MT.java:1498
            case 8330: return "Lazurite"; // MT.java:1516
            case 8331: return "Sodalite"; // MT.java:1517
            case 8332: return "Lapis"; // MT.java:1518
            case 8333: return "Redstone"; // MT.java:1540
            case 8334: return "Coal"; // MT.java:1522
            case 8335: return "HydratedCoal"; // MT.java:1534
            case 8336: return "Charcoal"; // MT.java:1521
            case 8337: return "Lignite"; // MT.java:1527
            case 8338: return "Monazite"; // MT.java:1626
            case 8340: return "Nikolite"; // MT.java:1541
            case 8341: return "Glowstone"; // MT.java:1544
            case 8342: return "Firestone"; // MT.java:1537
            case 8343: return "Force"; // MT.java:1627
            case 8344: return "Forcicium"; // MT.java:1628
            case 8345: return "Forcillium"; // MT.java:1629
            case 8346: return "NetherQuartz"; // MT.java:1554
            case 8347: return "CertusQuartz"; // MT.java:1564
            case 8348: return "ChargedCertusQuartz"; // MT.java:1565
            case 8349: return "CoalCoke"; // MT.java:1523
            case 8350: return "InfusedDull"; // MT.java:1585
            case 8351: return "InfusedVis"; // MT.java:1586
            case 8352: return "InfusedAir"; // MT.java:1587
            case 8353: return "InfusedFire"; // MT.java:1588
            case 8354: return "InfusedEarth"; // MT.java:1589
            case 8355: return "InfusedWater"; // MT.java:1590
            case 8356: return "InfusedEntropy"; // MT.java:1591
            case 8357: return "InfusedOrder"; // MT.java:1592
            case 8358: return "InfusedBalance"; // MT.java:1593
            case 8360: return "Peat"; // MT.java:1530
            case 8361: return "PeatBituminous"; // MT.java:1531
            case 8362: return "Anthracite"; // MT.java:1524
            case 8363: return "Prismane"; // MT.java:1525
            case 8364: return "Lonsdaleite"; // MT.java:1526
            case 8365: return "LigniteCoke"; // MT.java:1528
            case 8366: return "ArcaneCompound"; // MT.java:1481
            case 8367: return "ArcaneAshes"; // MT.java:1480
            case 8368: return "GlowstoneCeres"; // MT.java:1545
            case 8369: return "GlowstoneIo"; // MT.java:1546
            case 8370: return "GlowstoneEnceladus"; // MT.java:1547
            case 8371: return "GlowstoneProteus"; // MT.java:1548
            case 8372: return "GlowstonePluto"; // MT.java:1549
            case 8373: return "Ectoplasm"; // MT.java:1536
            case 8374: return "QuartzBlack"; // MT.java:1563
            case 8375: return "Redstonia"; // MT.java:1569
            case 8376: return "Palis"; // MT.java:1570
            case 8377: return "Diamantine"; // MT.java:1571
            case 8378: return "VoidCrystal"; // MT.java:1572
            case 8379: return "Emeradic"; // MT.java:1573
            case 8380: return "Enori"; // MT.java:1574
            case 8381: return "DarkMatter"; // MT.java:1577
            case 8382: return "RedMatter"; // MT.java:1578
            case 8383: return "PurpleSapphire"; // MT.java:1384
            case 8384: return "Heliodor"; // MT.java:1374
            case 8385: return "Goshenite"; // MT.java:1375
            case 8386: return "Bixbite"; // MT.java:1376
            case 8387: return "Maxixe"; // MT.java:1377
            case 8388: return "Alexandrite"; // MT.java:1430
            case 8389: return "Fluix"; // MT.java:1566
            case 8390: return "PetroleumCoke"; // MT.java:1529
            case 8391: return "Frezarite"; // MT.java:1504
            case 8392: return "RedMeteor"; // MT.java:1505
            case 8393: return "SunnyQuartz"; // MT.java:1556
            case 8394: return "LavenderQuartz"; // MT.java:1557
            case 8395: return "RedQuartz"; // MT.java:1558
            case 8396: return "BlazeQuartz"; // MT.java:1559
            case 8397: return "SmokeyQuartz"; // MT.java:1560
            case 8398: return "ManaQuartz"; // MT.java:1561
            case 8399: return "ElvenQuartz"; // MT.java:1562
            case 8400: return "ChloroauricAcid"; // MT.java:1163
            case 8401: return "ChloroplatinicAcid"; // MT.java:1164
            case 8402: return "StannicChloride"; // MT.java:1165
            case 8403: return "BlackVitriol"; // MT.java:1168
            case 8404: return "BlueVitriol"; // MT.java:1169
            case 8405: return "GreenVitriol"; // MT.java:1170
            case 8406: return "RedVitriol"; // MT.java:1171
            case 8407: return "PinkVitriol"; // MT.java:1172
            case 8408: return "CyanVitriol"; // MT.java:1173
            case 8409: return "WhiteVitriol"; // MT.java:1174
            case 8410: return "GrayVitriol"; // MT.java:1175
            case 8411: return "MartianVitriol"; // MT.java:1176
            case 8412: return "VitriolOfClay"; // MT.java:1177
            case 8413: return "TitaniumTetrachloride"; // MT.java:1087
            case 8414: return "Shimmerwood"; // MT.java:1253
            case 8415: return "OnyxRed"; // MT.java:1432
            case 8416: return "OnyxBlack"; // MT.java:1433
            case 8417: return "Amazonite"; // MT.java:1429
            case 8418: return "Bamboo"; // MT.java:1248
            case 8419: return "Zircon"; // MT.java:1511
            case 8420: return "Azurite"; // MT.java:1512
            case 8421: return "Eudialyte"; // MT.java:1513
            case 8422: return "DominicanAmber"; // MT.java:1441
            case 8423: return "DiamondIndustrial"; // MT.java:1365
            case 8424: return "PinkDiamond"; // MT.java:1364
            case 8425: return "OceanJasper"; // MT.java:1402
            case 8426: return "RainforestJasper"; // MT.java:1403
            case 8427: return "BlueJasper"; // MT.java:1404
            case 8428: return "GreenJasper"; // MT.java:1405
            case 8429: return "YellowJasper"; // MT.java:1406
            case 8430: return "TigerEye"; // MT.java:1409
            case 8431: return "CatsEye"; // MT.java:1410
            case 8432: return "DragonEye"; // MT.java:1411
            case 8433: return "HawksEye"; // MT.java:1412
            case 8434: return "BlackEye"; // MT.java:1413
            case 8435: return "TigerIron"; // MT.java:1414
            case 8436: return "RedFluorite"; // MT.java:1110
            case 8437: return "PinkFluorite"; // MT.java:1111
            case 8438: return "BlueFluorite"; // MT.java:1112
            case 8439: return "GreenFluorite"; // MT.java:1113
            case 8440: return "BlackFluorite"; // MT.java:1114
            case 8441: return "WhiteFluorite"; // MT.java:1115
            case 8442: return "YellowFluorite"; // MT.java:1116
            case 8443: return "OrangeFluorite"; // MT.java:1117
            case 8444: return "MagentaFluorite"; // MT.java:1118
            case 8445: return "MilkyQuartz"; // MT.java:1553
            case 8446: return "BrownAventurine"; // MT.java:1418
            case 8447: return "YellowAventurine"; // MT.java:1419
            case 8448: return "GreenAventurine"; // MT.java:1417
            case 8449: return "BlackAventurine"; // MT.java:1420
            case 8450: return "BlueAventurine"; // MT.java:1421
            case 8451: return "RedAventurine"; // MT.java:1422
            case 8452: return "Moonstone"; // MT.java:1482
            case 8453: return "Sunstone"; // MT.java:1483
            case 8454: return "Chimerite"; // MT.java:1484
            case 8455: return "ClayRed"; // MT.java:1283
            case 8456: return "Gloomstone"; // MT.java:1550
            case 8457: return "VoidQuartz"; // MT.java:1555
            case 8458: return "BluePhosphorus"; // MT.java:1295
            case 8459: return "RedPhosphorus"; // MT.java:1296
            case 8460: return "WhitePhosphorus"; // MT.java:1297
            case 8461: return "NiobiumPentoxide"; // MT.java:1068
            case 8462: return "TantalumPentoxide"; // MT.java:1071
            case 8463: return "Sugilite"; // MT.java:1434
            case 8464: return "BlueDiamond"; // MT.java:1359
            case 8465: return "GreenDiamond"; // MT.java:1360
            case 8466: return "PurpleDiamond"; // MT.java:1361
            case 8467: return "RedDiamond"; // MT.java:1362
            case 8468: return "YellowDiamond"; // MT.java:1363
            case 8469: return "GoldenAmber"; // MT.java:1440
            case 8470: return "Carminite"; // MT.java:1438
            case 8471: return "Breeze"; // MT.java:1276
            case 8500: return "Stone"; // MT.java:1631
            case 8501: return "Concrete"; // MT.java:1632
            case 8502: return "Netherrack"; // MT.java:1633
            case 8503: return "NetherBrick"; // MT.java:1634
            case 8504: return "Endstone"; // MT.java:1635
            case 8505: return "Basalt"; // MT.java:3818
            case 8506: return "Marble"; // MT.java:3819
            case 8507: return "GraniteRed"; // MT.java:3828
            case 8508: return "GraniteBlack"; // MT.java:3829
            case 8509: return "Redrock"; // MT.java:3814
            case 8511: return "Diorite"; // MT.java:3832
            case 8512: return "SpaceStone"; // MT.java:3797
            case 8513: return "MoonStone"; // MT.java:3798
            case 8514: return "MoonTurf"; // MT.java:3799
            case 8515: return "MarsStone"; // MT.java:3800
            case 8516: return "MarsSand"; // MT.java:3801
            case 8517: return "Umber"; // MT.java:3812
            case 8518: return "Granite"; // MT.java:3830
            case 8519: return "Betweenstone"; // MT.java:3806
            case 8520: return "Pitstone"; // MT.java:3807
            case 8521: return "Livingrock"; // MT.java:3804
            case 8522: return "Holystone"; // MT.java:3803
            case 8523: return "Deadrock"; // MT.java:3805
            case 8524: return "Cragrock"; // MT.java:3808
            case 8525: return "Templerock"; // MT.java:3809
            case 8526: return "Mazestone"; // MT.java:3810
            case 8527: return "Castlerock"; // MT.java:3811
            case 8528: return "SkyStone"; // MT.java:3802
            case 8599: return "Bedrock"; // MT.java:1637
            case 8600: return "Electrum"; // MT.java:1691
            case 8601: return "SterlingSilver"; // MT.java:1692
            case 8602: return "RoseGold"; // MT.java:1693
            case 8603: return "Angmallen"; // MT.java:1694
            case 8604: return "GoldInductive"; // MT.java:1695
            case 8605: return "CdInAgAlloy"; // MT.java:1696
            case 8606: return "GildedIron"; // MT.java:1697
            case 8610: return "Bronze"; // MT.java:1705
            case 8611: return "BlackBronze"; // MT.java:1706
            case 8612: return "BismuthBronze"; // MT.java:1707
            case 8613: return "Hepatizon"; // MT.java:1708
            case 8614: return "ArsenicCopper"; // MT.java:1709
            case 8615: return "ArsenicBronze"; // MT.java:1710
            case 8620: return "Brass"; // MT.java:1700
            case 8621: return "CobaltBrass"; // MT.java:1701
            case 8622: return "AluminiumAlloy"; // MT.java:1702
            case 8630: return "Steel"; // MT.java:1713
            case 8631: return "BlackSteel"; // MT.java:1714
            case 8632: return "BlueSteel"; // MT.java:1715
            case 8633: return "RedSteel"; // MT.java:1716
            case 8634: return "DamascusSteel"; // MT.java:1717
            case 8635: return "Tungstensteel"; // MT.java:1719
            case 8636: return "StainlessSteel"; // MT.java:1759
            case 8637: return "HSLASteel"; // MT.java:1721
            case 8638: return "TungstenCarbide"; // MT.java:1720
            case 8639: return "HSLASpringSteel"; // MT.java:1722
            case 8640: return "AnnealedCopper"; // MT.java:1656
            case 8641: return "DeepIron"; // MT.java:1673
            case 8642: return "PigIron"; // MT.java:1724
            case 8643: return "WroughtIron"; // MT.java:1655
            case 8644: return "IronCompressed"; // MT.java:1725
            case 8645: return "IronMagnetic"; // MT.java:1727
            case 8646: return "SteelMagnetic"; // MT.java:1728
            case 8647: return "NeodymiumMagnetic"; // MT.java:1729
            case 8648: return "DarkIron"; // MT.java:1730
            case 8649: return "MeteoricIron"; // MT.java:1737
            case 8650: return "MeteoricSteel"; // MT.java:1738
            case 8651: return "SteelGalvanized"; // MT.java:1731
            case 8652: return "TungstenSintered"; // MT.java:1732
            case 8653: return "VanadiumSteel"; // MT.java:1718
            case 8654: return "TitaniumGold"; // MT.java:1733
            case 8657: return "PurpleAlloy"; // MT.java:1746
            case 8658: return "ElectrotineAlloy"; // MT.java:1750
            case 8659: return "BlueAlloy"; // MT.java:1745
            case 8660: return "RedAlloy"; // MT.java:1744
            case 8661: return "Invar"; // MT.java:1754
            case 8662: return "Constantan"; // MT.java:1755
            case 8663: return "Nichrome"; // MT.java:1756
            case 8664: return "Kanthal"; // MT.java:1757
            case 8665: return "Magnalium"; // MT.java:1758
            case 8666: return "Ultimet"; // MT.java:1760
            case 8667: return "TinAlloy"; // MT.java:1761
            case 8668: return "BatteryAlloy"; // MT.java:1762
            case 8669: return "SolderingAlloy"; // MT.java:1763
            case 8670: return "ShadowIron"; // MT.java:1674
            case 8671: return "ShadowSteel"; // MT.java:1683
            case 8672: return "Ironwood"; // MT.java:1764
            case 8673: return "Steeleaf"; // MT.java:1765
            case 8674: return "Knightmetal"; // MT.java:1766
            case 8675: return "FierySteel"; // MT.java:1767
            case 8676: return "AstralSilver"; // MT.java:1679
            case 8677: return "Midasium"; // MT.java:1680
            case 8678: return "Mithril"; // MT.java:1681
            case 8679: return "Thaumium"; // MT.java:1774
            case 8680: return "DarkThaumium"; // MT.java:1775
            case 8681: return "VoidMetal"; // MT.java:1776
            case 8682: return "Osmiridium"; // MT.java:1777
            case 8683: return "Sunnarium"; // MT.java:1778
            case 8684: return "Trinaquadalloy"; // MT.java:1871
            case 8685: return "ChromiumDioxide"; // MT.java:1779
            case 8686: return "VanadiumGallium"; // MT.java:1780
            case 8687: return "YttriumBariumCuprate"; // MT.java:1781
            case 8688: return "NiobiumNitride"; // MT.java:1782
            case 8689: return "NiobiumTitanium"; // MT.java:1783
            case 8690: return "MeteoricBlackSteel"; // MT.java:1739
            case 8691: return "MeteoricBlueSteel"; // MT.java:1740
            case 8692: return "MeteoricRedSteel"; // MT.java:1741
            case 8693: return "MeteoflameSteel"; // MT.java:1769
            case 8694: return "MeteoflameBlackSteel"; // MT.java:1770
            case 8695: return "MeteoflameBlueSteel"; // MT.java:1771
            case 8696: return "MeteoflameRedSteel"; // MT.java:1772
            case 8697: return "FlamascusSteel"; // MT.java:1773
            case 8698: return "Fireleaf"; // MT.java:1768
            case 8700: return "AluminiumBrass"; // MT.java:1784
            case 8701: return "Aredrite"; // MT.java:1664
            case 8702: return "Alumite"; // MT.java:1786
            case 8703: return "Manyullyn"; // MT.java:1787
            case 8704: return "VibraniumSteel"; // MT.java:1788
            case 8705: return "VibraniumSilver"; // MT.java:1789
            case 8706: return "Vibramantium"; // MT.java:1790
            case 8707: return "Ardite"; // MT.java:1785
            case 8708: return "Signalum"; // MT.java:1791
            case 8709: return "Lumium"; // MT.java:1792
            case 8710: return "Enderium"; // MT.java:1794
            case 8711: return "ElectrumFlux"; // MT.java:1751
            case 8713: return "GlowstoneRefined"; // MT.java:1796
            case 8714: return "ObsidianRefined"; // MT.java:1797
            case 8715: return "Yellorium"; // MT.java:1798
            case 8716: return "Blutonium"; // MT.java:1799
            case 8717: return "Cyanite"; // MT.java:1800
            case 8718: return "BedrockHSLAAlloy"; // MT.java:1803
            case 8720: return "Manasteel"; // MT.java:1820
            case 8721: return "Terrasteel"; // MT.java:1821
            case 8722: return "ElvenElementium"; // MT.java:1822
            case 8723: return "Ludicrite"; // MT.java:1801
            case 8724: return "Yellorite"; // MT.java:1802
            case 8725: return "PulsatingIron"; // MT.java:1806
            case 8726: return "VibrantAlloy"; // MT.java:1808
            case 8727: return "ConductiveIron"; // MT.java:1752
            case 8728: return "EnergeticAlloy"; // MT.java:1807
            case 8729: return "EnderiumBase"; // MT.java:1793
            case 8730: return "ElectricalSteel"; // MT.java:1809
            case 8731: return "ObsidianSteel"; // MT.java:1805
            case 8732: return "Soularium"; // MT.java:1810
            case 8733: return "RedstoneAlloy"; // MT.java:1748
            case 8734: return "SpectreIron"; // MT.java:1819
            case 8735: return "GaiaSpirit"; // MT.java:1823
            case 8736: return "Endium"; // MT.java:1824
            case 8737: return "NikolineAlloy"; // MT.java:1749
            case 8739: return "Mauftrium"; // MT.java:1825
            case 8740: return "Elvorium"; // MT.java:1826
            case 8741: return "NiflheimPower"; // MT.java:1827
            case 8742: return "MuspelheimPower"; // MT.java:1828
            case 8743: return "Iffesal"; // MT.java:1829
            case 8744: return "AncientDebris"; // MT.java:1832
            case 8745: return "Netherite"; // MT.java:1833
            case 8746: return "NetherizedDiamond"; // MT.java:1834
            case 8747: return "Efrine"; // MT.java:1835
            case 8750: return "Desh"; // MT.java:1838
            case 8751: return "Duranium"; // MT.java:1840
            case 8752: return "Tritanium"; // MT.java:1841
            case 8753: return "Dolamide"; // MT.java:1842
            case 8754: return "Oriharukon"; // MT.java:1843
            case 8755: return "Adamantite"; // MT.java:1844
            case 8756: return "Duralumin"; // MT.java:1845
            case 8757: return "Meteorite"; // MT.java:1846
            case 8758: return "FrozenIron"; // MT.java:1847
            case 8759: return "Kreknorite"; // MT.java:1848
            case 8760: return "Alduorite"; // MT.java:1659
            case 8761: return "Infuscolium"; // MT.java:1660
            case 8762: return "Rubracium"; // MT.java:1661
            case 8763: return "Meutoite"; // MT.java:1662
            case 8764: return "Lemurite"; // MT.java:1663
            case 8765: return "Ceruclase"; // MT.java:1665
            case 8766: return "HSLATungstenAlloy"; // MT.java:1723
            case 8767: return "Oureclase"; // MT.java:1666
            case 8768: return "Kalendrite"; // MT.java:1667
            case 8769: return "Orichalcum"; // MT.java:1678
            case 8770: return "Carmot"; // MT.java:1668
            case 8771: return "Sanguinite"; // MT.java:1669
            case 8772: return "Vyroxeres"; // MT.java:1670
            case 8773: return "Eximite"; // MT.java:1671
            case 8774: return "Prometheum"; // MT.java:1676
            case 8775: return "Ignatius"; // MT.java:1672
            case 8776: return "Vulcanite"; // MT.java:1677
            case 8777: return "Inolashite"; // MT.java:1684
            case 8778: return "Haderoth"; // MT.java:1685
            case 8779: return "Celenegil"; // MT.java:1682
            case 8780: return "WorkersAlloy"; // MT.java:1839
            case 8781: return "Desichalkos"; // MT.java:1686
            case 8782: return "Tartarite"; // MT.java:1687
            case 8783: return "Amordrine"; // MT.java:1688
            case 8784: return "Adamantine"; // MT.java:1675
            case 8785: return "Syrmorite"; // MT.java:1849
            case 8786: return "Octine"; // MT.java:1850
            case 8790: return "Trinitanium"; // MT.java:1872
            case 8791: return "Draconium"; // MT.java:1861
            case 8792: return "DraconiumAwakened"; // MT.java:1862
            case 8793: return "TitaniumIridium"; // MT.java:1875
            case 8794: return "TitaniumAluminide"; // MT.java:1876
            case 8795: return "Bedrockium"; // MT.java:1858
            case 8796: return "HSSG"; // MT.java:1853
            case 8797: return "HSSE"; // MT.java:1854
            case 8798: return "HSSS"; // MT.java:1855
            case 8799: return "CrystalMatrix"; // MT.java:1865
            case 8800: return "CosmicNeutronium"; // MT.java:1866
            case 8801: return "Infinity"; // MT.java:1867
            case 8802: return "TantalumHafniumCarbide"; // MT.java:1734
            case 8803: return "CastIron"; // MT.java:1726
            case 8804: return "Mingrade"; // MT.java:1747
            case 8805: return "Unstable"; // MT.java:1868
            case 8806: return "ClayCompound"; // MT.java:1811
            case 8807: return "EndSteel"; // MT.java:1812
            case 8808: return "EnergeticSilver"; // MT.java:1753
            case 8809: return "MelodicAlloy"; // MT.java:1813
            case 8810: return "StellarAlloy"; // MT.java:1814
            case 8811: return "VividAlloy"; // MT.java:1815
            case 8812: return "CrystallineAlloy"; // MT.java:1816
            case 8813: return "CrystallinePinkSlime"; // MT.java:1817
            case 9000: return "Pumice"; // MT.java:3816
            case 9001: return "Diatomite"; // MT.java:3765
            case 9003: return "BasalticMineralSand"; // MT.java:3704
            case 9004: return "GraniticMineralSand"; // MT.java:3705
            case 9005: return "GarnetSand"; // MT.java:3776
            case 9006: return "QuartzSand"; // MT.java:3777
            case 9007: return "UraniumTetrafluoride"; // MT.java:1180
            case 9008: return "UraniumHexafluoride"; // MT.java:1181
            case 9009: return "Uranium238Tetrafluoride"; // MT.java:1182
            case 9010: return "Uranium238Hexafluoride"; // MT.java:1183
            case 9011: return "Uranium235Tetrafluoride"; // MT.java:1184
            case 9012: return "Uranium235Hexafluoride"; // MT.java:1185
            case 9100: return "RareEarth"; // MT.java:1625
            case 9101: return "Almandine"; // MT.java:1393
            case 9102: return "Andradite"; // MT.java:1397
            case 9103: return "Asbestos"; // MT.java:1235
            case 9104: return "Hematite"; // MT.java:1094
            case 9105: return "Bauxite"; // MT.java:3748
            case 9106: return "BrownLimonite"; // MT.java:3739
            case 9107: return "Calcite"; // MT.java:1108
            case 9108: return "Cassiterite"; // MT.java:3700
            case 9109: return "Realgar"; // MT.java:3707
            case 9110: return "Celestine"; // MT.java:3722
            case 9111: return "Chalcopyrite"; // MT.java:3713
            case 9112: return "Chalk"; // MT.java:1233
            case 9113: return "Chromite"; // MT.java:3749
            case 9114: return "Cinnabar"; // MT.java:3708
            case 9115: return "Cobaltite"; // MT.java:3715
            case 9116: return "Cooperite"; // MT.java:3717
            case 9117: return "Galena"; // MT.java:3716
            case 9118: return "Garnierite"; // MT.java:3701
            case 9119: return "Grossular"; // MT.java:1394
            case 9120: return "Ilmenite"; // MT.java:3747
            case 9122: return "Magnetite"; // MT.java:3703
            case 9123: return "Molybdenite"; // MT.java:3709
            case 9124: return "Powellite"; // MT.java:3750
            case 9125: return "Pyrite"; // MT.java:1237
            case 9126: return "Pyrolusite"; // MT.java:1090
            case 9127: return "Pyrope"; // MT.java:1395
            case 9128: return "Scheelite"; // MT.java:3724
            case 9129: return "Spessartine"; // MT.java:1396
            case 9130: return "Sphalerite"; // MT.java:3710
            case 9131: return "Stibnite"; // MT.java:3711
            case 9132: return "Tetrahedrite"; // MT.java:3718
            case 9133: return "Tungstate"; // MT.java:3728
            case 9134: return "Uraninite"; // MT.java:3702
            case 9135: return "Uvarovite"; // MT.java:1398
            case 9136: return "Wulfenite"; // MT.java:3751
            case 9137: return "YellowLimonite"; // MT.java:3740
            case 9138: return "Perlite"; // MT.java:3759
            case 9139: return "Borax"; // MT.java:3764
            case 9140: return "PotassiumFeldspar"; // MT.java:1238
            case 9141: return "Biotite"; // MT.java:1239
            case 9143: return "VanadiumMagnetite"; // MT.java:3742
            case 9144: return "Bastnasite"; // MT.java:3752
            case 9145: return "Pentlandite"; // MT.java:3712
            case 9146: return "Spodumene"; // MT.java:3767
            case 9147: return "Pollucite"; // MT.java:3737
            case 9148: return "Tantalite"; // MT.java:3743
            case 9149: return "Lepidolite"; // MT.java:3768
            case 9150: return "Glauconite"; // MT.java:3769
            case 9152: return "Vermiculite"; // MT.java:3771
            case 9153: return "Bentonite"; // MT.java:1284
            case 9154: return "Palygorskite"; // MT.java:1285
            case 9155: return "Pitchblende"; // MT.java:3753
            case 9156: return "Malachite"; // MT.java:3754
            case 9157: return "Mirabilite"; // MT.java:3761
            case 9158: return "Mica"; // MT.java:3772
            case 9159: return "Trona"; // MT.java:3760
            case 9160: return "Barite"; // MT.java:3721
            case 9161: return "Gypsum"; // MT.java:1106
            case 9162: return "Alunite"; // MT.java:3774
            case 9163: return "Dolomite"; // MT.java:1234
            case 9164: return "Wollastonite"; // MT.java:3734
            case 9165: return "Zeolite"; // MT.java:3736
            case 9166: return "Kyanite"; // MT.java:3773
            case 9167: return "Kaolinite"; // MT.java:1286
            case 9169: return "Talc"; // MT.java:1236
            case 9170: return "Gneiss"; // MT.java:3825
            case 9171: return "Greenschist"; // MT.java:3821
            case 9172: return "Greenstone"; // MT.java:1642
            case 9173: return "Greywacke"; // MT.java:3834
            case 9174: return "Graphite"; // MT.java:1292
            case 9175: return "Graphene"; // MT.java:1535
            case 9176: return "Gabbro"; // MT.java:3817
            case 9177: return "Komatiite"; // MT.java:3815
            case 9178: return "Siltstone"; // MT.java:3835
            case 9179: return "Rhyolite"; // MT.java:3836
            case 9180: return "Quartzite"; // MT.java:3827
            case 9181: return "Migmatite"; // MT.java:3837
            case 9182: return "Epidote"; // MT.java:1644
            case 9183: return "Emery"; // MT.java:1240
            case 9184: return "Blueschist"; // MT.java:3822
            case 9185: return "Bluestone"; // MT.java:1643
            case 9186: return "Chert"; // MT.java:3838
            case 9187: return "Dacite"; // MT.java:3839
            case 9188: return "Andesite"; // MT.java:3831
            case 9189: return "Limestone"; // MT.java:3820
            case 9190: return "Shale"; // MT.java:3813
            case 9191: return "Eclogite"; // MT.java:3842
            case 9192: return "Rutile"; // MT.java:1086
            case 9193: return "Stolzite"; // MT.java:3730
            case 9194: return "Ferberite"; // MT.java:3726
            case 9195: return "Huebnerite"; // MT.java:3727
            case 9196: return "Russellite"; // MT.java:3731
            case 9197: return "Pinalite"; // MT.java:3732
            case 9198: return "DiduraniumTrioxide"; // MT.java:3779
            case 9199: return "DuraniumHexafluoride"; // MT.java:3780
            case 9200: return "DuraniumHexachloride"; // MT.java:3781
            case 9201: return "DuraniumHexabromide"; // MT.java:3782
            case 9202: return "DuraniumHexaiodide"; // MT.java:3783
            case 9203: return "DuraniumHexaastatide"; // MT.java:3784
            case 9204: return "TritaniumDioxide"; // MT.java:3785
            case 9205: return "TritaniumHexafluoride"; // MT.java:3786
            case 9206: return "TritaniumHexachloride"; // MT.java:3787
            case 9207: return "TritaniumHexabromide"; // MT.java:3788
            case 9208: return "TritaniumHexaiodide"; // MT.java:3789
            case 9209: return "TritaniumHexaastatide"; // MT.java:3790
            case 9210: return "Bromargyrite"; // MT.java:3755
            case 9211: return "Smithsonite"; // MT.java:3756
            case 9212: return "Sperrylite"; // MT.java:3757
            case 9213: return "Kesterite"; // MT.java:3719
            case 9214: return "Stannite"; // MT.java:3720
            case 9215: return "Fluorite"; // MT.java:1109
            case 9216: return "Arsenopyrite"; // MT.java:3714
            case 9217: return "Wolframite"; // MT.java:3725
            case 9218: return "Kimberlite"; // MT.java:3826
            case 9219: return "Prismarine"; // MT.java:1639
            case 9220: return "PrismarineDark"; // MT.java:1640
            case 9221: return "Bischofite"; // MT.java:3762
            case 9222: return "Slate"; // MT.java:3840
            case 9223: return "Blackstone"; // MT.java:3833
            case 9243: return "ClayBrick"; // MT.java:1280
            case 9244: return "Grayschist"; // MT.java:3823
            case 9245: return "Pinkschist"; // MT.java:3824
            case 9246: return "Columbite"; // MT.java:3744
            case 9247: return "Coltan"; // MT.java:3745
            case 9248: return "Deepslate"; // MT.java:3841
            case 9249: return "PhobosRock"; // MT.java:3843
            case 9250: return "DeimosRock"; // MT.java:3844
            case 9251: return "VenusRock"; // MT.java:3845
            case 9252: return "MercuryRock"; // MT.java:3846
            case 9253: return "CeresRock"; // MT.java:3847
            case 9254: return "JupiterRock"; // MT.java:3848
            case 9255: return "IoRock"; // MT.java:3849
            case 9256: return "EuropaRock"; // MT.java:3850
            case 9257: return "GanymedeRock"; // MT.java:3851
            case 9258: return "CallistoRock"; // MT.java:3852
            case 9259: return "SaturnRock"; // MT.java:3853
            case 9260: return "RheaRock"; // MT.java:3854
            case 9261: return "TitanRock"; // MT.java:3855
            case 9262: return "OberonRock"; // MT.java:3856
            case 9263: return "IapetusRock"; // MT.java:3857
            case 9264: return "UranusRock"; // MT.java:3858
            case 9265: return "TitaniaRock"; // MT.java:3859
            case 9266: return "NeptuneRock"; // MT.java:3860
            case 9267: return "TritonRock"; // MT.java:3861
            case 9268: return "PlutoRock"; // MT.java:3862
            case 9269: return "ErisRock"; // MT.java:3863
            case 9270: return "Kepler22bRock"; // MT.java:3864
            case 9271: return "Quicklime"; // MT.java:1107
            case 9300: return "Oak"; // MT.java:3871
            case 9301: return "Birch"; // MT.java:3872
            case 9302: return "Spruce"; // MT.java:3873
            case 9303: return "Junglewood"; // MT.java:3874
            case 9304: return "Acacia"; // MT.java:3875
            case 9305: return "DarkOak"; // MT.java:3876
            case 9306: return "Crimsonwood"; // MT.java:3877
            case 9307: return "Warpedwood"; // MT.java:3878
            case 9308: return "WoodCompressed"; // MT.java:3881
            case 9309: return "WoodDead"; // MT.java:3882
            case 9310: return "WoodRotten"; // MT.java:3883
            case 9311: return "WoodMossy"; // MT.java:3884
            case 9312: return "WoodFrozen"; // MT.java:3885
            case 9313: return "Maple"; // MT.java:3891
            case 9314: return "Willow"; // MT.java:3892
            case 9315: return "BlueMahoe"; // MT.java:3893
            case 9316: return "Hazel"; // MT.java:3894
            case 9317: return "Cinnamonwood"; // MT.java:3895
            case 9318: return "Coconutwood"; // MT.java:3896
            case 9319: return "Rainbowood"; // MT.java:3897
            case 9320: return "Towerwood"; // MT.java:3900
            case 9321: return "Witchwood"; // MT.java:3901
            case 9322: return "Ogrewood"; // MT.java:3902
            case 9323: return "Wyvernwood"; // MT.java:3903
            case 9324: return "Aspen"; // MT.java:3904
            case 9325: return "DouglasFir"; // MT.java:3905
            case 9326: return "Sycamore"; // MT.java:3906
            case 9327: return "WhiteCedar"; // MT.java:3907
            case 9328: return "WhiteElm"; // MT.java:3908
            case 9329: return "Thorntree"; // MT.java:3909
            case 9330: return "SilverPine"; // MT.java:3910
            case 9331: return "Alder"; // MT.java:3911
            case 9332: return "Hawthorn"; // MT.java:3912
            case 9333: return "Rowan"; // MT.java:3913
            case 9334: return "Autumnwood"; // MT.java:3917
            case 9335: return "Pine"; // MT.java:3948
            case 9336: return "Cypress"; // MT.java:3918
            case 9337: return "Fir"; // MT.java:3919
            case 9338: return "JapaneseMaple"; // MT.java:3920
            case 9339: return "RainbowEucalyptus"; // MT.java:3921
            case 9340: return "Redwood"; // MT.java:3922
            case 9341: return "Sakura"; // MT.java:3923
            case 9342: return "Balsa"; // MT.java:3925
            case 9343: return "Baobab"; // MT.java:3926
            case 9344: return "Cherrywood"; // MT.java:3927
            case 9345: return "Chestnutwood"; // MT.java:3928
            case 9346: return "Citruswood"; // MT.java:3929
            case 9347: return "Cocobolowood"; // MT.java:3930
            case 9348: return "Ebony"; // MT.java:3931
            case 9349: return "Giganteumwood"; // MT.java:3932
            case 9350: return "Greenheart"; // MT.java:3933
            case 9351: return "Ipe"; // MT.java:3934
            case 9352: return "Kapok"; // MT.java:3935
            case 9353: return "Larch"; // MT.java:3936
            case 9354: return "Limewood"; // MT.java:3937
            case 9355: return "Mahoe"; // MT.java:3938
            case 9356: return "Mahogany"; // MT.java:3914
            case 9357: return "Padauk"; // MT.java:3939
            case 9358: return "Palm"; // MT.java:3915
            case 9359: return "Papayawood"; // MT.java:3940
            case 9360: return "Plumwood"; // MT.java:3941
            case 9361: return "Poplar"; // MT.java:3942
            case 9362: return "Sequoia"; // MT.java:3943
            case 9363: return "Teak"; // MT.java:3944
            case 9364: return "Walnutwood"; // MT.java:3945
            case 9365: return "Wenge"; // MT.java:3946
            case 9366: return "Zebrawood"; // MT.java:3947
            case 9367: return "Darkwood"; // MT.java:3950
            case 9368: return "Etherealwood"; // MT.java:3951
            case 9369: return "Goldwood"; // MT.java:3952
            case 9370: return "Hellbark"; // MT.java:3953
            case 9371: return "Jacaranda"; // MT.java:3954
            case 9372: return "Mangrove"; // MT.java:3955
            case 9373: return "SacredOak"; // MT.java:3956
            case 9374: return "Magicwood"; // MT.java:3957
            case 9375: return "Applewood"; // MT.java:3959
            case 9376: return "Ashwood"; // MT.java:3960
            case 9377: return "Beech"; // MT.java:3961
            case 9378: return "Boxwood"; // MT.java:3962
            case 9379: return "Brazilwood"; // MT.java:3963
            case 9380: return "Butternutwood"; // MT.java:3964
            case 9381: return "Cedar"; // MT.java:3965
            case 9382: return "Elderwood"; // MT.java:3966
            case 9383: return "Elm"; // MT.java:3967
            case 9384: return "Eucalyptus"; // MT.java:3968
            case 9385: return "Figwood"; // MT.java:3969
            case 9386: return "Gingko"; // MT.java:3970
            case 9387: return "Hemlock"; // MT.java:3971
            case 9388: return "Hickory"; // MT.java:3972
            case 9389: return "Holly"; // MT.java:3973
            case 9390: return "Hornbeam"; // MT.java:3974
            case 9391: return "Iroko"; // MT.java:3975
            case 9392: return "Locust"; // MT.java:3976
            case 9393: return "Logwood"; // MT.java:3977
            case 9394: return "Maclura"; // MT.java:3978
            case 9395: return "Olivewood"; // MT.java:3979
            case 9396: return "Pearwood"; // MT.java:3980
            case 9397: return "PinkIvory"; // MT.java:3981
            case 9398: return "Purpleheart"; // MT.java:3982
            case 9399: return "Rosewood"; // MT.java:3983
            case 9400: return "Sweetgum"; // MT.java:3984
            case 9401: return "Syzgium"; // MT.java:3985
            case 9402: return "Whitebeam"; // MT.java:3986
            case 9403: return "Yew"; // MT.java:3987
            case 9404: return "WoodScorched"; // MT.java:3886
            case 9405: return "WoodVarnished"; // MT.java:3887
            case 9406: return "WoodTainted"; // MT.java:3889
            case 9407: return "WoodBleached"; // MT.java:3888
            case 9408: return "Foxfirewood"; // MT.java:3879
            case 9409: return "BlueSpruce"; // MT.java:3898
            case 9700: return "MeatRaw"; // MT.java:1318
            case 9701: return "MeatCooked"; // MT.java:1317
            case 9702: return "Wheat"; // MT.java:1323
            case 9703: return "Sugar"; // MT.java:1038
            case 9704: return "Barley"; // MT.java:1324
            case 9705: return "Rye"; // MT.java:1325
            case 9706: return "Rice"; // MT.java:1326
            case 9707: return "Oat"; // MT.java:1327
            case 9708: return "Corn"; // MT.java:1329
            case 9709: return "Potato"; // MT.java:1330
            case 9710: return "MeatRotten"; // MT.java:1319
            case 9711: return "FishCooked"; // MT.java:1320
            case 9712: return "FishRaw"; // MT.java:1321
            case 9713: return "FishRotten"; // MT.java:1322
            case 9714: return "Almond"; // MT.java:1343
            case 9715: return "Marshmallow"; // MT.java:1257
            case 9716: return "PEZ"; // MT.java:1344
            case 9717: return "Licorice"; // MT.java:1345
            case 9718: return "Nougat"; // MT.java:1346
            case 9719: return "AbyssalOat"; // MT.java:1328
            case 9778: return "Tofu"; // MT.java:1331
            case 9779: return "SoylentGreen"; // MT.java:1332
            case 9780: return "Cheese"; // MT.java:1333
            case 9781: return "Chili"; // MT.java:1334
            case 9782: return "Cocoa"; // MT.java:1335
            case 9783: return "Chocolate"; // MT.java:1336
            case 9784: return "Coffee"; // MT.java:1337
            case 9785: return "Cinnamon"; // MT.java:1338
            case 9786: return "Nutmeg"; // MT.java:1339
            case 9787: return "Vanilla"; // MT.java:1039
            case 9788: return "PepperBlack"; // MT.java:1347
            case 9789: return "Curry"; // MT.java:1348
            case 9790: return "Milk"; // MT.java:1349
            case 9791: return "Honey"; // MT.java:1352
            case 9792: return "Tea"; // MT.java:1354
            case 9793: return "Honeydew"; // MT.java:1353
            case 9794: return "Mint"; // MT.java:1355
            case 9795: return "Peanut"; // MT.java:1340
            case 9796: return "Hazelnut"; // MT.java:1341
            case 9797: return "Pistachio"; // MT.java:1342
            case 9798: return "Butter"; // MT.java:1350
            case 9799: return "SaltedButter"; // MT.java:1351
            case 9800: return "Water"; // MT.java:1007
            case 9801: return "Snow"; // MT.java:1012
            case 9802: return "Ice"; // MT.java:1013
            case 9803: return "FreshWater"; // MT.java:1014
            case 9804: return "Saltwater"; // MT.java:1143
            case 9805: return "HolyWater"; // MT.java:1015
            case 9806: return "SeaWater"; // MT.java:1016
            case 9807: return "WaterDirty"; // MT.java:1017
            case 9808: return "WaterDistilled"; // MT.java:1018
            case 9809: return "HydrogenPeroxide"; // MT.java:1019
            case 9810: return "Lava"; // MT.java:1194
            case 9811: return "SemiheavyWater"; // MT.java:1008
            case 9812: return "HeavyWater"; // MT.java:1009
            case 9813: return "TritiatedWater"; // MT.java:1010
            case 9814: return "Steam"; // MT.java:1011
            case 9815: return "SaltedWater"; // MT.java:1160
            case 9820: return "NitroCarbon"; // MT.java:1191
            case 9821: return "Glyceryl"; // MT.java:1041
            case 9822: return "SodiumPersulfate"; // MT.java:1134
            case 9823: return "SodiumSulfide"; // MT.java:1135
            case 9824: return "SulfuricAcid"; // MT.java:1047
            case 9825: return "NitricAcid"; // MT.java:1031
            case 9826: return "HydrochloricAcid"; // MT.java:1020
            case 9827: return "AquaRegia"; // MT.java:1188
            case 9828: return "Glycerol"; // MT.java:1040
            case 9829: return "HydrogenFluoride"; // MT.java:1021
            case 9830: return "Air"; // MT.java:1027
            case 9831: return "NitrogenDioxide"; // MT.java:1029
            case 9832: return "Methane"; // MT.java:1037
            case 9833: return "MethaneIce"; // MT.java:1190
            case 9834: return "SulfurDioxide"; // MT.java:1044
            case 9835: return "SulfurTrioxide"; // MT.java:1045
            case 9836: return "CarbonDioxide"; // MT.java:1035
            case 9837: return "NitrogenMonoxide"; // MT.java:1028
            case 9838: return "CarbonMonoxide"; // MT.java:1034
            case 9839: return "HeliumNeon"; // MT.java:1024
            case 9840: return "Biomass"; // MT.java:1195
            case 9841: return "BioFuel"; // MT.java:1196
            case 9842: return "Ethanol"; // MT.java:1197
            case 9843: return "CarbonTrioxide"; // MT.java:1036
            case 9844: return "DisulfuricAcid"; // MT.java:1048
            case 9850: return "Oil"; // MT.java:1198
            case 9851: return "OilSand"; // MT.java:1199
            case 9852: return "CrudeOil"; // MT.java:1200
            case 9853: return "OilShale"; // MT.java:1646
            case 9860: return "Fuel"; // MT.java:1201
            case 9861: return "NitroFuel"; // MT.java:1202
            case 9862: return "Kerosine"; // MT.java:1203
            case 9863: return "Diesel"; // MT.java:1204
            case 9864: return "Petrol"; // MT.java:1205
            case 9865: return "Propane"; // MT.java:1206
            case 9866: return "Butane"; // MT.java:1207
            case 9867: return "Propylene"; // MT.java:1208
            case 9868: return "Ethylene"; // MT.java:1209
            case 9870: return "Creosote"; // MT.java:1210
            case 9871: return "FishOil"; // MT.java:1211
            case 9872: return "SeedOil"; // MT.java:1213
            case 9873: return "HempOil"; // MT.java:1214
            case 9874: return "LinOil"; // MT.java:1215
            case 9875: return "SunflowerOil"; // MT.java:1216
            case 9876: return "NutOil"; // MT.java:1217
            case 9877: return "OliveOil"; // MT.java:1218
            case 9880: return "FryingOilHot"; // MT.java:1219
            case 9881: return "Glue"; // MT.java:1220
            case 9882: return "Lubricant"; // MT.java:1221
            case 9883: return "ConstructionFoam"; // MT.java:1222
            case 9884: return "UUAmplifier"; // MT.java:1223
            case 9885: return "UUMatter"; // MT.java:1224
            case 9886: return "Latex"; // MT.java:1225
            case 9887: return "WhaleOil"; // MT.java:1212
            default: return null;
        }
    }
}

