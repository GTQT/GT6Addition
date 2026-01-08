package com.drppp.gt6addition.api.capability.impl;


import com.drppp.gt6addition.api.capability.interfaces.IColdEnergy;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;


public class ColdEnergyStore implements Capability.IStorage<IColdEnergy>{

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IColdEnergy> capability, IColdEnergy instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Cu",instance.getCold());
        return tag ;
    }

    @Override
    public void readNBT(Capability<IColdEnergy> capability, IColdEnergy instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag =  (NBTTagCompound)nbt;
        instance.setCuEnergy(tag.getInteger("Cu"));
    }
}
