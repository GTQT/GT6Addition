package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Verified MT.java metal/alloy factory MELTING inheritance and direct UNBURNABLE declarations.
 * This snapshot list does not infer flags from CEu ingot/fluid availability.
 */
final class GT6InheritedBurningExemptions {
    private static final Set<String> NAMES = new HashSet<>(Arrays.asList(
            // wood() calls positive setSmelting before per-material overrides.
            // Silverwood's later zero target does not remove its existing MELTING tag.
            "wood", "woodtreated", "treatedwood", "woodpolished", "woodrubber", "bamboo", "skyroot",
            "weedwood", "livingwood", "dreamwood", "shimmerwood", "greatwood", "silverwood",
            "peanutwood", "marshmallow",
            "ad", "adamantine", "adamantite", "adamantium", "alduorite",
            "aluminiumalloy", "aluminiumbrass", "alumite", "amordrine", "ancientdebris",
            "angmallen", "annealedcopper", "ardite", "aredrite", "arsenicbronze",
            "arseniccopper", "astralsilver", "basalt", "batteryalloy", "bedrock",
            "bedrockhslaalloy", "bedrockium", "bismuthbronze", "blackbronze", "blacksteel",
            "blaze", "bluealloy", "bluesteel", "blutonium", "brass",
            "bronze", "carborundum", "carmot", "castiron", "cdinagalloy",
            "celenegil", "ceruclase", "chromiumdioxide", "claycompound", "cobaltbrass",
            "conductiveiron", "constantan", "cosmicneutronium", "crimson", "crimsonwood",
            "crudesteel", "crystallinealloy", "crystallinepinkslime", "crystalmatrix", "cyanite",
            "damascussteel", "darkiron", "darkmatter", "darkthaumium", "deadrock",
            "deepiron", "desh", "deshalloy", "desichalkos", "diorite",
            "draconium", "draconiumawakened", "duralumin", "duranium", "duraniumalloy",
            "efrine", "electricalsteel", "electrotinealloy", "electrum", "electrumflux",
            "elvendragonstone", "elvenelementium", "elvorium", "enderium", "enderiumbase",
            "endium", "endsteel", "energeticalloy", "energeticsilver", "eximite",
            "ferrite", "fierysteel", "fireleaf", "firestone", "flamascussteel",
            "force", "foxfire", "foxfirewood", "frozeniron", "gabbro",
            "gaiaspirit", "gildediron", "glowstonerefined", "goldinductive", "haderoth",
            "hepatizon", "hsla", "hslaspringsteel", "hslasteel", "hslatungstenalloy",
            "hsse", "hssg", "hsss", "ignatius", "inductivealloy",
            "infinity", "infuscolium", "inolashite", "invar", "iritanium",
            "ironcast", "ironcompressed", "ironmagnetic", "ironwood", "kalendrite",
            "kanthal", "knightmetal", "komatiite", "kreknorite", "lemurite",
            "li2fe2o4", "ludicrite", "lumium", "magnalium", "manadiamond",
            "manasteel", "manyullyn", "mauftrium", "melodicalloy", "meteoflameblacksteel",
            "meteoflamebluesteel", "meteoflameredsteel", "meteoflamesteel", "meteoricblacksteel", "meteoricbluesteel",
            "meteoriciron", "meteoricredsteel", "meteoricsteel", "meteorite", "meutoite",
            "midasium", "mingrade", "mithril", "muspelheimpower", "neodymiummagnetic",
            "netherbrick", "netherite", "netherizeddiamond", "netherrack", "netherstar",
            "nichrome", "niflheimpower", "nikolinealloy", "niobiumnitride", "niobiumtitanium",
            "obsidian", "obsidianrefined", "obsidiansteel", "octine", "orichalcum",
            "oriharukon", "osmiridium", "oureclase", "pigiron", "prometheum",
            "pulsatingiron", "pumice", "purplealloy", "rainbowood", "redalloy",
            "redmatter", "redmeteor", "redsteel", "redstonealloy", "refinedglowstone",
            "refinedobsidian", "rosegold", "rubracium", "sanguinite", "shadowiron",
            "shadowsteel", "sic", "signalum", "solderingalloy", "soularium",
            "spectreiron", "springsteel", "stainlesssteel", "steel", "steeleaf",
            "steelgalvanized", "steelmagnetic", "stellaralloy", "sterlingsilver", "sunnarium",
            "syrmorite", "ta4hfc5", "tantalumhafniumcarbide", "tartarite", "terrasteel",
            "thaumium", "tinalloy", "titaniumaluminide", "titaniumgold", "titaniumiridium",
            "trinaquadalloy", "trinitanium", "tritanium", "tritaniumalloy", "tungstenalloy",
            "tungstencarbide", "tungstensintered", "tungstensteel", "ultimet", "unstable",
            "vanadiumgallium", "vanadiumsteel", "vb", "vibramantium", "vibranium",
            "vibraniumsilver", "vibraniumsteel", "vibrantalloy", "vinteum", "vinteumpurified",
            "vividalloy", "voidmetal", "vulcanite", "vyroxeres", "warped",
            "warpedwood", "waxrefractory", "workersalloy", "wroughtiron", "yellorium",
            "yttriumbariumcuprate"
    ));

    private GT6InheritedBurningExemptions() {}

    static boolean contains(String name) {
        return name != null && NAMES.contains(name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ""));
    }
}

