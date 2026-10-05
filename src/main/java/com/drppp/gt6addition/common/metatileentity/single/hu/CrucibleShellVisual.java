package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

/** GT6 Smeltery shell passes and its client-visible overheat warning. */
public final class CrucibleShellVisual {
    private CrucibleShellVisual() {}

    public static boolean isWarning(long temperature, int maximumTemperature) {
        // Equivalent to GT6's temperature + 100 > maximum, without long overflow.
        return temperature > (long) maximumTemperature - 100L;
    }

    public static int color(int baseColor, boolean warning) {
        if (!warning) return baseColor;
        int red = clamp(((baseColor >> 16) & 0xFF) * 2 + 50);
        int green = clamp(((baseColor >> 8) & 0xFF) * 2 + 50);
        int blue = clamp((baseColor & 0xFF) / 2 + 50);
        return (red << 16) | (green << 8) | blue;
    }

    public static boolean isEmissive(boolean warning, @Nullable Material vesselMaterial) {
        return warning || (vesselMaterial != null && vesselMaterial.hasFlag(MaterialFlags.GLOWING));
    }

    public static boolean shouldRenderFace(int pass, EnumFacing face) {
        // Smeltery.getTexture2: X walls omit Z/bottom; Z walls omit X/bottom;
        // the bottom plate only draws its top and bottom faces.
        switch (pass) {
            case 0: case 2:
                return face == EnumFacing.UP || face.getAxis() == EnumFacing.Axis.X;
            case 1: case 3:
                return face == EnumFacing.UP || face.getAxis() == EnumFacing.Axis.Z;
            case 4:
                return face.getAxis() == EnumFacing.Axis.Y;
            default:
                return false;
        }
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
