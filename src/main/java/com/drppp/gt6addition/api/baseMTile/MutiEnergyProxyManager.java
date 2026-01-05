package com.drppp.gt6addition.api.baseMTile;

import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.HeatEnergyHandler;
import com.drppp.gt6addition.api.capability.impl.KineticEnergyHandler;
import com.drppp.gt6addition.api.capability.impl.RotationEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IHeatEnergy;
import com.drppp.gt6addition.api.capability.interfaces.IKineticEnergy;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
import com.drppp.gt6addition.api.utils.EnergyTypeList;
import gregtech.api.GTValues;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class MutiEnergyProxyManager implements IMutiEnergyProxy{
    public String EnergyType="null";
    public IHeatEnergy huEnergy;
    public IRotationEnergy ruEnergy;
    public IKineticEnergy kuEnergy;
    public int tire;
    public MutiEnergyProxyManager(String EnergyType,int tire)
    {
        this.EnergyType = EnergyType;
        this.tire=tire;
        if(this.EnergyType.equals(EnergyTypeList.HU))
        {
            this.huEnergy = new HeatEnergyHandler();
        }else if(this.EnergyType.equals(EnergyTypeList.RU))
        {
            this.ruEnergy = new RotationEnergyHandler();
        }else if(this.EnergyType.equals(EnergyTypeList.KU))
        {
            this.kuEnergy = new KineticEnergyHandler();
        }
    }

    @Override
    public int getTire() {
        return this.tire;
    }

    @Override
    public int getEnergy() {
        switch (this.EnergyType)
        {
            case EnergyTypeList.HU:
                return this.huEnergy.getHeat();
            case EnergyTypeList.RU:
                return this.ruEnergy.getEnergyOutput();
            case EnergyTypeList.KU:
                return this.kuEnergy.getKinetic();
        }
        return 0;
    }

    @Override
    public void setEnergy(int energy) {
        switch (this.EnergyType)
        {
            case EnergyTypeList.HU:
                 this.huEnergy.setHuEnergy(energy);
                 break;
            case EnergyTypeList.RU:
                 this.ruEnergy.setRuEnergy(energy);
                break;
            case EnergyTypeList.KU:
                 this.kuEnergy.setKineticEnergy(energy);
                break;
        }
    }

    @Override
    public void changeEnergy(int energy) {
        switch (this.EnergyType)
        {
            case EnergyTypeList.HU:
                this.huEnergy.changeHuEnergy(energy);
                break;
            case EnergyTypeList.RU:
                this.ruEnergy.changeRuEnergy(energy);
                break;
            case EnergyTypeList.KU:
                this.kuEnergy.changeKineticEnergy(energy);
                break;
        }
    }

    @Override
    public boolean getNearEnergyToMyself(TileEntity te,EnumFacing facing) {
        switch (this.EnergyType)
        {
            case EnergyTypeList.HU:
                if(te.hasCapability(CapabilityHandler.CAPABILITY_HEAT_ENERGY,facing.getOpposite()))
                {
                    IHeatEnergy energy = te.getCapability(CapabilityHandler.CAPABILITY_HEAT_ENERGY,facing.getOpposite());
                    this.huEnergy.setHuEnergy(Math.min(energy.getHeat(), (int)GTValues.V[this.tire]*2));
                    return true;
                }
                return false;
            case EnergyTypeList.RU:
                if(te.hasCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY,facing.getOpposite()))
                {
                    IRotationEnergy energy = te.getCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY,facing.getOpposite());
                    this.ruEnergy.setRuEnergy(Math.min(energy.getEnergyOutput(),(int)GTValues.V[this.tire]*2));
                    return true;
                }
                return false;
            case EnergyTypeList.KU:
                if(te.hasCapability(CapabilityHandler.CAPABILITY_KINETIC_ENERGY,facing.getOpposite()))
                {
                    IKineticEnergy energy = te.getCapability(CapabilityHandler.CAPABILITY_KINETIC_ENERGY,facing.getOpposite());
                    this.kuEnergy.setKineticEnergy(Math.min(energy.getKinetic(), (int)GTValues.V[this.tire]*2));
                    return true;
                }
                return false;
        }
        return false;
    }
}
