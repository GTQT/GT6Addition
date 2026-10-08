package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** GT6 OreDictMaterialStack.saveList format with explicit snapshot ID mapping. */
final class CrucibleRecyclingOverride {
    static final String TAG = "gt.recycling.mats";
    private static final long GT6_UNIT = 648648000L; // gregapi.data.CS.U

    private CrucibleRecyclingOverride() {}

    static long convertAmount(long gt6Amount) {
        if (gt6Amount <= 0) return 0;
        BigInteger amount = BigInteger.valueOf(gt6Amount).multiply(BigInteger.valueOf(GTValues.M))
                .divide(BigInteger.valueOf(GT6_UNIT));
        return amount.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? -1 : amount.longValue();
    }

    static List<MaterialStack> parse(NBTTagCompound root, Function<String, Material> resolver) {
        if (root == null || !root.hasKey(TAG, 10)) return Collections.emptyList();
        NBTTagCompound list = root.getCompoundTag(TAG);
        if (!list.hasKey("size", 99)) return Collections.emptyList();
        int size = list.getInteger("size");
        // Bound iteration by the actual supplied compound, not an arbitrary
        // component limit or an untrusted NBT size alone.
        if (size <= 0 || size > list.getKeySet().size() - 1) return Collections.emptyList();
        List<MaterialStack> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String index = Integer.toString(i);
            if (!list.hasKey(index, 10)) return Collections.emptyList();
            NBTTagCompound entry = list.getCompoundTag(index);
            if (!entry.hasKey("a", 99)) return Collections.emptyList();
            String name;
            // GT6 loads i before m. Preserve that precedence, including refusal
            // of unmapped IDs rather than falling back to a conflicting name.
            if (entry.hasKey("i")) {
                if (!entry.hasKey("i", 99)) return Collections.emptyList();
                long id = entry.getLong("i");
                if (id <= 0 || id > Short.MAX_VALUE) return Collections.emptyList();
                name = GT6MaterialIdentity.recyclingName((int) id);
            } else {
                if (!entry.hasKey("m", 8)) return Collections.emptyList();
                name = entry.getString("m");
            }
            if (name == null || name.isEmpty()) return Collections.emptyList();
            Material material = resolver.apply(GT6MaterialIdentity.hostRecyclingName(name));
            long amount = convertAmount(entry.getLong("a"));
            if (material == null || material == Materials.NULL || amount <= 0) return Collections.emptyList();
            result.add(new MaterialStack(material, amount));
        }
        return result;
    }

    /** GT6 OreDictItemData(tData, rData) adds both material lists. */
    static List<MaterialStack> combine(List<MaterialStack> itemData, List<MaterialStack> overrideData) {
        Map<Material, Long> amounts = new LinkedHashMap<>();
        for (List<MaterialStack> data : java.util.Arrays.asList(itemData, overrideData)) {
            for (MaterialStack component : data) {
                if (component == null || component.material == null || component.material == Materials.NULL ||
                        component.amount <= 0) return Collections.emptyList();
                long previous = amounts.getOrDefault(component.material, 0L);
                if (!CrucibleTransferLogic.canMergeMaterialAmounts(previous, component.amount)) {
                    return Collections.emptyList();
                }
                amounts.put(component.material, previous + component.amount);
            }
        }
        List<MaterialStack> result = new ArrayList<>();
        for (Map.Entry<Material, Long> entry : amounts.entrySet()) {
            result.add(new MaterialStack(entry.getKey(), entry.getValue()));
        }
        result.sort((first, second) -> Long.compare(second.amount, first.amount));
        return result;
    }
}
