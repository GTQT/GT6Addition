package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GregTechAPI;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
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
        // Material/phase resolution is based on the registered Fluid object.
        // This path cannot preserve arbitrary per-stack payload when it turns
        // the fluid into a material entry, so tagged variants must be rejected.
        if (stack == null || stack.getFluid() == null || stack.tag != null) return null;
        Fluid fluid = stack.getFluid();
        if (fluid == FluidRegistry.WATER) return new CrucibleFluidInput(Materials.Water, 1000, false);
        if (fluid == FluidRegistry.LAVA) {
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

    /** GT6 Smeltery extraction uses the material's liquid binding, not its default fluid key. */
    static Fluid liquidFluid(Material material) {
        if (material == Materials.Water) return FluidRegistry.WATER;
        if (material == Materials.Lava) return FluidRegistry.LAVA;
        if (material == null || !material.hasProperty(PropertyKey.FLUID)) return null;
        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        Fluid molten = property.get(FluidStorageKeys.MOLTEN);
        if (isLiquid(molten)) return molten;
        Fluid liquid = property.get(FluidStorageKeys.LIQUID);
        if (isLiquid(liquid)) return liquid;
        FluidStorageKey primary = property.getPrimaryKey();
        if (primary != null && primary != FluidStorageKeys.GAS && primary != FluidStorageKeys.PLASMA &&
                primary != FluidStorageKeys.MOLTEN && primary != FluidStorageKeys.LIQUID) {
            Fluid fallback = property.get(primary);
            if (isLiquid(fallback)) return fallback;
        }
        return null;
    }

    static int liquidUnit(Material material) {
        if (material == Materials.Water) return 1000;
        if (material == Materials.Lava) return CrucibleFluidUnits.fluidUnit("lava");
        Fluid fluid = liquidFluid(material);
        CrucibleFluidInput input = fluid == null ? null : lookup(material, fluid);
        return input == null || input == AMBIGUOUS || input.plasma ? 0 : input.unit;
    }

    static FluidStack liquidStack(Material material, int amount) {
        Fluid fluid = amount <= 0 ? null : liquidFluid(material);
        return fluid == null ? null : new FluidStack(fluid, amount);
    }

    private static boolean isLiquid(Fluid fluid) {
        return fluid != null && !fluid.isGaseous() &&
                (!(fluid instanceof AttributedFluid) || ((AttributedFluid) fluid).getState() == FluidState.LIQUID);
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
            if (fluid instanceof AttributedFluid) {
                FluidState state = ((AttributedFluid) fluid).getState();
                if (state == FluidState.PLASMA) {
                    return new CrucibleFluidInput(material, CrucibleFluidUnits.plasmaUnit(material.getName()), true);
                }
                if (state == FluidState.GAS) {
                    return new CrucibleFluidInput(material, CrucibleFluidUnits.gasUnit(material.getName()), false);
                }
            }
            if (fluid.isGaseous()) {
                return new CrucibleFluidInput(material, CrucibleFluidUnits.gasUnit(material.getName()), false);
            }
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
