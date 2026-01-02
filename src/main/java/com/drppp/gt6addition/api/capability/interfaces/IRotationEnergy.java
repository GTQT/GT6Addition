package com.drppp.gt6addition.api.capability.interfaces;

public interface IRotationEnergy {
    int getEnergyOutput();
    boolean isOutPut();
    void setOutPut(boolean b);
    void setRuEnergy(int energy);
    void changeRuEnergy(int energy);
}