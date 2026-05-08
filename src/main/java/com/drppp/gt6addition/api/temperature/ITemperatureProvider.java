package com.drppp.gt6addition.api.temperature;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public interface ITemperatureProvider {

    long getTemperatureValue(@Nullable EnumFacing side);

    long getTemperatureMax(@Nullable EnumFacing side);
}
