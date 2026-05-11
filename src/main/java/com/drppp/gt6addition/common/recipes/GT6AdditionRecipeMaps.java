package com.drppp.gt6addition.common.recipes;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMapBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;

public final class GT6AdditionRecipeMaps {

    public static final RecipeMap<SimpleRecipeBuilder> LASER_WELDER_RECIPES =
            new RecipeMapBuilder<>("laser_welder", new SimpleRecipeBuilder())
                    .itemInputs(1)
                    .itemOutputs(1)
                    .progressBar(GuiTextures.PROGRESS_BAR_CIRCUIT)
                    .itemSlotOverlay(GuiTextures.IN_SLOT_OVERLAY, false)
                    .itemSlotOverlay(GuiTextures.OUT_SLOT_OVERLAY, true)
                    .sound(SoundEvents.BLOCK_ANVIL_USE)
                    .build();

    private static boolean initialized;

    private GT6AdditionRecipeMaps() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        registerLaserWelderRecipes();
    }

    private static void registerLaserWelderRecipes() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            ItemStack screw = OreDictUnifier.get(OrePrefix.screw, material, 4);
            ItemStack stick = OreDictUnifier.get(OrePrefix.stick, material, 1);
            ItemStack longStick = OreDictUnifier.get(OrePrefix.stickLong, material, 1);

            if (!screw.isEmpty() && !stick.isEmpty()) {
                LASER_WELDER_RECIPES.recipeBuilder()
                        .inputs(screw)
                        .outputs(stick.copy())
                        .duration(20)
                        .EUt(16)
                        .buildAndRegister();
            }

            if (!stick.isEmpty() && !longStick.isEmpty()) {
                LASER_WELDER_RECIPES.recipeBuilder()
                        .inputs(OreDictUnifier.get(OrePrefix.stick, material, 2))
                        .outputs(longStick.copy())
                        .duration(20)
                        .EUt(16)
                        .buildAndRegister();
            }
        }
    }
}
