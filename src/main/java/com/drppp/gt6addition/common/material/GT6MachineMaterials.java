package com.drppp.gt6addition.common.material;

import com.drppp.gt6addition.Tags;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

public final class GT6MachineMaterials {
    public static final int ANTHRACITE_BURN_TIME = 3200;
    static final String FORMULA_ARSENIC_COPPER = "Cu3As";
    static final String FORMULA_ARSENIC_BRONZE = "AsBronze4";
    static final String FORMULA_ANCIENT_DEBRIS = "AncientDebris";
    static final String FORMULA_NETHERITE = "Au4AncientDebris4";
    static final String FORMULA_TANTALUM_HAFNIUM_CARBIDE = "Ta4HfC5";
    static final String FORMULA_ANTHRACITE = "C";

    public static Material ARSENIC_COPPER;
    public static Material ARSENIC_BRONZE;
    public static Material ANCIENT_DEBRIS;
    public static Material NETHERITE;
    public static Material TANTALUM_HAFNIUM_CARBIDE;
    public static Material ANTHRACITE;
    public static Material MEAT_RAW;
    public static Material MEAT_COOKED;
    public static Material MEAT_ROTTEN;
    public static Material SOYLENT_GREEN;
    public static Material SNOW;
    public static Material DURANIUM_ALLOY;
    public static Material TRITANIUM_ALLOY;
    public static Material PHOSPHORUS_BLUE;
    public static Material PHOSPHORUS_RED;
    public static Material PHOSPHORUS_WHITE;
    public static Material PHOSPHORUS_FAMILY;
    public static Material MAGIC_IRON_FAMILY;
    public static Material WOOD_PLASTIC_FAMILY;
    public static Material GARNET_FAMILY;
    public static Material JASPER_FAMILY;
    public static Material TIGER_EYE_FAMILY;
    public static Material AVENTURINE_FAMILY;
    public static Material AMBER_FAMILY;
    public static Material WAX;
    public static Material SAND;
    public static Material BLAZE_FAMILY;
    public static Material PRISMARINE_LIGHT;
    public static Material PRISMARINE_DARK;
    public static Material PRISMARINE_FAMILY;
    public static Material WHEAT;
    public static Material BARLEY;
    public static Material RYE;
    public static Material RICE;
    public static Material OAT;
    public static Material ABYSSAL_OAT;
    public static Material CORN;
    public static Material POTATO;
    public static Material GRAINS_FAMILY;
    public static Material FISH_COOKED;
    public static Material FISH_RAW;
    public static Material FISH_ROTTEN;
    public static Material TOFU;
    public static Material THAUMIC_CRYSTAL_FAMILY;
    public static Material HEXORIUM_FAMILY;
    public static Material HEMATITE;
    public static Material FLUORITE;
    public static Material NIOBIUM_PENTOXIDE;
    public static Material COLUMBITE;
    public static Material ADAMANTIUM;
    public static Material ADAMANTINE;
    public static Material DOLAMIDE;

    private GT6MachineMaterials() {}

