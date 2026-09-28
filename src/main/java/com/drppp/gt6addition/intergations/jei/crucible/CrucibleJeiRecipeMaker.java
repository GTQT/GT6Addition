package com.drppp.gt6addition.intergations.jei.crucible;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.material.properties.IngotProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public final class CrucibleJeiRecipeMaker {

    private static final int MAX_COMPONENT_SLOTS = 9;

    private CrucibleJeiRecipeMaker() {}

    public static List<CrucibleJeiRecipe> createRecipes() {
        List<CrucibleJeiRecipe> recipes = new ArrayList<>();
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (!isUsableOutputMaterial(material)) {
                continue;
            }
            addMeltingRecipe(recipes, material);
            addAlloyRecipe(recipes, material);
        }
        return recipes;
    }

    private static void addMeltingRecipe(List<CrucibleJeiRecipe> recipes, Material material) {
        List<ItemStack> input = getMaterialInputs(material, GTValues.M);
        Material target = getSmeltingTarget(material);
        FluidStack output = target.hasFluid() ? target.getFluid(GTValues.L) : null;
        if (input.isEmpty() || output == null) {
            return;
        }

        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(input);
        recipes.add(new CrucibleJeiRecipe(inputs, output, getMeltingTemperature(material), false));
    }

    private static void addAlloyRecipe(List<CrucibleJeiRecipe> recipes, Material alloy) {
        List<MaterialStack> components = alloy.getMaterialComponents();
        if (components == null || components.size() < 2 || components.size() > MAX_COMPONENT_SLOTS) {
            return;
        }

        List<List<ItemStack>> inputs = new ArrayList<>();
        List<String> componentInfo = new ArrayList<>();
        long divisor = 0;
        for (MaterialStack component : components) {
            divisor = gcd(divisor, Math.max(1L, component.amount));
        }
        long outputUnits = 0;
        List<Integer> meltingPoints = new ArrayList<>();
        for (MaterialStack component : components) {
            Material componentMaterial = getSmeltingTarget(component.material);
            if (componentMaterial == null || componentMaterial == Materials.NULL) {
                return;
            }
            // Material components are ratios, not GTValues.M-based stored quantities.
            long parts = Math.max(1L, component.amount) / divisor;
            if (parts > 64) return;
            long componentAmount = parts * GTValues.M;
            List<ItemStack> input = getMaterialInputs(componentMaterial, componentAmount);
            if (input.isEmpty()) {
                return;
            }
            inputs.add(input);
            componentInfo.add(componentMaterial.getLocalizedName() + " × " + parts);
            meltingPoints.add(getMeltingTemperature(componentMaterial));
            outputUnits += componentAmount;
        }

        FluidStack output = alloy.getFluid(toFluidAmount(outputUnits));
        if (output == null || output.amount <= 0) {
            return;
        }
        // Runtime alloying requires the product melting point and at most one solid component.
        java.util.Collections.sort(meltingPoints);
        int temperature = Math.max(getMeltingTemperature(alloy), meltingPoints.get(meltingPoints.size() - 2));
        recipes.add(new CrucibleJeiRecipe(inputs, output, temperature, true, componentInfo));
    }

    static long gcd(long a, long b) {
        while (b != 0) {
            long remainder = a % b;
            a = b;
            b = remainder;
        }
        return a;
    }

    private static boolean isUsableOutputMaterial(Material material) {
        return material != null && material != Materials.NULL && material.hasFluid() && material.getFluid(GTValues.L) != null;
    }

    private static List<ItemStack> getMaterialInputs(Material material, long amount) {
        List<ItemStack> inputs = new ArrayList<>();
        addPrefixInput(inputs, OrePrefix.ingot, material, amount);
        addPrefixInput(inputs, OrePrefix.dust, material, amount);
        addPrefixInput(inputs, OrePrefix.gem, material, amount);
        addPrefixInput(inputs, OrePrefix.nugget, material, amount);
        addPrefixInput(inputs, OrePrefix.dustSmall, material, amount);
        addPrefixInput(inputs, OrePrefix.dustTiny, material, amount);
        ItemStack stack = OreDictUnifier.getIngotOrDust(material, amount);
        addUniqueInput(inputs, stack == null ? ItemStack.EMPTY : stack);
        return inputs;
    }

    private static void addPrefixInput(List<ItemStack> inputs, OrePrefix prefix, Material material, long amount) {
        long prefixAmount = prefix.getMaterialAmount(material);
        if (prefixAmount <= 0 || amount % prefixAmount != 0) {
            return;
        }
        long count = amount / prefixAmount;
        if (count <= 0 || count > 64) {
            return;
        }
        addUniqueInput(inputs, OreDictUnifier.get(prefix, material, (int) count));
    }

    private static void addUniqueInput(List<ItemStack> inputs, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        for (ItemStack existing : inputs) {
            if (ItemStack.areItemsEqual(existing, stack) && ItemStack.areItemStackTagsEqual(existing, stack) &&
                    existing.getCount() == stack.getCount()) {
                return;
            }
        }
        inputs.add(stack);
    }

    private static Material getSmeltingTarget(Material material) {
        if (material != null && material.hasProperty(PropertyKey.INGOT)) {
            IngotProperty property = material.getProperty(PropertyKey.INGOT);
            if (property.getSmeltingInto() != null) {
                return property.getSmeltingInto();
            }
        }
        return material;
    }

    private static int getMeltingTemperature(Material material) {
        if (material.hasFluid()) {
            return material.getFluid().getTemperature();
        }
        int blastTemperature = material.getBlastTemperature();
        return blastTemperature > 0 ? blastTemperature : 1811;
    }

    static int toFluidAmount(long materialAmount) {
        return (int) Math.min(Integer.MAX_VALUE, materialAmount * GTValues.L / GTValues.M);
    }
}
