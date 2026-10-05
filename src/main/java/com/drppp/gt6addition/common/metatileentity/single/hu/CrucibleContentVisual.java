package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

/** Appearance of internal material contents, not an external fill/drain projection. */
public final class CrucibleContentVisual {
    private CrucibleContentVisual() {}

    @Nullable
    public static FluidStack liquidAppearance(@Nullable Material material) {
        if (material == null) return null;
        // These contents have a known vanilla liquid identity even when the
        // host changes its default storage key. No quantity conversion here.
        if (material == Materials.Water) return new FluidStack(FluidRegistry.WATER, 1);
        if (material == Materials.Lava) return new FluidStack(FluidRegistry.LAVA, 1);
        if (!material.hasProperty(PropertyKey.FLUID)) return null;
        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        Fluid fluid = property.get(FluidStorageKeys.MOLTEN);
        if (!isLiquid(fluid)) fluid = property.get(FluidStorageKeys.LIQUID);
        if (!isLiquid(fluid)) {
            // Preserve explicitly liquid custom primary keys, but never use a
            // GAS/PLASMA default to draw an internal melt.
            if (property.getPrimaryKey() == null || property.getPrimaryKey() == FluidStorageKeys.GAS ||
                    property.getPrimaryKey() == FluidStorageKeys.PLASMA) return null;
            fluid = property.get(property.getPrimaryKey());
            if (fluid != null && (fluid == property.get(FluidStorageKeys.GAS) ||
                    fluid == property.get(FluidStorageKeys.PLASMA))) return null;
        }
        return isLiquid(fluid) ? new FluidStack(fluid, 1) : null;
    }

    private static boolean isLiquid(@Nullable Fluid fluid) {
        return fluid != null && !fluid.isGaseous() &&
                (!(fluid instanceof AttributedFluid) || ((AttributedFluid) fluid).getState() == FluidState.LIQUID);
    }

    public static int color(Material material, boolean molten) {
        FluidStack liquid = molten ? liquidAppearance(material) : null;
        if (liquid != null) {
            int rgb = liquid.getFluid().getColor(liquid) & 0xFFFFFF;
            if (rgb != 0xFFFFFF) return rgb;
        }
        return material.getMaterialRGB() & 0xFFFFFF;
    }
}
