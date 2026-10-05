package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** Per-item material data from GT6 LoaderItemData, not food healing values. */
final class CrucibleFoodInput {
    private static final Rule CONFLICT = new Rule(new String[0], new long[0]);

    private CrucibleFoodInput() {}

    /** Null means unhandled; a recognized but unresolved rule must not fall back. */
    static Rule find(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem().getRegistryName() == null) return null;
        Rule explicit = vanilla(stack.getItem().getRegistryName().toString(), stack.getMetadata());
        if (explicit != null) return explicit;
        Rule selected = null;
        for (int id : OreDictionary.getOreIDs(stack)) {
            Rule candidate = oreName(OreDictionary.getOreName(id));
            if (candidate == null) continue;
            if (selected != null && !selected.sameContents(candidate)) return CONFLICT;
            selected = candidate;
        }
        return selected;
    }

    static Rule vanilla(String itemName, int metadata) {
        if (itemName == null) return null;
        // LoaderItemData.java:2589-2600,2664. Meat/cooked-fish entries use
        // wildcard metadata; fish has four vanilla overrides and a fallback.
        switch (itemName) {
            case "minecraft:porkchop":
            case "minecraft:beef":
            case "minecraft:chicken": return meat("gt6addition:meat_raw");
            case "minecraft:cooked_porkchop":
            case "minecraft:cooked_beef":
            case "minecraft:cooked_chicken": return meat("gt6addition:meat_cooked");
            case "minecraft:rotten_flesh": return meat("gt6addition:meat_rotten");
            case "minecraft:cooked_fish": return meat("gt6addition:fish_cooked");
            case "minecraft:fish":
                return new Rule(new String[]{"gt6addition:fish_raw", "gregtech:bone", "gregtech:fish_oil"},
                        new long[]{metadata == 3 ? GTValues.M : 2 * GTValues.M,
                                metadata == 3 ? GTValues.M / 3 : GTValues.M / 9,
                                (metadata == 0 ? 2 : metadata == 1 ? 4 : 1) * GTValues.M});
            // LoaderOreDictReRegistrations.java:1433,1440,1457 and
            // LoaderItemList.Bale_Wheat: seeds 1/9 U, crop 1 U, bale 9 U.
            case "minecraft:wheat": return one("gregtech:wheat", GTValues.M);
            case "minecraft:wheat_seeds": return one("gregtech:wheat", GTValues.M / 9);
            case "minecraft:hay_block": return one("gregtech:wheat", 9 * GTValues.M);
            case "minecraft:potato": return one("gt6addition:potato", GTValues.M);
            default: return null;
        }
    }

    static Rule oreName(String name) {
        if (name == null) return null;
        switch (name) {
            case "wheat": // Explicit bidirectional aliases at :146-147.
            case "itemWheat":
            case "cropWheat": return one("gregtech:wheat", GTValues.M);
            case "seedWheat": return one("gregtech:wheat", GTValues.M / 9);
            case "baleWheat": return one("gregtech:wheat", 9 * GTValues.M);
            // LoaderOreDictReRegistrations.java:1434-1445,1458-1464.
            case "seedBarley": return one("gt6addition:barley", GTValues.M / 9);
            case "cropBarley": return one("gt6addition:barley", GTValues.M);
            case "baleBarley": return one("gt6addition:barley", 9 * GTValues.M);
            case "seedRye": return one("gt6addition:rye", GTValues.M / 9);
            case "cropRye": return one("gt6addition:rye", GTValues.M);
            case "baleRye": return one("gt6addition:rye", 9 * GTValues.M);
            case "seedRice": return one("gt6addition:rice", GTValues.M / 9);
            case "cropRice": return one("gt6addition:rice", GTValues.M);
            case "baleRice": return one("gt6addition:rice", 9 * GTValues.M);
            case "seedOats": return one("gt6addition:oat", GTValues.M / 9);
            case "cropOats": return one("gt6addition:oat", GTValues.M);
            case "baleOats": return one("gt6addition:oat", 9 * GTValues.M);
            case "seedAbyssalOats": return one("gt6addition:abyssal_oat", GTValues.M / 9);
            case "cropAbyssalOats": return one("gt6addition:abyssal_oat", GTValues.M);
            case "baleAbyssalOats": return one("gt6addition:abyssal_oat", 9 * GTValues.M);
            case "seedCorn": return one("gt6addition:corn", GTValues.M / 9);
            case "cropCorn": return one("gt6addition:corn", GTValues.M);
            case "cropPotato": return one("gt6addition:potato", GTValues.M);
            // :158-159 maps these foods to dustTofu/ingotTofu (one U).
            case "foodSilkentofu":
            case "foodFirmtofu": return one("gt6addition:tofu", GTValues.M);
            default: return null;
        }
    }

    private static Rule meat(String material) {
        return new Rule(new String[]{material, "gregtech:bone"}, new long[]{2 * GTValues.M, GTValues.M / 9});
    }

    private static Rule one(String material, long amount) {
        return new Rule(new String[]{material}, new long[]{amount});
    }

    static final class Rule {
        final String[] materials;
        final long[] amounts;

        private Rule(String[] materials, long[] amounts) {
            this.materials = materials;
            this.amounts = amounts;
        }

        boolean sameContents(Rule other) {
            return other != null && Arrays.equals(materials, other.materials) && Arrays.equals(amounts, other.amounts);
        }

        List<MaterialStack> resolve(Function<String, Material> resolver) {
            List<MaterialStack> result = new ArrayList<>();
            for (int i = 0; i < materials.length; i++) {
                Material material = resolver.apply(materials[i]);
                if (material == null || material == Materials.NULL) return Collections.emptyList();
                result.add(new MaterialStack(material, amounts[i]));
            }
            return result;
        }
    }
}
