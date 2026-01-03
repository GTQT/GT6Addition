package com.drppp.gt6addition.api.baseMTile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public interface IMutiEnergyProxy {
    int getTire();
    int getEnergy();
    void setEnergy(int energy);
    void changeEnergy(int energy);
    boolean getNearEnergyToMyself(TileEntity te,EnumFacing facing);
}
