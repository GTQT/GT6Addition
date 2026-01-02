package com.drppp.gt6addition.api.capability.impl;


import com.drppp.gt6addition.api.capability.interfaces.IKineticEnergy;

public class KineticEnergyHandler implements IKineticEnergy {
    private int ku=0;

    @Override
    public int getKinetic() {
        return ku;
    }

    @Override
    public void setKineticEnergy(int energy) {
        this.ku=energy;
    }

    @Override
    public void changeKineticEnergy(int energy) {
        this.ku += energy;
        if(this.ku<0)
            this.ku=0;
    }
}
