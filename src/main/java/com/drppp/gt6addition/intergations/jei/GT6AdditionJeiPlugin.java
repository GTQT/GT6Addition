package com.drppp.gt6addition.intergations.jei;

import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCoolingMold;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.drppp.gt6addition.intergations.jei.crucible.CrucibleJeiCategory;
import com.drppp.gt6addition.intergations.jei.crucible.CrucibleJeiRecipe;
import com.drppp.gt6addition.intergations.jei.crucible.CrucibleJeiRecipeMaker;
import gregtech.api.recipes.RecipeMaps;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

import java.util.List;

@JEIPlugin
public class GT6AdditionJeiPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new CrucibleJeiCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        List<CrucibleJeiRecipe> crucibleRecipes = CrucibleJeiRecipeMaker.createRecipes();
        registry.addRecipes(crucibleRecipes, CrucibleJeiCategory.UID);
        for (MetaTileEntityCrucible crucible : MetaTileEntityHandler.CRUCIBLE_HU) {
            if (crucible != null) {
                registry.addRecipeCatalyst(crucible.getStackForm(), CrucibleJeiCategory.UID);
            }
        }
        String solidifierUid = RecipeMaps.FLUID_SOLIDFICATION_RECIPES.getPrimaryRecipeCategory().getUniqueID();
        for (MetaTileEntityCoolingMold coolingMold : MetaTileEntityHandler.COOLING_MOLDS) {
            if (coolingMold != null) {
                registry.addRecipeCatalyst(coolingMold.getStackForm(), solidifierUid);
            }
        }
    }
}
