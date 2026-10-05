package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GregTechAPI;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/** Resolve by registered fluid identity, not a fluid-name prefix heuristic. */
final class CrucibleFluidInput {
    // Distinguish an ambiguous binding from "not this material" while
    // scanning the registry. A later valid candidate cannot mask a conflict.
    private static final CrucibleFluidInput AMBIGUOUS = new CrucibleFluidInput(null, 0, false);
    final Material material;
    final int unit;
    final boolean plasma;

    private CrucibleFluidInput(Material material, int unit, boolean plasma) {
        this.material = material;
        this.unit = unit;
        this.plasma = plasma;
    }

    static CrucibleFluidInput resolve(FluidStack stack) {
        if (stack == null || stack.getFluid() == null) return null;
        Fluid fluid = stack.getFluid();
        if ("water".equals(fluid.getName())) return new CrucibleFluidInput(Materials.Water, 1000, false);
        if ("lava".equals(fluid.getName())) {
            return new CrucibleFluidInput(Materials.Lava, CrucibleFluidUnits.fluidUnit("lava"), false);
        }
        CrucibleFluidInput selected = null;
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            CrucibleFluidInput candidate = lookup(material, fluid);
            if (candidate == AMBIGUOUS) return null;
            if (candidate == null) continue;
            if (selected != null && (selected.material != candidate.material || selected.unit != candidate.unit ||
                    selected.plasma != candidate.plasma)) return null;
            selected = candidate;
        }
        return selected;
    }

    static CrucibleFluidInput forMaterial(Material material, Fluid fluid) {
        CrucibleFluidInput candidate = lookup(material, fluid);
        return candidate == AMBIGUOUS ? null : candidate;
    }

    private static CrucibleFluidInput lookup(Material material, Fluid fluid) {
        if (material == null || fluid == null || !material.hasProperty(PropertyKey.FLUID)) return null;
        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        CrucibleFluidInput selected = null;
        if (property.get(FluidStorageKeys.PLASMA) == fluid) {
            selected = new CrucibleFluidInput(material, CrucibleFluidUnits.plasmaUnit(material.getName()), true);
        }
        if (property.get(FluidStorageKeys.GAS) == fluid) {
            selected = mergeBinding(selected,
                    new CrucibleFluidInput(material, CrucibleFluidUnits.gasUnit(material.getName()), false));
        }
        if (property.get(FluidStorageKeys.LIQUID) == fluid ||
                property.get(FluidStorageKeys.MOLTEN) == fluid) {
            selected = mergeBinding(selected,
                    new CrucibleFluidInput(material, CrucibleFluidUnits.fluidUnit(material.getName()), false));
        }
        // The default fluid is not another LIQUID binding when its explicit
        // key already says GAS/PLASMA. Preserve custom primary-key fallback
        // only when no standard phase key has identified this fluid.
        if (selected == null && material.getFluid() == fluid) {
            return new CrucibleFluidInput(material, CrucibleFluidUnits.fluidUnit(material.getName()), false);
        }
        return selected;
    }

    private static CrucibleFluidInput mergeBinding(CrucibleFluidInput selected, CrucibleFluidInput candidate) {
        if (selected == null) return candidate;
        if (selected == AMBIGUOUS || selected.unit != candidate.unit || selected.plasma != candidate.plasma)
            return AMBIGUOUS;
        return selected;
    }
}
