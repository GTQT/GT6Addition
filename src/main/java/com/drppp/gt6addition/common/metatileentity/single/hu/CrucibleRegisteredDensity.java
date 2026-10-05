package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraftforge.fluids.Fluid;

/** Last resort only: explicit material tables and composition take precedence. */
final class CrucibleRegisteredDensity {
    private CrucibleRegisteredDensity() {}

    static double get(Material material) {
        if (material == null || !material.hasProperty(PropertyKey.FLUID)) return fallback();
        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        double density = liquidDensity(property.get(FluidStorageKeys.MOLTEN));
        if (density <= 0.0D) density = liquidDensity(property.get(FluidStorageKeys.LIQUID));
        if (density <= 0.0D) {
            Fluid gas = property.get(FluidStorageKeys.GAS);
            if (gas != property.get(FluidStorageKeys.PLASMA)) density = positiveDensity(gas);
        }
        if (density <= 0.0D) {
            FluidStorageKey key = property.getPrimaryKey();
            // Standard keys have already been examined with their own phase
            // rules. A default key must not bypass a rejected liquid binding.
            if (key != null && key != FluidStorageKeys.PLASMA && key != FluidStorageKeys.GAS &&
                    key != FluidStorageKeys.MOLTEN && key != FluidStorageKeys.LIQUID) {
                Fluid fluid = property.get(key);
                if (fluid != property.get(FluidStorageKeys.PLASMA)) density = positiveDensity(fluid);
            }
        }
        return density > 0.0D ? density : fallback();
    }

    private static double liquidDensity(Fluid fluid) {
        if (fluid == null || fluid.isGaseous() ||
                (fluid instanceof AttributedFluid && ((AttributedFluid) fluid).getState() != FluidState.LIQUID))
            return 0.0D;
        return positiveDensity(fluid);
    }

    private static double positiveDensity(Fluid fluid) {
        if (fluid == null ||
                (fluid instanceof AttributedFluid && ((AttributedFluid) fluid).getState() == FluidState.PLASMA))
            return 0.0D;
        // Negative Forge density encodes buoyancy, not a negative physical mass.
        // CEu also uses generic -100/-100000 defaults; there is no reliable
        // provenance to invert arbitrary third-party values into a bulk density.
        int density = fluid.getDensity();
        return density > 0 ? density : 0.0D;
    }

    private static double fallback() {
        return CrucibleTransferLogic.DEFAULT_UNKNOWN_MATERIAL_DENSITY_KG_PER_CUBIC_METER;
    }
}
