package com.drppp.gt6addition.common.material;

import com.drppp.gt6addition.Tags;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.util.ResourceLocation;

/** Anthracite is the sole retained addon material. Never register missing compatibility targets here. */
public final class GT6MachineMaterials {
    public static final int ANTHRACITE_BURN_TIME = 3200;
    public static Material ANTHRACITE;

    private GT6MachineMaterials() {}

    public static void register() {
        ANTHRACITE = new Material.Builder(8362, new ResourceLocation(Tags.MOD_ID, "anthracite"))
                .gem().dust().ore(2, 1).burnTime(ANTHRACITE_BURN_TIME).color(90, 90, 90)
                .iconSet(MaterialIconSet.LIGNITE)
                .flags(MaterialFlags.FLAMMABLE, MaterialFlags.NO_SMELTING, MaterialFlags.NO_SMASHING,
                        MaterialFlags.MORTAR_GRINDABLE, MaterialFlags.EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        MaterialFlags.DISABLE_DECOMPOSITION)
                .components(new MaterialStack(Materials.Carbon, 1)).build();
        ANTHRACITE.setFormula("C", true);
        ANTHRACITE.getProperty(PropertyKey.ORE).setOreByProducts(Materials.Coal);
    }
}
