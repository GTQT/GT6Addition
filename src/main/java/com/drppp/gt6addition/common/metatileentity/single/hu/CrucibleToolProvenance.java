package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.recipes.RecyclingHandler;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.RecyclingData;
import gregtech.common.crafting.GTShapedOreRecipe;
import gregtech.common.crafting.GTShapelessOreRecipe;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Per-craft material account when static OreDictionary alternatives are not equivalent. */
public final class CrucibleToolProvenance {
    static final String TAG = "gt6addition.toolMaterials";
    private static final Map<ResourceLocation, Boolean> CLEARING_RECIPES = new HashMap<>();

    private CrucibleToolProvenance() {}

    public static void rememberRecipe(ResourceLocation name, boolean clearing) {
        CLEARING_RECIPES.put(name, clearing);
    }

    public static void capture(IRecipe recipe, ItemStack output, IInventory inventory) {
        if (!(recipe instanceof GTShapedOreRecipe) && !(recipe instanceof GTShapelessOreRecipe)) return;
        Boolean clearing = CLEARING_RECIPES.get(recipe.getRegistryName());
        if (clearing == null) return;
        CrucibleComponentProvenance.captureCrafting(output, inventory, clearing);
        if (!CrucibleToolRecycling.isSafeTool(output) ||
                output.getTagCompound().hasKey(CrucibleRecyclingOverride.TAG)) return;
        NBTTagCompound root = output.getTagCompound();
        // Failure is explicit: never fall back to another assembly's static account.
        root.setTag(TAG, new NBTTagCompound());
        List<MaterialStack> totals = Collections.emptyList();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack input = inventory.getStackInSlot(i);
            if (input.isEmpty()) continue;
            ItemStack unit = input.copy();
            unit.setCount(1);
            if (!clearing && unit.getItem().hasContainerItem(unit.copy())) continue;
            if (unit.getMetadata() == GTValues.W || unit.hasTagCompound() || unit.isItemDamaged() ||
                    unit.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ||
                    unit.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) ||
                    unit.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) return;
            RecyclingData data = RecyclingHandler.getRecyclingIngredients(1,
                    Collections.singletonList(new GTRecipeItemInput(unit)), null);
            if (data == null || data.getMaterials().isEmpty()) return;
            for (MaterialStack component : data.getMaterials()) {
                if (component == null || component.material == null || component.material == Materials.NULL ||
                        component.material instanceof MarkerMaterial || component.amount <= 0) return;
            }
            totals = CrucibleRecyclingOverride.combine(totals, data.getMaterials());
            if (totals.isEmpty()) return;
        }
        if (totals.isEmpty()) return;
        NBTTagList entries = new NBTTagList();
        for (MaterialStack component : totals) {
            long amount = CrucibleToolRecycling.exactPerOutputAmount(component.amount, output.getCount());
            if (amount <= 0) return;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("material", component.material.getRegistryName());
            entry.setLong("amount", amount);
            entries.appendTag(entry);
        }
        NBTTagCompound account = new NBTTagCompound();
        account.setInteger("version", 1);
        account.setString("item", output.getItem().getRegistryName().toString());
        account.setInteger("metadata", output.getMetadata());
        account.setString("toolMaterial", root.getCompoundTag("GT.Tool").getString("Material"));
        account.setTag("materials", entries);
        root.setTag(TAG, account);
    }

    static List<MaterialStack> resolve(ItemStack stack) {
        if (!CrucibleToolRecycling.isSafeTool(stack)) return Collections.emptyList();
        NBTTagCompound root = stack.getTagCompound(), account = root.getCompoundTag(TAG);
        NBTTagCompound tool = root.getCompoundTag("GT.Tool");
        if (!account.hasKey("version", 3) || account.getInteger("version") != 1 ||
                !account.hasKey("item", 8) || stack.getItem().getRegistryName() == null ||
                !account.getString("item").equals(stack.getItem().getRegistryName().toString()) ||
                !account.hasKey("metadata", 3) || account.getInteger("metadata") != stack.getMetadata() ||
                !account.hasKey("toolMaterial", 8) || !account.getString("toolMaterial").equals(tool.getString("Material")) ||
                !tool.hasKey("Durability", 3) || !tool.hasKey("MaxDurability", 3)) return Collections.emptyList();
        int damage = tool.getInteger("Durability"), maximum = tool.getInteger("MaxDurability");
        if (maximum <= 0 || damage < 0 || damage >= maximum) return Collections.emptyList();
        List<MaterialStack> materials = CrucibleElectricProvenance.parseList(account, "materials",
                MetaTileEntityCrucible::resolveMaterial);
        List<MaterialStack> result = new ArrayList<>();
        for (MaterialStack component : materials) {
            long amount = CrucibleTransferLogic.remainingDurabilityMaterial(component.amount, damage, maximum);
            if (amount > 0) result.add(new MaterialStack(component.material, amount));
        }
        return result;
    }
}
