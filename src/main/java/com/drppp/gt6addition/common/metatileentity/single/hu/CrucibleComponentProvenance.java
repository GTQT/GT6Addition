package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecyclingHandler;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.RecyclingData;
import gregtech.common.blocks.BlockBatteryPart.BatteryPartType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Actual battery-product manufacturing inputs, not an arbitrary static recipe or EU capacity. */
public final class CrucibleComponentProvenance {
    static final String TAG = "gt6addition.componentMaterials";

    private CrucibleComponentProvenance() {}

    private static boolean isOneOf(ItemStack stack, MetaItem<?>.MetaValueItem... items) {
        if (stack.isEmpty()) return false;
        for (MetaItem<?>.MetaValueItem item : items) {
            if (item != null && ItemStack.areItemsEqual(stack, item.getStackForm())) return true;
        }
        return false;
    }

    static boolean isHull(ItemStack stack) {
        return isOneOf(stack, MetaItems.BATTERY_HULL_LV, MetaItems.BATTERY_HULL_MV, MetaItems.BATTERY_HULL_HV,
                MetaItems.BATTERY_HULL_SMALL_VANADIUM, MetaItems.BATTERY_HULL_MEDIUM_VANADIUM,
                MetaItems.BATTERY_HULL_LARGE_VANADIUM, MetaItems.BATTERY_HULL_MEDIUM_NAQUADRIA,
                MetaItems.BATTERY_HULL_LARGE_NAQUADRIA);
    }

    private static boolean isElectricBattery(ItemStack stack) {
        return isOneOf(stack, MetaItems.BATTERY_ULV_TANTALUM,
                MetaItems.BATTERY_LV_LITHIUM, MetaItems.BATTERY_LV_CADMIUM, MetaItems.BATTERY_LV_SODIUM,
                MetaItems.BATTERY_MV_LITHIUM, MetaItems.BATTERY_MV_CADMIUM, MetaItems.BATTERY_MV_SODIUM,
                MetaItems.BATTERY_HV_LITHIUM, MetaItems.BATTERY_HV_CADMIUM, MetaItems.BATTERY_HV_SODIUM,
                MetaItems.BATTERY_EV_VANADIUM, MetaItems.BATTERY_IV_VANADIUM, MetaItems.BATTERY_LUV_VANADIUM,
                MetaItems.BATTERY_ZPM_NAQUADRIA, MetaItems.BATTERY_UV_NAQUADRIA,
                MetaItems.ENERGIUM_CRYSTAL, MetaItems.LAPOTRON_CRYSTAL,
                MetaItems.ENERGY_LAPOTRONIC_ORB, MetaItems.ENERGY_LAPOTRONIC_ORB_CLUSTER,
                MetaItems.ENERGY_MODULE, MetaItems.ENERGY_CLUSTER, MetaItems.ZERO_POINT_MODULE,
                MetaItems.QUANTUM_CORE, MetaItems.SINGULARITY_CELL, MetaItems.CHRONO_MATRIX,
                MetaItems.TACHYON_REACTOR, MetaItems.COSMIC_STRING, MetaItems.ULTIMATE_BATTERY);
    }

    /** Battery block variants are recipe products too, but are not electric-item capabilities. */
    private static boolean isBatteryPart(ItemStack stack) {
        return batteryPartType(stack) != null;
    }

    private static BatteryPartType batteryPartType(ItemStack stack) {
        if (stack.isEmpty() || MetaBlocks.BATTERY_BLOCK == null) return null;
        for (BatteryPartType type : BatteryPartType.values()) {
            if (ItemStack.areItemsEqual(stack, MetaBlocks.BATTERY_BLOCK.getItemVariant(type))) return type;
        }
        return null;
    }

    private static boolean isEmptyBatteryPart(ItemStack stack) {
        BatteryPartType type = batteryPartType(stack);
        return type != null && type.getCapacity() == 0L;
    }

