package com.drppp.gt6addition.api.capability.impl;


import com.drppp.gt6addition.api.capability.interfaces.IMagnetEnergy;

public class MagnetEnergyHandler implements IMagnetEnergy {
    private int magnet=0;
    @Override
    public int getMagnet() {
        return magnet;
    }

    @Override
    public void setMuEnergy(int energy) {
        this.magnet  = energy;
    }

    @Override
    public void changeMuEnergy(int energy) {
        this.magnet += energy;
        if(this.magnet<0)
            this.magnet=0;
    }
}
