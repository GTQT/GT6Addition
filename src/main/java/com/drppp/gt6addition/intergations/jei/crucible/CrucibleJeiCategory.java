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
    public static final String ALLOY_UID = Tags.MOD_ID + ".crucible_alloying";

    private final IDrawable background;
    private final IDrawable slot;
    private final boolean alloying;

    public CrucibleJeiCategory(IGuiHelper guiHelper) {
        this(guiHelper, false);
    }

    public CrucibleJeiCategory(IGuiHelper guiHelper, boolean alloying) {
        this.alloying = alloying;
        this.background = guiHelper.createBlankDrawable(176, 138);
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public String getUid() {
        return alloying ? ALLOY_UID : UID;
    }

    @Override
    public String getTitle() {
        return I18n.format(alloying ? "gt6addition.jei.crucible.alloy_title" : "gt6addition.jei.crucible.title");
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
        minecraft.fontRenderer.drawString(I18n.format("gt6addition.jei.crucible.input"), 4, 4, 0x404040);
        minecraft.fontRenderer.drawString(I18n.format("gt6addition.jei.crucible.output"), 130, 4, 0x404040);
        minecraft.fontRenderer.drawString("->", 94, 43, 0x806040);
        net.minecraft.client.gui.Gui.drawRect(128, 19, 152, 79, 0xFF555555);
        net.minecraft.client.gui.Gui.drawRect(129, 20, 151, 78, 0xFFDDDDDD);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CrucibleJeiRecipe recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        for (int i = 0; i < recipeWrapper.getInputCount(); i++) {
            int x = 4 + (i % 3) * 22;
            int y = 20 + (i / 3) * 20;
            if (!recipeWrapper.getItemInputs(i).isEmpty()) {
                itemStacks.init(i, true, x, y);
                itemStacks.setBackground(i, slot);
                itemStacks.set(i, recipeWrapper.getItemInputs(i));
            }
            if (!recipeWrapper.getFluidInputs(i).isEmpty()) {
                int capacity = recipeWrapper.getFluidInputs(i).get(0).amount;
                // Fluid index 0 is reserved for the output. Rows retain their
                // component index even when preceding rows are item inputs.
                fluidStacks.init(i + 1, true, x + 1, y + 1, 16, 16, capacity, true, null);
                fluidStacks.setBackground(i + 1, slot);
                fluidStacks.set(i + 1, recipeWrapper.getFluidInputs(i));
            }
        }
        itemStacks.addTooltipCallback((index, input, stack, tooltip) -> {
            if (input && index < recipeWrapper.getComponentInfo().size()) {
                tooltip.add(I18n.format("gt6addition.jei.crucible.ratio_component",
                        recipeWrapper.getComponentInfo().get(index)));
            }
            if (input) tooltip.add(I18n.format("gt6addition.jei.crucible.alternatives"));
            else if (recipeWrapper.hasInternalOutput())
                tooltip.add(I18n.format("gt6addition.jei.crucible.internal_note"));
        });

        if (recipeWrapper.hasInternalOutput()) {
            if (!recipeWrapper.getOutputPreview().isEmpty()) {
                itemStacks.init(100, false, 131, 40);
                itemStacks.setBackground(100, slot);
                itemStacks.set(100, recipeWrapper.getOutputPreview());
            }
        } else {
            fluidStacks.init(0, false, 132, 23, 16, 52, recipeWrapper.getOutputFluid().amount, false, null);
            fluidStacks.set(0, recipeWrapper.getOutputFluid());
        }
        fluidStacks.addTooltipCallback((index, input, stack, tooltip) -> {
            int row = index - 1;
            if (input && row >= 0 && row < recipeWrapper.getComponentInfo().size()) {
                tooltip.add(I18n.format("gt6addition.jei.crucible.ratio_component",
                        recipeWrapper.getComponentInfo().get(row)));
            }
            if (input) tooltip.add(I18n.format("gt6addition.jei.crucible.fluid_input"));
        });
    }
}
