package com.drppp.gt6addition.common.item;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.material.GT6MaterialCompatibility;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/** Extra GT6 casting form without adding an incompatible IngotProperty to a gem. */
public final class GT6CastingIngotItem extends MetaPrefixItem {
    private static GT6CastingIngotItem fluoriteIngot;
    private final Material castingMaterial;

    private GT6CastingIngotItem(Material material) {
        super(material.getRegistry(), OrePrefix.ingot);
        castingMaterial = material;
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "fluorite_ingot"));
    }

    /** PostMaterialEvent is before CEu registers MetaItems and their OreDict entries. */
    public static void registerFluorite() {
        Material material = GT6MaterialCompatibility.findExternal("fluorite");
        if (material != null && fluoriteIngot == null && !material.hasProperty(PropertyKey.INGOT)) {
            // MetaItem's constructor adds this to CEu's item/model/colour
            // lifecycle. Keep the ordinary ingot prefix and its exact one-M
            // amount, localized name and CEu ingot icon, without editing the
            // shared prefix's generation predicate or another mod's material.
            fluoriteIngot = new GT6CastingIngotItem(material);
        }
    }

    @Override
    public void registerSubItems() {
        addItem(0, new UnificationEntry(OrePrefix.ingot, castingMaterial).toString());
    }

    @Nullable
    @Override
    public Material getMaterial(ItemStack stack) {
        // Fixed metadata does not depend on the optional Core's material ID.
        return stack.getMetadata() == 0 ? castingMaterial : null;
    }

    @Override
    protected Material getMaterial(int metadata) {
        if (metadata != 0) throw new IllegalArgumentException("Unknown casting ingot metadata: " + metadata);
        return castingMaterial;
    }
}
