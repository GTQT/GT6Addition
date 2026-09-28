package com.drppp.gt6addition.common.recipes;

import com.drppp.gt6addition.common.fluid.GT6PotionFluids;
import gregtech.api.recipes.RecipeMaps;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public final class GT6AdditionPotionRecipes {

    private static final int BREWING_DURATION = 128;
    private static final int CANNING_DURATION = 16;
    private static boolean initialized;

    private GT6AdditionPotionRecipes() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        registerPotionContainerRecipes();
        registerPotionFormConversions();
        registerVanillaPotionConversions();
    }

    private static void registerPotionContainerRecipes() {
        for (GT6PotionFluids.PotionDefinition definition : GT6PotionFluids.getDefinitions()) {
            PotionType potionType = definition.getPotionType();
            if (potionType == null) continue;

            for (GT6PotionFluids.PotionForm form : GT6PotionFluids.PotionForm.values()) {
                Fluid fluid = GT6PotionFluids.getFluid(potionType, form);
                if (fluid == null) continue;

                ItemStack potion = makePotionStack(form, potionType);
                RecipeMaps.CANNER_RECIPES.recipeBuilder()
                        .inputs(new ItemStack(Items.GLASS_BOTTLE))
                        .fluidInputs(new FluidStack(fluid, GT6PotionFluids.POTION_VOLUME))
                        .outputs(potion.copy())
                        .duration(CANNING_DURATION)
                        .EUt(4)
                        .buildAndRegister();

                RecipeMaps.CANNER_RECIPES.recipeBuilder()
                        .inputs(potion)
                        .outputs(new ItemStack(Items.GLASS_BOTTLE))
                        .fluidOutputs(new FluidStack(fluid, GT6PotionFluids.POTION_VOLUME))
                        .duration(CANNING_DURATION)
                        .EUt(4)
                        .buildAndRegister();
            }
        }
    }

    private static ItemStack makePotionStack(GT6PotionFluids.PotionForm form, PotionType potionType) {
        Item item;
        switch (form) {
            case SPLASH:
                item = Items.SPLASH_POTION;
                break;
            case LINGERING:
                item = Items.LINGERING_POTION;
                break;
            case DRINKABLE:
            default:
                item = Items.POTIONITEM;
                break;
        }
        return PotionUtils.addPotionToItemStack(new ItemStack(item), potionType);
    }

    private static void registerPotionFormConversions() {
        for (GT6PotionFluids.PotionDefinition definition : GT6PotionFluids.getDefinitions()) {
            PotionType potionType = definition.getPotionType();
            if (potionType == null) continue;

            addFormConversion(potionType, GT6PotionFluids.PotionForm.DRINKABLE,
                    GT6PotionFluids.PotionForm.SPLASH, Items.GUNPOWDER);
            addFormConversion(potionType, GT6PotionFluids.PotionForm.SPLASH,
                    GT6PotionFluids.PotionForm.LINGERING, Items.DRAGON_BREATH);
        }
    }

    private static void addFormConversion(PotionType potionType, GT6PotionFluids.PotionForm inputForm,
                                          GT6PotionFluids.PotionForm outputForm, Item reagent) {
        FluidStack input = GT6PotionFluids.getFluidStack(potionType, inputForm, GT6PotionFluids.POTION_VOLUME);
        FluidStack output = GT6PotionFluids.getFluidStack(potionType, outputForm, GT6PotionFluids.POTION_VOLUME);
        if (input == null || output == null) return;
        addBrewingRecipe(reagent, input, output);
    }

    private static void registerVanillaPotionConversions() {
        addMix(PotionTypes.WATER, Items.SPECKLED_MELON, PotionTypes.MUNDANE);
        addMix(PotionTypes.WATER, Items.GHAST_TEAR, PotionTypes.MUNDANE);
        addMix(PotionTypes.WATER, Items.RABBIT_FOOT, PotionTypes.MUNDANE);
        addMix(PotionTypes.WATER, Items.BLAZE_POWDER, PotionTypes.MUNDANE);
        addMix(PotionTypes.WATER, Items.SPIDER_EYE, PotionTypes.MUNDANE);
        addMix(PotionTypes.WATER, Items.SUGAR, PotionTypes.MUNDANE);
        addMix(PotionTypes.WATER, Items.MAGMA_CREAM, PotionTypes.MUNDANE);
        addMix(PotionTypes.WATER, Items.GLOWSTONE_DUST, PotionTypes.THICK);
        addMix(PotionTypes.WATER, Items.REDSTONE, PotionTypes.MUNDANE);
        addMix(PotionTypes.WATER, Items.NETHER_WART, PotionTypes.AWKWARD);

        addMix(PotionTypes.AWKWARD, Items.GOLDEN_CARROT, PotionTypes.NIGHT_VISION);
        addMix(PotionTypes.NIGHT_VISION, Items.REDSTONE, PotionTypes.LONG_NIGHT_VISION);
        addMix(PotionTypes.NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, PotionTypes.INVISIBILITY);
        addMix(PotionTypes.LONG_NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, PotionTypes.LONG_INVISIBILITY);
        addMix(PotionTypes.INVISIBILITY, Items.REDSTONE, PotionTypes.LONG_INVISIBILITY);

        addMix(PotionTypes.AWKWARD, Items.MAGMA_CREAM, PotionTypes.FIRE_RESISTANCE);
        addMix(PotionTypes.FIRE_RESISTANCE, Items.REDSTONE, PotionTypes.LONG_FIRE_RESISTANCE);
        addMix(PotionTypes.AWKWARD, Items.RABBIT_FOOT, PotionTypes.LEAPING);
        addMix(PotionTypes.LEAPING, Items.REDSTONE, PotionTypes.LONG_LEAPING);
        addMix(PotionTypes.LEAPING, Items.GLOWSTONE_DUST, PotionTypes.STRONG_LEAPING);
        addMix(PotionTypes.LEAPING, Items.FERMENTED_SPIDER_EYE, PotionTypes.SLOWNESS);
        addMix(PotionTypes.LONG_LEAPING, Items.FERMENTED_SPIDER_EYE, PotionTypes.LONG_SLOWNESS);
        addMix(PotionTypes.SLOWNESS, Items.REDSTONE, PotionTypes.LONG_SLOWNESS);
        addMix(PotionTypes.SWIFTNESS, Items.FERMENTED_SPIDER_EYE, PotionTypes.SLOWNESS);
        addMix(PotionTypes.LONG_SWIFTNESS, Items.FERMENTED_SPIDER_EYE, PotionTypes.LONG_SLOWNESS);
        addMix(PotionTypes.AWKWARD, Items.SUGAR, PotionTypes.SWIFTNESS);
        addMix(PotionTypes.SWIFTNESS, Items.REDSTONE, PotionTypes.LONG_SWIFTNESS);
        addMix(PotionTypes.SWIFTNESS, Items.GLOWSTONE_DUST, PotionTypes.STRONG_SWIFTNESS);

        addMix(PotionTypes.AWKWARD,
                new ItemStack(Items.FISH, 1, ItemFishFood.FishType.PUFFERFISH.getMetadata()),
                PotionTypes.WATER_BREATHING);
        addMix(PotionTypes.WATER_BREATHING, Items.REDSTONE, PotionTypes.LONG_WATER_BREATHING);
        addMix(PotionTypes.AWKWARD, Items.SPECKLED_MELON, PotionTypes.HEALING);
        addMix(PotionTypes.HEALING, Items.GLOWSTONE_DUST, PotionTypes.STRONG_HEALING);
        addMix(PotionTypes.HEALING, Items.FERMENTED_SPIDER_EYE, PotionTypes.HARMING);
        addMix(PotionTypes.STRONG_HEALING, Items.FERMENTED_SPIDER_EYE, PotionTypes.STRONG_HARMING);
        addMix(PotionTypes.HARMING, Items.GLOWSTONE_DUST, PotionTypes.STRONG_HARMING);
        addMix(PotionTypes.POISON, Items.FERMENTED_SPIDER_EYE, PotionTypes.HARMING);
        addMix(PotionTypes.LONG_POISON, Items.FERMENTED_SPIDER_EYE, PotionTypes.HARMING);
        addMix(PotionTypes.STRONG_POISON, Items.FERMENTED_SPIDER_EYE, PotionTypes.STRONG_HARMING);
        addMix(PotionTypes.AWKWARD, Items.SPIDER_EYE, PotionTypes.POISON);
        addMix(PotionTypes.POISON, Items.REDSTONE, PotionTypes.LONG_POISON);
        addMix(PotionTypes.POISON, Items.GLOWSTONE_DUST, PotionTypes.STRONG_POISON);
        addMix(PotionTypes.AWKWARD, Items.GHAST_TEAR, PotionTypes.REGENERATION);
        addMix(PotionTypes.REGENERATION, Items.REDSTONE, PotionTypes.LONG_REGENERATION);
        addMix(PotionTypes.REGENERATION, Items.GLOWSTONE_DUST, PotionTypes.STRONG_REGENERATION);
        addMix(PotionTypes.AWKWARD, Items.BLAZE_POWDER, PotionTypes.STRENGTH);
        addMix(PotionTypes.STRENGTH, Items.REDSTONE, PotionTypes.LONG_STRENGTH);
        addMix(PotionTypes.STRENGTH, Items.GLOWSTONE_DUST, PotionTypes.STRONG_STRENGTH);
        addMix(PotionTypes.WATER, Items.FERMENTED_SPIDER_EYE, PotionTypes.WEAKNESS);
        addMix(PotionTypes.WEAKNESS, Items.REDSTONE, PotionTypes.LONG_WEAKNESS);
    }

    private static void addMix(PotionType inputType, Item reagent, PotionType outputType) {
        addMix(inputType, new ItemStack(reagent), outputType);
    }

    private static void addMix(PotionType inputType, ItemStack reagent, PotionType outputType) {
        for (GT6PotionFluids.PotionForm form : GT6PotionFluids.PotionForm.values()) {
            FluidStack input = GT6PotionFluids.getFluidStack(inputType, form, GT6PotionFluids.POTION_VOLUME);
            FluidStack output = GT6PotionFluids.getFluidStack(outputType, form, GT6PotionFluids.POTION_VOLUME);
            if (input == null || output == null) continue;
            addBrewingRecipe(reagent, input, output);
        }
    }

    private static void addBrewingRecipe(Item reagent, FluidStack input, FluidStack output) {
        addBrewingRecipe(new ItemStack(reagent), input, output);
    }

    private static void addBrewingRecipe(ItemStack reagent, FluidStack input, FluidStack output) {
        RecipeMaps.BREWING_RECIPES.recipeBuilder()
                .inputs(reagent.copy())
                .fluidInputs(input)
                .fluidOutputs(output)
                .duration(BREWING_DURATION)
                .EUt(4)
                .buildAndRegister();
    }
}
