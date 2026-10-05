package com.drppp.gt6addition.api.crucible;

import gregtech.api.unification.material.Material;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public interface ICrucibleMold {

    boolean isMoldInputSide(@Nullable EnumFacing side);

    long getMoldMaxTemperature();

    /** Estimated demand in source-material units; not a substitute for fill simulation. */
    long getMoldRequiredMaterialUnits(@Nullable Material material);

    /** Returns consumed source-material units. Simulation must not mutate inventory or the world. */
    long fillMold(Material material, long materialAmount, long temperature,
                  @Nullable EnumFacing side, boolean simulate);
}
