package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.RecyclingData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** Material recovery for CEu electric items whose material identity is static. */
final class CrucibleElectricRecycling {
    private CrucibleElectricRecycling() {}

    static List<MaterialStack> resolve(ItemStack stack, Function<String, Material> resolver) {
        if (stack.isEmpty() || CrucibleToolRecycling.isTool(stack) || stack.isItemDamaged()) {
            return Collections.emptyList();
        }
        // Query only a copy: a third-party capability getter is not necessarily pure.
        ItemStack pristine = stack.copy();
        pristine.setCount(1);
        if (pristine.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ||
                pristine.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return Collections.emptyList();
        }
        IElectricItem electric = pristine.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (!(electric instanceof ElectricItem)) return Collections.emptyList();
        NBTTagCompound root = pristine.getTagCompound();
        if (!hasStaticElectricTags(root)) return Collections.emptyList();
        boolean provenance = root != null && root.hasKey(CrucibleElectricProvenance.TAG);
        boolean explicit = provenance || root != null && root.hasKey(CrucibleRecyclingOverride.TAG);
        long actualCapacity = electric.getMaxCharge();
        if (actualCapacity <= 0) return Collections.emptyList();
        if (root != null) {
            root.removeTag("Charge");
            root.removeTag("MaxCharge");
            root.removeTag("Infinite");
        }
        long nativeCapacity = electric.getMaxCharge();
        // Power units can contain different batteries. Their MaxCharge override
        // cannot be ignored when resolving a static material recipe.
        if (!hasResolvedCapacity(actualCapacity, nativeCapacity, explicit)) return Collections.emptyList();
        if (provenance) {
            if (!CrucibleElectricProvenance.matchesItem(pristine)) return Collections.emptyList();
            return CrucibleElectricProvenance.parse(root, resolver);
        }
        if (explicit) {
            return CrucibleRecyclingOverride.parse(root, resolver);
        }
        RecyclingData data = GregTechAPI.RECYCLING_MANAGER.getRecyclingData(pristine);
        if (data == null || data.getMaterials().isEmpty()) return Collections.emptyList();
        List<MaterialStack> result = new ArrayList<>();
        for (MaterialStack component : data.getMaterials()) {
            if (component == null || component.material == null || component.material == Materials.NULL ||
                    component.material instanceof MarkerMaterial || component.amount <= 0) {
                return Collections.emptyList();
            }
            result.add(new MaterialStack(component.material, component.amount));
        }
        // Charge is not a material and does not become HU. Only a committed
        // insertion consumes the original item, together with its stored EU.
        return result;
    }

    static boolean hasResolvedCapacity(long actual, long nativeCapacity, boolean explicit) {
        return actual > 0 && nativeCapacity > 0 && (explicit || actual == nativeCapacity);
    }

    static boolean hasStaticElectricTags(NBTTagCompound root) {
        if (root == null) return true;
        for (String key : root.getKeySet()) {
            switch (key) {
                case "Charge":
                    if (!root.hasKey(key, 4) || root.getLong(key) < 0) return false;
                    break;
                case "MaxCharge":
                    if (!root.hasKey(key, 4) || root.getLong(key) <= 0) return false;
                    break;
                case "Infinite":
                    if (!root.hasKey(key, 1) || (root.getByte(key) != 0 && root.getByte(key) != 1)) return false;
                    break;
                case "display":
                case "ench":
                case "RepairCost":
                case "Unbreakable":
                case "HideFlags":
                case CrucibleRecyclingOverride.TAG:
                case CrucibleElectricProvenance.TAG:
                    break;
                default: return false;
            }
        }
        return true;
    }
}
