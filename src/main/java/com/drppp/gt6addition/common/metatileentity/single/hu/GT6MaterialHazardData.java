package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraftforge.fluids.Fluid;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/** Verified GT6 material hazard tags, not guesses based on chemical name fragments. */
final class GT6MaterialHazardData {
    private static final int FLAMMABLE = 1;
    private static final int EXPLOSIVE = 2;
    private static final Map<String, Integer> COMBUSTION = new HashMap<>();

    static {
        // MT.java:392-404,461 and explicit material declarations.
        putCombustion(FLAMMABLE, "carbon", "carbon13", "carbon14", "magnesium", "sulfur", "sulphur");
        putCombustion(FLAMMABLE | EXPLOSIVE, "phosphor", "phosphorus", "phosphorous", "hafnium",
                "glyceryl", "phosphate", "tricalciumphosphate", "nitrocarbon", "bluephosphorus", "redphosphorus",
                "whitephosphorus", "anyphosphorus", "gunpowder", "dynamite", "tnt");
        putCombustion(EXPLOSIVE, "naquadahenriched", "enrichednaquadah", "naquadria"); // :745-746
        // MT.java:1201-1205 lqudexpl actually grants both tags; literal aliases included.
        putCombustion(FLAMMABLE | EXPLOSIVE, "fuel", "fueloil", "nitrofuel", "kerosine", "diesel",
                "petrol", "gasoline");
        // In this snapshot gasexpl -> gas -> create only grants DECOMPOSABLE.
        // Do not infer flags from its method name or override it with host flags.
        putCombustion(0, "propane", "butane", "propylene", "ethylene"); // :1206-1209
        // ANY technical families do not copy their source's flags via steal.
        putCombustion(0, "anymagiciron", "anywoodorplastic");
        putCombustion(0, "anygarnet", "anyjasper", "anytigereye", "anyaventurine", "anyamber");
        putCombustion(0, "wax", "stone", "calcite", "sand", "silicondioxide");
        putCombustion(0, "blaze", "anyblaze", "prismarine", "prismarinedark", "anyprismarine");
        putCombustion(0, "fishraw", "fishcooked", "fishrotten", "tofu", "bone");
        putCombustion(0, "potato"); // setBurning(Ash, U9) does not grant FLAMMABLE.
        putCombustion(0, "anythaumiccrystal", "anyhexorium");
        // MT.java:180,198-214,1389-1437,1498,1508,1596-1611. These
        // disabled-smelting gems have neither FLAMMABLE nor EXPLOSIVE. Component
        // configuration/generification does not copy those tags. Keep explicit
        // zero records rather than allowing unrelated host flags to burn/explode
        // them; a zero smelting target alone is not proof (e.g. Silverwood).
        putCombustion(0, "spinel", "balasruby", "foolsruby",
                "almandine", "grossular", "pyrope", "spessartine", "andradite", "uvarovite",
                "garnetred", "garnetorange", "garnetpurple", "garnet", "garnetyellow", "garnetgreen",
                "redjasper", "jasper", "oceanjasper", "rainforestjasper", "bluejasper", "greenjasper", "yellowjasper",
                "tigereye", "yellowtigereye", "catseye", "greentigereye", "dragoneye", "redtigereye",
                "hawkseye", "bluetigereye", "blackeye", "blacktigereye", "tigeriron",
                "greenaventurine", "aventurine", "brownaventurine", "yellowaventurine",
                "blackaventurine", "blueaventurine", "redaventurine",
                "topaz", "bluetopaz", "tanzanite", "zanite", "amazonite", "alexandrite",
                "opal", "onyxred", "onyxblack", "onyx", "sugilite", "peridot", "olivine",
                "amethyst", "dioptase", "amethystender", "enderamethyst", "dilithium",
                "hexoriumblack", "hexoriumred", "hexoriumgreen", "hexoriumblue", "hexoriumwhite");
        putCombustion(FLAMMABLE, "anygrains");
        // MT.java:1037-1198 explicit tags and lqudflam; :1210-1220 oil factories.
        putCombustion(FLAMMABLE, "methane", "sugar", "glycerol", "hydrosulfuricacid", "hydrogensulfide",
                "sodiumnitrate", "potassiumnitrate", "methaneice", "biomass", "biofuel", "ethanol", "oil",
                "creosote", "creosoteoil", "fishoil", "whaleoil", "seedoil", "hempoil", "linoil",
                "sunfloweroil", "nutoil", "oliveoil", "fryingoilhot", "glue");
        // MT.java:312 wood factory and :1243-1259; trace/copy calls do not copy flags.
        putCombustion(FLAMMABLE, "bark", "wood", "woodtreated", "treatedwood", "woodpolished", "woodrubber",
                "bamboo", "skyroot", "weedwood", "livingwood", "dreamwood", "shimmerwood", "greatwood",
                "silverwood", "peanutwood", "marshmallow", "petrifiedwood");
        // MT.java:1293-1314 and explicit ore/material aliases.
        putCombustion(FLAMMABLE, "niter", "nitre", "apatite", "phosphorite", "rubber", "plastic", "teflon",
                "polymer", "ptfe", "polytetrafluoroethylene", "pvc", "polyvinylchloride", "bakelite",
                "hardplastic", "polycarbonate", "leather", "indigo");
        // MT.java grain()/coal() factories and :1323-1329,1521-1531,1633,1646.
        putCombustion(FLAMMABLE, "wheat", "barley", "rye", "rice", "oat", "abyssaloat", "corn",
                "charcoal", "coal", "coalcoke", "coke", "anthracite", "prismane", "lonsdaleite",
                "lignite", "lignitecoke", "petroleumcoke", "petcoke", "peat", "peatbituminous",
                "netherrack", "oilshale");
    }

