package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.recipes.RecyclingHandler;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.RecyclingData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** NBT-material-specific tool weights captured from real CEu recipes. */
public final class CrucibleToolRecycling {
    private static final Map<Item, Map<String, List<MaterialStack>>> RECIPES = new HashMap<>();
    private static final Map<Item, Set<String>> CONFLICTS = new HashMap<>();

    private CrucibleToolRecycling() {}

    public static void capture(ItemStack output, boolean clearNbt, Object[] recipe) {
        if (!isSafeTool(output) || output.getCount() <= 0 || recipe == null) return;
        // Use the host's actual symbol expansion (including returned tools),
        // not RecyclingHandler's partial, first-alternative shaped accounting.
        Object[] normalized = ModHandler.finalizeShapedRecipeInput(recipe.clone());
        Map<Character, Integer> counts = shapedSymbolCounts(normalized);
        if (counts == null) return;
        int index = 0;
        while (index < normalized.length && normalized[index] instanceof String) index++;
        Map<Character, Object> ingredients = new HashMap<>();
        while (index < normalized.length) {
            if (index + 1 >= normalized.length || !(normalized[index] instanceof Character)) return;
            ingredients.put((Character) normalized[index], normalized[index + 1]);
            index += 2;
        }
        List<GTRecipeInput> inputs = new ArrayList<>();
        for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
            Object ingredient = ingredients.get(entry.getKey());
            ItemStack[] alternatives = ingredientStacks(ingredient);
            if (alternatives == null || alternatives.length == 0) return;
            if (ingredient instanceof String && isReturnedCraftingTool((String) ingredient, clearNbt)) {
                for (ItemStack tool : alternatives) {
                    if (tool.isEmpty() || !tool.getItem().hasContainerItem(tool.copy())) return;
                }
                continue;
            }
            inputs.add(new GTRecipeItemInput(alternatives, entry.getValue()));
        }
        captureInputs(output, inputs);
    }

    /** Count occupied slots, never the count on an ingredient description. */
    static Map<Character, Integer> shapedSymbolCounts(Object[] recipe) {
        if (recipe == null) return null;
        Map<Character, Integer> counts = new HashMap<>();
        int rows = 0;
        while (rows < recipe.length && recipe[rows] instanceof String) {
            String row = (String) recipe[rows++];
            if (rows > 3 || row.isEmpty() || row.length() > 3) return null;
            for (char symbol : row.toCharArray()) {
                if (symbol != ' ') counts.put(symbol, counts.getOrDefault(symbol, 0) + 1);
            }
        }
        return rows == 0 || counts.isEmpty() ? null : counts;
    }

    private static ItemStack[] ingredientStacks(Object ingredient) {
        if (ingredient instanceof String) {
            return OreDictionary.getOres((String) ingredient, false).toArray(new ItemStack[0]);
        }
        if (ingredient instanceof ItemStack) return new ItemStack[]{(ItemStack) ingredient};
        if (ingredient instanceof Item) return new ItemStack[]{new ItemStack((Item) ingredient, 1, GTValues.W)};
        if (ingredient instanceof Block) return new ItemStack[]{new ItemStack((Block) ingredient, 1, GTValues.W)};
        return null;
    }

    /** Called after CEu has normalized and registered a shapeless recipe. */
    public static void captureShapeless(ItemStack output, boolean clearNbt, Object[] recipe) {
        if (!isSafeTool(output) || output.getCount() <= 0 || recipe == null) return;
        List<GTRecipeInput> inputs = new ArrayList<>();
        for (Object ingredient : recipe) {
            ItemStack[] alternatives;
            if (ingredient instanceof String) {
                String oreName = (String) ingredient;
                // Normal CEu crafting returns/damages these tools. Clearing
                // recipes return nothing, so they must not silently exclude them.
                List<ItemStack> ores = OreDictionary.getOres(oreName, false);
                if (ores.isEmpty()) return;
                if (isReturnedCraftingTool(oreName, clearNbt)) {
                    for (ItemStack tool : ores) {
                        if (tool.isEmpty() || !tool.getItem().hasContainerItem(tool.copy())) return;
                    }
                    continue;
                }
                alternatives = ores.toArray(new ItemStack[0]);
            } else if (ingredient instanceof ItemStack) {
                alternatives = new ItemStack[]{(ItemStack) ingredient};
            } else if (ingredient instanceof Item) {
                alternatives = new ItemStack[]{new ItemStack((Item) ingredient, 1, GTValues.W)};
            } else if (ingredient instanceof Block) {
                alternatives = new ItemStack[]{new ItemStack((Block) ingredient, 1, GTValues.W)};
            } else return;
            // One shapeless entry consumes one crafting slot, regardless of the
            // count attached to the stack used to describe that ingredient.
            inputs.add(new GTRecipeItemInput(alternatives, 1));
        }
        captureInputs(output, inputs);
    }

    static boolean isReturnedCraftingTool(String oreName, boolean clearNbt) {
        return !clearNbt && oreName != null &&
                (oreName.startsWith("tool") || oreName.startsWith("craftingTool"));
    }

    /** Machine inputs must have a single unambiguous material account. */
    public static void captureMachineRecipe(Recipe recipe) {
        if (recipe == null || recipe.getOutputs().size() != 1) return;
        ItemStack output = recipe.getOutputs().get(0);
        if (!isSafeTool(output) || output.getCount() <= 0 ||
                !recipe.getChancedOutputs().getChancedEntries().isEmpty() ||
                !recipe.getFluidOutputs().isEmpty() ||
                !recipe.getChancedFluidOutputs().getChancedEntries().isEmpty() ||
                !recipe.getFluidInputs().isEmpty()) return;
        captureInputs(output, recipe.getInputs());
    }

    private static void captureInputs(ItemStack output, List<GTRecipeInput> inputs) {
        // CEu's recycling helper chooses only the first alternative and silently
        // omits unknown ingredients. Check every consumable before using its data.
        Map<Material, Long> totals = new HashMap<>();
        for (GTRecipeInput input : inputs) {
            if (input == null) return;
            if (input.isNonConsumable()) continue;
            if (input.getAmount() <= 0 || input.hasNBTMatchingCondition()) return;
            ItemStack[] alternatives = input.getInputStacks();
            if (alternatives == null || alternatives.length == 0) return;
            List<MaterialStack> first = null;
            for (ItemStack alternative : alternatives) {
                if (alternative == null || alternative.isEmpty() || alternative.hasTagCompound() ||
                        alternative.getMetadata() == GTValues.W || alternative.isItemDamaged() ||
                        alternative.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ||
                        alternative.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) ||
                        alternative.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) return;
                ItemStack unit = alternative.copy();
                unit.setCount(1);
                RecyclingData data = RecyclingHandler.getRecyclingIngredients(1,
                        Collections.singletonList(new GTRecipeItemInput(unit)), null);
                if (data == null || data.getMaterials().isEmpty()) return;
                for (MaterialStack component : data.getMaterials()) {
                    if (component == null || component.material == null || component.amount <= 0) return;
                }
                if (first == null) first = data.getMaterials();
                else if (!sameMaterials(first, data.getMaterials())) return;
            }
            for (MaterialStack component : first) {
                if (component.amount > Long.MAX_VALUE / input.getAmount()) return;
                long amount = component.amount * input.getAmount();
                long previous = totals.getOrDefault(component.material, 0L);
                if (amount > Long.MAX_VALUE - previous) return;
                totals.put(component.material, previous + amount);
            }
        }
        if (totals.isEmpty()) return;
        List<MaterialStack> materials = new ArrayList<>();
        for (Map.Entry<Material, Long> entry : totals.entrySet()) {
            // A fractional internal unit cannot be stored exactly per output.
            long amount = exactPerOutputAmount(entry.getValue(), output.getCount());
            if (amount <= 0) return;
            materials.add(new MaterialStack(entry.getKey(), amount));
        }
        remember(output, materials);
    }

    static long exactPerOutputAmount(long total, int outputCount) {
        return total <= 0 || outputCount <= 0 || total % outputCount != 0 ? 0 : total / outputCount;
    }

    private static void remember(ItemStack output, List<MaterialStack> components) {
        String key = key(output);
        List<MaterialStack> materials = new ArrayList<>();
        for (MaterialStack component : components) {
            if (component == null || component.material == null || component.amount <= 0) return;
            materials.add(new MaterialStack(component.material, component.amount));
        }
        Map<String, List<MaterialStack>> byMaterial = RECIPES.computeIfAbsent(output.getItem(), i -> new HashMap<>());
        List<MaterialStack> previous = byMaterial.get(key);
        if (previous != null && !sameMaterials(previous, materials)) {
            CONFLICTS.computeIfAbsent(output.getItem(), i -> new HashSet<>()).add(key);
            return;
        }
        byMaterial.put(key, materials);
    }

    static boolean isTool(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("GT.Tool", 10);
    }

    static List<MaterialStack> resolve(ItemStack stack) {
        if (!isSafeTool(stack)) return Collections.emptyList();
        String key = key(stack);
        if (CONFLICTS.getOrDefault(stack.getItem(), Collections.emptySet()).contains(key)) {
            return Collections.emptyList();
        }
        List<MaterialStack> recipe = RECIPES.getOrDefault(stack.getItem(), Collections.emptyMap()).get(key);
        if (recipe == null) return Collections.emptyList();
        // Do not call ToolHelper.getToolTag/getToolMaterial: they can mutate a
        // malformed stack during a simulated insertion by creating/defaulting NBT.
        NBTTagCompound tool = stack.getTagCompound().getCompoundTag("GT.Tool");
        int maximum = tool.getInteger("MaxDurability");
        int damage = tool.getInteger("Durability");
        if (maximum <= 0 || damage < 0 || damage >= maximum) return Collections.emptyList();
        List<MaterialStack> result = new ArrayList<>();
        for (MaterialStack component : recipe) {
            long amount = CrucibleTransferLogic.remainingDurabilityMaterial(component.amount, damage, maximum);
            if (amount > 0) result.add(new MaterialStack(component.material, amount));
        }
        return result;
    }

    private static String key(ItemStack stack) {
        return stack.getMetadata() + ":" + stack.getTagCompound().getCompoundTag("GT.Tool").getString("Material");
    }

    private static boolean sameMaterials(List<MaterialStack> first, List<MaterialStack> second) {
        Map<gregtech.api.unification.material.Material, Long> totals = new HashMap<>();
        for (MaterialStack component : first) {
            long previous = totals.getOrDefault(component.material, 0L);
            if (component.amount > Long.MAX_VALUE - previous) return false;
            totals.put(component.material, previous + component.amount);
        }
        for (MaterialStack component : second) {
            long remaining = totals.getOrDefault(component.material, 0L);
            if (component.amount > remaining) return false;
            totals.put(component.material, remaining - component.amount);
        }
        for (long remaining : totals.values()) if (remaining != 0) return false;
        return true;
    }

    static boolean isSafeTool(ItemStack stack) {
        if (stack.isEmpty() || stack.getMetadata() == GTValues.W || !isTool(stack) ||
                stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ||
                stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) ||
                stack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) return false;
        NBTTagCompound root = stack.getTagCompound();
        return hasSafeToolTags(root);
    }

    /** Shared read-only metadata validation; callers separately validate capabilities. */
    static boolean hasSafeToolTags(NBTTagCompound root) {
        if (root == null || !root.hasKey("GT.Tool", 10)) return false;
        for (String tag : root.getKeySet()) {
            if (!"GT.Tool".equals(tag) && !"GT.Behaviours".equals(tag) && !"display".equals(tag) &&
                    !"ench".equals(tag) && !"RepairCost".equals(tag) && !"Unbreakable".equals(tag) &&
                    !"HideFlags".equals(tag) && !"DisallowContainerItem".equals(tag) &&
                    !CrucibleToolProvenance.TAG.equals(tag) &&
                    !CrucibleRecyclingOverride.TAG.equals(tag)) return false;
        }
        NBTTagCompound tool = root.getCompoundTag("GT.Tool");
        if (!tool.hasKey("Material", 8) || tool.getString("Material").isEmpty()) return false;
        for (String tag : tool.getKeySet()) {
            if (!"Material".equals(tag) && !"Durability".equals(tag) && !"MaxDurability".equals(tag) &&
                    !"ToolSpeed".equals(tag) && !"AttackDamage".equals(tag) && !"AttackSpeed".equals(tag) &&
                    !"Enchantability".equals(tag) && !"HarvestLevel".equals(tag) &&
                    !"LastCraftingUse".equals(tag)) return false;
            if (!"Material".equals(tag) && !tool.hasKey(tag, 99)) return false;
        }
        if (root.hasKey("GT.Behaviours")) {
            if (!root.hasKey("GT.Behaviours", 10)) return false;
            NBTTagCompound behaviours = root.getCompoundTag("GT.Behaviours");
            for (String tag : behaviours.getKeySet()) if (!behaviours.hasKey(tag, 99)) return false;
        }
        return true;
    }
}
