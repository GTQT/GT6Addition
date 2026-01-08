package com.drppp.gt6addition.api.capability.impl;


import com.drppp.gt6addition.api.capability.interfaces.IColdEnergy;

public class ColdEnergyHandler implements IColdEnergy {
    private int cold=0;
    @Override
    public int getCold() {
        return cold;
    }

    @Override
    public void setCuEnergy(int energy) {
        this.cold  = energy;
    }

    @Override
    public void changeCuEnergy(int energy) {
        this.cold += energy;
        if(this.cold<0)
            this.cold=0;
    }
}
