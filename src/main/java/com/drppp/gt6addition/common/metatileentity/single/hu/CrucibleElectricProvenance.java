package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Actual crafting inputs, in CEu material units; never inferred from stored EU. */
public final class CrucibleElectricProvenance {
    static final String TAG = "gt6addition.electricMaterials";

    private CrucibleElectricProvenance() {}

    public static void capture(ItemStack output, IInventory inventory) {
        if (output.isEmpty() || output.getCount() <= 0 ||
                !output.copy().hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) return;
        // An unresolved assembly is explicitly marked invalid. It must not fall
        // back to a static recipe for another battery with the same capacity.
        NBTTagCompound root = output.getTagCompound();
        if (root == null) { root = new NBTTagCompound(); output.setTagCompound(root); }
        root.setTag(TAG, new NBTTagCompound());
        List<MaterialStack> totals = Collections.emptyList();
        List<MaterialStack> powerMaterials = Collections.emptyList();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (input.isEmpty()) continue;
            ItemStack unit = input.copy();
            unit.setCount(1); // Crafting consumes one per occupied slot.
            if (unit.getItem().hasContainerItem(unit.copy())) continue;
            List<MaterialStack> components = componentMaterials(unit);
            if (components.isEmpty()) return;
            totals = CrucibleRecyclingOverride.combine(totals, components);
            if (totals.isEmpty()) return;
            if (unit.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
                powerMaterials = CrucibleRecyclingOverride.combine(powerMaterials, components);
                if (powerMaterials.isEmpty()) return;
            }
        }
        NBTTagList entries = encode(totals, output.getCount());
        if (entries == null) return;
        NBTTagCompound data = new NBTTagCompound();
        bindItem(data, output);
        data.setTag("materials", entries);
        if (CrucibleToolRecycling.isTool(output)) {
            // Bind the account to this head. A blindly copied tag must not make
            // a different-material tool recover the original head's materials.
            data.setString("toolMaterial", root.getCompoundTag("GT.Tool").getString("Material"));
            NBTTagList power = encode(powerMaterials, output.getCount());
            if (power != null) data.setTag("powerMaterials", power);
        }
        root.setTag(TAG, data);
    }

    static NBTTagList encode(List<MaterialStack> materials, int outputCount) {
        NBTTagList entries = new NBTTagList();
        for (MaterialStack component : materials) {
            long amount = CrucibleToolRecycling.exactPerOutputAmount(component.amount, outputCount);
            if (amount <= 0) return null;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("material", component.material.getRegistryName());
            entry.setLong("amount", amount);
            entries.appendTag(entry);
        }
        return entries.tagCount() == 0 ? null : entries;
    }

    /** Called only after the host's actual two-input replacement recipe succeeds. */
    public static void captureHeadReplacement(ItemStack output, IInventory inventory) {
        if (output.isEmpty() || !CrucibleToolRecycling.isTool(output)) return;
        NBTTagCompound root = output.getTagCompound();
        root.setTag(TAG, new NBTTagCompound());
        ItemStack oldTool = ItemStack.EMPTY, head = ItemStack.EMPTY;
        int occupied = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (input.isEmpty()) continue;
            if (++occupied > 2) return;
            if (input.getItem() instanceof IGTTool) {
                if (!oldTool.isEmpty()) return;
                oldTool = input.copy();
            } else {
                if (!head.isEmpty()) return;
                head = input.copy();
                head.setCount(1);
            }
        }
        if (occupied != 2 || oldTool.isEmpty() || head.isEmpty() ||
                resolveTool(oldTool, MetaTileEntityCrucible::resolveMaterial).isEmpty()) return;
        gregtech.api.unification.stack.UnificationEntry headIdentity =
                gregtech.api.unification.OreDictUnifier.getUnificationEntry(head);
        if (headIdentity == null || headIdentity.material == null ||
                !headIdentity.material.getRegistryName().equals(root.getCompoundTag("GT.Tool").getString("Material"))) {
            return;
        }
        NBTTagCompound original = oldTool.getTagCompound().getCompoundTag(TAG);
        // Old accounts without a separate power unit cannot safely be split.
        List<MaterialStack> power = parseList(original, "powerMaterials", MetaTileEntityCrucible::resolveMaterial);
        List<MaterialStack> originalMaterials = parse(oldTool.getTagCompound(), MetaTileEntityCrucible::resolveMaterial);
        if (power.isEmpty() || !isSubset(power, originalMaterials)) return;
        List<MaterialStack> replacement = componentMaterials(head);
        if (replacement.isEmpty()) return;
        List<MaterialStack> combined = CrucibleRecyclingOverride.combine(power, replacement);
        NBTTagList materials = encode(combined, output.getCount());
        NBTTagList retainedPower = encode(power, output.getCount());
        if (materials == null || retainedPower == null) return;
        NBTTagCompound data = new NBTTagCompound();
        bindItem(data, output);
        data.setString("toolMaterial", root.getCompoundTag("GT.Tool").getString("Material"));
        data.setTag("materials", materials);
        data.setTag("powerMaterials", retainedPower);
        root.setTag(TAG, data);
    }

    static boolean isSubset(List<MaterialStack> subset, List<MaterialStack> all) {
        for (MaterialStack component : subset) {
            long available = 0;
            for (MaterialStack candidate : all) {
                if (candidate.material == component.material) available = candidate.amount;
            }
            if (component.amount > available) return false;
        }
        return true;
    }

    static List<MaterialStack> resolveTool(ItemStack stack, Function<String, Material> resolver) {
        return resolveTool(stack, resolver, false);
    }

    private static List<MaterialStack> resolveTool(ItemStack stack, Function<String, Material> resolver,
                                                   boolean returningBrokenPowerUnit) {
        if (stack.isEmpty() || !CrucibleToolRecycling.isTool(stack)) return Collections.emptyList();
        ItemStack copy = stack.copy();
        if (copy.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ||
                copy.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return Collections.emptyList();
        }
        gregtech.api.capability.IElectricItem electric = copy.getCapability(
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (!(electric instanceof gregtech.api.capability.impl.ElectricItem) || electric.getMaxCharge() <= 0) {
            return Collections.emptyList();
        }
        NBTTagCompound root = copy.getTagCompound();
        NBTTagCompound metadata = root.copy();
        metadata.removeTag("GT.Tool");
        metadata.removeTag("GT.Behaviours");
        metadata.removeTag("DisallowContainerItem");
        if (!CrucibleElectricRecycling.hasStaticElectricTags(metadata)) return Collections.emptyList();
        NBTTagCompound toolMetadata = root.copy();
        toolMetadata.removeTag("Charge");
        toolMetadata.removeTag("MaxCharge");
        toolMetadata.removeTag("Infinite");
        toolMetadata.removeTag(TAG);
        if (!CrucibleToolRecycling.hasSafeToolTags(toolMetadata) || !hasMatchingToolMaterial(root) || !matchesItem(copy)) {
            return Collections.emptyList();
        }
        NBTTagCompound tool = root.getCompoundTag("GT.Tool");
        if (!tool.hasKey("MaxDurability", 3) || !tool.hasKey("Durability", 3)) return Collections.emptyList();
        int maximum = tool.getInteger("MaxDurability"), damage = tool.getInteger("Durability");
        if (!validToolDurability(maximum, damage, returningBrokenPowerUnit)) return Collections.emptyList();
        List<MaterialStack> account = parse(root, resolver);
        if (returningBrokenPowerUnit) return account;
        List<MaterialStack> result = new ArrayList<>();
        for (MaterialStack component : account) {
            long amount = CrucibleTransferLogic.remainingDurabilityMaterial(component.amount, damage, maximum);
            if (amount > 0) result.add(new MaterialStack(component.material, amount));
        }
        return result;
    }

    static boolean validToolDurability(int maximum, int damage, boolean returningBrokenPowerUnit) {
        return maximum > 0 && damage >= 0 && (returningBrokenPowerUnit || damage < maximum);
    }

    /** Enrich the host's existing break result; do not create or deliver another item. */
    public static ItemStack inheritBrokenPowerUnit(ItemStack hostResult, ItemStack original) {
        if (hostResult.isEmpty() || CrucibleToolRecycling.isTool(hostResult) ||
                !hostResult.copy().hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
            return hostResult;
        }
        ItemStack result = hostResult.copy();
        NBTTagCompound root = result.getTagCompound();
        if (root == null) { root = new NBTTagCompound(); result.setTagCompound(root); }
        root.setTag(TAG, new NBTTagCompound());
        List<MaterialStack> full = resolveTool(original, MetaTileEntityCrucible::resolveMaterial, true);
        if (full.isEmpty()) return result;
        NBTTagCompound source = original.getTagCompound().getCompoundTag(TAG);
        List<MaterialStack> power = parseList(source, "powerMaterials", MetaTileEntityCrucible::resolveMaterial);
        if (power.isEmpty() || !isSubset(power, full)) return result;
        NBTTagList materials = encode(power, result.getCount());
        if (materials == null) return result;
        NBTTagCompound data = new NBTTagCompound();
        bindItem(data, result);
        data.setTag("materials", materials);
        root.setTag(TAG, data);
        // Capacity, charge and delivery are left to ToolEventHandlers.
        return result;
    }

    /** Crafting lacks the destroy-event capacity transfer; preserve the installed battery here. */
    public static ItemStack inheritCraftingBrokenPowerUnit(ItemStack hostResult, ItemStack original) {
        ItemStack result = inheritBrokenPowerUnit(hostResult, original);
        if (result == hostResult || result.isEmpty() || !matchesItem(result) ||
                parse(result.getTagCompound(), MetaTileEntityCrucible::resolveMaterial).isEmpty()) return result;
        gregtech.api.capability.IElectricItem source = original.copy().getCapability(
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        gregtech.api.capability.IElectricItem target = result.getCapability(
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (source != null && target instanceof gregtech.api.capability.impl.ElectricItem) {
            ((gregtech.api.capability.impl.ElectricItem) target).setMaxChargeOverride(source.getMaxCharge());
            // The host has just consumed the crafting energy. Never duplicate
            // the pre-use charge from original; only the battery capacity persists.
        }
        return result;
    }

    static boolean hasMatchingToolMaterial(NBTTagCompound root) {
        if (root == null || !root.hasKey(TAG, 10) || !root.hasKey("GT.Tool", 10)) return false;
        NBTTagCompound account = root.getCompoundTag(TAG), tool = root.getCompoundTag("GT.Tool");
        return account.hasKey("toolMaterial", 8) && tool.hasKey("Material", 8) &&
                !account.getString("toolMaterial").isEmpty() &&
                account.getString("toolMaterial").equals(tool.getString("Material"));
    }

    static void bindItem(NBTTagCompound data, ItemStack stack) {
        data.setInteger("version", 2);
        if (stack.getItem().getRegistryName() != null) {
            data.setString("item", stack.getItem().getRegistryName().toString());
        }
        data.setInteger("metadata", stack.getMetadata());
    }

    static boolean matchesItem(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound() || !stack.getTagCompound().hasKey(TAG, 10)) return false;
        NBTTagCompound data = stack.getTagCompound().getCompoundTag(TAG);
        if (!data.hasKey("version", 3)) return false;
        // Version 1 predates item binding. Preserve its existing recovery only;
        // new crafting, replacement and break results always write version 2.
        if (data.getInteger("version") == 1) return true;
        return data.getInteger("version") == 2 && data.hasKey("item", 8) &&
                data.hasKey("metadata", 3) && stack.getItem().getRegistryName() != null &&
                data.getString("item").equals(stack.getItem().getRegistryName().toString()) &&
                data.getInteger("metadata") == stack.getMetadata();
    }

    private static List<MaterialStack> componentMaterials(ItemStack unit) {
        return CrucibleComponentProvenance.componentMaterials(unit);
    }

    static List<MaterialStack> parse(NBTTagCompound root, Function<String, Material> resolver) {
        if (root == null || !root.hasKey(TAG, 10)) return Collections.emptyList();
        NBTTagCompound data = root.getCompoundTag(TAG);
        if (!data.hasKey("version", 3) || !data.hasKey("materials", 9)) return Collections.emptyList();
        int version = data.getInteger("version");
        if (version != 1 && version != 2) return Collections.emptyList();
        if (version == 2 && (!data.hasKey("item", 8) || data.getString("item").isEmpty() ||
                !data.hasKey("metadata", 3))) return Collections.emptyList();
        return parseList(data, "materials", resolver);
    }

    static List<MaterialStack> parseList(NBTTagCompound data, String key, Function<String, Material> resolver) {
        if (!data.hasKey(key, 9)) return Collections.emptyList();
        NBTTagList entries = data.getTagList(key, 10);
        List<MaterialStack> result = new ArrayList<>();
        for (int i = 0; i < entries.tagCount(); i++) {
            NBTTagCompound entry = entries.getCompoundTagAt(i);
            if (!entry.hasKey("material", 8) || !entry.hasKey("amount", 4)) return Collections.emptyList();
            Material material = resolver.apply(entry.getString("material"));
            long amount = entry.getLong("amount");
            if (material == null || material == Materials.NULL || material instanceof MarkerMaterial || amount <= 0) {
                return Collections.emptyList();
            }
            result.add(new MaterialStack(material, amount));
        }
        return CrucibleRecyclingOverride.combine(Collections.emptyList(), result);
    }
}
