package com.drppp.gt6addition.api.capability.impl;


import com.drppp.gt6addition.api.capability.interfaces.IHeatEnergy;
import com.drppp.gt6addition.api.capability.interfaces.IMagnetEnergy;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;


public class MagnetEnergyStore implements Capability.IStorage<IMagnetEnergy>{

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IMagnetEnergy> capability, IMagnetEnergy instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Mu",instance.getMagnet());
        return tag ;
    }

    @Override
    public void readNBT(Capability<IMagnetEnergy> capability, IMagnetEnergy instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag =  (NBTTagCompound)nbt;
        instance.setMuEnergy(tag.getInteger("Mu"));
    }
}