    public static void register() {
        // Use the stable GT6 material IDs instead of 0..4, which collide with
        // GTCE's built-in element/material IDs and corrupt formula displays.
        ARSENIC_COPPER = new Material.Builder(8614, id("arsenic_copper"))
                .ingot().fluid().color(210, 160, 60).iconSet(MaterialIconSet.METALLIC)
                .components(componentStacks(arsenicCopperFormula())).build();
        ARSENIC_COPPER.setFormula(FORMULA_ARSENIC_COPPER, true);
        ARSENIC_BRONZE = new Material.Builder(8615, id("arsenic_bronze"))
                .ingot().fluid().color(200, 200, 222).iconSet(MaterialIconSet.METALLIC)
                .components(componentStacks(arsenicBronzeFormula())).build();
        ARSENIC_BRONZE.setFormula(FORMULA_ARSENIC_BRONZE, true);
        // GT6's Netherite is an alloy of four gold units and four Ancient Debris units.
        // Keep Ancient Debris as its own material so the formula remains inspectable in JEI.
        ANCIENT_DEBRIS = new Material.Builder(8744, id("ancient_debris"))
                .ingot().fluid().color(110, 80, 90).iconSet(MaterialIconSet.METALLIC).blast(2046).build();
        ANCIENT_DEBRIS.setFormula(FORMULA_ANCIENT_DEBRIS, true);
        NETHERITE = new Material.Builder(8745, id("netherite"))
                .ingot().fluid().color(80, 70, 80).iconSet(MaterialIconSet.METALLIC).blast(2500)
                .components(componentStacks(netheriteFormula())).build();
        NETHERITE.setFormula(FORMULA_NETHERITE, true);
        TANTALUM_HAFNIUM_CARBIDE = new Material.Builder(8802, id("tantalum_hafnium_carbide"))
                .ingot().color(32, 128, 32).iconSet(MaterialIconSet.METALLIC).blast(4263)
                .components(componentStacks(tantalumHafniumCarbideFormula())).build();
        TANTALUM_HAFNIUM_CARBIDE.setFormula(FORMULA_TANTALUM_HAFNIUM_CARBIDE, true);
        // MT.java:1840-1841: separate alloys, not CEu's elemental materials.
        DURANIUM_ALLOY = new Material.Builder(8751, id("duranium_alloy"))
                .ingot().fluid().color(75, 175, 175).iconSet(MaterialIconSet.METALLIC)
                .components(new MaterialStack(Materials.Duranium, 7), new MaterialStack(Materials.Magnesium, 1))
                .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        TRITANIUM_ALLOY = new Material.Builder(8752, id("tritanium_alloy"))
                .ingot().fluid().color(55, 155, 155).iconSet(MaterialIconSet.METALLIC)
                .components(new MaterialStack(Materials.Tritanium, 3), new MaterialStack(Materials.Duranium, 1))
                .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        // MT.java:1295-1297: these are GT6's Ca3(PO4)2 materials, not
        // elemental phosphorus allotropes. ANY.Phosphorus has separate
        // MELTING semantics and therefore must retain a distinct identity.
        PHOSPHORUS_BLUE = phosphorusCompound(8458, "blue_phosphorus", 155, 227, 228);
        PHOSPHORUS_RED = phosphorusCompound(8459, "red_phosphorus", 119, 4, 14);
        PHOSPHORUS_WHITE = phosphorusCompound(8460, "white_phosphorus", 236, 234, 221);
        // Local technical ID: GT6 saves this negative-ID family by name.
        PHOSPHORUS_FAMILY = phosphorusCompound(30001, "any_phosphorus", 255, 255, 0);
        // ANY.java:124 and :145: distinct statistics and processing targets.
        // These local IDs identify technical families, not GT6 literal IDs.
        MAGIC_IRON_FAMILY = new Material.Builder(30002, id("any_magic_iron"))
                .ingot().color(110, 200, 250).iconSet(MaterialIconSet.METALLIC)
                .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        WOOD_PLASTIC_FAMILY = new Material.Builder(30003, id("any_wood_or_plastic"))
                .dust().color(102, 79, 47).iconSet(MaterialIconSet.WOOD)
                .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        // ANY.java:102-106 copies looks/stats, but not a member's targets.
        GARNET_FAMILY = gemFamily(30004, "any_garnet", MaterialIconSet.RUBY, 255, 100, 100);
        JASPER_FAMILY = gemFamily(30005, "any_jasper", MaterialIconSet.GLASS, 200, 80, 80);
        TIGER_EYE_FAMILY = gemFamily(30006, "any_tiger_eye", MaterialIconSet.GLASS, 142, 116, 61);
        AVENTURINE_FAMILY = gemFamily(30007, "any_aventurine", MaterialIconSet.GLASS, 66, 189, 133);
        AMBER_FAMILY = gemFamily(30008, "any_amber", MaterialIconSet.RUBY, 255, 180, 0);
        // ANY.java:134-135 only copies looks/statistics. No member targets,
        // Forge fluid, ore forms or dependency on Thaumcraft/Hexorium.
        THAUMIC_CRYSTAL_FAMILY = gemFamily(30012, "any_thaumic_crystal", MaterialIconSet.GLASS, 252, 252, 252);
        HEXORIUM_FAMILY = gemFamily(30013, "any_hexorium", MaterialIconSet.GLASS, 224, 224, 224);
        // MT.java:1262 and :1618: actual base materials used by ANY families.
        WAX = new Material.Builder(8235, id("wax")).ingot().dust()
                .color(250, 250, 250).iconSet(MaterialIconSet.FINE)
                .flags(gregtech.api.unification.material.info.MaterialFlags.MORTAR_GRINDABLE,
                        gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        SAND = new Material.Builder(8100, id("sand")).dust()
                .color(250, 250, 200).iconSet(MaterialIconSet.SAND)
                .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        // ANY.Blaze has default self targets, and no MT.Blaze fluid binding.
        BLAZE_FAMILY = new Material.Builder(30009, id("any_blaze")).dust()
                .color(255, 200, 0).iconSet(MaterialIconSet.FINE)
                .flags(gregtech.api.unification.material.info.MaterialFlags.MORTAR_GRINDABLE,
                        gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        // MT.java:1639-1640: separate colours, default 1000/3000 K and density 1.
        PRISMARINE_LIGHT = prismarine(9219, "prismarine", 110, 178, 165);
        PRISMARINE_DARK = prismarine(9220, "prismarine_dark", 88, 125, 108);
        PRISMARINE_FAMILY = gemFamily(30010, "any_prismarine", MaterialIconSet.LAPIS, 110, 178, 165);
        // GT6-style solid fuel form; world generation uses our own block, not CEu's ore-vein registry.
        ANTHRACITE = new Material.Builder(8362, id("anthracite"))
                .gem().dust().ore(2, 1).burnTime(ANTHRACITE_BURN_TIME).color(90, 90, 90).iconSet(MaterialIconSet.LIGNITE)
                .flags(gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE,
                        gregtech.api.unification.material.info.MaterialFlags.NO_SMELTING,
                        gregtech.api.unification.material.info.MaterialFlags.NO_SMASHING,
                        gregtech.api.unification.material.info.MaterialFlags.MORTAR_GRINDABLE,
                        gregtech.api.unification.material.info.MaterialFlags.EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION)
                .components(new MaterialStack(Materials.Carbon, 1)).build();
        ANTHRACITE.setFormula(FORMULA_ANTHRACITE, true);
        ANTHRACITE.getProperty(gregtech.api.unification.material.properties.PropertyKey.ORE)
                .setOreByProducts(Materials.Coal);

        // GT6 MT.java:1317-1319, 1332: distinct food materials, not CEu Meat.
        // Cooked meat must exist before raw meat's explicit smelting target.
        MEAT_COOKED = new Material.Builder(9701, id("meat_cooked"))
                .ingot().color(150, 60, 20).iconSet(MaterialIconSet.FINE).build();
        MEAT_RAW = new Material.Builder(9700, id("meat_raw"))
                .ingot().ingotSmeltInto(MEAT_COOKED)
                .color(255, 100, 100).iconSet(MaterialIconSet.FINE).build();
        MEAT_ROTTEN = new Material.Builder(9710, id("meat_rotten"))
                .ingot().color(255, 150, 100).iconSet(MaterialIconSet.FINE).build();
        // MT.java:1320-1323/1331 and ANY.java:111-113.
        FISH_COOKED = new Material.Builder(9711, id("fish_cooked"))
                .ingot().color(150, 120, 20).iconSet(MaterialIconSet.FINE).build();
        FISH_RAW = new Material.Builder(9712, id("fish_raw"))
                .ingot().ingotSmeltInto(FISH_COOKED).color(255, 150, 100).iconSet(MaterialIconSet.FINE).build();
        FISH_ROTTEN = new Material.Builder(9713, id("fish_rotten"))
                .ingot().color(220, 200, 100).iconSet(MaterialIconSet.FINE).build();
        // CEu already registers Wheat with the same dust/color identity.
        // OreDict entries do not carry namespaces: a second Wheat steals
        // dustWheat registrations and makes CEu's wheat_to_dust output empty.
        WHEAT = Materials.Wheat;
        WHEAT.addFlags(gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE,
                        gregtech.api.unification.material.info.MaterialFlags.MORTAR_GRINDABLE,
                        gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION);
        // MT.java:1324-1330. Potato is dustfood, not grain: setBurning
        // configures an output but does not add the FLAMMABLE material tag.
        BARLEY = foodPowder(9704, "barley", 196, 255, 196, true);
        RYE = foodPowder(9705, "rye", 255, 230, 180, true);
        RICE = foodPowder(9706, "rice", 252, 252, 240, true);
        OAT = foodPowder(9707, "oat", 240, 240, 222, true);
        ABYSSAL_OAT = foodPowder(9719, "abyssal_oat", 133, 62, 25, true);
        CORN = foodPowder(9708, "corn", 250, 240, 111, true);
        POTATO = foodPowder(9709, "potato", 240, 240, 164, false);
        // All three flour/grain families copy the same statistics, targets and
        // flags. Keep their common technical identity separate from Wheat.
        GRAINS_FAMILY = new Material.Builder(30011, id("any_grains"))
                .dust().color(255, 255, 196).iconSet(MaterialIconSet.FINE)
                .flags(gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE,
                        gregtech.api.unification.material.info.MaterialFlags.MORTAR_GRINDABLE,
                        gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        TOFU = new Material.Builder(9778, id("tofu"))
                .ingot().color(222, 222, 222).iconSet(MaterialIconSet.FINE).build();
        SOYLENT_GREEN = new Material.Builder(9779, id("soylent_green"))
                .ingot().color(0, 222, 0).iconSet(MaterialIconSet.FINE).build();
        // MT.java:1012: Snow has dust forms, H2O composition and density 1 g/cm^3.
        SNOW = new Material.Builder(9801, id("snow"))
                .dust().color(250, 250, 250).iconSet(MaterialIconSet.FINE)
                .components(new MaterialStack(Materials.Hydrogen, 2), new MaterialStack(Materials.Oxygen, 1))
                .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
    }

    static ComponentPart[] arsenicCopperFormula() {
        return new ComponentPart[]{new ComponentPart(Component.COPPER, 3), new ComponentPart(Component.ARSENIC, 1)};
    }

    /** Runs late in MaterialEvent, while registration is still open. */
    public static void registerCrucibleTargets() {
        // MT.java:1094 explicitly aliases Hematite to BandedIron. CEu's
        // BandedIron has the same Fe2O3 composition and 145/90/90 colour.
        HEMATITE = Materials.BandedIron;
        FLUORITE = existing("gtqtcore:fluorite", "gregtech:fluorite", "gt6addition:fluorite");
        if (FLUORITE == null) {
            FLUORITE = new Material.Builder(9215, id("fluorite"))
                    // CEu rejects simultaneous GemProperty/IngotProperty.
                    // Preserve Fluorite's gem identity and molten material;
                    // don't bypass its property validation to create ingots.
                    .gem().ore().color(225, 185, 140).iconSet(MaterialIconSet.RUBY)
                    .liquid(new FluidBuilder().temperature(1633).attribute(FluidAttributes.ACID))
                    .components(new MaterialStack(Materials.Calcium, 1), new MaterialStack(Materials.Fluorine, 2))
                    .flags(gregtech.api.unification.material.info.MaterialFlags.MORTAR_GRINDABLE,
                            gregtech.api.unification.material.info.MaterialFlags.CRYSTALLIZABLE,
                            gregtech.api.unification.material.info.MaterialFlags.NO_SMASHING,
                            gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        }
        // MT.java:1068/3744: use the actual 7:1 oxide mixture, not the mineral's
        // approximate formula comment or a direct conversion into niobium.
        NIOBIUM_PENTOXIDE = existing("gtqtcore:niobium_pentoxide", "gregtech:niobium_pentoxide",
                "gt6addition:niobium_pentoxide");
        if (NIOBIUM_PENTOXIDE == null) {
            NIOBIUM_PENTOXIDE = new Material.Builder(8461, id("niobium_pentoxide"))
                    .dust().ore().color(50, 64, 10).iconSet(MaterialIconSet.FINE)
                    .components(new MaterialStack(Materials.Niobium, 2), new MaterialStack(Materials.Oxygen, 5))
                    .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        }
        COLUMBITE = new Material.Builder(9246, id("columbite"))
                .dust().ore().color(65, 77, 14).iconSet(MaterialIconSet.METALLIC)
                .components(new MaterialStack(NIOBIUM_PENTOXIDE, 7), new MaterialStack(Materials.Pyrolusite, 1))
                .flags(gregtech.api.unification.material.info.MaterialFlags.MORTAR_GRINDABLE,
                        gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        // MT.java:794/1675: the fictional element and its oxide are distinct.
        ADAMANTIUM = new Material.Builder(2220, id("adamantium"))
                .ingot().ore(2, 1).color(255, 255, 255).iconSet(MaterialIconSet.SHINY)
                .liquid(new FluidBuilder().temperature(5225))
                .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        ADAMANTIUM.setFormula("Ad", true);
        ADAMANTINE = new Material.Builder(8784, id("adamantine"))
                .ingot().ore().color(255, 0, 64).iconSet(MaterialIconSet.METALLIC)
                .components(new MaterialStack(ADAMANTIUM, 3), new MaterialStack(Materials.Oxygen, 4))
                .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
        // MT.java:1842: no component configuration, heat override or MOLTEN.
        DOLAMIDE = new Material.Builder(8753, id("dolamide"))
                .dust().ore().color(188, 100, 122).iconSet(MaterialIconSet.METALLIC)
                .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
    }

    private static Material existing(String... registryNames) {
        for (String name : registryNames) {
            Material material = GregTechAPI.materialManager.getMaterial(name);
            // getRegistry falls back to gregtech when a namespace is absent.
            if (material != null && name.equals(material.getRegistryName())) return material;
        }
        return null;
    }

    private static Material foodPowder(int materialId, String name, int red, int green, int blue, boolean flammable) {
        Material.Builder builder = new Material.Builder(materialId, id(name))
                .dust().color(red, green, blue).iconSet(MaterialIconSet.FINE)
                .flags(gregtech.api.unification.material.info.MaterialFlags.MORTAR_GRINDABLE,
                        gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION);
        if (flammable) builder.flags(gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE);
        return builder.build();
    }

    private static Material phosphorusCompound(int materialId, String name, int red, int green, int blue) {
        Material.Builder builder = new Material.Builder(materialId, id(name))
                .gem().dust().color(red, green, blue).iconSet(MaterialIconSet.FLINT)
                .components(new MaterialStack(Materials.Calcium, 3),
                        new MaterialStack(Materials.Phosphorus, 2), new MaterialStack(Materials.Oxygen, 8))
                .flags(gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE,
                        gregtech.api.unification.material.info.MaterialFlags.EXPLOSIVE,
                        gregtech.api.unification.material.info.MaterialFlags.MORTAR_GRINDABLE,
                        gregtech.api.unification.material.info.MaterialFlags.NO_SMELTING,
                        gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION);
        // MT.java:1998-2000; no fabricated ore form for the technical family.
        if (materialId != 30001) builder.ore(3, 1);
        return builder.build();
    }

    private static Material gemFamily(int materialId, String name, MaterialIconSet icon, int red, int green, int blue) {
        // No chemical composition: stealStatsElement does not copy it in GT6.
        // No ore or Forge fluid is invented for these technical families.
        return new Material.Builder(materialId, id(name)).gem().dust().color(red, green, blue).iconSet(icon)
                .flags(gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
    }

    private static Material prismarine(int materialId, String name, int red, int green, int blue) {
        return new Material.Builder(materialId, id(name)).gem().dust().ore(1, 1)
                .color(red, green, blue).iconSet(MaterialIconSet.LAPIS)
                .flags(gregtech.api.unification.material.info.MaterialFlags.MORTAR_GRINDABLE,
                        gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION).build();
    }

    static ComponentPart[] arsenicBronzeFormula() {
        return new ComponentPart[]{new ComponentPart(Component.BRONZE, 4), new ComponentPart(Component.ARSENIC, 1)};
    }

    static ComponentPart[] netheriteFormula() {
        return new ComponentPart[]{new ComponentPart(Component.GOLD, 4), new ComponentPart(Component.ANCIENT_DEBRIS, 4)};
    }

    static ComponentPart[] tantalumHafniumCarbideFormula() {
        return new ComponentPart[]{new ComponentPart(Component.TANTALUM, 4),
                new ComponentPart(Component.HAFNIUM, 1), new ComponentPart(Component.CARBON, 5)};
    }

    private static MaterialStack[] componentStacks(ComponentPart[] formula) {
        return Arrays.stream(formula)
                .map(part -> new MaterialStack(componentMaterial(part.component), part.amount))
                .toArray(MaterialStack[]::new);
    }

    private static Material componentMaterial(Component component) {
        switch (component) {
            case COPPER: return Materials.Copper;
            case ARSENIC: return Materials.Arsenic;
            case BRONZE: return Materials.Bronze;
            case GOLD: return Materials.Gold;
            case ANCIENT_DEBRIS: return ANCIENT_DEBRIS;
            case TANTALUM: return Materials.Tantalum;
            case HAFNIUM: return Materials.Hafnium;
            case CARBON: return Materials.Carbon;
            default: throw new IllegalArgumentException("Unmapped GT6 composition component: " + component);
        }
    }

    enum Component {
        COPPER, ARSENIC, BRONZE, GOLD, ANCIENT_DEBRIS, TANTALUM, HAFNIUM, CARBON
    }

    static final class ComponentPart {
        final Component component;
        final long amount;

        ComponentPart(Component component, long amount) {
            this.component = component;
            this.amount = amount;
        }
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }
}
