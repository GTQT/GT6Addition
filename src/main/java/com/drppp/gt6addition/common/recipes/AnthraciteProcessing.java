package com.drppp.gt6addition.common.recipes;

import com.drppp.gt6addition.GT6AdditionMain;
import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.material.GT6MachineMaterials;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

/** Adds anthracite processing variants while preserving CEu's base recipes and byproduct behavior. */
public final class AnthraciteProcessing {
    private static boolean cokingRecipesRegistered;

    private AnthraciteProcessing() {}

    public static void register() {
        ResourceLocation key = new ResourceLocation(Tags.MOD_ID, "anthracite_byproducts");
        RecipeMaps.MACERATOR_RECIPES.onRecipeBuild(key, AnthraciteProcessing::adjust);
        RecipeMaps.ORE_WASHER_RECIPES.onRecipeBuild(key, AnthraciteProcessing::adjust);
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.onRecipeBuild(key, AnthraciteProcessing::adjust);
        RecipeMaps.CENTRIFUGE_RECIPES.onRecipeBuild(key, AnthraciteProcessing::adjust);
    }

    /** Registers anthracite equivalents of CEu's coal coking recipes after the base recipe maps are loaded. */
    public static void registerCokingRecipes() {
        if (cokingRecipesRegistered) return;
        cokingRecipesRegistered = true;

        Material anthracite = GT6MachineMaterials.ANTHRACITE;
        if (anthracite == null) {
            GT6AdditionMain.LOGGER.warn("Anthracite is unavailable; its Coke Oven and Pyrolyse Oven recipes were not registered.");
            return;
        }
        ItemStack anthraciteGem = OreDictUnifier.get(OrePrefix.gem, anthracite, 1);
        if (anthraciteGem.isEmpty()) {
            GT6AdditionMain.LOGGER.warn("Anthracite has no registered gem form; its Coke Oven and Pyrolyse Oven recipes were not registered.");
            return;
        }

        Recipe cokeRecipe = findCoalCokeOvenRecipe();
        if (cokeRecipe == null) {
            GT6AdditionMain.LOGGER.warn("Could not uniquely identify CEu's coal Coke Oven recipe; the anthracite recipe was not registered.");
        } else {
            int coalInputIndex = findOreInputIndex(cokeRecipe, "gemCoal");
            registerAnthraciteVariant(RecipeMaps.COKE_OVEN_RECIPES, cokeRecipe, coalInputIndex, anthraciteGem);
        }

        Recipe pyrolysisRecipe = findCoalPyrolysisRecipe();
        if (pyrolysisRecipe == null) {
            GT6AdditionMain.LOGGER.warn("Could not uniquely identify CEu's coal Pyrolyse Oven recipe; the anthracite recipe was not registered.");
        } else {
            int coalInputIndex = findVanillaCoalInputIndex(pyrolysisRecipe);
            registerAnthraciteVariant(RecipeMaps.PYROLYSE_RECIPES, pyrolysisRecipe, coalInputIndex, anthraciteGem);
        }
    }

    private static Recipe findCoalCokeOvenRecipe() {
        Recipe match = null;
        for (Recipe recipe : RecipeMaps.COKE_OVEN_RECIPES.getRecipeList()) {
            int coalInputIndex = findOreInputIndex(recipe, "gemCoal");
            if (coalInputIndex < 0 || recipe.getInputs().get(coalInputIndex).getAmount() != 1 ||
                    !hasMaterialOutput(recipe, OrePrefix.gem, Materials.Coke) ||
                    !hasFluidOutput(recipe, Materials.Creosote)) {
                continue;
            }
            if (match != null) return null;
            match = recipe;
        }
        return match;
    }

    private static Recipe findCoalPyrolysisRecipe() {
        Recipe match = null;
        for (Recipe recipe : RecipeMaps.PYROLYSE_RECIPES.getRecipeList()) {
            if (findVanillaCoalInputIndex(recipe) < 0 || !hasFluidOutput(recipe, Materials.CoalTar)) {
                continue;
            }
            if (match != null) return null;
            match = recipe;
        }
        return match;
    }

    private static int findOreInputIndex(Recipe recipe, String oreName) {
        for (int i = 0; i < recipe.getInputs().size(); i++) {
            GTRecipeInput input = recipe.getInputs().get(i);
            if (input.isOreDict() && oreName.equals(OreDictionary.getOreName(input.getOreDict()))) {
                return i;
            }
        }
        return -1;
    }

    private static int findVanillaCoalInputIndex(Recipe recipe) {
        for (int i = 0; i < recipe.getInputs().size(); i++) {
            GTRecipeInput input = recipe.getInputs().get(i);
            if (input.isOreDict()) continue;
            for (ItemStack stack : input.getInputStacks()) {
                if (!stack.isEmpty() && stack.getItem() == Items.COAL && stack.getMetadata() == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static boolean hasMaterialOutput(Recipe recipe, OrePrefix prefix, Material material) {
        for (ItemStack output : recipe.getOutputs()) {
            MaterialStack outputMaterial = OreDictUnifier.getMaterial(output);
            if (outputMaterial != null && outputMaterial.material == material &&
                    OreDictUnifier.getPrefix(output) == prefix) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasFluidOutput(Recipe recipe, Material material) {
        FluidStack expected = material.getFluid(1);
        if (expected == null) return false;
        for (FluidStack output : recipe.getFluidOutputs()) {
            if (output.isFluidEqual(expected)) return true;
        }
        return false;
    }

    private static void registerAnthraciteVariant(RecipeMap<?> recipeMap, Recipe sourceRecipe,
                                                   int coalInputIndex, ItemStack anthraciteGem) {
        if (coalInputIndex < 0 || coalInputIndex >= sourceRecipe.getInputs().size() ||
                sourceRecipe.getDuration() < 4 || sourceRecipe.getDuration() % 4 != 0) {
            GT6AdditionMain.LOGGER.warn("CEu coal recipe cannot be copied exactly for anthracite; leaving it unchanged.");
            return;
        }

        int inputAmount = sourceRecipe.getInputs().get(coalInputIndex).getAmount();
        ItemStack anthraciteInput = anthraciteGem.copy();
        anthraciteInput.setCount(inputAmount);

        RecipeBuilder<?> builder = recipeMap.recipeBuilder();
        builder.append(sourceRecipe, 1, false);
        builder.getInputs().set(coalInputIndex, new GTRecipeItemInput(anthraciteInput));
        builder.duration(sourceRecipe.getDuration() / 4);
        builder.buildAndRegister();
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