    private GT6MaterialHazardData() {}

    private static void putCombustion(int flags, String... names) {
        for (String name : names) COMBUSTION.put(name, flags);
    }

    private static Integer combustionFlags(String name) {
        return name == null ? null : COMBUSTION.get(GT6MaterialIdentity.canonicalCompoundName(
                name.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "")));
    }

    static boolean isExplosive(String name) {
        Integer flags = combustionFlags(name);
        return flags != null && (flags & EXPLOSIVE) != 0;
    }

    static boolean isExplosiveMaterial(Material material) {
        if (material == null) return false;
        Integer flags = combustionFlags(material.getName());
        return flags == null ? material.hasFlags(MaterialFlags.EXPLOSIVE) : (flags & EXPLOSIVE) != 0;
    }

    static boolean shouldBurn(Material material, long temperature) {
        return material != null && shouldBurn(material.getName(), temperature,
                material.hasFlags(MaterialFlags.FLAMMABLE));
    }

    /** Unknown host materials keep their explicit host flag; known GT6 tags take priority. */
    static boolean shouldBurn(String name, long temperature, boolean hostFlammable) {
        Integer flags = combustionFlags(name);
        boolean flammable = flags == null ? hostFlammable : (flags & FLAMMABLE) != 0;
        return temperature > 313 && flammable &&
                !GT6ElementPhaseData.hasMeltingFlag(name) &&
                !GT6DeclaredPhaseData.hasBurningExemption(name) &&
                !GT6InheritedBurningExemptions.contains(name) &&
                !CrucibleSmeltingRule.hasMeltingFlag(name);
    }

    static boolean isAcidMaterial(Material material) {
        if (material == null) return false;
        if (isAcid(material.getName())) return true;
        if (!material.hasProperty(PropertyKey.FLUID)) return false;
        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        // Contents store material identity, not their historical input phase.
        // A primary-key change must not hide a registered acid attribute.
        return hasAcidAttribute(property.get(FluidStorageKeys.MOLTEN)) ||
                hasAcidAttribute(property.get(FluidStorageKeys.LIQUID)) ||
                hasAcidAttribute(property.get(FluidStorageKeys.GAS)) ||
                hasAcidAttribute(property.get(FluidStorageKeys.PLASMA)) ||
                hasAcidAttribute(primaryFluid(property));
    }

    private static boolean hasAcidAttribute(Fluid fluid) {
        return fluid instanceof AttributedFluid &&
                ((AttributedFluid) fluid).getAttributes().contains(FluidAttributes.ACID);
    }

    /** CEu fallback only when no authoritative boiling point is known.
     * A registered gas/plasma variant does not make a condensed material a gas.
     * Do not fabricate a boiling temperature from any of these fluid bindings.
     */
    static boolean isGasOnlyMaterial(Material material) {
        if (material == null || !material.hasProperty(PropertyKey.FLUID)) return false;
        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        if (isLiquid(property.get(FluidStorageKeys.MOLTEN)) ||
                isLiquid(property.get(FluidStorageKeys.LIQUID))) return false;
        FluidStorageKey primary = property.getPrimaryKey();
        Fluid primaryFluid = primaryFluid(property);
        if (primary != FluidStorageKeys.GAS && primary != FluidStorageKeys.PLASMA &&
                primaryFluid != property.get(FluidStorageKeys.GAS) &&
                primaryFluid != property.get(FluidStorageKeys.PLASMA) &&
                isLiquid(primaryFluid)) return false;
        // Standard storage keys also identify plain Forge fluids which do
        // not implement CEu's AttributedFluid interface.
        return property.get(FluidStorageKeys.GAS) != null ||
                property.get(FluidStorageKeys.PLASMA) != null ||
                (primaryFluid != null && (primaryFluid.isGaseous() ||
                        (primaryFluid instanceof AttributedFluid &&
                                ((AttributedFluid) primaryFluid).getState() != FluidState.LIQUID)));
    }

    private static Fluid primaryFluid(FluidProperty property) {
        FluidStorageKey primary = property.getPrimaryKey();
        return primary == null ? null : property.get(primary);
    }

    private static boolean isLiquid(Fluid fluid) {
        return fluid != null && !fluid.isGaseous() &&
                (!(fluid instanceof AttributedFluid) || ((AttributedFluid) fluid).getState() == FluidState.LIQUID);
    }

    /** MT.java gasacid/lqudacid factories, fluorite(), and explicit ACID declarations.
     * This is GT6 gameplay data: notably ammonia is tagged ACID, not a claim
     * that ammonia is chemically acidic. Host fluid attributes remain a separate source.
     */
    static boolean isAcid(String name) {
        if (name == null) return false;
        String normalized = name.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        switch (normalized) {
            // MT.java:1020-1054.
            case "hydrochloricacid":
            case "hydrogenfluoride":
            case "ammonia":
            case "nitricacid":
            case "hydrosulfuricacid":
            case "sulfuricacid":
            case "sulphuricacid": // Explicit MT.java:1047 ore alias.
            case "disulfuricacid":
            case "hexafluorosilicicacid":
            // Verified host identifiers for the same HF/H2S compounds.
            case "hydrofluoricacid":
            case "hydrogensulfide":
            // MT.java:1036,1078,1082,1087.
            case "carbontrioxide":
            case "tungsticacid":
            case "aluminiumfluoride":
            case "aluminumfluoride":
            case "titaniumtetrachloride":
            // MT.java:1163-1188, including explicit ore aliases.
            case "chloroauricacid":
            case "chloroplatinicacid":
            case "stannicchloride":
            case "blackvitriol":
            case "bluevitriol":
            case "romanvitriol":
            case "cyprusvitriol":
            case "solutionbluevitriol":
            case "greenvitriol":
            case "redvitriol":
            case "pinkvitriol":
            case "cyanvitriol":
            case "solutionnickelsulfate":
            case "solutionnickelsulphate":
            case "whitevitriol":
            case "grayvitriol":
            case "martianvitriol":
            case "vitriolofclay":
            case "aquaregia":
            // MT.java:215,1109-1118: all defined fluorite variants inherit ACID.
            case "fluorite":
            case "redfluorite":
            case "pinkfluorite":
            case "bluefluorite":
            case "greenfluorite":
            case "blackfluorite":
            case "whitefluorite":
            case "yellowfluorite":
            case "orangefluorite":
            case "magentafluorite": return true;
            default: return false;
        }
    }
}
