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
            CrucibleFluidInput candidate = forMaterial(material, fluid);
            if (candidate == null) continue;
            if (selected != null && (selected.material != candidate.material || selected.unit != candidate.unit ||
                    selected.plasma != candidate.plasma)) return null;
            selected = candidate;
        }
        return selected;
    }

    static CrucibleFluidInput forMaterial(Material material, Fluid fluid) {
        if (material == null || fluid == null || !material.hasProperty(PropertyKey.FLUID)) return null;
        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        if (property.get(FluidStorageKeys.PLASMA) == fluid) {
            return new CrucibleFluidInput(material, CrucibleFluidUnits.plasmaUnit(material.getName()), true);
        }
        if (property.get(FluidStorageKeys.GAS) == fluid) {
            return new CrucibleFluidInput(material, CrucibleFluidUnits.gasUnit(material.getName()), false);
        }
        if (property.get(FluidStorageKeys.LIQUID) == fluid ||
                property.get(FluidStorageKeys.MOLTEN) == fluid || material.getFluid() == fluid) {
            return new CrucibleFluidInput(material, CrucibleFluidUnits.fluidUnit(material.getName()), false);
        }
        return null;
    }
}
