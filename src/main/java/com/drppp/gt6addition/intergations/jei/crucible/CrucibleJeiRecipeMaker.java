package com.drppp.gt6addition.intergations.jei.crucible;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleFluidUnits;
import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleMaterialPhaseData;
import com.drppp.gt6addition.common.metatileentity.single.hu.GT6AlloyRecipes;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
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
        for (GT6AlloyRecipes definition : GT6AlloyRecipes.getRecipes()) {
            addGt6AlloyRecipe(recipes, definition);
        }
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material == null || material == Materials.NULL) continue;
            addMeltingRecipe(recipes, material);
            if (isUsableOutputMaterial(material) && !GT6AlloyRecipes.hasCompleteRecipeSet(material.getName())) {
                addAlloyRecipe(recipes, material);
            }
        }
        return recipes;
    }

    private static void addMeltingRecipe(List<CrucibleJeiRecipe> recipes, Material material) {
        List<ItemStack> input = getMaterialInputs(material, GTValues.M);
        Material target = MetaTileEntityCrucible.getSmeltingTarget(material);
        if (input.isEmpty() || !MetaTileEntityCrucible.hasMaterialMeltingTarget(material)) return;
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(input);
        FluidStack fluidOutput = MetaTileEntityCrucible.getSmeltingOutputFluidStack(
                material, target, GTValues.M);
        if (fluidOutput != null) {
            FluidStack output = fluidOutput;
            recipes.add(new CrucibleJeiRecipe(inputs, output, getMeltingTemperature(material), false));
        } else {
            long outputAmount = MetaTileEntityCrucible.getSmeltingOutputAmount(material, GTValues.M);
            if (outputAmount <= 0) return;
            List<ItemStack> previews = getMaterialInputs(target, GTValues.M);
            recipes.add(new CrucibleJeiRecipe(inputs, java.util.Collections.singletonList(
                    java.util.Collections.emptyList()), target.getLocalizedName(), outputAmount,
                    previews.isEmpty() ? ItemStack.EMPTY : previews.get(0),
                    getMeltingTemperature(material), false, java.util.Collections.emptyList()));
        }
    }

    private static void addAlloyRecipe(List<CrucibleJeiRecipe> recipes, Material alloy) {
        List<MaterialStack> components = MetaTileEntityCrucible.normalizeAlloyComponents(alloy.getMaterialComponents());
        if (components == null || components.size() < 2 || components.size() > MAX_COMPONENT_SLOTS) {
            return;
        }

        long divisor = 0;
        for (MaterialStack component : components) {
            divisor = gcd(divisor, Math.max(1L, component.amount));
        }
        long outputUnits = 0;
        Material[] materials = new Material[components.size()];
        long[] units = new long[components.size()];
        for (int i = 0; i < components.size(); i++) {
            MaterialStack component = components.get(i);
            // Runtime matches the exact recipe material, not its smelting target.
            Material componentMaterial = component.material;
            if (componentMaterial == null || componentMaterial == Materials.NULL) {
                return;
            }
            // Material components are ratios, not GTValues.M-based stored quantities.
            long parts = Math.max(1L, component.amount) / divisor;
            if (parts > 64) return;
            materials[i] = componentMaterial;
            units[i] = parts;
            outputUnits += parts;
        }
        addRatioRecipe(recipes, alloy, materials, units, outputUnits);
    }

    private static void addGt6AlloyRecipe(List<CrucibleJeiRecipe> recipes, GT6AlloyRecipes definition) {
        Material alloy = MetaTileEntityCrucible.resolveMaterial(definition.getOutputName());
        if (alloy == null || alloy == Materials.NULL) return;
        String[] names = definition.getInputNames();
        long[] units = definition.getInputUnits();
        if (names.length < 1 || names.length > MAX_COMPONENT_SLOTS || units.length != names.length) return;
        Material[] materials = new Material[names.length];
        for (int i = 0; i < names.length; i++) {
            Material component = MetaTileEntityCrucible.resolveMaterial(names[i]);
            if (component == null || units[i] <= 0 || units[i] > 64) return;
            materials[i] = component;
        }
        long outputUnits = definition.getOutputUnits();
        if (outputUnits <= 0 || outputUnits > Long.MAX_VALUE / GTValues.M) return;
        addRatioRecipe(recipes, alloy, materials, units, outputUnits);
    }

    private static void addRatioRecipe(List<CrucibleJeiRecipe> recipes, Material alloy,
                                       Material[] materials, long[] units, long outputUnits) {
        List<Integer> meltingPoints = new ArrayList<>();
        for (Material material : materials) meltingPoints.add(getMeltingTemperature(material));
        int temperature = requiredAlloyTemperature(getMeltingTemperature(alloy), meltingPoints);
        // Try complete units first, then exact smaller batches. Every input must have
        // a real, lossless item/fluid representation; never round a recipe ingredient.
        for (int scale = 1; scale <= 64; scale++) {
            if (!batchFitsCapacity(units, outputUnits, scale)) continue;
            List<List<ItemStack>> inputs = new ArrayList<>();
            List<List<FluidStack>> fluids = new ArrayList<>();
            List<String> info = new ArrayList<>();
            boolean representable = true;
            for (int i = 0; i < materials.length; i++) {
                long amount = scaledMaterialAmount(units[i], scale);
                if (!addMaterialInput(inputs, fluids, materials[i], amount)) {
                    representable = false;
                    break;
                }
                long common = gcd(units[i], scale);
                String quantity = scale / common == 1 ? Long.toString(units[i] / common) :
                        (units[i] / common) + "/" + (scale / common);
                info.add(materials[i].getLocalizedName() + " × " + quantity);
            }
            if (!representable) continue;
            long outputAmount = scaledMaterialAmount(outputUnits, scale);
            if (isUsableOutputMaterial(alloy)) {
                int volume = exactDisplayFluidAmount(outputAmount,
                        MetaTileEntityCrucible.getLiquidFluidUnit(alloy));
                FluidStack output = MetaTileEntityCrucible.getLiquidFluidStack(alloy, volume);
                if (output != null && output.amount > 0) {
                    recipes.add(new CrucibleJeiRecipe(inputs, fluids, output, temperature, true, info));
                    return;
                }
            }
            List<ItemStack> previews = getMaterialInputs(alloy, GTValues.M);
            recipes.add(new CrucibleJeiRecipe(inputs, fluids, alloy.getLocalizedName(), outputAmount,
                    previews.isEmpty() ? ItemStack.EMPTY : previews.get(0), temperature, true, info));
            return;
        }
    }

    static long scaledMaterialAmount(long units, int divisor) {
        if (units <= 0 || divisor <= 0) return 0;
        java.math.BigInteger[] result = java.math.BigInteger.valueOf(units)
                .multiply(java.math.BigInteger.valueOf(GTValues.M))
                .divideAndRemainder(java.math.BigInteger.valueOf(divisor));
        return result[1].signum() == 0 && result[0].bitLength() < 64 ? result[0].longValue() : 0;
    }

    static boolean batchFitsCapacity(long[] inputs, long output, int divisor) {
        // Runtime executes integer conversions of the original, unreduced coefficients.
        // Even when individual rows divide exactly, a fractional conversion would lose yield.
        if (divisor <= 0 || GTValues.M % divisor != 0) return false;
        long capacity = MetaTileEntityCrucible.getMaterialCapacity();
        long product = scaledMaterialAmount(output, divisor);
        if (inputs.length == 0 || product <= 0 || product > capacity) return false;
        long total = 0;
        for (long units : inputs) {
            long amount = scaledMaterialAmount(units, divisor);
            if (amount <= 0 || amount > capacity - total) return false;
            total += amount;
        }
        return true;
    }

    static int exactDisplayFluidAmount(long amount, String materialName) {
        return exactDisplayFluidAmount(amount, CrucibleFluidUnits.fluidUnit(materialName));
    }

    static int exactDisplayFluidAmount(long amount, int unit) {
        if (amount <= 0 || unit <= 0) return 0;
        java.math.BigInteger[] result = java.math.BigInteger.valueOf(amount)
                .multiply(java.math.BigInteger.valueOf(unit))
                .divideAndRemainder(java.math.BigInteger.valueOf(GTValues.M));
        return result[1].signum() == 0 && result[0].signum() > 0 && result[0].bitLength() < 32 ?
                result[0].intValue() : 0;
    }

    private static boolean addMaterialInput(List<List<ItemStack>> items, List<List<FluidStack>> fluids,
                                            Material material, long amount) {
        List<ItemStack> alternatives = getMaterialInputs(material, amount);
        if (!alternatives.isEmpty()) {
            items.add(alternatives);
            fluids.add(java.util.Collections.emptyList());
            return true;
        }
        if (!material.hasFluid()) return false;
        int volume = exactDisplayFluidAmount(amount, CrucibleFluidUnits.defaultFluidUnit(material));
        if (volume <= 0) return false;
        FluidStack fluid = material.getFluid(volume);
        if (fluid == null || fluid.amount <= 0) return false;
        items.add(java.util.Collections.emptyList());
        fluids.add(java.util.Collections.singletonList(fluid));
        return true;
    }

    static long gcd(long a, long b) {
        while (b != 0) {
            long remainder = a % b;
            a = b;
            b = remainder;
        }
        return a;
    }

    /** At least one input must be molten, and at most one may remain solid. */
    static int requiredAlloyTemperature(int productTemperature, List<Integer> inputTemperatures) {
        if (inputTemperatures.isEmpty()) throw new IllegalArgumentException("Missing alloy inputs");
        List<Integer> sorted = new ArrayList<>(inputTemperatures);
        java.util.Collections.sort(sorted);
        int threshold = sorted.size() == 1 ? sorted.get(0) : sorted.get(sorted.size() - 2);
        return Math.max(productTemperature, threshold);
    }

    private static boolean isUsableOutputMaterial(Material material) {
        return material != null && material != Materials.NULL &&
                MetaTileEntityCrucible.getLiquidFluidUnit(material) > 0;
    }

    private static List<ItemStack> getMaterialInputs(Material material, long amount) {
        List<ItemStack> inputs = new ArrayList<>();
        addPrefixInput(inputs, OrePrefix.ingot, material, amount);
        addPrefixInput(inputs, OrePrefix.dust, material, amount);
        addPrefixInput(inputs, OrePrefix.gem, material, amount);
        addPrefixInput(inputs, OrePrefix.nugget, material, amount);
        addPrefixInput(inputs, OrePrefix.dustSmall, material, amount);
        addPrefixInput(inputs, OrePrefix.dustTiny, material, amount);
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

    private static int getMeltingTemperature(Material material) {
        return CrucibleMaterialPhaseData.meltingPoint(material);
    }

    static int toFluidAmount(long materialAmount) {
        return toFluidAmount(materialAmount, null);
    }

    static int toFluidAmount(long materialAmount, String materialName) {
        return CrucibleFluidUnits.fluidAmount(materialAmount, 0, CrucibleFluidUnits.fluidUnit(materialName));
    }
}