    static boolean isUntrackedFilledBatteryPart(ItemStack stack) {
        if (!isBatteryPart(stack) || isEmptyBatteryPart(stack)) return false;
        return !stack.hasTagCompound() || !stack.getTagCompound().hasKey(TAG, 10);
    }

    static boolean isBattery(ItemStack stack) {
        return isElectricBattery(stack) || isBatteryPart(stack);
    }

    private static String provenanceKey(ItemStack stack) {
        return isHull(stack) || isBatteryPart(stack) ? TAG : CrucibleElectricProvenance.TAG;
    }

    public static boolean tracks(Recipe recipe) {
        return recipe.getOutputs().stream().anyMatch(stack -> isHull(stack) || isBattery(stack));
    }

    /** Own bookkeeping is invisible to ordinary recipe matching, not to explicit NBT matchers. */
    public static ItemStack matchingCopy(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) return stack;
        String key = isHull(stack) || isBattery(stack) ? provenanceKey(stack) : null;
        if (key == null || !stack.getTagCompound().hasKey(key)) return stack;
        ItemStack copy = stack.copy();
        copy.getTagCompound().removeTag(key);
        if (copy.getTagCompound().isEmpty()) copy.setTagCompound(null);
        return copy;
    }

    static void captureCrafting(ItemStack output, IInventory inventory, boolean clearing) {
        if (!isHull(output) && !isBattery(output)) return;
        List<ItemStack> consumed = new ArrayList<>();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack input = inventory.getStackInSlot(i);
            if (input.isEmpty()) continue;
            ItemStack unit = input.copy();
            unit.setCount(1);
            if (!clearing && unit.getItem().hasContainerItem(unit.copy())) continue;
            consumed.add(unit);
        }
        write(output, account(output, consumed, Collections.emptyList()));
    }

    /** Material data for one actual input item. Unknown payloads never use static fallback. */
    static List<MaterialStack> componentMaterials(ItemStack input) {
        ItemStack unit = input.copy();
        unit.setCount(1);
        if (unit.isEmpty() || unit.getMetadata() == GTValues.W || unit.isItemDamaged() ||
                CrucibleToolRecycling.isTool(unit) ||
                unit.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ||
                unit.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return Collections.emptyList();
        }
        if (unit.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
            return CrucibleElectricRecycling.resolve(unit, MetaTileEntityCrucible::resolveMaterial);
        }
        if (unit.hasTagCompound() && (unit.getTagCompound().hasKey(TAG) ||
                unit.getTagCompound().hasKey(CrucibleElectricProvenance.TAG))) return resolve(unit);
        // Filled battery blocks contain a core which is not represented by the
        // static recycling entry for the empty shell. Never recover the shell
        // recipe alone as if it were the complete filled block.
        if (isBatteryPart(unit) && !isEmptyBatteryPart(unit)) return Collections.emptyList();
        // Hull routes differ. An untracked old hull does not acquire a made-up history.
        if (isHull(unit) || unit.hasTagCompound()) return Collections.emptyList();
        RecyclingData data = RecyclingHandler.getRecyclingIngredients(1,
                Collections.singletonList(new GTRecipeItemInput(unit)), null);
        if (data == null || !valid(data.getMaterials())) return Collections.emptyList();
        return data.getMaterials();
    }

    static List<MaterialStack> resolve(ItemStack stack) {
        if ((!isHull(stack) && !isBatteryPart(stack)) || stack.isItemDamaged() || !stack.hasTagCompound()) {
            return Collections.emptyList();
        }
        ItemStack copy = stack.copy();
        if (copy.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ||
                copy.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) ||
                copy.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) return Collections.emptyList();
        NBTTagCompound root = copy.getTagCompound();
        for (String key : root.getKeySet()) {
            if (!key.equals(TAG) && !key.equals("display") && !key.equals("ench") &&
                    !key.equals("RepairCost") && !key.equals("Unbreakable") && !key.equals("HideFlags")) {
                return Collections.emptyList();
            }
        }
        String accountKey = provenanceKey(copy);
        if (!root.hasKey(accountKey, 10) || !bound(root.getCompoundTag(accountKey), copy)) {
            return Collections.emptyList();
        }
        return CrucibleElectricProvenance.parseList(root.getCompoundTag(accountKey), "materials",
                MetaTileEntityCrucible::resolveMaterial);
    }

    private static boolean valid(List<MaterialStack> materials) {
        if (materials.isEmpty()) return false;
        for (MaterialStack component : materials) {
            if (component == null || component.material == null || component.material == Materials.NULL ||
                    component.material instanceof MarkerMaterial || component.amount <= 0) return false;
        }
        return true;
    }

    private static boolean bound(NBTTagCompound account, ItemStack stack) {
        return account.hasKey("version", 3) && account.getInteger("version") == 2 &&
                account.hasKey("item", 8) && stack.getItem().getRegistryName() != null &&
                account.getString("item").equals(stack.getItem().getRegistryName().toString()) &&
                account.hasKey("metadata", 3) && account.getInteger("metadata") == stack.getMetadata();
    }

    private static NBTTagCompound account(ItemStack output, List<ItemStack> items, List<FluidStack> fluids) {
        NBTTagCompound invalid = new NBTTagCompound();
        if (output == null || output.isEmpty() || output.getCount() <= 0 || GTValues.L <= 0 || GTValues.M <= 0) {
            return invalid;
        }
        List<MaterialStack> totals = Collections.emptyList();
        List<MaterialStack> hullTotals = Collections.emptyList();
        ItemStack hull = ItemStack.EMPTY;
        int hullCount = 0;
        for (ItemStack input : items) {
            if (isHull(output) && isElectricBattery(input)) {
                // The extractor discards filler, not the original manufactured hull.
                if (items.size() != 1 || !fluids.isEmpty() || !input.hasTagCompound()) return invalid;
                NBTTagCompound battery = input.getTagCompound().getCompoundTag(CrucibleElectricProvenance.TAG);
                if (CrucibleElectricRecycling.resolve(input, MetaTileEntityCrucible::resolveMaterial).isEmpty() ||
                        !battery.hasKey("hullItem", 8) || !battery.hasKey("hullMetadata", 3) ||
                        !battery.getString("hullItem").equals(output.getItem().getRegistryName().toString()) ||
                        battery.getInteger("hullMetadata") != output.getMetadata() ||
                        input.getCount() != output.getCount()) return invalid;
                List<MaterialStack> retained = CrucibleElectricProvenance.parseList(battery, "hullMaterials",
                        MetaTileEntityCrucible::resolveMaterial);
                List<MaterialStack> all = CrucibleElectricProvenance.parse(input.getTagCompound(),
                        MetaTileEntityCrucible::resolveMaterial);
                if (retained.isEmpty() || !CrucibleElectricProvenance.isSubset(retained, all)) return invalid;
                return encoded(output, retained, 1);
            }
            List<MaterialStack> unit = componentMaterials(input);
            if (unit.isEmpty()) return invalid;
            List<MaterialStack> consumed = multiply(unit, input.getCount());
            if (consumed.isEmpty()) return invalid;
            totals = CrucibleRecyclingOverride.combine(totals, consumed);
            if (totals.isEmpty()) return invalid;
            if (isHull(input)) {
                if (!hull.isEmpty() && !ItemStack.areItemsEqual(hull, input)) return invalid;
                hull = input;
                if (hullCount > Integer.MAX_VALUE - input.getCount()) return invalid;
                hullCount += input.getCount();
                hullTotals = CrucibleRecyclingOverride.combine(hullTotals, consumed);
                if (hullTotals.isEmpty()) return invalid;
            }
        }
        // Keep CEu's M/L material conversion exact until all item and fluid
        // inputs have been combined. Truncating each fluid stack separately
        // loses material when several fractional inputs together make a unit.
        Map<Material, BigInteger> scaledTotals = new LinkedHashMap<>();
        BigInteger fluidDenominator = BigInteger.valueOf(GTValues.L);
        for (MaterialStack component : totals) {
            if (component == null || component.material == null || component.amount <= 0) return invalid;
            scaledTotals.put(component.material, BigInteger.valueOf(component.amount).multiply(fluidDenominator));
        }
        for (FluidStack fluid : fluids) {
            if (fluid == null || fluid.getFluid() == null || fluid.tag != null) return invalid;
            Material material = FluidUnifier.getMaterialFromFluid(fluid.getFluid());
            // CEu manufacturing/recycling measures dust-bearing fluids in M/L.
            // Do not interpret unrelated coolant, gases or unknown fluids as metal.
            if (material == null || material == Materials.NULL || material instanceof MarkerMaterial ||
                    !material.hasProperty(PropertyKey.DUST) || fluid.amount <= 0) return invalid;
            BigInteger amount = BigInteger.valueOf(GTValues.M).multiply(BigInteger.valueOf(fluid.amount));
            scaledTotals.merge(material, amount, BigInteger::add);
        }
        BigInteger perOutputDenominator = fluidDenominator.multiply(BigInteger.valueOf(output.getCount()));
        List<MaterialStack> perOutputMaterials = new ArrayList<>();
        for (Map.Entry<Material, BigInteger> entry : scaledTotals.entrySet()) {
            BigInteger[] perOutput = entry.getValue().divideAndRemainder(perOutputDenominator);
            if (perOutput[1].signum() != 0 || perOutput[0].signum() <= 0 ||
                    perOutput[0].compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) return invalid;
            perOutputMaterials.add(new MaterialStack(entry.getKey(), perOutput[0].longValue()));
        }
        if (perOutputMaterials.isEmpty()) return invalid;
        NBTTagCompound result = encoded(output, perOutputMaterials, 1);
        if (result.isEmpty()) return invalid;
        if (isElectricBattery(output) && !hull.isEmpty()) {
            if (hullCount != output.getCount()) return invalid;
            net.minecraft.nbt.NBTTagList entries = CrucibleElectricProvenance.encode(hullTotals, output.getCount());
            if (entries == null) return invalid;
            result.setString("hullItem", hull.getItem().getRegistryName().toString());
            result.setInteger("hullMetadata", hull.getMetadata());
            result.setTag("hullMaterials", entries);
        }
        return result;
    }

    private static NBTTagCompound encoded(ItemStack output, List<MaterialStack> materials, int divisor) {
        NBTTagCompound account = new NBTTagCompound();
        net.minecraft.nbt.NBTTagList entries = CrucibleElectricProvenance.encode(materials, divisor);
        if (entries == null) return account;
        CrucibleElectricProvenance.bindItem(account, output);
        account.setTag("materials", entries);
        return account;
    }

    private static List<MaterialStack> multiply(List<MaterialStack> materials, int count) {
        if (count <= 0) return Collections.emptyList();
        List<MaterialStack> result = new ArrayList<>();
        for (MaterialStack component : materials) {
            if (component.amount > Long.MAX_VALUE / count) return Collections.emptyList();
            result.add(new MaterialStack(component.material, component.amount * count));
        }
        return result;
    }

    private static void write(ItemStack output, NBTTagCompound account) {
        NBTTagCompound root = output.getTagCompound();
        if (root == null) { root = new NBTTagCompound(); output.setTagCompound(root); }
        root.setTag(provenanceKey(output), account.copy());
    }

    /** Host matching on copies selects actual slots, alternatives and non-consumables. */
    public static Preparation prepare(Recipe recipe, IItemHandlerModifiable inputs, IMultipleTankHandler tanks) {
        List<ItemStack> before = new ArrayList<>(), after = new ArrayList<>();
        for (int i = 0; i < inputs.getSlots(); i++) {
            before.add(inputs.getStackInSlot(i).copy());
            after.add(inputs.getStackInSlot(i).copy());
        }
        List<FluidStack> beforeFluids = new ArrayList<>(), afterFluids = new ArrayList<>();
        for (int i = 0; i < tanks.getTanks(); i++) {
            FluidStack fluid = tanks.getTankAt(i).getFluid();
            beforeFluids.add(fluid == null ? null : fluid.copy());
            afterFluids.add(fluid == null ? null : fluid.copy());
        }
        List<OutputAccount> outputAccounts = new ArrayList<>();
        List<ItemStack> consumed = new ArrayList<>();
        List<FluidStack> consumedFluids = new ArrayList<>();
        if (recipe.matches(true, after, afterFluids)) {
            for (int i = 0; i < before.size(); i++) {
                int delta = before.get(i).getCount() - after.get(i).getCount();
                if (delta > 0) { ItemStack item = before.get(i).copy(); item.setCount(delta); consumed.add(item); }
            }
            for (int i = 0; i < beforeFluids.size(); i++) {
                FluidStack old = beforeFluids.get(i), remaining = afterFluids.get(i);
                int delta = old == null ? 0 : old.amount - (remaining == null ? 0 : remaining.amount);
                if (delta > 0) { FluidStack fluid = old.copy(); fluid.amount = delta; consumedFluids.add(fluid); }
            }
            if (recipe.getOutputs().size() == 1 && recipe.getFluidOutputs().isEmpty() &&
                    recipe.getChancedOutputs().getChancedEntries().isEmpty() &&
                    recipe.getChancedFluidOutputs().getChancedEntries().isEmpty()) {
                ItemStack output = recipe.getOutputs().get(0);
                outputAccounts.add(new OutputAccount(output, account(output, consumed, consumedFluids)));
            } else {
                outputAccounts.addAll(disassemblyAccounts(recipe, consumed, consumedFluids));
            }
        }
        return new Preparation(outputAccounts, after, afterFluids);
    }

    /** Split a tracked filled battery block back into its empty shell and exact core. */
    private static List<OutputAccount> disassemblyAccounts(Recipe recipe, List<ItemStack> consumed,
                                                            List<FluidStack> consumedFluids) {
        List<ItemStack> outputs = recipe.getOutputs();
        if (outputs.size() != 2 || !recipe.getFluidOutputs().isEmpty() || !consumedFluids.isEmpty() ||
                !recipe.getChancedOutputs().getChancedEntries().isEmpty() ||
                !recipe.getChancedFluidOutputs().getChancedEntries().isEmpty() || consumed.size() != 1) {
            return Collections.emptyList();
        }
        ItemStack emptyPart = null;
        ItemStack electricCore = null;
        for (ItemStack output : outputs) {
            if (isEmptyBatteryPart(output)) emptyPart = output;
            else if (isElectricBattery(output)) electricCore = output;
            else return Collections.emptyList();
        }
        ItemStack filledPart = consumed.get(0);
        if (emptyPart == null || electricCore == null || !isBatteryPart(filledPart) ||
                isEmptyBatteryPart(filledPart) || filledPart.getCount() != emptyPart.getCount() ||
                filledPart.getCount() != electricCore.getCount()) return Collections.emptyList();

        List<MaterialStack> source = multiply(componentMaterials(filledPart), filledPart.getCount());
        List<MaterialStack> core = multiply(componentMaterials(electricCore), electricCore.getCount());
        List<MaterialStack> shell = subtract(source, core);
        if (source.isEmpty() || core.isEmpty() || shell.isEmpty()) return Collections.emptyList();
        NBTTagCompound shellAccount = encoded(emptyPart, shell, emptyPart.getCount());
        NBTTagCompound coreAccount = encoded(electricCore, core, electricCore.getCount());
        if (!bound(shellAccount, emptyPart) || !bound(coreAccount, electricCore)) return Collections.emptyList();
        List<OutputAccount> accounts = new ArrayList<>(2);
        accounts.add(new OutputAccount(emptyPart, shellAccount));
        accounts.add(new OutputAccount(electricCore, coreAccount));
        return accounts;
    }

    private static List<MaterialStack> subtract(List<MaterialStack> source, List<MaterialStack> removed) {
        if (source.isEmpty() || removed.isEmpty()) return Collections.emptyList();
        java.util.Map<Material, Long> amounts = new java.util.LinkedHashMap<>();
        for (MaterialStack component : source) {
            if (component == null || component.material == null || component.amount <= 0) return Collections.emptyList();
            long previous = amounts.getOrDefault(component.material, 0L);
            if (!CrucibleTransferLogic.canMergeMaterialAmounts(previous, component.amount)) return Collections.emptyList();
            amounts.put(component.material, previous + component.amount);
        }
        for (MaterialStack component : removed) {
            if (component == null || component.material == null || component.amount <= 0) return Collections.emptyList();
            long previous = amounts.getOrDefault(component.material, 0L);
            if (component.amount > previous) return Collections.emptyList();
            amounts.put(component.material, previous - component.amount);
        }
        List<MaterialStack> result = new ArrayList<>();
        for (java.util.Map.Entry<Material, Long> entry : amounts.entrySet()) {
            if (entry.getValue() > 0L) result.add(new MaterialStack(entry.getKey(), entry.getValue()));
        }
        return CrucibleRecyclingOverride.combine(Collections.emptyList(), result);
    }

    private static final class OutputAccount {
        private final ItemStack output;
        private final NBTTagCompound account;

        private OutputAccount(ItemStack output, NBTTagCompound account) {
            this.output = output.copy();
            this.account = account.copy();
        }
    }

    public static final class Preparation {
        private final List<OutputAccount> outputAccounts;
        private final List<ItemStack> expectedItems;
        private final List<FluidStack> expectedFluids;

        private Preparation(List<OutputAccount> outputAccounts, List<ItemStack> expectedItems, List<FluidStack> expectedFluids) {
            this.outputAccounts = outputAccounts;
            this.expectedItems = expectedItems;
            this.expectedFluids = expectedFluids;
        }

        /** A nonstandard inventory must not turn a failed extraction into valid provenance. */
        public void verifyConsumed(IItemHandlerModifiable inputs, IMultipleTankHandler tanks) {
            for (int i = 0; i < expectedItems.size(); i++) {
                if (!ItemStack.areItemStacksEqual(expectedItems.get(i), inputs.getStackInSlot(i))) {
                    outputAccounts.clear(); return;
                }
            }
            for (int i = 0; i < expectedFluids.size(); i++) {
                FluidStack actual = tanks.getTankAt(i).getFluid(), expected = expectedFluids.get(i);
                if (expected == null ? actual != null && actual.amount > 0 :
                        actual == null || !expected.isFluidStackIdentical(actual)) {
                    outputAccounts.clear(); return;
                }
            }
        }

        public List<ItemStack> decorate(List<ItemStack> outputs) {
            List<ItemStack> result = new ArrayList<>();
            for (ItemStack output : outputs) {
                ItemStack copy = output.copy();
                if (isHull(copy) || isBattery(copy)) {
                    NBTTagCompound account = findAccount(copy);
                    write(copy, account != null && bound(account, copy) ? account : new NBTTagCompound());
                }
                result.add(copy);
            }
            return result;
        }

        private NBTTagCompound findAccount(ItemStack output) {
            for (OutputAccount candidate : outputAccounts) {
                if (ItemStack.areItemsEqual(candidate.output, output)) return candidate.account;
            }
            return null;
        }
    }
}
