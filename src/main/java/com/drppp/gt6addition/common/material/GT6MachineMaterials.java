package com.drppp.gt6addition.common.material;

import com.drppp.gt6addition.Tags;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

public final class GT6MachineMaterials {
    static final String FORMULA_ARSENIC_COPPER = "Cu3As";
    static final String FORMULA_ARSENIC_BRONZE = "AsBronze4";
    static final String FORMULA_ANCIENT_DEBRIS = "AncientDebris";
    static final String FORMULA_NETHERITE = "Au4AncientDebris4";
    static final String FORMULA_TANTALUM_HAFNIUM_CARBIDE = "Ta4HfC5";

    public static Material ARSENIC_COPPER;
    public static Material ARSENIC_BRONZE;
    public static Material ANCIENT_DEBRIS;
    public static Material NETHERITE;
    public static Material TANTALUM_HAFNIUM_CARBIDE;

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
    }

    static ComponentPart[] arsenicCopperFormula() {
        return new ComponentPart[]{new ComponentPart(Component.COPPER, 3), new ComponentPart(Component.ARSENIC, 1)};
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
