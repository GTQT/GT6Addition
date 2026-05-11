package com.drppp.gt6addition.api.capability.interfaces;

public interface ILaserEnergy {

    int getEnergyOutput();

    boolean isOutPut();

    void setOutPut(boolean outPut);

    void setLuEnergy(int energy);

    void changeLuEnergy(int energy);
}
