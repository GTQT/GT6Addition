package com.drppp.gt6addition.intergations.jei.crucible;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public class CrucibleJeiRecipe implements IRecipeWrapper {

    private final List<List<ItemStack>> inputs;
    private final FluidStack outputFluid;
    private final int temperature;
    private final boolean alloying;
    private final List<String> componentInfo;

    public CrucibleJeiRecipe(List<List<ItemStack>> inputs, FluidStack outputFluid, int temperature, boolean alloying) {
        this(inputs, outputFluid, temperature, alloying, Collections.emptyList());
    }

    public CrucibleJeiRecipe(List<List<ItemStack>> inputs, FluidStack outputFluid, int temperature, boolean alloying,
                             List<String> componentInfo) {
        this.inputs = inputs;
        this.outputFluid = outputFluid;
        this.temperature = temperature;
        this.alloying = alloying;
        this.componentInfo = componentInfo;
    }

    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, inputs);
        ingredients.setOutput(FluidStack.class, outputFluid);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String typeKey = alloying ? "gt6addition.jei.crucible.alloying" : "gt6addition.jei.crucible.melting";
        minecraft.fontRenderer.drawString(I18n.format(typeKey), 3, 58, 0x404040);
        minecraft.fontRenderer.drawString(I18n.format("gt6addition.jei.crucible.temperature", temperature), 3, 70, 0x404040);
        if (alloying) {
            drawComponentInfo(minecraft);
        }
    }

    private void drawComponentInfo(Minecraft minecraft) {
        String text = componentInfo.isEmpty() ?
                I18n.format("gt6addition.jei.crucible.alloy_note") :
                I18n.format("gt6addition.jei.crucible.components", String.join(" + ", componentInfo));
        List<String> lines = minecraft.fontRenderer.listFormattedStringToWidth(text, 92);
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            minecraft.fontRenderer.drawString(lines.get(i), 66, 58 + i * 10, 0x606060);
        }
    }
}
