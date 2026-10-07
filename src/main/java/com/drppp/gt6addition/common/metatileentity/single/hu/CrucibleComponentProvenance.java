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
import gregtech.common.items.MetaItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Actual battery manufacturing inputs, not an arbitrary static recipe or EU capacity. */
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

    static boolean isBattery(ItemStack stack) {
        return isOneOf(stack, MetaItems.BATTERY_ULV_TANTALUM,
                MetaItems.BATTERY_LV_LITHIUM, MetaItems.BATTERY_LV_CADMIUM, MetaItems.BATTERY_LV_SODIUM,
                MetaItems.BATTERY_MV_LITHIUM, MetaItems.BATTERY_MV_CADMIUM, MetaItems.BATTERY_MV_SODIUM,
                MetaItems.BATTERY_HV_LITHIUM, MetaItems.BATTERY_HV_CADMIUM, MetaItems.BATTERY_HV_SODIUM,
                MetaItems.BATTERY_EV_VANADIUM, MetaItems.BATTERY_IV_VANADIUM, MetaItems.BATTERY_LUV_VANADIUM,
                MetaItems.BATTERY_ZPM_NAQUADRIA, MetaItems.BATTERY_UV_NAQUADRIA);
    }

    public static boolean tracks(Recipe recipe) {
        return recipe.getOutputs().stream().anyMatch(stack -> isHull(stack) || isBattery(stack));
    }

    /** Own bookkeeping is invisible to ordinary recipe matching, not to explicit NBT matchers. */
    public static ItemStack matchingCopy(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) return stack;
        String key = isHull(stack) ? TAG : isBattery(stack) ? CrucibleElectricProvenance.TAG : null;
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
        if (unit.hasTagCompound() && unit.getTagCompound().hasKey(TAG)) return resolve(unit);
        // Hull routes differ. An untracked old hull does not acquire a made-up history.
        if (isHull(unit) || unit.hasTagCompound()) return Collections.emptyList();
        RecyclingData data = RecyclingHandler.getRecyclingIngredients(1,
                Collections.singletonList(new GTRecipeItemInput(unit)), null);
        if (data == null || !valid(data.getMaterials())) return Collections.emptyList();
        return data.getMaterials();
    }

    static List<MaterialStack> resolve(ItemStack stack) {
        if (!isHull(stack) || stack.isItemDamaged() || !stack.hasTagCompound()) return Collections.emptyList();
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
        if (!root.hasKey(TAG, 10) || !bound(root.getCompoundTag(TAG), copy)) return Collections.emptyList();
        return CrucibleElectricProvenance.parseList(root.getCompoundTag(TAG), "materials",
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
        List<MaterialStack> totals = Collections.emptyList();
        List<MaterialStack> hullTotals = Collections.emptyList();
        ItemStack hull = ItemStack.EMPTY;
        int hullCount = 0;
        for (ItemStack input : items) {
            if (isHull(output) && isBattery(input)) {
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
        for (FluidStack fluid : fluids) {
            Material material = FluidUnifier.getMaterialFromFluid(fluid.getFluid());
            // CEu manufacturing/recycling measures dust-bearing fluids in M/L.
            // Do not interpret unrelated coolant, gases or unknown fluids as metal.
            if (material == null || material == Materials.NULL || material instanceof MarkerMaterial ||
                    !material.hasProperty(PropertyKey.DUST) || fluid.amount <= 0) return invalid;
            long amount = (long) GTValues.M * fluid.amount / GTValues.L;
            totals = CrucibleRecyclingOverride.combine(totals, Collections.singletonList(new MaterialStack(material, amount)));
            if (totals.isEmpty()) return invalid;
        }
        NBTTagCompound result = encoded(output, totals, output.getCount());
        if (result.isEmpty()) return invalid;
        if (isBattery(output) && !hull.isEmpty()) {
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
        root.setTag(isBattery(output) ? CrucibleElectricProvenance.TAG : TAG, account.copy());
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
        NBTTagCompound account = new NBTTagCompound();
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
                account = account(recipe.getOutputs().get(0), consumed, consumedFluids);
            }
        }
        return new Preparation(account, after, afterFluids);
    }

    public static final class Preparation {
        private NBTTagCompound account;
        private final List<ItemStack> expectedItems;
        private final List<FluidStack> expectedFluids;

        private Preparation(NBTTagCompound account, List<ItemStack> expectedItems, List<FluidStack> expectedFluids) {
            this.account = account;
            this.expectedItems = expectedItems;
            this.expectedFluids = expectedFluids;
        }

        /** A nonstandard inventory must not turn a failed extraction into valid provenance. */
        public void verifyConsumed(IItemHandlerModifiable inputs, IMultipleTankHandler tanks) {
            for (int i = 0; i < expectedItems.size(); i++) {
                if (!ItemStack.areItemStacksEqual(expectedItems.get(i), inputs.getStackInSlot(i))) {
                    account = new NBTTagCompound(); return;
                }
            }
            for (int i = 0; i < expectedFluids.size(); i++) {
                FluidStack actual = tanks.getTankAt(i).getFluid(), expected = expectedFluids.get(i);
                if (expected == null ? actual != null && actual.amount > 0 :
                        actual == null || !expected.isFluidStackIdentical(actual)) {
                    account = new NBTTagCompound(); return;
                }
            }
        }

        public List<ItemStack> decorate(List<ItemStack> outputs) {
            List<ItemStack> result = new ArrayList<>();
            for (ItemStack output : outputs) {
                ItemStack copy = output.copy();
                if (isHull(copy) || isBattery(copy)) write(copy, bound(account, copy) ? account : new NBTTagCompound());
                result.add(copy);
            }
            return result;
        }
    }
}
