package com.drppp.gt6addition.api.capability.impl;

import com.drppp.gt6addition.api.capability.interfaces.ILaserEnergy;

public class LaserEnergyHandler implements ILaserEnergy {

    private int outputLu = 0;
    private boolean isOutput = true;

    @Override
    public int getEnergyOutput() {
        return outputLu;
    }

    @Override
    public boolean isOutPut() {
        return isOutput;
    }

    @Override
    public void setOutPut(boolean outPut) {
        isOutput = outPut;
    }

    @Override
    public void setLuEnergy(int energy) {
        outputLu = energy;
    }

    @Override
    public void changeLuEnergy(int energy) {
        outputLu += energy;
        if (outputLu < 0) {
            outputLu = 0;
        }
    }
}
