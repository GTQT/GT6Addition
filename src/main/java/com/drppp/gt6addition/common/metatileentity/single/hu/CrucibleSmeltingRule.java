package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.math.BigInteger;
import java.util.Locale;

/** Explicit MT.java targetSmelting ratios, in material units (not fluid mB). */
final class CrucibleSmeltingRule {
    final String target;
    final int meltingPoint;
    final long boilingPoint;
    private final int numerator;
    private final int denominator;

    private CrucibleSmeltingRule(String target, int numerator, int denominator, int melt, long boil) {
        this.target = target;
        this.numerator = numerator;
        this.denominator = denominator;
        this.meltingPoint = melt;
        this.boilingPoint = boil;
    }

    static CrucibleSmeltingRule find(String name) {
        if (name == null) return null;
        GT6TechnicalMaterialData.Profile family = GT6TechnicalMaterialData.find(name);
        if (family != null) {
            if (family.targetsSource == null) return null; // Constructor self/U; steal is not target copying.
            CrucibleSmeltingRule source = find(family.targetsSource);
            // Native default self/U and explicit self targets both refer to
            // the native material, not the technical family doing the copy.
            // Heat still comes from statsSource, notably for ANY.MagicIron.
            return source == null ? ratio(family.nativeSelfOutput, 1, 1) :
                    ratio(source.target == null ? family.nativeSelfOutput : source.target,
                            source.numerator, source.denominator);
        }
        if (GT6WoodMaterialData.contains(name)) return new CrucibleSmeltingRule("ash", 1, 4, 400, 500);
        switch (GT6MaterialIdentity.canonicalOreName(name.toLowerCase(Locale.ROOT)
                .replace("_", "").replace("-", "").replace(" ", ""))) {
            // MT.java:414,418 retain default self smelting. Wrought Iron and
            // Annealed Copper are separate alloy refinements (1655-1656),
            // not CEu IngotProperty targets applied at the base melting point.
            case "iron": return new CrucibleSmeltingRule(null, 1, 1, 1811, 3134);
            case "copper": return new CrucibleSmeltingRule(null, 1, 1, 1357, 2835);
            // GTQTCore's explicitly configured Fe2O3 is GT6's Hematite. CEu's
            // BandedIron already is that same identity and retains self targets.
            case "ironiiioxide": return new CrucibleSmeltingRule("hematite", 1, 1, 1207, 2414);
            // MT.java:167 wax() grants MELTING; default one-U self target.
            case "wax": return ratio(null, 1, 1);
            // MT.java:217,1281-1286; clay() always has the same explicit
            // ceramic target, regardless of its trace component/divider.
            case "clay":
            case "claybrown":
            case "clayred":
            case "bentonite":
            case "palygorskite":
            case "fullersearth":
            case "kaolinite": return new CrucibleSmeltingRule("ceramic", 1, 1, 2000, 4000);
            // MT.java:215,1109-1118: only the ordinary Fluorite targets self.
            case "fluorite": return new CrucibleSmeltingRule(null, 1, 1, 1633, 3266);
            case "redfluorite":
            case "pinkfluorite":
            case "bluefluorite":
            case "greenfluorite":
            case "blackfluorite":
            case "whitefluorite":
            case "yellowfluorite":
            case "orangefluorite":
            case "magentafluorite": return new CrucibleSmeltingRule("fluorite", 1, 1, 1633, 3266);
            // MT.java:332,1569-1574 gem_aa copies the existing targets,
            // including their quantities, rather than merely copying identity.
            case "redstonia": return ratio("redstone", 1, 1);
            case "palis": return ratio("lapis", 1, 1);
            case "diamantine": return ratio("carbon", 2, 1);
            case "voidcrystal": return ratio("carbon", 1, 2);
            case "emeradic": return ratio("beryllium", 1, 36);
            case "enori": return ratio("iron", 1, 1);
            // MT.java:1055 metalore grants MELTING; default target is self.
            case "carborundum":
            case "siliconcarbide": return new CrucibleSmeltingRule(null, 1, 1, 3000, 3100);
            case "duraniumalloy":
            case "tritaniumalloy": return ratio(null, 1, 1);
            // GT6 defaults targetSmelting to self. Its steal/pulver/generify
            // calls do not change that target to the unrefined metal.
            case "wrougtiron":
            case "wroughtiron": return new CrucibleSmeltingRule(null, 1, 1, 2011, 3134);
            case "annealedcopper": return new CrucibleSmeltingRule(null, 1, 1, 2800, 2835);
            case "water": return new CrucibleSmeltingRule(null, 1, 1, 273, 373);
            case "ice": return new CrucibleSmeltingRule("water", 1, 1, 273, 373);
            case "pyrolusite": return new CrucibleSmeltingRule("manganese", 3, 4, 808, 2334);
            case "aluminiumhydroxide": return new CrucibleSmeltingRule("alumina", 5, 14, 573, 1146);
            case "tungsticacid": return new CrucibleSmeltingRule("tungsten_trioxide", 4, 7, 373, 1746);
            // Composition-derived thermal data lives in GT6DeclaredPhaseData.
            case "vanadiumpentoxide": return ratio("vanadium", 1, 7);
            // GT6 MT.java mineral targetSmelting definitions, including late initialization.
            case "ferricoxyhydroxide": return new CrucibleSmeltingRule("hematite", 5, 14, 1207, 2414);
            case "chalk":
            case "marble": return ratio("calcite", 2, 3);
            case "dolomite":
            case "limestone": return ratio("calcite", 1, 2);
            case "cassiterite":
            case "cassiteritesand": return new CrucibleSmeltingRule("tin", 3, 4, 757, 1514);
            case "garnierite": return ratio("nickel", 3, 4);
            case "uraninite": return ratio("uranium_238", 1, 3);
            case "realgar": return ratio("arsenic", 1, 3);
            case "cinnabar": return ratio("mercury", 1, 3);
            case "molybdenite": return ratio("molybdenum", 1, 4);
            case "sphalerite": return ratio("zinc", 1, 3);
            case "stibnite": return new CrucibleSmeltingRule("antimony", 1, 4, 823, 1646);
            case "pentlandite": return ratio("nickel", 1, 3);
            case "chalcopyrite": return ratio("copper", 2, 9);
            case "arsenopyrite": return ratio("arsenic", 1, 4);
            case "cobaltite": return ratio("cobalt", 1, 4);
            case "galena": return ratio("lead", 1, 3);
            case "cooperite": return ratio("platinum", 1, 3);
            case "tetrahedrite": return ratio("copper", 1, 4);
            case "kesterite":
            case "stannite": return ratio("copper", 1, 9);
            case "barite": return new CrucibleSmeltingRule("barium", 1, 9, 1853, 3706);
            case "celestine": return ratio("strontium", 1, 9);
            case "stolzite": return ratio("tungsten_trioxide", 4, 6);
            case "russellite": return ratio("tungsten_trioxide", 4, 9);
            case "pinalite": return ratio("tungsten_trioxide", 4, 11);
            case "brownlimonite":
            case "yellowlimonite": return new CrucibleSmeltingRule("hematite", 1, 2, 1523, 3046);
            case "chromite": return ratio("chrome", 2, 9);
            case "powellite":
            case "wulfenite": return ratio("molybdenum", 1, 9);
            case "bastnasite": return ratio("cerium", 1, 9);
            case "pitchblende": return ratio("uranium_238", 1, 5);
            case "malachite": return ratio("copper", 1, 6);
            case "bromargyrite": return ratio("silver", 1, 3);
            case "smithsonite": return ratio("zinc", 1, 6);
            case "sperrylite": return ratio("platinum", 1, 4);
            case "quartzsand": return ratio("silicon_dioxide", 1, 3);
            case "zircon": return ratio("zirconium", 1, 9);
            case "azurite": return ratio("copper", 1, 9);
            case "steeleaf": return ratio("steel", 1, 4);
            case "sand":
            case "redsand":
            case "endsandwhite":
            case "whiteendsand":
            case "endsandblack":
            case "blackendsand": return new CrucibleSmeltingRule("glass", 1, 1, 1000, 3000);
            case "concrete": return new CrucibleSmeltingRule("stone", 1, 1, 500, 1000);
            case "gildediron":
            case "ironcompressed": return ratio("iron", 1, 1);
            case "ironmagnetic":
            case "frozeniron": return new CrucibleSmeltingRule("iron", 1, 1, 1811, 3134);
            case "steelmagnetic": return new CrucibleSmeltingRule("steel", 1, 1, 2046, 3134);
            case "neodymiummagnetic": return new CrucibleSmeltingRule("neodymium", 1, 1, 1297, 3347);
            case "pigiron": return new CrucibleSmeltingRule("wrought_iron", 1, 1, 2011, 3134);
            case "galvanizedsteel":
            case "steelgalvanized": return ratio("steel", 1, 1);
            case "fireleaf": return new CrucibleSmeltingRule("fiery_steel", 1, 4, 2934, 3634);
            case "leather": return ratio("ash", 1, 9);
            case "arcanecompound": return ratio("arcane_ash", 2, 1);
            case "yellorite": return ratio("yellorium", 1, 3);
            case "netherrack": return ratio("nether_brick", 1, 1);
            // setAllToTheOutputOf copies the target's existing smelting output.
            case "brick":
            case "claybrick": return ratio("ceramic", 1, 1);
            case "tungstensintered":
            case "sinteredtungsten": return ratio("tungsten", 1, 1);
            case "refinediron": return ratio("iron", 1, 1);
            case "refinedglowstone":
            case "glowstonerefined": return ratio("glowstone", 1, 1);
            case "refinedobsidian":
            case "obsidianrefined": return ratio("lava", 1, 1);
            case "meatraw": return new CrucibleSmeltingRule("meat_cooked", 1, 1, 477, 550);
            case "snow": return new CrucibleSmeltingRule("water", 1, 1, 273, 373); // MT.java:1012.
            case "fishraw": return new CrucibleSmeltingRule("fish_cooked", 1, 1, 477, 550);
            case "coal":
            case "charcoal":
            case "anthracite":
            case "graphite": return new CrucibleSmeltingRule("carbon", 1, 2, 1700, 4300);
            case "coke":
            case "coalcoke": return new CrucibleSmeltingRule("carbon", 1, 1, 1700, 4300);
            case "graphene": return new CrucibleSmeltingRule("carbon", 1, 2, 4300, 4400);
            case "wood":
            case "bark": return new CrucibleSmeltingRule("ash", 1, 4, 400, 500);
            // Peanutwood's steal(Wood) copies heat/stats, not processing
            // targets. Marshmallow only uses wood(); both retain self/U.
            case "woodtreated":
            case "treatedwood":
            case "woodpolished": return new CrucibleSmeltingRule("ash", 1, 4, 500, 600);
            case "woodrubber":
            case "bamboo":
            case "skyroot":
            case "weedwood": return new CrucibleSmeltingRule("ash", 1, 2, 350, 450);
            case "livingwood": return new CrucibleSmeltingRule("ash", 1, 2, 350, 500);
            case "dreamwood":
            case "shimmerwood": return new CrucibleSmeltingRule("ash", 1, 2, 350, 550);
            case "greatwood": return new CrucibleSmeltingRule("ash", 1, 2, 400, 600);
            // null target is GT6's explicit self-target, with reduced amount.
            case "rubber": return new CrucibleSmeltingRule(null, 2, 3, 410, 820);
            case "plastic":
            case "teflon":
            case "ptfe":
            case "polymer":
            case "polytetrafluoroethylene":
            case "pvc":
            case "polyvinylchloride":
            case "bakelite":
            case "hardplastic":
            case "polycarbonate": return new CrucibleSmeltingRule(null, 2, 3, 423, 846);
            // Material factory inheritance in MT.java, not guessed gem composition.
            case "diamond":
            case "diamondblue":
            case "bluediamond":
            case "diamondgreen":
            case "greendiamond":
            case "diamondpurple":
            case "purplediamond":
            case "diamondred":
            case "reddiamond":
            case "diamondyellow":
            case "yellowdiamond":
            case "diamondpink":
            case "pinkdiamond":
            case "diamondindustrial":
            case "manadiamond":
            case "elvendragonstone":
            case "gravitite": return ratio("carbon", 2, 1);
            case "sapphire":
            case "saphire":
            case "ruby":
            case "bluesapphire":
            case "greensapphire":
            case "purplesapphire":
            case "yellowsapphire":
            case "orangesapphire": return ratio("alumina", 3, 4);
            case "emerald":
            case "aquamarine":
            case "morganite":
            case "heliodor":
            case "goshenite":
            case "bixbite":
            case "scarletemerald":
            case "maxixe": return ratio("beryllium", 1, 36);
            case "flint":
            case "quartzite":
            case "milkyquartz":
            case "netherquartz":
            case "voidquartz":
            case "sunnyquartz":
            case "lavenderquartz":
            case "redquartz":
            case "blazequartz":
            case "smokeyquartz":
            case "smokyquartz":
            case "quartzsmoky":
            case "manaquartz":
            case "elvenquartz":
            case "blackquartz":
            case "quartzblack":
            case "certusquartz":
            case "chargedcertusquartz":
            case "fluix": return ratio("silicon_dioxide", 1, 1);
            // valgemdcmp -> valgemcent -> valgemelec inherit disabled smelting;
            // Zircon and sapphire/emerald/diamond factories override it explicitly.
            case "spinel":
            // MT.java:203,1596-1611: only the five uncommented materials.
            case "hexoriumblack":
            case "hexoriumred":
            case "hexoriumgreen":
            case "hexoriumblue":
            case "hexoriumwhite":
            // MT.java:212-214 jasper/tigereye/aventurine -> valgemelec.
            case "redjasper":
            case "jasper":
            case "oceanjasper":
            case "rainforestjasper":
            case "bluejasper":
            case "greenjasper":
            case "yellowjasper":
            case "tigereye":
            case "yellowtigereye":
            case "catseye":
            case "greentigereye":
            case "dragoneye":
            case "redtigereye":
            case "hawkseye":
            case "bluetigereye":
            case "blackeye":
            case "blacktigereye":
            case "tigeriron":
            case "greenaventurine":
            case "aventurine":
            case "brownaventurine":
            case "yellowaventurine":
            case "blackaventurine":
            case "blueaventurine":
            case "redaventurine":
            // MT.java:211 garnet -> valgemelec -> valgemdcmp; no positive override.
            case "almandine":
            case "grossular":
            case "pyrope":
            case "spessartine":
            case "andradite":
            case "uvarovite":
            case "garnetred":
            case "garnetorange":
            case "garnetpurple":
            case "garnet":
            case "garnetyellow":
            case "garnetgreen":
            case "balasruby":
            case "foolsruby":
            case "topaz":
            case "bluetopaz":
            case "tanzanite":
            case "zanite":
            case "amazonite":
            case "alexandrite":
            case "opal":
            case "onyxred":
            case "onyxblack":
            case "onyx": // MT.java:1433 explicit ore-name alias.
            case "sugilite":
            case "peridot":
            case "olivine": // MT.java:1435 explicit ore-name alias.
            case "amethyst":
            case "dioptase":
            // MT.java:1498: field EnderAmethyst, actual saved name AmethystEnder.
            case "amethystender":
            case "enderamethyst":
            case "dilithium":
            case "silverwood": return ratio("", 0, 1);
            default: return null;
        }
    }

    static boolean hasMeltingFlag(String name) {
        CrucibleSmeltingRule rule = find(name);
        return rule != null && rule.numerator > 0;
    }

    static boolean hasDefaultSelfTarget(String name) {
        // OreDictMaterial.java:286 initializes targetSmelting to this/U.
        // Explicit rules (including zero targets) always take precedence.
        // A default target does NOT itself grant GT6's MELTING flag.
        return find(name) == null && CrucibleSolidifyingRule.isSnapshotMaterial(name);
    }

    private static CrucibleSmeltingRule ratio(String target, int numerator, int denominator) {
        return new CrucibleSmeltingRule(target, numerator, denominator, -1, Long.MAX_VALUE);
    }

    long convert(long amount) {
        if (amount <= 0) return 0;
        BigInteger result = BigInteger.valueOf(amount).multiply(BigInteger.valueOf(numerator))
                .divide(BigInteger.valueOf(denominator));
        return result.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? -1 : result.longValue();
    }

    CrucibleFluidUnits.Quantity convertStored(long amount, int remainder) {
        return CrucibleFluidUnits.scaleStored(amount, remainder, numerator, denominator);
    }
}
