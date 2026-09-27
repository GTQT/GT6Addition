package com.drppp.gt6addition.common.recipes;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.material.GT6MachineMaterials;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** Retain CEu's processing recipes, changing only anthracite's coal byproducts. */
public final class AnthraciteProcessing {
    private AnthraciteProcessing() {}

    public static void register() {
        ResourceLocation key = new ResourceLocation(Tags.MOD_ID, "anthracite_byproducts");
        RecipeMaps.MACERATOR_RECIPES.onRecipeBuild(key, AnthraciteProcessing::adjust);
        RecipeMaps.ORE_WASHER_RECIPES.onRecipeBuild(key, AnthraciteProcessing::adjust);
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.onRecipeBuild(key, AnthraciteProcessing::adjust);
        RecipeMaps.CENTRIFUGE_RECIPES.onRecipeBuild(key, AnthraciteProcessing::adjust);
    }

    private static void adjust(RecipeBuilder<?> builder) {
        boolean anthracite = false;
        for (GTRecipeInput input : builder.getInputs()) {
            for (ItemStack stack : input.getInputStacks()) {
                MaterialStack material = OreDictUnifier.getMaterial(stack);
                if (material != null && material.material == GT6MachineMaterials.ANTHRACITE) {
                    anthracite = true;
                    break;
                }
            }
        }
        if (!anthracite) return;
        for (int i = 0; i < builder.getChancedOutputs().size(); i++) {
            ChancedItemOutput output = builder.getChancedOutputs().get(i);
            MaterialStack material = OreDictUnifier.getMaterial(output.getIngredient());
            if (material != null && material.material == Materials.Coal) {
                builder.getChancedOutputs().set(i, new ChancedItemOutput(
                        OreDictUnifier.get(OrePrefix.dust, Materials.Coal, output.getIngredient().getCount()),
                        5000, 0));
            }
        }
    }
}
