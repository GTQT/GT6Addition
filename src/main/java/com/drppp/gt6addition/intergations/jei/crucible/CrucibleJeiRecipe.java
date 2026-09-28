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

    public boolean isAlloying() { return alloying; }
    public int getInputCount() { return inputs.size(); }
    public List<String> getComponentInfo() { return componentInfo; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, inputs);
        ingredients.setOutput(FluidStack.class, outputFluid);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String typeKey = alloying ? "gt6addition.jei.crucible.alloying" : "gt6addition.jei.crucible.melting";
        drawLine(minecraft, I18n.format(typeKey) + " · " + outputFluid.getLocalizedName(), 84, 0x404040);
        drawLine(minecraft, I18n.format("gt6addition.jei.crucible.temperature", temperature), 97, 0xA04420);
        drawLine(minecraft, I18n.format("gt6addition.jei.crucible.yield", outputFluid.amount), 110, 0x404040);
        drawLine(minecraft, I18n.format(alloying ? "gt6addition.jei.crucible.ratio_hint" :
                "gt6addition.jei.crucible.melting_hint"), 123, 0x606060);
    }

    private void drawLine(Minecraft minecraft, String text, int y, int color) {
        if (minecraft.fontRenderer.getStringWidth(text) > 168) {
            text = minecraft.fontRenderer.trimStringToWidth(text, 156) + "...";
        }
        minecraft.fontRenderer.drawString(text, 4, y, color);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX < 0 || mouseX >= 176 || mouseY < 82 || mouseY >= 138) return Collections.emptyList();
        List<String> tooltip = new java.util.ArrayList<>();
        tooltip.add(outputFluid.getLocalizedName());
        tooltip.add(I18n.format("gt6addition.jei.crucible.temperature", temperature));
        tooltip.add(I18n.format("gt6addition.jei.crucible.yield", outputFluid.amount));
        if (alloying) {
            tooltip.add(I18n.format("gt6addition.jei.crucible.components", ""));
            tooltip.addAll(componentInfo);
            tooltip.add(I18n.format("gt6addition.jei.crucible.ratio_unit"));
            tooltip.add(I18n.format("gt6addition.jei.crucible.alloy_note"));
        }
        tooltip.add(I18n.format("gt6addition.jei.crucible.heat_note"));
        return tooltip;
    }
}
