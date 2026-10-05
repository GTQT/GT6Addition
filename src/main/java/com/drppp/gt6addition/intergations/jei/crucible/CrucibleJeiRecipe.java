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
    private final List<List<FluidStack>> fluidInputs;
    private final FluidStack outputFluid;
    private final ItemStack outputPreview;
    private final String internalOutputName;
    private final long internalOutputAmount;
    private final int temperature;
    private final boolean alloying;
    private final List<String> componentInfo;

    public CrucibleJeiRecipe(List<List<ItemStack>> inputs, FluidStack outputFluid, int temperature, boolean alloying) {
        this(inputs, outputFluid, temperature, alloying, Collections.emptyList());
    }

    public CrucibleJeiRecipe(List<List<ItemStack>> inputs, FluidStack outputFluid, int temperature, boolean alloying,
                             List<String> componentInfo) {
        this(inputs, Collections.nCopies(inputs.size(), Collections.emptyList()), outputFluid, temperature,
                alloying, componentInfo);
    }

    /** Input rows keep recipe-component order independently of ingredient type. */
    public CrucibleJeiRecipe(List<List<ItemStack>> inputs, List<List<FluidStack>> fluidInputs,
                             FluidStack outputFluid, int temperature, boolean alloying, List<String> componentInfo) {
        if (inputs.size() != fluidInputs.size()) throw new IllegalArgumentException("Mismatched ingredient rows");
        this.inputs = inputs;
        this.fluidInputs = fluidInputs;
        this.outputFluid = outputFluid;
        this.outputPreview = ItemStack.EMPTY;
        this.internalOutputName = "";
        this.internalOutputAmount = 0;
        this.temperature = temperature;
        this.alloying = alloying;
        this.componentInfo = componentInfo;
    }

    /** The icon is a material preview, not an automatically ejected solid product. */
    public CrucibleJeiRecipe(List<List<ItemStack>> inputs, List<List<FluidStack>> fluidInputs,
                             String outputName, long outputAmount, ItemStack preview,
                             int temperature, boolean alloying, List<String> componentInfo) {
        if (inputs.size() != fluidInputs.size()) throw new IllegalArgumentException("Mismatched ingredient rows");
        if (outputName == null || outputName.isEmpty() || outputAmount <= 0)
            throw new IllegalArgumentException("Invalid internal material output");
        this.inputs = inputs;
        this.fluidInputs = fluidInputs;
        this.outputFluid = null;
        this.outputPreview = preview == null ? ItemStack.EMPTY : preview.copy();
        if (!this.outputPreview.isEmpty()) this.outputPreview.setCount(1);
        this.internalOutputName = outputName;
        this.internalOutputAmount = outputAmount;
        this.temperature = temperature;
        this.alloying = alloying;
        this.componentInfo = componentInfo;
    }

    public boolean hasInternalOutput() { return internalOutputAmount > 0; }
    public long getInternalOutputAmount() { return internalOutputAmount; }
    public ItemStack getOutputPreview() { return outputPreview.copy(); }
    private String outputName() { return hasInternalOutput() ? internalOutputName : outputFluid.getLocalizedName(); }
    private String yieldText() {
        if (!hasInternalOutput()) return I18n.format("gt6addition.jei.crucible.yield", outputFluid.amount);
        long unit = gregtech.api.GTValues.M;
        String amount = internalOutputAmount % unit == 0 ? Long.toString(internalOutputAmount / unit) :
                internalOutputAmount + "/" + unit;
        return I18n.format("gt6addition.jei.crucible.internal_yield", amount);
    }

    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    public boolean isAlloying() { return alloying; }
    public int getInputCount() { return inputs.size(); }
    public List<String> getComponentInfo() { return componentInfo; }
    public List<ItemStack> getItemInputs(int row) { return inputs.get(row); }
    public List<FluidStack> getFluidInputs(int row) { return fluidInputs.get(row); }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // JEI search indexes only actual ingredients, not placeholder rows.
        List<List<ItemStack>> items = new java.util.ArrayList<>();
        List<List<FluidStack>> fluids = new java.util.ArrayList<>();
        for (int row = 0; row < inputs.size(); row++) {
            if (!inputs.get(row).isEmpty()) items.add(inputs.get(row));
            if (!fluidInputs.get(row).isEmpty()) fluids.add(fluidInputs.get(row));
        }
        ingredients.setInputLists(ItemStack.class, items);
        ingredients.setInputLists(FluidStack.class, fluids);
        if (hasInternalOutput()) {
            if (!outputPreview.isEmpty()) ingredients.setOutput(ItemStack.class, outputPreview.copy());
        } else {
            ingredients.setOutput(FluidStack.class, outputFluid);
        }
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String typeKey = alloying ? "gt6addition.jei.crucible.alloying" : "gt6addition.jei.crucible.melting";
        drawLine(minecraft, I18n.format(typeKey) + " · " + outputName(), 84, 0x404040);
        drawLine(minecraft, I18n.format("gt6addition.jei.crucible.temperature", temperature), 97, 0xA04420);
        drawLine(minecraft, yieldText(), 110, 0x404040);
        drawLine(minecraft, I18n.format(hasInternalOutput() ? "gt6addition.jei.crucible.internal_hint" :
                alloying ? "gt6addition.jei.crucible.ratio_hint" :
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
        tooltip.add(outputName());
        tooltip.add(I18n.format("gt6addition.jei.crucible.temperature", temperature));
        tooltip.add(yieldText());
        if (hasInternalOutput()) tooltip.add(I18n.format("gt6addition.jei.crucible.internal_note"));
        if (alloying) {
            tooltip.add(I18n.format("gt6addition.jei.crucible.components", ""));
            tooltip.addAll(componentInfo);
            tooltip.add(I18n.format("gt6addition.jei.crucible.ratio_unit"));
            tooltip.add(I18n.format("gt6addition.jei.crucible.alloy_note"));
            tooltip.add(I18n.format("gt6addition.jei.crucible.batch_note"));
        }
        tooltip.add(I18n.format("gt6addition.jei.crucible.heat_note"));
        return tooltip;
    }
}
