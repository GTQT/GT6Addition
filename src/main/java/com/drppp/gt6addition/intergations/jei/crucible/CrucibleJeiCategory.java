package com.drppp.gt6addition.intergations.jei.crucible;

import com.drppp.gt6addition.Tags;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class CrucibleJeiCategory implements IRecipeCategory<CrucibleJeiRecipe> {

    public static final String UID = Tags.MOD_ID + ".crucible_smelting";
    private static final int INPUT_SLOT_COUNT = 6;

    private final IDrawable background;

    public CrucibleJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 84);
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("gt6addition.jei.crucible.title");
    }

    @Override
    public String getModName() {
        return Tags.MOD_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        minecraft.fontRenderer.drawString("->", 112, 27, 0x404040);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CrucibleJeiRecipe recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            itemStacks.init(i, true, 3 + (i % 3) * 20, 8 + (i / 3) * 20);
        }
        itemStacks.set(ingredients);

        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        fluidStacks.init(0, false, 134, 8, 16, 48, recipeWrapper.getOutputFluid().amount, false, null);
        fluidStacks.set(ingredients);
    }
}
