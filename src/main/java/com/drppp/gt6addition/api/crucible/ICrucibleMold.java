package com.drppp.gt6addition.api.crucible;

import gregtech.api.unification.material.Material;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public interface ICrucibleMold {

    boolean isMoldInputSide(@Nullable EnumFacing side);

    long getMoldMaxTemperature();

    long getMoldRequiredMaterialUnits(@Nullable Material material);

    long fillMold(Material material, long materialAmount, long temperature,
                  @Nullable EnumFacing side, boolean simulate);
}
